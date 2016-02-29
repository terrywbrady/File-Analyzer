package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;

public class FABagHelperData {
    public File parent;
    public File source;
    public File newBag;
    public Bag bag;
    public BagFactory bf;

    public FABagHelperData() {
    }
}