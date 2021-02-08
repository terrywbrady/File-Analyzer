package gov.nara.nwts.ftapp.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.stats.DirStats;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.text.Normalizer;

/**
 * Create FileAnalyzer statistics by directory.
 * @author TBrady
 *
 */
public class DirMatch extends DefaultFileTest {

	public DirMatch(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "Match By RelPath";
	}
	public String getKey(File f) {
		return getKey(f, f.getParentFile());
	}
	
	public String getKey(File f, Object parentdir) {
		String key = "";
		if (parentdir instanceof File) {
			key = f.getPath().substring(((File)parentdir).getPath().length());
		}
		return Normalizer.normalize(key, Normalizer.Form.NFD);;		
	}
	
    public String getShortName(){return "Path";}

	public Object fileTest(File f) {
		return null;
	}
    public Stats createStats(String key){ 
    	return DirStats.Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return DirStats.details;
    }
	public void initFilters() {
		initAllFilters();
	}

	public String getDescription() {
		return "Report on items by relative path.";
	}

}
