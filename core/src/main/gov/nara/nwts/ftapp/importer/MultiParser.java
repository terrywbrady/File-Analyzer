package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropFile;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Base class parser to ananlyze and ingest individual rows from a file using a regular expression pattern.
  * @author TBrady
 *
 */
public class MultiParser extends DefaultImporter {
	public static enum status {PASS,WARN,ERROR,SKIP,FAIL}
	public static enum ParserStatsItems implements StatsItemEnum {
		Row(StatsItem.makeStringStatsItem("Row",60)),
		PassFail(StatsItem.makeEnumStatsItem(status.class, "Pass/Fail").setInitVal(status.PASS)),
		Label(StatsItem.makeStringStatsItem("Label").makeFilter(true)),
		Data(StatsItem.makeStringStatsItem("Data",300));
		
		StatsItem si;
		ParserStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}
	
	public static StatsItemConfig details = StatsItemConfig.create(ParserStatsItems.class);
	public StatsItemConfig getDetails() {
		return details;
	}

	public static final String F_PARSE = "Parser Rule File";
	public static final String F_CASE = "Case Insensitive";
	private ParserFile pParseRule;
	
	public MultiParser(FTDriver dt) {
		super(dt);
		pParseRule = new ParserFile(dt);
		this.ftprops.add(pParseRule);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), F_CASE, F_CASE,
				"Treat patterns as case insensitive", YN.values(), YN.N));
	}
	
	enum ParserFileMode {NA,COLS,FILTERS,PATTS;}
	int getPatternFlags() {
		return getProperty(F_CASE) == YN.Y ? Pattern.CASE_INSENSITIVE : 0;

	}
	
	class ParserPattern {
		status stat = status.FAIL;
		Pattern p;
		String label;
		
		ParserPattern(String category, String pattern, String label) throws PatternSyntaxException {
			this.stat = status.valueOf(category);
			this.p = Pattern.compile(pattern, MultiParser.this.getPatternFlags());
			this.label = label;
		}
	}
	
	class ParserFile extends FTPropFile {
		String[] groups = new String[0];
		String[] fgroups = new String[0];
		Vector<ParserPattern> patterns = new Vector<ParserPattern>();
		Pattern pCat;
		
		ParserFile(FTDriver dt) {
			super(dt, MultiParser.this.getClass().getName(), F_PARSE, F_PARSE,
				"Parser Rule File", "");
			String s = "^\\[CATEGORY:\\s*(";
			StringBuilder sb = new StringBuilder(s);
			for(status stat: status.values()) {
                if (sb.length() > s.length()) sb.append("|");
                sb.append(stat.toString());
			}
			sb.append("):?(.*)\\]$");
			pCat = Pattern.compile(sb.toString());
		}

	    public InitializationStatus initValidation(File refFile){
	    	patterns.clear();
	        InitializationStatus iStat = new InitializationStatus();
			String category = "";
			String label = "";
			
			try (BufferedReader br = new BufferedReader(new FileReader(this.getFile()))) {
				ParserFileMode pfm = ParserFileMode.NA;
				for(String line=br.readLine(); line!=null; line=br.readLine()) {
					line = line.trim();
					if (line.startsWith("#")) continue;
					if (line.isEmpty()) continue;
					
					if (line.equals("[COLS]")) {
						pfm = ParserFileMode.COLS;
						continue;
					} 
					if (line.equals("[FILTERS]")) {
						pfm = ParserFileMode.FILTERS;
						continue;
					} 
					if (line.equals("[PATTERNS]")) {
						pfm = ParserFileMode.PATTS;
						continue;
					}

					Matcher m = pCat.matcher(line);
					if (m.matches()) {
						category = m.group(1);
						label = m.group(2) == null ? "" : m.group(2).trim();
						continue;
					}
					
					if (pfm == ParserFileMode.COLS) {
						groups = line.split(",");
					} else if (pfm == ParserFileMode.FILTERS) {
						fgroups = line.split(",");
					} else if (pfm == ParserFileMode.PATTS) {
						patterns.add(new ParserPattern(category, line, label));
					}
				}
			} catch (PatternSyntaxException|IOException e) {
                iStat.addMessage(e);
			} 
	        return iStat;
		}
	}
	
	
	public ActionResult importFile(File selectedFile) throws IOException {
		details = StatsItemConfig.create(ParserStatsItems.class);
		for(String grp: pParseRule.groups) {
			details.addStatsItem(grp, StatsItem.makeStringStatsItem(grp));
		}
		for(String grp: pParseRule.fgroups) {
			StatsItem si = details.getByKey(grp);
			if (si != null) {
				si.setWidth(150).makeFilter(true);
			}
		}

		Timer timer = new Timer();
		TreeMap<String,Stats> types = new TreeMap<String,Stats>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(selectedFile));
			int i=1000000;
			for(String line=br.readLine(); line!=null; line=br.readLine()){
				String key = ""+ (i++);
				Stats stats = Stats.Generator.INSTANCE.create(details, key);
				stats.setVal(ParserStatsItems.PassFail, status.FAIL);
				types.put(key, stats);
				for(ParserPattern patt: pParseRule.patterns) {
					Matcher m = patt.p.matcher(line);
					if (m.matches()) {
						stats.setVal(ParserStatsItems.PassFail, patt.stat);
						stats.setVal(ParserStatsItems.Label, patt.label);
						for(String s: pParseRule.groups) {
							try {
								StatsItem si = details.getByKey(s);
								stats.setKeyVal(si, m.group(s));
							} catch (Exception e) {
							}
						}
						break;
					}
				}
				stats.setVal(ParserStatsItems.Data, line);
			}
			br.close();
			details.createFilters(types);
			return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, false, timer.getDuration());
	}

	public String toString() {
		return "Multi Parser";
	}
	public String getDescription() {
		return "This rule will parse each line of a file and add it to the results table.\n" +
				"This rule takes advantage of named groups in a regular expression match.\n" +
				"[COLS]\n" +
				"FIRST,LAST,ID,COST\n" +
				"[FILTERS]\n" +
				"LAST,COST\n" +
				"[PATTERNS]\n" +
				"# Sample Comment 1\n" +
				"[CATEGORY: SKIP: label]\n" +
				"^(?<FIRST>[^\\t\\-]+)-(?<ID>[^\\t]+)\\t(?<LAST>[^\\t]+).*\\$(?<COST>\\d+).*$\n" +
				"^(?<FIRST>[^\\t\\-]+)-(?<ID>[^\\t]+)\\t(?<LAST>[^\\t]+).*$\n" +
				"# Sample Comment 2\n" +
				"[CATEGORY: WARN: label]";
	}
	public String getShortName() {
		return "MultiParse";
	}

}
