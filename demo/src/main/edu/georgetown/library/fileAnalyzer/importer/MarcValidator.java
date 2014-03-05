package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.TreeMap;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

import edu.georgetown.library.fileAnalyzer.importer.MarcXmlValidator.Generator;
import edu.georgetown.library.fileAnalyzer.importer.MarcXmlValidator.MarcStatsItems;
import edu.georgetown.library.fileAnalyzer.importer.MarcXmlValidator.STAT;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

/**
 * Importer for tab delimited files
 * 
 * @author TBrady
 * 
 */
public class MarcValidator extends DefaultImporter {

	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
	}
	
	public static enum STAT {
		VALID,
		INVALID,
	}

	public static enum COUNT {
		MISSING,
		NOT_PRESENT,
		PRESENT_ONCE,
		MULTIPLE_COPIES,
		CONTENT_ERR
	}
	
	public static enum MarcStatsItems implements StatsItemEnum {
		ItemNum(StatsItem.makeStringStatsItem("ItemNum", 100)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Status").setWidth(150)),
		Author(StatsItem.makeStringStatsItem("Author", 250)), 
		Title(StatsItem.makeStringStatsItem("Title", 250)),

		f949i(StatsItem.makeStringStatsItem("949 i").setWidth(150)),
		f949l(StatsItem.makeStringStatsItem("949 l").setWidth(50)),
		f949s(StatsItem.makeStringStatsItem("949 s").setWidth(50)),
		f949t(StatsItem.makeStringStatsItem("949 t").setWidth(50)),
		f949z(StatsItem.makeStringStatsItem("949 z").setWidth(50)),
		f949a(StatsItem.makeStringStatsItem("949 a").setWidth(100)),
		f949b(StatsItem.makeStringStatsItem("949 b").setWidth(100)),
		f949(StatsItem.makeEnumStatsItem(COUNT.class, "949").setWidth(120)),
		Stat949(StatsItem.makeStringStatsItem("Status 949").setWidth(350)),

		f980(StatsItem.makeEnumStatsItem(COUNT.class, "980").setWidth(120)),
		Stat980(StatsItem.makeStringStatsItem("Status 980").setWidth(350)),

		f981(StatsItem.makeEnumStatsItem(COUNT.class, "981").setWidth(120)),
		f981b(StatsItem.makeStringStatsItem("981 b").setWidth(50)),
		Stat981(StatsItem.makeStringStatsItem("Status 981").setWidth(350)),

		f935(StatsItem.makeEnumStatsItem(COUNT.class, "935").setWidth(120)),
		f935a(StatsItem.makeStringStatsItem("935 a").setWidth(50)),
		Stat935(StatsItem.makeStringStatsItem("Status 935").setWidth(350)),
		;

		StatsItem si;

		MarcStatsItems(StatsItem si) {
			this.si = si;
		}

		public StatsItem si() {
			return si;
		}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {
			return new Stats(details, key);
		}
	}

	public static StatsItemConfig details = StatsItemConfig
			.create(MarcStatsItems.class);

	public MarcValidator(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "MARC Validator";
	}

	public String getDescription() {
		return "Look for common issues when importing Marc Records.";
	}

	public String getShortName() {
		return "Marc";
	}

	public void setFieldCount(Stats stat, String s, MarcStatsItems msi, boolean required) {
		if (s.equals("1")) {
			stat.setVal(msi, COUNT.PRESENT_ONCE);
		} else {
			if (s.equals("0")) {
				if (required) {
					stat.setVal(msi, COUNT.MISSING);
				} else {
					stat.setVal(msi, COUNT.NOT_PRESENT);
					return;
				}
			} else {
				stat.setVal(msi, COUNT.MULTIPLE_COPIES);						
			}
			stat.setVal(MarcStatsItems.Stat, STAT.INVALID);						
		}
		
	}
	
	public void setFieldError(Stats stat, String s, MarcStatsItems msiMsg, MarcStatsItems msi) {
		stat.setVal(msiMsg, s);
		if (!s.isEmpty()) {
			stat.setVal(MarcStatsItems.Stat, STAT.INVALID);						
			stat.setVal(msi, COUNT.CONTENT_ERR);
		}
	}
	
	public static void statSubfield(Stats stats, MarcStatsItems si, DataField df, char f) { 
		stats.setVal(si, getSubfield(df, f));
	}
	
	public static String getSubfield(DataField df, char f) {
		Subfield sf = df.getSubfield(f);
		if (sf == null) return "";
		return sf.getData();
	}

	public ActionResult importFile(File selectedFile) throws IOException {
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
	    InputStream in = new FileInputStream(selectedFile);
        MarcReader reader = new MarcStreamReader(in);
        int i=0;
        while (reader.hasNext()) {
			String key = nf.format(i++);
			Stats stat = Generator.INSTANCE.create(key);
			stat.setVal(MarcStatsItems.Stat, STAT.VALID);
			types.put(stat.key, stat);
            Record record = reader.next();
            
            for(DataField df: record.getDataFields()) {
            	String tag = df.getTag();
            	if (tag.equals("245")) {
            		statSubfield(stat, MarcStatsItems.Title, df, 'a');
            	} else if (tag.equals("100")) {
            		statSubfield(stat, MarcStatsItems.Author, df, 'a');
            	}
             }
             
        }    

		return new ActionResult(selectedFile, selectedFile.getName(),
				this.toString(), details, types, true, timer.getDuration());
	}
	
	public void doLine(String line) {
		
	}
	
}
