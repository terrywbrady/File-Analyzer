package edu.georgetown.library.fileAnalyzer.filetest;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.utilities.SimpleResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import edu.georgetown.library.fileAnalyzer.stats.DetailedBagStatsItems;
import edu.georgetown.library.fileAnalyzer.util.FABagHelper;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class VerifyBag extends DefaultFileTest { 
	public static final String BAGIT = "bagit.txt";
    public enum STAT {
        VALID,
        INVALID, 
        ERROR,
        NA
    }
    
    public static enum Generator implements StatsGenerator {
        INSTANCE;
        class BagStats extends Stats {
            public BagStats(String key) {
                super(details, key);
            }

        }
        public BagStats create(String key) {return new BagStats(key);}
    }
    public static StatsItemConfig details = StatsItemConfig.create(DetailedBagStatsItems.class);

    long counter = 1000000;
    public VerifyBag(FTDriver dt) {
        super(dt);
    }

    public String toString() {
        return "Verify Bag - Dir";
    }
    public String getKey(File f) {
        return this.getRelPath(f);
    }
    
    public String getShortName(){return "Ver Bag";}

    public InitializationStatus init() {
    	details = StatsItemConfig.create(DetailedBagStatsItems.class);
    	return new InitializationStatus();
    }
    
    public File prepareFile(File f) throws IOException {
    	return f;
    }
    
    public Object fileTest(File f) {
    	String fname = f.getName();
        Stats s = getStats(f);
        try {
			f = prepareFile(f);
			BagFactory bf = new BagFactory();
			try (Bag bag = bf.createBag(f);) {
				s.setVal(DetailedBagStatsItems.Count, bag.getPayload().size());
				SimpleResult result = bag.verifyValid();
				if (result.isSuccess()) {
					BagInfoTxt bit = bag.getBagInfoTxt();
					
					if (bit == null) {
                        s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
                        s.setVal(DetailedBagStatsItems.Message, "Bag Info Not Found. ");					    
					} else {
	                    s.setVal(DetailedBagStatsItems.Stat, STAT.VALID);
	                    s.setVal(DetailedBagStatsItems.Message, "");
	                    s.setVal(DetailedBagStatsItems.BagSourceOrg, bit.getSourceOrganization());
	                    s.setVal(DetailedBagStatsItems.BagSenderDesc, bit.getInternalSenderDescription());
	                    s.setVal(DetailedBagStatsItems.BagSenderId, bit.getInternalSenderIdentifier());
	                    
	                    String countstr = bit.getBagCount() == null ? "" : bit.getBagCount().trim();
	                    
	                    Matcher m = FABagHelper.pBagCountStr.matcher(countstr); 
	                    if (m.matches()) {
	                        s.setVal(DetailedBagStatsItems.BagCount, m.group(1));
	                        s.setVal(DetailedBagStatsItems.BagTotal, m.group(2));
	                    } else {                        
	                        s.setVal(DetailedBagStatsItems.BagCount, countstr);
	                        s.setVal(DetailedBagStatsItems.BagTotal, "");
	                    }
	                    validateBagMetadata(bag, fname, s);					    
					}
				    
				} else {
				    s.setVal(DetailedBagStatsItems.Stat, STAT.ERROR);
				    for(String m: result.getMessages()) {
				        s.appendVal(DetailedBagStatsItems.Message, m +" ");             
				    }
				}
			} 
		} catch (Exception e) {
		    e.printStackTrace();
		    s.setVal(DetailedBagStatsItems.Message, "Bag Error: " + e.getClass().getName() +" " + e.getMessage());
		    s.setVal(DetailedBagStatsItems.Stat, STAT.NA);
		}
        return s.getVal(DetailedBagStatsItems.Count);
    }
    
    public Stats createStats(String key){ 
        return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
        return details; 
    }

    public String getDescription() {
        return "This rule will scan for directories with names ending with '_bag'.  BagIt verify valid will be run on the bag.";
    }
    
    @Override public boolean isTestDirectory(File f) {
    	return hasDescendant(f, BAGIT);
    }
    @Override public boolean processRoot() {
        return true;
    }

    @Override public boolean isTestFiles() {
        return false; 
    }

    public boolean hasBagFile(File f) {
        return (new File(f, BAGIT)).exists();
    }
    @Override public boolean isTestable(File f) {
        return hasBagFile(f) && f.getName().endsWith("_bag");
    }
    
    public void validateBagMetadata(Bag bag, String fname, Stats stats) {
    }

}
