package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.IOException;

import gov.loc.repository.bagit.writer.impl.ZipWriter;

public class ZipBagHelper extends FABagHelper {
    
    public ZipBagHelper(File source) {
    	super(source);
    }
    
    @Override public void createBagFile() throws IncompleteSettingsException {
    	validate();
		data.newBag = new File(data.parent, data.source.getName() + "_bag.zip");
    }
    
    @Override public void writeBagFile() throws IOException, IncompleteSettingsException {
    	validate();
    	if (data.newBag == null) throw new IncompleteSettingsException("Bag File must be created - call createBagFile()");
	    ZipWriter writer = new ZipWriter(data.bf); 
	    writer.setBagDir(data.source.getName());
	    data.bag.write(writer, data.newBag);
	    data.bag.close();
    }
    
}
