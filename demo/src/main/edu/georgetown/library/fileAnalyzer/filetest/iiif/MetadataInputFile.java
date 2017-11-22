package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public interface MetadataInputFile {
        public File getFile();
        public String getValue(IIIFLookup key, String def);
        public InputFileType getInputFileType();
        public void setCurrentKey(String key);

}
