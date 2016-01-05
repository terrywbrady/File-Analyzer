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
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;

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
    
    static enum BagStatsItems implements StatsItemEnum {
        Key(StatsItem.makeStringStatsItem("Bag Path", 200)),
        Stat(StatsItem.makeEnumStatsItem(STAT.class, "Bag Status")),
        Count(StatsItem.makeIntStatsItem("Item Count")),
        BagSourceOrg(StatsItem.makeStringStatsItem("Source Org",150)),
        BagSenderDesc(StatsItem.makeStringStatsItem("Sender Desc",150)),
        BagSenderId(StatsItem.makeStringStatsItem("Sender Id",150)),
        BagCount(StatsItem.makeStringStatsItem("Bag Count",150)),
        Message(StatsItem.makeStringStatsItem("Message",400)),
        ;
        StatsItem si;
        BagStatsItems(StatsItem si) {this.si=si;}
        public StatsItem si() {return si;}
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
    public static StatsItemConfig details = StatsItemConfig.create(BagStatsItems.class);

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
    	details = StatsItemConfig.create(BagStatsItems.class);
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
				s.setVal(BagStatsItems.Count, bag.getPayload().size());
				SimpleResult result = bag.verifyValid();
				if (result.isSuccess()) {
					BagInfoTxt bit = bag.getBagInfoTxt();
					
				    s.setVal(BagStatsItems.Stat, STAT.VALID);
				    s.setVal(BagStatsItems.Message, "");
				    s.setVal(BagStatsItems.BagSourceOrg, bit.getSourceOrganization());
				    s.setVal(BagStatsItems.BagSenderDesc, bit.getInternalSenderDescription());
				    s.setVal(BagStatsItems.BagSenderId, bit.getInternalSenderIdentifier());
				    s.setVal(BagStatsItems.BagCount, bit.getBagCount());
				    
				    validateBagMetadata(bag, fname, s);
				} else {
				    s.setVal(BagStatsItems.Stat, STAT.ERROR);
				    for(String m: result.getMessages()) {
				        s.appendVal(BagStatsItems.Message, m +" ");             
				    }
				}
			} 
		} catch (Exception e) {
		    s.setVal(BagStatsItems.Message, e.getMessage());
		    s.setVal(BagStatsItems.Stat, STAT.NA);
		}
        return s.getVal(BagStatsItems.Count);
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
