package gov.nara.nwts.ftapp.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.DefaultFileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class DigitalDerivatives extends DefaultFileTest { 
	public static enum FOUND {
		NOT_FOUND,
		FOUND,
		FOUND_DUP
		;
	}

	public static enum STAT {
		INCOMPLETE,
		COMPLETE,
		EXTRA
	}
	
	private static enum DerivStatsItems implements StatsItemEnum {
		Basename(StatsItem.makeStringStatsItem("Basename", 200)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class,"Status")),
		Count(StatsItem.makeIntStatsItem("Count")),
		CountExtra(StatsItem.makeIntStatsItem("Count Extra")),
		Extras(StatsItem.makeStringStatsItem("Extra Items")),
		;
		
		StatsItem si;
		DerivStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		class DerivStats extends Stats {
			public DerivStats(String key) {
				super(details, key);
			}

		}
		public DerivStats create(String key) {return new DerivStats(key);}
	}
	public static StatsItemConfig details = StatsItemConfig.create(DerivStatsItems.class);

	long counter = 1000000;
	
	public static final String REGX_MATCH = "regex-match";
	public static final String REGX_REPLACE = "regex-replace";
	public static final String EXT_REQ = "file-extensions-req";
	public static final String EXT_OPT = "file-extensions-opt";
	
	public static final String DEF_MATCH = "^(.*)\\.[^\\.]+$";
	public static final String DEF_REPLACE = "$1";
	
	public DigitalDerivatives(FTDriver dt) {
		super(dt);
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  REGX_MATCH, REGX_MATCH,
				"Regex pattern to compute basename", DEF_MATCH));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  REGX_REPLACE, REGX_REPLACE,
				"Regex replacement for basename", DEF_REPLACE));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  EXT_REQ, EXT_REQ,
				"Required file extensions to report (lower-case, comma separated list)", "tif,jpg,xml"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  EXT_OPT, EXT_OPT,
				"Optional file extensions to report (lower-case, comma separated list)", ""));
		extensions = new Vector<String>();
		extensionsReq = new Vector<String>();
	}

	public String toString() {
		return "Digital Derivatives";
	}
	
	String match;
	Pattern pMatch;
	String replace;
	Vector<String>extensions;
	Vector<String>extensionsReq;
	
	@Override public InitializationStatus init() {
		match = getProperty(REGX_MATCH, DEF_MATCH).toString();
		try {
			pMatch = Pattern.compile(match);
		} catch (Exception e) {
		}
		replace = getProperty(REGX_REPLACE, DEF_REPLACE).toString();
		
		details = StatsItemConfig.create(DerivStatsItems.class);
		extensions.clear();
		extensionsReq.clear();

		String ext = getProperty(EXT_REQ, "").toString();
		for(String s:ext.split(",")) {
			if (s.isEmpty()) continue;
			s = s.trim().toLowerCase();
			extensions.add(s);
			extensionsReq.add(s);
			details.addStatsItem(s, StatsItem.makeEnumStatsItem(FOUND.class, s+"*"));
		}
		ext = getProperty(EXT_OPT, "").toString();
		for(String s:ext.split(",")) {
			if (s.isEmpty()) continue;
			s = s.trim().toLowerCase();
			extensions.add(s);
			details.addStatsItem(s, StatsItem.makeEnumStatsItem(FOUND.class, s));
		}
        return super.init();
	}
	
	public String getExt(String test) {
		test = test.toLowerCase();
		for(String ext: extensions) {
			if (test.endsWith(ext)) return ext;
		}
		return null;
	}

	//public boolean isTestable(File f) {
	//	return getExt(f.getName()) != null;
	//}
	
	public String getKey(File f) {
		String s = f.getName().toLowerCase();
		if (pMatch == null) return s;
		Matcher m = pMatch.matcher(s);
		if (m.matches()) {
			s = m.replaceFirst(replace);
		}
		return s;
	}
	
    public String getShortName(){return "Deriv";}

    
	public Object fileTest(File f) {
		Stats s = getStats(f);
		String suff = f.getName().toLowerCase().substring(s.key.length()); 
		s.sumVal(DerivStatsItems.Count, 1);
		
		String ext = getExt(f.getName().toLowerCase());
		if (ext == null) {
			s.sumVal(DerivStatsItems.CountExtra, 1);
			s.appendVal(DerivStatsItems.Extras, suff+" ");
		} else {
			StatsItem si = details.getByKey(ext);
			if (si == null) return null;
			FOUND found = (FOUND) s.getKeyVal(si, FOUND.NOT_FOUND);
			if (found == FOUND.NOT_FOUND) {
				found = FOUND.FOUND;
			} else if (found == FOUND.FOUND) {
				found = FOUND.FOUND_DUP;
			}
			s.setKeyVal(si, found);			
		}
		return s.key;
	}
	
	@Override public void refineResults() {
		for(Stats curr: dt.types.values()) {
			curr.setVal(DerivStatsItems.Stat, STAT.COMPLETE);
			
			for(String s: extensionsReq) {
				StatsItem si = details.getByKey(s);
				if (si == null) continue;
				FOUND found = (FOUND)curr.getKeyVal(si, FOUND.NOT_FOUND);
				if (found == FOUND.NOT_FOUND) {
					curr.setVal(DerivStatsItems.Stat, STAT.INCOMPLETE);
				}
			}
			if (curr.getVal(DerivStatsItems.Stat) == STAT.INCOMPLETE) continue;
			if (curr.getIntVal(DerivStatsItems.CountExtra) > 0) {
				curr.setVal(DerivStatsItems.Stat, STAT.EXTRA);
				continue;
			} 
			for(String s: extensions) {
				StatsItem si = details.getByKey(s);
				if (si == null) continue;
				FOUND found = (FOUND)curr.getKeyVal(si, FOUND.NOT_FOUND);
				if (found == FOUND.FOUND_DUP) {
					curr.setVal(DerivStatsItems.Stat, STAT.EXTRA);
				}
			}
		}
	}
	
    public Stats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details; 
    }

	public void initFilters() {
		filters.add(new DefaultFileTestFilter());
	}

	public String getDescription() {
		return "";
	}

}
