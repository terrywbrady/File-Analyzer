package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.text.NumberFormat;

import edu.georgetown.library.fileAnalyzer.filter.GUImageFileTestFilter;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filetest.FileTest;
import gov.nara.nwts.ftapp.filter.Jp2FileTestFilter;
import gov.nara.nwts.ftapp.filter.JpegFileTestFilter;
import gov.nara.nwts.ftapp.filter.PdfFileTestFilter;
import gov.nara.nwts.ftapp.filter.TiffFileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

public class IngestInventory extends DefaultFileTest {

	private static enum InventoryStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Key")),
		File(StatsItem.makeStringStatsItem("File")),
		ThumbFile(StatsItem.makeStringStatsItem("Thumb File"))
		;
		
		StatsItem si;
		InventoryStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {
			return new Stats(details, key) {
				public Object compute(File f, FileTest fileTest) {
					Object o = fileTest.fileTest(f);
					
					String path = f.getAbsolutePath().substring(fileTest.getRoot().getAbsolutePath().length()+1);
					String tpath = path + ".jpg";
					String tfull = fileTest.getRoot().getAbsolutePath() + "\\" + tpath;
					File f2 = new File(tfull);
					if (!f2.exists()) tpath = "";
					
					setVal(InventoryStatsItems.File, path);
					setVal(InventoryStatsItems.ThumbFile, tpath);
					
					return o;
				}				
			};
		}
	}
	static StatsItemConfig details = StatsItemConfig.create(InventoryStatsItems.class);

	public static final String[] META = { "NA", "dc.contributor",
			"dc.coverage.spatial", "dc.coverage.temporal", "dc.creator",
			"dc.date", "dc.date.accessioned", "dc.date.available",
			"dc.date.copyright", "dc.date.created", "dc.date.issued",
			"dc.date.submitted", "dc.identifier", "dc.identifier.citation",
			"dc.identifier.uri", "dc.description", "dc.description.abstract",
			"dc.description.tableofcontents", "dc.description.uri",
			"dc.format", "dc.format.extent", "dc.format.medium", "dc.language",
			"dc.language.iso", "dc.publisher", "dc.relation",
			"dc.relation.isformatof", "dc.relation.ispartof",
			"dc.relation.haspart", "dc.relation.isversionof",
			"dc.relation.hasversion", "dc.relation.isreferencedby",
			"dc.relation.requires", "dc.relation.replaces",
			"dc.relation.isreplacedby", "dc.relation.uri", "dc.rights",
			"dc.rights.uri", "dc.source", "dc.source.uri", "dc.subject",
			"dc.subject.ddc", "dc.subject.lcc", "dc.subject.lcsh",
			"dc.subject.mesh", "dc.title", "dc.title.alternative", "dc.type" };

	NumberFormat nf;
	public IngestInventory(FTDriver dt) {
		super(dt);
		nf = NumberFormat.getNumberInstance();
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
		this.ftprops.add(new FTPropEnum(this, "metadata 1", "m1",
				"field to be populated for each item found", META, "dc.title"));
		this.ftprops.add(new FTPropEnum(this, "metadata 2", "m2",
				"field to be populated for each item found", META, "dc.date.creator"));
		this.ftprops.add(new FTPropEnum(this, "metadata 3", "m3",
				"field to be populated for each item found", META, "NA"));
		this.ftprops.add(new FTPropEnum(this, "metadata 4", "m4",
				"field to be populated for each item found", META, "NA"));
		this.ftprops.add(new FTPropEnum(this, "metadata 5", "m5",
				"field to be populated for each item found", META, "NA"));
		this.ftprops.add(new FTPropEnum(this, "metadata 6", "m6",
				"field to be populated for each item found", META, "NA"));
		this.ftprops.add(new FTPropEnum(this, "metadata 7", "m7",
				"field to be populated for each item found", META, "NA"));
		this.ftprops.add(new FTPropEnum(this, "metadata 8", "m8",
				"field to be populated for each item found", META, "NA"));
	}

	public String getDescription() {
		return "Create an inventory spreadsheet.  Save this spreadsheet in the root directory where the search was launched.\n" +
				"Use the filters to select files of interest (i.e. pdf or image).  \n" +
				"Thumbnail files will be identified if they conform to DSpace naming conventions (i.e. filename.pdf.jpg)\n" +
				"Use the property fields to designate the metadata that will be ingested for each item. \n"+
				"dc.title and dc.date.created are required.\n" +
				"Items that appear to be thumbnails will be skipped.  (i.e. a.jpg.jpg will be skipped if a.jpg exists)";
	}

	public String getKey(File f) {
		return "folder_" + nf.format(count);
	}
	
	int count;
	public Object fileTest(File f) {
		count++;
		return count;
	}

	public String getShortName() {
		return "Ingest Inventory";
	}

	public String toString() {
		return "Ingest Inventory";
	}

    public StatsItemConfig getStatsDetails() {
    	return details;
    }

    
    public void init() {
    	details = StatsItemConfig.create(InventoryStatsItems.class);
    	for(FTProp prop: ftprops) {
    		if (prop.getValue().equals("NA")) continue;
    		details.addStatsItem(prop.getValue(), StatsItem.makeStringStatsItem(prop.getValue().toString()));
    	}
    }
    
    public boolean isTestable(File f) {
		String name = f.getName().toLowerCase();
		if (!name.endsWith(".jpg")) return true;
		String x = name.substring(0, name.length()-4);
		File f2 = new File(f.getParentFile(), x);
		if (f2.exists()) return false;
		return true;
	}

  	public void initFilters() {
		filters.add(new PdfFileTestFilter());
		filters.add(new GUImageFileTestFilter());
		filters.add(new TiffFileTestFilter());
		filters.add(new JpegFileTestFilter());
		filters.add(new Jp2FileTestFilter());
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }

}
