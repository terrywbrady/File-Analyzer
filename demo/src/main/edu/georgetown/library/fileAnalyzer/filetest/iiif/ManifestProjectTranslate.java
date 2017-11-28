package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public interface ManifestProjectTranslate {
        public String getSequenceValue(int count, MetadataInputFile itemMeta); 
        public boolean includeItem(MetadataInputFile itemMeta);
        public String translate(IIIFType type, IIIFProp key, String val); 
        public String getRangeName(String key, File f, MetadataInputFile itemMeta);
}
