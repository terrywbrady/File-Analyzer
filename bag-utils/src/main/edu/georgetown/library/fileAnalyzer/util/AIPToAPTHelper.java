package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class AIPToAPTHelper {
    public static final String AIPEXTRACT = "aipextract_";
    public static final String METSXML = "mets.xml";
    public AIPToAPTHelper(){}
    
    abstract public int fillBag(File f, APTrustHelper aptHelper) throws FileNotFoundException, IOException, InvalidMetadataException;
    public int bag(File f, APTrustHelper aptHelper) throws IOException, IncompleteSettingsException, InvalidMetadataException {
        int count = fillBag(f, aptHelper);
        aptHelper.createBagFile();
        aptHelper.generateBagInfoFiles();
        aptHelper.writeBagFile();
        return count;
    }


}
