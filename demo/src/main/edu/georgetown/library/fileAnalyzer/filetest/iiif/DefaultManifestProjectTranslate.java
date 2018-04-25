package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class DefaultManifestProjectTranslate implements ManifestProjectTranslate {
        @Override
        public String getSequenceValue(String key, MetadataInputFile itemMeta) {
                return key;
        }

        @Override
        public boolean includeItem(MetadataInputFile itemMeta) {
                return true;
        }

        public String getSubtitle() {return "";}
        
        @Override
        public String translate(IIIFType type, IIIFProp key, String val) {
                if (type == IIIFType.typeManifest && key == IIIFStandardProp.label) {
                        String suff = getSubtitle().isEmpty() ? "" : " - " + getSubtitle();
                        return val + suff;
                }
                if (type == IIIFType.typeRange && key == IIIFStandardProp.label) {
                        return rangeTranslate(val);
                }
                return val;
        }

        @Override
        public RangePath getPrimaryRangePath(IIIFManifest manifest, String key, File f, MetadataInputFile itemMeta) {
                String s = manifest.getProperty(IIIFStandardProp.label, "");
                return RangePath.makeRangePath(manifest, s, s);
        }

        public static String getDecade(String dateCreated) {
                Pattern p = Pattern.compile("^(\\d\\d\\d)\\d.*");
                Matcher m = p.matcher(dateCreated);
                if (m.matches()) {
                        int year = Integer.parseInt(m.group(1));
                        return String.format("%d0 - %d", year, year*10+9);
                }

                return "Date Unknown";

        }

        @Override
        public String rangeTranslate(String val) {
                return val;
        }

        @Override
        public void registerEADRange(XPath xp, Node n, RangePath rangePath) {
        }

        @Override
        public boolean processInitRanges() {
                return false;
        }
        @Override
        public void initProjectRanges(IIIFManifest manifest, File root, RangePath top) {
        }

        @Override
        public boolean showFolderRanges() {
                return false;
        }

        @Override
        public boolean isOneItemPerRange() {
                 return false;
        }

}
