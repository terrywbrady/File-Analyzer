package edu.georgetown.library.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.TreeMap;

public class DemoImporter extends DefaultImporter {
    private enum DEMO {ONE,TWO,THREE;}
    private static enum DemoStatsItems implements StatsItemEnum {
        Key(StatsItem.makeStringStatsItem("Path", 200)),
        Name(StatsItem.makeStringStatsItem("Name")),
        Data(StatsItem.makeStringStatsItem("Param")),
        EnumVal(StatsItem.makeEnumStatsItem(DEMO.class, "Sample Enum"))
        ;
        
        StatsItem si;
        DemoStatsItems(StatsItem si) {this.si=si;}
        public StatsItem si() {return si;}
    }
	public static enum Generator implements StatsGenerator {
		INSTANCE;

		public Stats create(String key) {return new Stats(details, key);}
	}
	
	public static StatsItemConfig details = StatsItemConfig.create(DemoStatsItems.class);

    public static final String PARAM = "param";
	public DemoImporter(FTDriver dt) {
		super(dt);
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  PARAM, PARAM,
                "Sample Parameter", ""));
	}

	public ActionResult importFile(File selectedFile) throws IOException {
        Timer timer = new Timer();
	    TreeMap<String,Stats> types = new TreeMap<String,Stats>();
	    NumberFormat nf = NumberFormat.getIntegerInstance();
	    nf.setMinimumIntegerDigits(8);
	    nf.setGroupingUsed(false);
	    int lineno = 0;
	    try(BufferedReader br = new BufferedReader(new FileReader(selectedFile))){
	        for(String line=br.readLine(); line != null; line = br.readLine()) {
	            lineno++;
	            String key = nf.format(lineno);
	            Stats stats = Generator.INSTANCE.create(key);
	            types.put(key, stats);
	            
	            stats.setVal(DemoStatsItems.Data, this.getProperty(PARAM));
	            stats.setVal(DemoStatsItems.Name, line.length() > 10 ? line.substring(0, 9) : line);
	            stats.setVal(DemoStatsItems.EnumVal, DEMO.TWO);
	        }
	    }		

		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
	
	public String toString() {
		return "Demo Importer";
	}
	public String getDescription() {
		return "This process does nothing but read the lines of a text file.  It is a placeholder for creating a new importer.";
	}
	public String getShortName() {
		return "Demo";
	}
}
