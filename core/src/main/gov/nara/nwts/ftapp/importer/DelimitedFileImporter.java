package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.Vector;

import gov.nara.nwts.ftapp.YN;

/**
 * Abstract class handling the import of a character-delimited text file allowing for individual values to be wrapped by quotation marks.
 * @author TBrady
 *
 */
public class DelimitedFileImporter extends DefaultImporter {
	public static enum Separator{
		Comma(","),
		Tab("\t"),
		Semicolon(";"),
		Pipe("|");
		
		public String separator;
		Separator(String s) {separator = s;}
	}
	public static final String DELIM = "Delimiter";
	public static final String HEADROW = "HeadRow";
	public DelimitedFileImporter(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), DELIM, "delim",
				"Delimiter character separating fields", Separator.values(), Separator.Comma));
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), HEADROW, HEADROW,
				"Treat first row as header", YN.values(), YN.Y));
	}
	boolean forceKey;
	int rowKey = 1000000;
	
	public static String KEY = "key";
	
	public DelimitedFileReader getDelimitedFileReader(File selectedFile, String sep) throws FileNotFoundException, UnsupportedEncodingException {
	        return new DelimitedFileReader(selectedFile, sep);
	}

	public ActionResult importFile(File selectedFile) throws IOException {
		Separator fileSeparator = (Separator)getProperty(DELIM);
		Timer timer = new Timer();
		forceKey = dt.getImporterForceKey();

		int colcount = 0;
		TreeMap<String,Stats> types = new TreeMap<String,Stats>();
		StatsItemConfig details = new StatsItemConfig();
		if (forceKey) {
			details.addStatsItem(KEY, StatsItem.makeStringStatsItem("Auto Num").setExport(false));
		}
		
		int colset = 0;

		DelimitedFileReader dfr = getDelimitedFileReader(selectedFile, fileSeparator.separator);
		boolean firstRow = (YN)getProperty(HEADROW) == YN.Y;
		
		for(Vector<String> cols = dfr.getRow(); cols != null; cols = dfr.getRow()){
			colcount = Math.max(colcount,cols.size());
			for(int i=colset; i<colcount; i++) {
				String colkey = "Col"+(i+1);
				details.addStatsItem(colkey, StatsItem.makeStringStatsItem(firstRow ?  cols.get(i) : colkey));
				colset++;
			}			
			firstRow = false;
		}

		firstRow = (YN)getProperty(HEADROW) == YN.Y;
		dfr = getDelimitedFileReader(selectedFile, fileSeparator.separator);
		for(Vector<String> cols = dfr.getRow(); cols != null; cols = dfr.getRow()){
			if (firstRow) {
				firstRow = false;
				continue;
			}
			String key = cols.get(0);
			if (forceKey) {
				key = "" + (rowKey++);
			} 
			Stats stats = Stats.Generator.INSTANCE.create(key);
			stats.init(details);
			int start = 1;
			if (forceKey) {
				stats.setKeyVal(details.getByKey(KEY), cols.get(0));
				start = 0;
			}
			for(int i=start; i<cols.size(); i++){
				String colkey = "Col"+(i+1);
				stats.setKeyVal(details.getByKey(colkey), cols.get(i));
			}
			types.put(key, stats);
		}
		
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
	
	public boolean allowForceKey() {
		return true;
	}
	public String toString() {
		return "Import Delimited File";
	}
	public String getDescription() {
		return "This rule will import a delimited file (comma separated, tab separated, etc).  Please specify the delimiter character to use.";
	}
	public String getShortName() {
		return "Delim";
	}
}
