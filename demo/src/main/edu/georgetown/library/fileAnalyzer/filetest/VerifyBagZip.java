package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class VerifyBagZip extends VerifyBag { 
    public static StatsItemConfig details = StatsItemConfig.create(BagStatsItems.class);

    public VerifyBagZip(FTDriver dt) {
        super(dt);
    }

    public String toString() {
        return "Verify Bag - Zip";
    }
    public String getShortName(){return "Ver Bag Zip";}
    
    public String getDescription() {
        return "This rule will validate the contents of a bag zip file";
    }
    
    @Override public boolean isTestDirectory() {
    	return false;
    }
    @Override public boolean processRoot() {
        return false;
    }

    @Override public boolean isTestFiles() {
        return true; 
    }

    @Override public boolean isTestable(File f) {
    	return f.getName().toLowerCase().endsWith(".zip");
    }

	public void initFilters() {
		filters.add(new ZipFilter());
	}
    
}
