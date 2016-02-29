package edu.georgetown.library.fileAnalyzer.filetest;

import gov.loc.repository.bagit.Bag;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropInt;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.io.IOException;

import edu.georgetown.library.fileAnalyzer.util.APTrustHelper.Access;
import edu.georgetown.library.fileAnalyzer.util.IncompleteSettingsException;
import edu.georgetown.library.fileAnalyzer.util.FABagHelper;
import edu.georgetown.library.fileAnalyzer.stats.BagStatsItems;
import edu.georgetown.library.fileAnalyzer.util.APTrustHelper;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class CreateAPTrustBag extends DefaultFileTest {  
	
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
	
    private FTPropInt pBagCount = new FTPropInt(dt, this.getClass().getSimpleName(), APTrustHelper.P_BAGCOUNT, APTrustHelper.P_BAGCOUNT,
            "Sequence number within a multi-part bag.  Use 1 if not multi-part", 1);
    private FTPropInt pBagTotal = new FTPropInt(dt, this.getClass().getSimpleName(), APTrustHelper.P_BAGTOTAL, APTrustHelper.P_BAGTOTAL,
            "If this is a multi-part bag, set to the number of parts; otherwise set to 1", 1);
    private FTPropEnum pAccess = new FTPropEnum(dt, this.getClass().getSimpleName(), APTrustHelper.P_ACCESS, APTrustHelper.P_ACCESS,
            "Access condition within APTrust.", APTrustHelper.Access.values(), APTrustHelper.Access.Institution);
    private FTPropEnum pTopFolder = new FTPropEnum(dt, this.getClass().getSimpleName(),  CreateBag.P_TOPFOLDER, CreateBag.P_TOPFOLDER,
            "Retain containing folder in payload", YN.values(), YN.Y);
    
	public CreateAPTrustBag(FTDriver dt) {
		super(dt);
        ftprops.add(pTopFolder);
		FTPropString fps = new FTPropString(dt, this.getClass().getSimpleName(), APTrustHelper.P_SRCORG, APTrustHelper.P_SRCORG,
                "This should be the human readable name of the APTrust partner organization.", "");
		fps.setFailOnEmpty(true);
        ftprops.add(fps);

        fps = new FTPropString(dt, this.getClass().getSimpleName(),  APTrustHelper.P_INSTID, APTrustHelper.P_INSTID,
                "Institutional ID.", "");
        fps.setFailOnEmpty(true);
        ftprops.add(fps);

        fps = new FTPropString(dt, this.getClass().getSimpleName(), APTrustHelper.P_ITEMUID, APTrustHelper.P_ITEMUID,
                "Item UID.", "");
        fps.setFailOnEmpty(true);
        ftprops.add(fps);
        ftprops.add(pBagTotal);
        ftprops.add(pBagCount);

        fps = new FTPropString(dt, this.getClass().getSimpleName(), APTrustHelper.P_INTSENDDESC, APTrustHelper.P_INTSENDDESC,
                "[Optional] Human readable description of the contents of the bag.", "");
        ftprops.add(fps);

        fps = new FTPropString(dt, this.getClass().getSimpleName(), APTrustHelper.P_INTSENDID, APTrustHelper.P_INTSENDID,
                " [Optional] Internal or alternate identifier used at the senders location.", "");
        ftprops.add(fps);

        fps = new FTPropString(dt, this.getClass().getSimpleName(), APTrustHelper.P_TITLE, APTrustHelper.P_TITLE,
                "Human readable title for searching and listing in APTrust.", "");
        fps.setFailOnEmpty(true);
        ftprops.add(fps);

        ftprops.add(pAccess);
	}

    @Override public InitializationStatus init() {
        InitializationStatus istat = super.init();
        try {
            FABagHelper.validateBagCount(
                this.getProperty(APTrustHelper.P_BAGCOUNT).toString(), 
                this.getProperty(APTrustHelper.P_BAGTOTAL).toString() 
            );                    
        } catch(IncompleteSettingsException e) {
            istat.addFailMessage(e.getMessage());                    
        }
        return istat;
    }

    public String toString() {
		return "Create APTrust Bag";
	}
	public String getKey(File f) {
		return f.getName();
	}
	
    public String getShortName(){return "APTBag";}

	public Object fileTest(File f) {
		boolean retainTop = ((YN)this.getProperty(CreateBag.P_TOPFOLDER) == YN.Y);
		
		Stats s = getStats(f);
		
        try {
    		APTrustHelper aptHelper = new APTrustHelper(f);
    		aptHelper.setAccessType((Access)pAccess.getValue());
    		aptHelper.setBagCount(pBagCount.getIntValue(1));
    		aptHelper.setBagTotal(pBagTotal.getIntValue(1));
    		aptHelper.setInstitutionalSenderDesc(this.getProperty(APTrustHelper.P_INTSENDDESC).toString());
    		aptHelper.setInstitutionalSenderId(this.getProperty(APTrustHelper.P_INTSENDID).toString());
    		aptHelper.setInstitutionId(this.getProperty(APTrustHelper.P_INSTID).toString());
    		aptHelper.setItemIdentifer(this.getProperty(APTrustHelper.P_ITEMUID).toString());
    		aptHelper.setSourceOrg(this.getProperty(APTrustHelper.P_SRCORG).toString());
    		aptHelper.setTitle(this.getProperty(APTrustHelper.P_TITLE).toString());
    		
    		Bag bag = aptHelper.getBag();
    		
    		if (retainTop) {
    			bag.addFileToPayload(f);
    		} else {
    			for(File payloadFile: f.listFiles()) {
    				bag.addFileToPayload(payloadFile);			
    			}			
    		}
    		
    		aptHelper.createBagFile();  
    		aptHelper.generateBagInfoFiles();
    		aptHelper.writeBagFile();
    		
			s.setVal(BagStatsItems.Bag, aptHelper.getFinalBagName());
			s.setVal(BagStatsItems.Stat, FABagHelper.STAT.VALID);
			s.setVal(BagStatsItems.Count, bag.getPayload().size());
		} catch (IOException e) {
			s.setVal(BagStatsItems.Stat, FABagHelper.STAT.ERROR);
			s.setVal(BagStatsItems.Message, e.getMessage());
		} catch (IncompleteSettingsException e) {
			s.setVal(BagStatsItems.Stat, FABagHelper.STAT.ERROR);
			s.setVal(BagStatsItems.Message, e.getMessage());
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
		return "This rule will create a bag according to the APTrust Bag Standards: \n" +
				"https://sites.google.com/a/aptrust.org/aptrust-wiki/technical-documentation/processing-ingest/aptrust-bagit-profile \n" + 
				"The bag will be created as a sibling directory using the institutionid and itemid as part of the filename.";
	}
	
	@Override public boolean isTestDirectory() {
		return true;
	}

	@Override public boolean isTestable(File f) {
		return f.equals(getRoot());
	}

	@Override public boolean isTestFiles() {
		return false; 
	}
	
	@Override public boolean processRoot() {
		return true;
	}
	
}
