package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import gov.loc.repository.bagit.Bag;

public class AIPDirToAPTHelper extends AIPToAPTHelper {

    File outdir;
    public AIPDirToAPTHelper() {
    }
    
    @Override
    public int fillBag(File f, APTrustHelper aptHelper) throws FileNotFoundException, IOException, InvalidMetadataException {
        Bag bag = aptHelper.getBag();
        for(File cf: f.listFiles()) {
            bag.addFileToPayload(cf);
        }
        findMetadata(f, aptHelper);
        return bag.getPayload().size();
    }

    public void findMetadata(File f, APTrustHelper aptHelper) throws IOException, InvalidMetadataException {
        if (f.isDirectory()) {
            for(File cf: f.listFiles()) {
                findMetadata(cf, aptHelper);
            }
        } else {
            if (f.getName().equals(METSXML)) {
                aptHelper.parseMetsFile(f);
            }
        }
    }


}
