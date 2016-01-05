package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filter.TarFilter;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;

import java.io.File;
import java.io.IOException;

import edu.georgetown.library.fileAnalyzer.util.TarUtil;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class VerifyBagTar extends VerifyBag { 
    public static StatsItemConfig details = StatsItemConfig.create(BagStatsItems.class);

    public VerifyBagTar(FTDriver dt) {
        super(dt);
    }

    public String toString() {
        return "Verify Bag - Tar";
    }
    public String getShortName(){return "Ver Bag Tar";}
    
    public String getDescription() {
        return "This rule will validate the contents of a bag tar file";
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
    	return f.getName().toLowerCase().endsWith(".tar");
    }

	@Override public void initFilters() {
		filters.add(new TarFilter());
	}
    
	@Override public File prepareFile(File f) throws IOException {
		File untarred = TarUtil.untar(f);
		if (untarred.listFiles().length == 1) {
			return untarred.listFiles()[0];
		}
		return untarred;
    }
   
}
