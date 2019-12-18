package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.IOException;

import edu.georgetown.library.fileAnalyzer.util.TarUtil;

public class TarBagHelper extends FABagHelper {
    
    public TarBagHelper(File source) {
    	super(source);
    }
    
    @Override public void writeBagFile() throws IOException, IncompleteSettingsException {
    	super.writeBagFile();

    	data.newBag = TarUtil.tarFolderAndDeleteFolder(data.newBag);
    }
    
}
