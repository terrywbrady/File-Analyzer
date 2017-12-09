package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;

public class CreateDateProjectTranslate extends DefaultManifestProjectTranslate {
        TreeMap<String, RangePath> decadeRanges = new TreeMap<>();
        RangePath decadesRangeRoot;
        
        @Override
        public void initProjectRanges(File root, RangePath top) {
                decadesRangeRoot = top;
        }

        @Override
        public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                return itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY) + "_" + super.getSequenceValue(count, itemMeta);
        }
        @Override public String getSubtitle() {return "By Creation Date";}
        
        @Override
        public RangePath getPrimaryRangePath(String key, File f, MetadataInputFile itemMeta) {
                String decade = getDecade(itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY));
                RangePath dr = decadeRanges.get(decade);
                if (dr == null) {
                        dr = new RangePath(decade, decade);
                        decadeRanges.put(decade, dr);
                }
                decadesRangeRoot.addChildRange(dr);
                return dr;
        }
}
