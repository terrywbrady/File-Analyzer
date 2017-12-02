package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public enum DefaultManifestProjectTranslate implements ManifestProjectTranslate {
        Default,
        ByCreationDate {
                @Override
                public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                        return itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY) + "_" + super.getSequenceValue(count, itemMeta);
                }
                @Override public String getSubtitle() {return "By Creation Date";}
                
                @Override
                public String getRangeNames(String key, File f, MetadataInputFile itemMeta) {
                        return getDecade(itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY));
                }
        }
        ;

        @Override
        public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                return String.format("%06d", count);
        }

        @Override
        public boolean includeItem(MetadataInputFile itemMeta) {
                return true;
        }

        public String getSubtitle() {return "";}
        
        @Override
        public String translate(IIIFType type, IIIFProp key, String val) {
                if (type == IIIFType.typeManifest && key == IIIFProp.label) {
                        String suff = getSubtitle().isEmpty() ? "" : " - " + getSubtitle();
                        return val + suff;
                }
                if (type == IIIFType.typeRange && key == IIIFProp.label) {
                        return rangeTranslate(val);
                }
                return val;
        }

        @Override
        public String getRangeNames(String key, File f, MetadataInputFile itemMeta) {
                return IIIFManifest.EMPTY;
        }

        public static String getDecade(String dateCreated) {
                Pattern p = Pattern.compile("^(\\d\\d\\d)\\d.*");
                Matcher m = p.matcher(dateCreated);
                if (m.matches()) {
                        int year = Integer.parseInt(m.group(1));
                        return String.format("%d0 - %d0", year, year+1);
                }

                return "Date Unknown";

        }

        @Override
        public String rangeTranslate(String val) {
                return val;
        }

        @Override
        public JSONObject getParentRange(String rangePath, JSONObject top, TreeMap<String,JSONObject> orderedRanges) {
                return top;
        }
}
