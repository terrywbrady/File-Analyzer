package edu.georgetown.library.fileAnalyzer.filetest.iiif;

public interface ManifestProjectTranslate {
        public static final String IDENTIFIER = "identifier";
        public String translate(String key, String value);
        public String getSequenceValue(int count, MetadataInputFile itemMeta); 
        public boolean includeItem(MetadataInputFile itemMeta); 
}
