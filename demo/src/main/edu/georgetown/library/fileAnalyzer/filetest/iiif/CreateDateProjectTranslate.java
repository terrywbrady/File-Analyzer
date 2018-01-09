package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookupEnum;

public class CreateDateProjectTranslate extends DefaultManifestProjectTranslate {
        TreeMap<String, RangePath> decadeRanges = new TreeMap<>();
        RangePath decadesRangeRoot;
        
        @Override
        public void initProjectRanges(IIIFManifest manifest, File root, RangePath top) {
                decadesRangeRoot = top;
        }

        @Override
        public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                return itemMeta.getValue(IIIFLookupEnum.DateCreated.getLookup(), IIIFManifest.EMPTY) + "_" + super.getSequenceValue(count, itemMeta);
        }
        @Override public String getSubtitle() {return "By Creation Date";}
        
        @Override
        public RangePath getPrimaryRangePath(IIIFManifest manifest, String key, File f, MetadataInputFile itemMeta) {
                String decade = getDecade(itemMeta.getValue(IIIFLookupEnum.DateCreated.getLookup(), IIIFManifest.EMPTY));
                RangePath dr = decadeRanges.get(decade);
                if (dr == null) {
                        dr = new RangePath(manifest, decade, decade);
                        decadeRanges.put(decade, dr);
                }
                decadesRangeRoot.addChildRange(dr);
                return dr;
        }
}
