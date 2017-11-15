package edu.georgetown.library.fileAnalyzer.filetest.iiif;

public enum DefaultManifestProjectTranslate implements ManifestProjectTranslate {
        Default,
        Hoya {
                public String translate(String key, String value) {
                        return value;
                }                
        }
        ;

        public String translate(String key, String value) {
                return value;
        }
        
}
