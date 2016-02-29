package gov.nara.nwts.ftapp.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DelimitedFileImporter.Separator;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

public class CountKey extends DefaultImporter {
	public static enum MULT {ONE, MANY;}
	private static enum CountStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Key", 100)),
		Count(StatsItem.makeIntStatsItem("Count")),
		Stat(StatsItem.makeEnumStatsItem(MULT.class, "Multiple?")),
        AltKeysAll(StatsItem.makeStringStatsItem("Alt Keys",150)),
        AltKeysFirst(StatsItem.makeStringStatsItem("Alt Keys First",150)),
        AltKeysSubseq(StatsItem.makeStringStatsItem("Alt Keys Sybseq",150))
		;
		
		StatsItem si;
		CountStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}
	public static enum Generator implements StatsGenerator {
		INSTANCE;

		public Stats create(String key) {return new Stats(details, key);}
	}
	
	public static StatsItemConfig details = StatsItemConfig.create(CountStatsItems.class);

	public static final String DELIM = "Delimiter";
	public static final String HEADROW = "HeadRow";
	public static final String COL = "COL";
    public static final String ALTCOL = "ALTCOL";
    public static final String DEDUP = "DeDup";
	public CountKey(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), DELIM, "delim",
				"Delimiter character separating fields", Separator.values(), Separator.Comma));
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), HEADROW, HEADROW,
				"Treat first row as header", YN.values(), YN.Y));
		this.ftprops.add(new FTPropString(dt, this.getClass().getName(), COL, COL,
				"Key Column starting at 1", "1"));
        this.ftprops.add(new FTPropString(dt, this.getClass().getName(), ALTCOL, ALTCOL,
                "Alt Key Column (optional)", ""));
        this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), DEDUP, DEDUP,
                "Create De-duplicated file set", YN.values(), YN.N));
	}

	public ActionResult importFile(File selectedFile) throws IOException {
		
		int col = 0;
		try {
			col = Integer.parseInt(this.getProperty(COL,"").toString());
			col--;
		} catch (NumberFormatException e) {
		}
        int altcol = -1;
        try {
            altcol = Integer.parseInt(this.getProperty(ALTCOL,"").toString());
            altcol--;
        } catch (NumberFormatException e) {
        }
		
		Separator fileSeparator = (Separator)getProperty(DELIM);
		Timer timer = new Timer();

		TreeMap<String,Stats> types = new TreeMap<String,Stats>();
		
		DelimitedFileReader dfr = new DelimitedFileReader(selectedFile, fileSeparator.separator);
		boolean firstRow = (YN)getProperty(HEADROW) == YN.Y;
        boolean dedup = (YN)getProperty(DEDUP) == YN.Y;
        DelimitedFileWriter bwDedup = null;
        DelimitedFileWriter bwDup = null;
        if (dedup) {
            bwDedup = new DelimitedFileWriter(getNewFile(selectedFile, "dedup"), fileSeparator.separator);
            bwDup = new DelimitedFileWriter(getNewFile(selectedFile, "dup-drop"), fileSeparator.separator);
        }
		
		firstRow = (YN)getProperty(HEADROW) == YN.Y;
		dfr = new DelimitedFileReader(selectedFile, fileSeparator.separator);
		for(Vector<String> cols = dfr.getRow(); cols != null; cols = dfr.getRow()){
			if (firstRow) {
				firstRow = false;
				if (dedup) {
				    bwDedup.writeRow(cols);
                    bwDup.writeRow(cols);
				}
				continue;
			}
			String key = cols.get(col < cols.size() ? col : 0);
            String altkey = (altcol < 0 || altcol >= cols.size()) ? "" : cols.get(altcol);
			Stats stats = types.get(key);
            if (stats == null) {
				stats = Generator.INSTANCE.create(key);
				stats.setVal(CountStatsItems.Count, 1);
				stats.setVal(CountStatsItems.Stat, MULT.ONE);
				stats.setVal(CountStatsItems.AltKeysAll, altkey);
                stats.setVal(CountStatsItems.AltKeysFirst, altkey);
				types.put(key, stats);
                if (dedup) {
                    bwDedup.writeRow(cols);
                }
			} else {
				stats.sumVal(CountStatsItems.Count, 1);
				stats.setVal(CountStatsItems.Stat, MULT.MANY);
                stats.appendVal(CountStatsItems.AltKeysAll, altkey, ", ");
                stats.appendVal(CountStatsItems.AltKeysSubseq, altkey, ", ");
                if (dedup) {
                    bwDup.writeRow(cols);
                }
			}
		}

        if (dedup) {
            bwDedup.close();
            bwDup.close();

            DelimitedFileWriter bwNoDup = new DelimitedFileWriter(getNewFile(selectedFile, "no-dup"), fileSeparator.separator);
            DelimitedFileWriter bwAllDup = new DelimitedFileWriter(getNewFile(selectedFile, "all-dup"), fileSeparator.separator);
            dfr = new DelimitedFileReader(selectedFile, fileSeparator.separator);
            firstRow = (YN)getProperty(HEADROW) == YN.Y;
            for(Vector<String> cols = dfr.getRow(); cols != null; cols = dfr.getRow()){
                if (firstRow) {
                    firstRow = false;
                    if (dedup) {
                        bwNoDup.writeRow(cols);
                        bwAllDup.writeRow(cols);
                    }
                    continue;
                }
                String key = cols.get(col < cols.size() ? col : 0);
                Stats stats = types.get(key);
                if (stats == null) {
                } else if (stats.getVal(CountStatsItems.Stat) == MULT.ONE) {
                    bwNoDup.writeRow(cols);
                } else {
                    bwAllDup.writeRow(cols);
                }
            }
            bwNoDup.close();
            bwAllDup.close();
        }


		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
	
	public String toString() {
		return "Count Key";
	}
	public String getDescription() {
		return "Count the number of times a key appears in a file.";
	}
	public String getShortName() {
		return "Key";
	}
    public String getExt(File f) {
        String ext = "";
        StringTokenizer st = new StringTokenizer(f.getName(), ".");
        while(st.hasMoreElements()) {
            ext = st.nextElement().toString();
        }   
        return ext;
    }
    
    public String getNewName(File f, String suff) {
        String ext = getExt(f);
        String bname = f.getName();
        return (ext.isEmpty()) ? bname + "." + suff : bname.replaceFirst(ext + "$", suff + "." + ext);
    }
    public File getNewFile(File f, String suff) {
        return new File(f.getParentFile(), getNewName(f, suff));
    }

}
