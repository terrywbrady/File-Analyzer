package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;

public enum DefaultManifestProjectTranslate implements ManifestProjectTranslate {
        Default,
        Hoya {
                public String translate(String key, String value) {
                        return value;
                }                
        },
        UAPhotos {
                public String getSequenceValue(int count, JSONObject canvas) {
                        return IIIFManifest.getProperty(canvas, IIIFProp.dateCreated, "") + "." + super.getSequenceValue(count, canvas);
                }
        }
        ;

        public String translate(String key, String value) {
                return value;
        }
        
        public String getSequenceValue(int count, JSONObject canvas) {
                return String.format("%06d", count);
        }
}
