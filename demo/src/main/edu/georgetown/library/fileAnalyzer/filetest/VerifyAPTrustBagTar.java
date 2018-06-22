package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.georgetown.library.fileAnalyzer.stats.DetailedBagStatsItems;
import edu.georgetown.library.fileAnalyzer.util.FABagHelper;
import edu.georgetown.library.fileAnalyzer.util.IncompleteSettingsException;
import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class VerifyAPTrustBagTar extends VerifyBagTar { 
	public static final String APTRUST_INFO = "aptrust-info.txt";
	public static final String APT_TITLE = "APT Title";
	public static final String APT_ACCESS = "APT Access";
	
    static Pattern pAPTMulti = Pattern.compile("^.+\\..+\\.b(\\d{3,3})\\.of(\\d{3,3})\\.tar$");
    static Pattern pAPT = Pattern.compile("^.+\\..+\\.tar$");
    static Pattern pTitle = Pattern.compile("^Title:\\s*(.*)$");
    static Pattern pAccess = Pattern.compile("^Access:\\s*([Cc]onsortia|[Rr]estricted|[Ii]nstitution)\\s*$");
    
    public static StatsItemConfig details = StatsItemConfig.create(DetailedBagStatsItems.class);

    public String getDescription() {
        return "This rule will validate the contents of an APTrust tar file";
   }
   public VerifyAPTrustBagTar(FTDriver dt) {
        super(dt);
    }

    public String toString() {
        return "Verify APTrust Bag - TAR";
    }
    public String getShortName(){return "Ver APT TAR";}

    
    public Stats createStats(String key){ 
        return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
        return details; 
    }

    public InitializationStatus init() {
    	details = StatsItemConfig.create(DetailedBagStatsItems.class);
    	details.addStatsItem(APT_TITLE, StatsItem.makeStringStatsItem(APT_TITLE));
    	details.addStatsItem(APT_ACCESS, StatsItem.makeStringStatsItem(APT_ACCESS));
    	return new InitializationStatus();
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
    
    @Override public void validateBagMetadata(Bag bag, String fname, Stats stats) {
    	validateAPTrustBagMetadata(bag, fname, stats);
    }
        
    public static void validateAPTrustBagMetadata(Bag bag, String fname, Stats s) {
	    s.setVal(DetailedBagStatsItems.Stat, STAT.VALID);
	    s.setVal(DetailedBagStatsItems.Message, "");
    	String source = s.getStringVal(DetailedBagStatsItems.BagSourceOrg,"");  	
    	if (source.isEmpty()) {
    	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
    	    s.appendVal(DetailedBagStatsItems.Message, "Source Org should not be null. "); 
    	}
    	
        int total = -1;
        int count = -1;
    	String scount = s.getStringVal(DetailedBagStatsItems.BagCount, "");    	
        String stotal = s.getStringVal(DetailedBagStatsItems.BagTotal, "");
        
        try {
            FABagHelper.validateBagCount(scount, stotal);
            total = Integer.parseInt(stotal);
            count = Integer.parseInt(scount);
        } catch (IncompleteSettingsException e) {
            s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
            s.appendVal(DetailedBagStatsItems.Message, "Invalid Bag Count. " + e.getMessage());             
        }
    	
    	Matcher m = pAPTMulti.matcher(fname);
    	if (m.matches()) {
    	    if (!String.format("%03d", count).equals(m.group(1))) {
        	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
        	    s.appendVal(DetailedBagStatsItems.Message, String.format("Bag number (%s) mismatch in bag file name %s. ", scount, m.group(1)));     
    	    }
            if (!String.format("%03d", total).equals(m.group(2))) {
                s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
                s.appendVal(DetailedBagStatsItems.Message, String.format("Bag total (%s) mismatch in bag file name %s. ", scount, m.group(2)));     
            }
        } else if (total == 1) {
            m = pAPT.matcher(fname);                
            if (!m.matches()) {
                s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
                s.appendVal(DetailedBagStatsItems.Message, "Single (Non-multipart) APTrust Bags must be named <instid>.<itemid>.tar.)");                     
            }
    	} else {
    	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
    	    s.appendVal(DetailedBagStatsItems.Message, "Multipart APTrust Bags must be named <instid>.<itemid>.b<bag>.of<total>.tar where bag and total are 3 digits.)");     
    	}

    	boolean hasTitle = false;
    	boolean hasAccess = false;
    	boolean hasApt = false;
    	
    	for(BagFile bf: bag.getTags()) {
    		if (bf.getFilepath().equals(APTRUST_INFO)) {
    			hasApt = true;
    			try(InputStream apt = bf.newInputStream()){
    				BufferedReader br = new BufferedReader(new InputStreamReader(apt));
            		for(String line = br.readLine(); line != null; line = br.readLine()) {
            			m = pTitle.matcher(line);
            			if (m.matches()) {
            				if (m.groupCount() >= 1) {
            					if (!m.group(1).isEmpty()) {
            						hasTitle = true;
            						s.setKeyVal(details.getByKey(APT_TITLE), m.group(1));
            					}
            				}
            			} else {
            				m = pAccess.matcher(line);
            				if (m.matches()) {
            					hasAccess = true;
            					s.setKeyVal(details.getByKey(APT_ACCESS), m.group(1));
            				}
            			}
            		}
               	} catch (FileNotFoundException e) {
        			e.printStackTrace();
        		} catch (IOException e) {
        			e.printStackTrace();
        		}    		
    		}
    	}
    	
    	if (hasApt) {
        	if (!hasTitle) {
           	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
        	    s.appendVal(DetailedBagStatsItems.Message, String.format("%s file must contain a title. ", APTRUST_INFO));     		    		
        	}

        	if (!hasAccess) {
           	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
        	    s.appendVal(DetailedBagStatsItems.Message, String.format("%s must have access set to Consortia, Restricted, or Institution. ", APTRUST_INFO));     		    		
        	}
    	} else {
       	    s.setVal(DetailedBagStatsItems.Stat, STAT.INVALID);
    	    s.appendVal(DetailedBagStatsItems.Message, String.format("%s file does not exist. ", APTRUST_INFO));     		    		
    	}
    	
    }

    public boolean hasAptFile(File f) {
        return (new File(f, APTRUST_INFO)).exists();
    }
}
