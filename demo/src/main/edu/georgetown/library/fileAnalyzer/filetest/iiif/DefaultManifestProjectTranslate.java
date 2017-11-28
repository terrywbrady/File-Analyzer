package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;

public enum DefaultManifestProjectTranslate implements ManifestProjectTranslate {
        Default,
        Hoya {
                public String translate(String key, String value) {
                        return value;
                }                
        },
        UAPhotosDate {
                public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                        return itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY) + "." + super.getSequenceValue(count, itemMeta);
                }
        },
        UAPhotosArch {
                public boolean includeItem(MetadataInputFile itemMeta) {
                        return itemMeta.getValue(IIIFLookup.Subject, "").toLowerCase().contains("hall");
                }
        }
        ;

        public String translate(String key, String value) {
                return value;
        }
        
        public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                return String.format("%06d", count);
        }

        public boolean includeItem(MetadataInputFile itemMeta) {
                return true;
        }
}
