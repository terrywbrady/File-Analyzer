package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                @Override String getSubtitle() {return "By Creation Date";}
                
                @Override
                public String getRangeName(String key, File f, MetadataInputFile itemMeta) {
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

        String getSubtitle() {return "";}
        
        @Override
        public String translate(IIIFType type, IIIFProp key, String val) {
                if (type == IIIFType.typeManifest && key == IIIFProp.label) {
                        String suff = getSubtitle().isEmpty() ? "" : " - " + getSubtitle();
                        return val + suff;
                }
                return val;
        }

        @Override
        public String getRangeName(String key, File f, MetadataInputFile itemMeta) {
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
}
