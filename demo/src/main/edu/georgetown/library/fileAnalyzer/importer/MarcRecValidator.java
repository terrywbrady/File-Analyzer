package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.TreeMap;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

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
public class MarcRecValidator extends DefaultImporter {

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
	}
	
	public static enum MarcStatsItems implements StatsItemEnum {
		ItemNum(StatsItem.makeStringStatsItem("Item No.", 100)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Status").setWidth(100)),
		Author(StatsItem.makeStringStatsItem("Author", 200)), 
		Title(StatsItem.makeStringStatsItem("Title", 300)),

		f949(StatsItem.makeEnumStatsItem(COUNT.class, "949").setWidth(125)),
		f949i(StatsItem.makeStringStatsItem("barcode (949$i)").setWidth(100)),
		f949l(StatsItem.makeStringStatsItem("location (949$l)").setWidth(100)),
		f949s(StatsItem.makeStringStatsItem("status (949$s)").setWidth(100)),
		f949t(StatsItem.makeStringStatsItem("type (949$t)").setWidth(100)),
		f949z(StatsItem.makeStringStatsItem("call no. (949$z)").setWidth(100)),
		f949a(StatsItem.makeStringStatsItem("call no. (949$a)").setWidth(100)),
		f949b(StatsItem.makeStringStatsItem("call no. (949$b)").setWidth(100)),
		
		f960(StatsItem.makeEnumStatsItem(COUNT.class, "960").setWidth(125)),
		f960u(StatsItem.makeStringStatsItem("fund (960$u)", 100)),
		f960v(StatsItem.makeStringStatsItem("vendor (960$v)", 100)),
		
		f980(StatsItem.makeEnumStatsItem(COUNT.class, "980").setWidth(125)),
		f980f(StatsItem.makeStringStatsItem("invoice no. (980$f)").setWidth(100)),
		
		f935(StatsItem.makeEnumStatsItem(COUNT.class, "935").setWidth(125)),
		f935a(StatsItem.makeStringStatsItem("935 a").setWidth(100)),
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

	public MarcRecValidator(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "MARC Rec Validator";
	}

	public String getDescription() {
		return "The Marc Record Validator Looks for common issues when importing Marc Records, including 949 (item record), 960 (order record), 980 (invoice record), and 935 (matching point).\n\nLibrarian can request to add more fields to be validated if necessary.";
	}

	public String getShortName() {
		return "Marc";
	}

	public void setFieldCount(Stats stat, int count, MarcStatsItems msi, boolean required)
	{
		if (count == 0)
		{
			if (required)
			{
				stat.setVal(msi, COUNT.MISSING);
				stat.setVal(MarcStatsItems.Stat, STAT.INVALID);
			} else
			{
				stat.setVal(msi, COUNT.NOT_PRESENT);
			}
			
		} else if (count == 1)
		{
			stat.setVal(msi, COUNT.PRESENT_ONCE);
		} else if (count > 1)
		{
			stat.setVal(msi, COUNT.MULTIPLE_COPIES);
		}
		
	}
	
/*	
*   public void setFieldError(Stats stat, String s, MarcStatsItems msiMsg, MarcStatsItems msi) {
*		stat.setVal(msiMsg, s);
*		if (!s.isEmpty()) {
*			stat.setVal(MarcStatsItems.Stat, STAT.INVALID);						
*			stat.setVal(msi, COUNT.CONTENT_ERR);
*		}
*	}
*/

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
            
            int count_949 = 0;
            int count_960 = 0;
            int count_980 = 0;
            int count_935 = 0;
            
            for(DataField df: record.getDataFields()) {
            	String tag = df.getTag();
            	if (tag.equals("245")) {
            		statSubfield(stat, MarcStatsItems.Title, df, 'a');
            	} else if (tag.equals("100")) {
            		statSubfield(stat, MarcStatsItems.Author, df, 'a');
            	} else if (tag.equals("949")) {
            		statSubfield(stat, MarcStatsItems.f949i, df, 'i');
            		statSubfield(stat, MarcStatsItems.f949l, df, 'l');
            		statSubfield(stat, MarcStatsItems.f949s, df, 's');
            		statSubfield(stat, MarcStatsItems.f949t, df, 't');
            		statSubfield(stat, MarcStatsItems.f949z, df, 'z');
            		statSubfield(stat, MarcStatsItems.f949a, df, 'a');
            		statSubfield(stat, MarcStatsItems.f949b, df, 'b');
            		count_949++;
            	} else if (tag.equals("960")) {
            		statSubfield(stat, MarcStatsItems.f960u, df, 'u');
            		statSubfield(stat, MarcStatsItems.f960v, df, 'v');
            		count_960++;
            	} else if (tag.equals("980")) {
            		statSubfield(stat, MarcStatsItems.f980f, df, 'f');
            		count_980++;
            	} else if (tag.equals("935")) {
            		statSubfield(stat, MarcStatsItems.f935a, df, 'a');
            		count_935++;
            	}
             }
            
            setFieldCount(stat, count_949, MarcStatsItems.f949, true);
            setFieldCount(stat, count_960, MarcStatsItems.f960, true);
            setFieldCount(stat, count_980, MarcStatsItems.f980, true);
            setFieldCount(stat, count_935, MarcStatsItems.f935, true);
             
        }    

		return new ActionResult(selectedFile, selectedFile.getName(),
				this.toString(), details, types, true, timer.getDuration());
	}
		
}
