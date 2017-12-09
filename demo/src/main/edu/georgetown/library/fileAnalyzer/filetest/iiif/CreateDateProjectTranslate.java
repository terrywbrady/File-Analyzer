package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;

public class CreateDateProjectTranslate extends DefaultManifestProjectTranslate {
        @Override
        public String getSequenceValue(int count, MetadataInputFile itemMeta) {
                return itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY) + "_" + super.getSequenceValue(count, itemMeta);
        }
        @Override public String getSubtitle() {return "By Creation Date";}
        
        @Override
        public RangePath getPrimaryRangePath(String key, File f, MetadataInputFile itemMeta) {
                String decade = getDecade(itemMeta.getValue(IIIFLookup.DateCreated, IIIFManifest.EMPTY));
                return new RangePath(decade, decade);
        }
}
