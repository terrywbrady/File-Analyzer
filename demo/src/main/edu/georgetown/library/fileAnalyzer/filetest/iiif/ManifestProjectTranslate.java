package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import org.json.JSONObject;

public interface ManifestProjectTranslate {
        public static final String IDENTIFIER = "identifier";
        public String translate(String key, String value);
        public String getSequenceValue(int count, JSONObject canvas); 
}
