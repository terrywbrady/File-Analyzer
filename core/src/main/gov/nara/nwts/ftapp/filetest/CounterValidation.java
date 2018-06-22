package gov.nara.nwts.ftapp.filetest;

import gov.nara.nwts.ftapp.counter.CheckResult;
import gov.nara.nwts.ftapp.counter.CounterData;
import gov.nara.nwts.ftapp.counter.CounterRec;
import gov.nara.nwts.ftapp.counter.CounterStat;
import gov.nara.nwts.ftapp.counter.REV;
import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.CSVFilter;
import gov.nara.nwts.ftapp.filter.CounterFilter;
import gov.nara.nwts.ftapp.filter.TsvFilter;
import gov.nara.nwts.ftapp.filter.TxtFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.importer.DelimitedFileWriter;
import gov.nara.nwts.ftapp.importer.Importer;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;
import java.util.Vector;


/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
public class CounterValidation extends DefaultFileTest implements Importer { 
	public static enum Separator{
		DefaultForType(""),
		Comma(","),
		Tab("\t"),
		Semicolon(";"),
		Pipe("|");
		
		public String separator;
		Separator(String s) {separator = s;}
	}
	public static final String DELIM = "Delimiter";
	public static enum FIXABLE {
		NA,YES,NO;
	}
	
	public static enum CounterStatsItems implements StatsItemEnum {
		File_Cell(StatsItem.makeStringStatsItem("File [cell row, cell col]", 100)),
		Filename(StatsItem.makeStringStatsItem("File", 150).makeFilter(true)),
		Cell(StatsItem.makeStringStatsItem("Cell", 50)),
		Rec(StatsItem.makeEnumStatsItem(CounterRec.class, "Record").setWidth(60)),
		Stat(StatsItem.makeEnumStatsItem(CounterStat.class, "Compliance").setWidth(170)),
		Fixable(StatsItem.makeEnumStatsItem(FIXABLE.class, "Fixable").setWidth(40)),
		Report(StatsItem.makeStringStatsItem("Counter Report", 150).makeFilter(true)),
		Version(StatsItem.makeEnumStatsItem(REV.class, "Rev").setWidth(40)),
		Message(StatsItem.makeStringStatsItem("Message", 400)),
		CellValue(StatsItem.makeStringStatsItem("Cell Value", 250)),
		Replacement(StatsItem.makeStringStatsItem("Replacement", 250)),
		;
		StatsItem si;
		CounterStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		class CounterStats extends Stats {
			public CounterStats(String key) {
				super(details, key);
				this.setVal(CounterStatsItems.Rec, CounterRec.FILE);
			}
			public CounterStats(File f, String cellname) {
				super(details, getKey(f, cellname));
				this.setVal(CounterStatsItems.Rec, CounterRec.CELL);
			}

		}

		public CounterStats create(String key) {return new CounterStats(key);}
		public CounterStats create(File f, String cellname) {return new CounterStats(f, cellname);}

	}
	public static StatsItemConfig details = StatsItemConfig.create(CounterStatsItems.class);

	long counter = 1000000;
	boolean showValid = false;
	
	public static final String FIXSUFF = "fix-suffix";
	public CounterValidation(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), DELIM, "delim",
				"Delimiter character separating fields - if not default", Separator.values(), Separator.Comma));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  FIXSUFF, FIXSUFF,
				"Suffix to add to fix files, leave blank for not fix file generation", ""));
	}
	public String toString() {
		return "Counter Compliance - CSV";
	}
	public String getKey(File f) {
		return f.getName();
	}
	
	
	public static String getKey(File f, String cellname) {
		return f.getName() + " [" + cellname + "]";
	}
	
    public String getShortName(){return "Counter";}


    public String getSeparator(File f, String ext) {
		Separator sep = (Separator)this.getProperty(DELIM, Separator.DefaultForType);
    	if (sep != Separator.DefaultForType) return sep.separator;
    	if (ext.equals("CSV")) return ",";
    	if (ext.equals("TXT")) return "\t";
    	if (ext.equals("TSV")) return "\t";
    	return "\t";
    }
    
    public Vector<Vector<String>> getDataFromFile(File f, Stats s) {
		String ext = getExt(f);
		String sep = getSeparator(f, ext);
		try {
			Vector<Vector<String>> data = new Vector<Vector<String>>();
			if (ext.equals("CSV") || ext.endsWith("TXT")) {
				data = DelimitedFileReader.parseFile(f, sep);
			}
			return data;
		} catch (IOException e) {
			s.setVal(CounterStatsItems.Stat, CounterStat.UNSUPPORTED_FILE);
			s.setVal(CounterStatsItems.Message, e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			s.setVal(CounterStatsItems.Stat, CounterStat.UNSUPPORTED_FILE);
			s.setVal(CounterStatsItems.Message, e.toString());
		}
		return null;
    }
    
    @Override public boolean isTestable(File f) {
    	String suff = getProperty(FIXSUFF,"").toString().toLowerCase();
    	if (suff.isEmpty()) return true;
    	if (f.getName().toLowerCase().endsWith(suff)) return false;
    	return true;
    }
    
	public Object fileTest(File f) {
		Stats s = getStats(f);
		
		Vector<Vector<String>> data = getDataFromFile(f, s);
		if (data == null) return s.getVal(CounterStatsItems.Stat);
			
		CounterData cd = new CounterData(data);
		cd.validate();
		
		if (cd.report != null) {
			s.setVal(CounterStatsItems.Report, cd.report);
		}
		if (cd.version != null) s.setVal(CounterStatsItems.Version, REV.find(cd.version));							
		s.setVal(CounterStatsItems.Filename, f.getName());			

		if (cd.rpt == null) {
			s.setVal(CounterStatsItems.Stat, CounterStat.UNSUPPORTED_REPORT);
			s.setVal(CounterStatsItems.Message, "Report Type could not be identified");
		} else {
			setCellStats(f, cd);
			
			s.setVal(CounterStatsItems.Stat, cd.getStat());
			if (cd.getStat() != CounterStat.VALID) {
				s.setVal(CounterStatsItems.Fixable, cd.isFixable() ? FIXABLE.YES : FIXABLE.NO);
			}
			s.setVal(CounterStatsItems.CellValue, cd.title);
			s.setVal(CounterStatsItems.Message, cd.getMessage());				
		}			
		
		return s.getVal(CounterStatsItems.Stat);
	}
	
	void setCellStats(File f, CounterData cd) {
		for(CheckResult result: cd.results) {
			if (result.stat == CounterStat.VALID && !showValid) continue;
			Stats stat = Generator.INSTANCE.create(f, result.cell.getCellSort());
			this.dt.types.put(stat.key, stat);
			stat.setVal(CounterStatsItems.Stat, result.stat);
			stat.setVal(CounterStatsItems.Report, cd.rpt.name);
			stat.setVal(CounterStatsItems.Version, cd.rpt.rev);							
			stat.setVal(CounterStatsItems.Filename, f.getName());							
			stat.setVal(CounterStatsItems.Cell, result.cell.getCellname());
			stat.setVal(CounterStatsItems.Message, result.message);
			
			stat.setVal(CounterStatsItems.Replacement, result.newVal == null ? "" : result.newVal);
			
			String cellval = "";
			if (result.cell != null) {
				String s = cd.getCellValue(result.cell);
				cellval = (s == null) ? "" : s;
			}
			stat.setVal(CounterStatsItems.CellValue, cellval);
			
			FIXABLE fix = FIXABLE.NA;
			if (result.stat == CounterStat.VALID) {
			} else if (result.stat.ordinal() >= CounterStat.ERROR.ordinal()) {
				fix = FIXABLE.NO;
			} else {
				if (result.ignoreVal == false) {
					fix = result.newVal == null ? FIXABLE.NO : FIXABLE.YES;
				}
			}
			stat.setVal(CounterStatsItems.Fixable, fix);
		}
		
		if (!getProperty(FIXSUFF,"").toString().isEmpty()) {
			if (cd.isFixable()) {
				File fixf = new File(f.getParentFile(), f.getName() + getProperty(FIXSUFF,""));
				try {
					DelimitedFileWriter.writeCsv(fixf, cd.getFix(false));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	

	public void refineResults() {
		getStatsDetails().createFilters(this.dt.types);
		showValid = false;
	}
	
    public Stats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details; 
    }

	public String getDescription() {
		return "Verify Counter Compliance (V3 or V4) of a supported report";
	}
	
	public void initFilters() {
		filters.add(new CounterFilter());
		filters.add(new CSVFilter());
		filters.add(new TxtFilter());
		filters.add(new TsvFilter());
	}
	@Override
	public ActionResult importFile(File selectedFile) throws IOException {
		showValid = true;
		Timer timer = new Timer();
		dt.types.clear();
		init();
		fileTest(selectedFile);
		refineResults();
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, dt.types, true, timer.getDuration());
	}
	@Override
	public boolean allowForceKey() {
		return false;
	}
    @Override
    public InitializationStatus initValidate(File refFile) {
        return init();
    }
	

}
