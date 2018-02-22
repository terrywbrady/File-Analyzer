package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

public class ItemFolderProjectTranslate extends FolderProjectTranslate {
        @Override
        public RangePath makeRangePath(IIIFManifest manifest, File f) {
                RangePath rp = new RangePath(manifest, getRelPath(f), rangeTranslate(f.getName()));
                top.addChildRange(rp);
                return rp;
        }
        @Override
        public boolean isOneItemPerRange() {
                 return true;
        }
}
