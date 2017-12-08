package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.List;
import java.util.TreeMap;

import javax.xml.xpath.XPath;

import org.json.JSONObject;
import org.w3c.dom.Node;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public interface ManifestProjectTranslate {
        public String getSubtitle();
        public String getSequenceValue(int count, MetadataInputFile itemMeta); 
        public boolean includeItem(MetadataInputFile itemMeta);
        public String translate(IIIFType type, IIIFProp key, String val); 
        public List<String> getRangeNames(String key, File f, MetadataInputFile itemMeta);
        public String rangeTranslate(String val);
        public void registerEADRange(XPath xp, Node n, String rangePath);
        public JSONObject getParentRange(String rangePath, JSONObject top, TreeMap<String,JSONObject> orderedRanges);
        public String getPrimaryRangeName(String key, File f, MetadataInputFile itemMeta); 
}
