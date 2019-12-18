package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filter.ZipFilter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import edu.georgetown.library.fileAnalyzer.util.AIPToAPTHelper;
import edu.georgetown.library.fileAnalyzer.util.AIPZipToAPTHelper;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class AIPZipToAPT extends AIPToAPT { 
	
    public AIPZipToAPT(FTDriver dt) {
        super(dt);
    }

	public String getKey(File f) {
		return f.getName();
	}
    public String toString() {
        return "Package AIP Zip for APT";
    }
    public String getShortName(){return "AIP Zip->APT";}
    
    public String getDescription() {
        return "This rule will package a DSpace AIP Zip package for APTrust";
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

	private File outdir;

	@Override public void cleanup(int count) {
		if (outdir != null) {
			try {
                FileUtils.deleteDirectory(outdir);
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
    @Override
    public AIPToAPTHelper getAIPToAPTHelper() throws IOException {
        outdir = AIPZipToAPTHelper.createTempDir();
        return new AIPZipToAPTHelper(outdir);
    }
	
}
