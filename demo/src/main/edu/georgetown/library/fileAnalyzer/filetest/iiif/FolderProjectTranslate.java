package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookupEnum;

public abstract class FolderProjectTranslate extends DefaultManifestProjectTranslate {
        File root;
        TreeMap<String,RangePath> dirPaths = new TreeMap<>();
        RangePath top;
        
        @Override
        public void initProjectRanges(IIIFManifest manifest, File root, RangePath top) {
                this.root = root;
                this.top = top;
        }
        @Override
        public boolean showFolderRanges() {
                return true;
        }
        
        public String getRelPath(File f) {
                return f.getAbsolutePath().substring(root.getAbsolutePath().length()).replaceAll("[\\\\\\/]", "_");
        }
        
        public abstract RangePath makeRangePath(IIIFManifest manifest, File f);
        
        @Override
        public RangePath getPrimaryRangePath(IIIFManifest manifest, String key, File f, MetadataInputFile itemMeta) {
                if (dirPaths.containsKey(f.getAbsolutePath())) {
                        return dirPaths.get(f.getAbsolutePath());
                }
                RangePath rp = makeRangePath(manifest, f);
                if (!manifest.isCollectionManifest() && manifest.getManifestProjectTranslate().isOneItemPerRange()) {
                        rp.setDisplayPath(itemMeta.getValue(IIIFLookupEnum.Title.getLookup(), rp.displayPath));
                }
                dirPaths.put(f.getAbsolutePath(), rp);
                return rp;
        }

}
