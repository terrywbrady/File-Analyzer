package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;

import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public interface ManifestProjectTranslate {
        public String getSubtitle();
        public String name();
        public String getSequenceValue(int count, MetadataInputFile itemMeta); 
        public boolean includeItem(MetadataInputFile itemMeta);
        public String translate(IIIFType type, IIIFProp key, String val); 
        public String getRangeNames(String key, File f, MetadataInputFile itemMeta);
        public String rangeTranslate(String val);
        public JSONObject getParentRange(String rangePath, JSONObject top, TreeMap<String,JSONObject> orderedRanges);
}
