package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public interface ManifestProjectTranslate {
        public String getSubtitle();
        public String getSequenceValue(int count, MetadataInputFile itemMeta); 
        public boolean includeItem(MetadataInputFile itemMeta);
        public String translate(IIIFType type, IIIFProp key, String val); 
        public String rangeTranslate(String val);
        public void registerEADRange(XPath xp, Node n, RangePath rangePath);
        public RangePath getPrimaryRangePath(String key, File f, MetadataInputFile itemMeta); 
        public boolean processInitRanges();
        public boolean showFolderRanges();
        public void initProjectRanges(File root, RangePath top);
}
