package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;

/**
 * @author TBrady
 *
 */
class DemoFileTest extends DefaultFileTest { 
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

	long counter = 1000000;
	public static final String PARAM = "param";
	public DemoFileTest(FTDriver dt) {
		super(dt);
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  PARAM, PARAM,
				"Sample Parameter", ""));
	}

	public String toString() {
		return "Demo File Test";
	}
    public String getKey(File f) {
        return getRelPath(f);
    }
	
    public String getShortName(){return "Demo";}

    
	public Object fileTest(File f) {
		Stats s = getStats(f);
		s.setVal(DemoStatsItems.Data, this.getProperty(PARAM));
        s.setVal(DemoStatsItems.Name, f.getName());
		s.setVal(DemoStatsItems.EnumVal, DEMO.TWO);
		return s;
	}
    public Stats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details; 
    }

	public void initFilters() {
        initAllFilters();
		//filters.add(new PdfFileTestFilter());
	}

	public String getDescription() {
		return "This task does nothing.  This is a sample place holder for adding custom code.";
	}

}
