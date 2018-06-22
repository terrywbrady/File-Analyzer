package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;

import java.io.File;
import java.io.IOException;

import edu.georgetown.library.fileAnalyzer.util.AIPDirToAPTHelper;
import edu.georgetown.library.fileAnalyzer.util.AIPToAPTHelper;


/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class AIPDirToAPT extends AIPToAPT { 
    public AIPDirToAPT(FTDriver dt) {
        super(dt);
    }

    public String toString() {
        return "Package AIP Dir for APT";
    }
    public String getShortName(){return "AIP Dir->APT";}
    
    public String getDescription() {
        return "This rule will package a DSpace AIP Directory for APTrust";
    }
    
    @Override public boolean isTestDirectory() {
        return true;
    }

    @Override public boolean isTestable(File f) {
        return f.equals(getRoot());
    }

    @Override public boolean isTestFiles() {
        return false; 
    }
    
    @Override public boolean processRoot() {
        return true;
    }

    @Override
    public AIPToAPTHelper getAIPToAPTHelper() throws IOException {
        return new AIPDirToAPTHelper();
    }

}
