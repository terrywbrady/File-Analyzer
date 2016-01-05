package edu.georgetown.library.fileAnalyzer.filetest;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;
import gov.loc.repository.bagit.writer.impl.ZipWriter;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;

import edu.georgetown.library.fileAnalyzer.BAG_TYPE;
import edu.georgetown.library.fileAnalyzer.util.TarUtil;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class CreateBag extends DefaultFileTest { 
	public enum STAT {
		VALID,
		INVALID, 
		ERROR
	}
	
    public static final String P_BAGTYPE = "bag-type";
    public static final String P_TOPFOLDER = "top-folder";
    
    private FTPropEnum pBagType = new FTPropEnum(dt, this.getClass().getSimpleName(),  CreateBag.P_BAGTYPE, CreateBag.P_BAGTYPE,
            "Type of bag to create", BAG_TYPE.values(), BAG_TYPE.DIRECTORY);
    private FTPropEnum pTopFolder = new FTPropEnum(dt, this.getClass().getSimpleName(),  CreateBag.P_TOPFOLDER, CreateBag.P_TOPFOLDER,
            "Retain containing folder in payload", YN.values(), YN.Y);

	private static enum BagStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Source", 200)),
		Bag(StatsItem.makeStringStatsItem("Bag", 200)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Bag Status")),
		Count(StatsItem.makeIntStatsItem("Item Count")),
		Message(StatsItem.makeStringStatsItem("Message", 200)),
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
	public CreateBag(FTDriver dt) {
		super(dt);
        ftprops.add(pBagType);
        ftprops.add(pTopFolder);
	}

	public String toString() {
		return "Create Bag";
	}
	public String getKey(File f) {
		return f.getName();
	}
	
    public String getShortName(){return "Bag";}

    
	public Object fileTest(File f) {
		BAG_TYPE bagType = (BAG_TYPE)this.getProperty(CreateBag.P_BAGTYPE);
		boolean retainTop = ((YN)this.getProperty(CreateBag.P_TOPFOLDER) == YN.Y);
		Stats s = getStats(f);
		File newBag = null;
		if (bagType == BAG_TYPE.ZIP) {
			newBag = new File(f.getParentFile(), f.getName() + "_bag.zip");
		} else {
			newBag = new File(f.getParentFile(), f.getName() + "_bag");
		}
		BagFactory bf = new BagFactory();
		Bag bag = bf.createBag();
		
		if (retainTop) {
			bag.addFileToPayload(f);
		} else {
			for(File payloadFile: f.listFiles()) {
				bag.addFileToPayload(payloadFile);			
			}			
		}
		try {
			DefaultCompleter comp = new DefaultCompleter(bf);
			comp.setGenerateBagInfoTxt(true);
			comp.setUpdateBaggingDate(true);
			comp.setUpdateBagSize(true);
			comp.setUpdatePayloadOxum(true);
			comp.setGenerateTagManifest(true);
			bag = comp.complete(bag);
		    Writer writer = (bagType == BAG_TYPE.ZIP) ? new ZipWriter(bf) : new FileSystemWriter(bf); 
		    if (writer instanceof ZipWriter) {
		    	((ZipWriter)writer).setBagDir(f.getName());
		    }
		    bag.write(writer, newBag);
		    bag.close();
		    if (bagType == BAG_TYPE.TAR) {
		    	newBag = TarUtil.tarFolderAndDeleteFolder(newBag);
		    }
			s.setVal(BagStatsItems.Bag, newBag.getName());
			s.setVal(BagStatsItems.Stat, STAT.VALID);
			s.setVal(BagStatsItems.Count, bag.getPayload().size());
		} catch (IOException e) {
			s.setVal(BagStatsItems.Stat, STAT.ERROR);
			s.setVal(BagStatsItems.Message, e.getMessage());
			e.printStackTrace();
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
		return "This rule will bag the contents of the selected directory 'X' and create a new bag directory name 'X_bag'.\n" +
				"Some thought is still needed on how to iteratively bag up subfolders or files into independent bags.";
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
