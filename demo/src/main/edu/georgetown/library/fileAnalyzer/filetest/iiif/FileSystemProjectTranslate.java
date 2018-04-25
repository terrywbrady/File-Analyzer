package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

public class FileSystemProjectTranslate extends FolderProjectTranslate {
        RangePath containers;
        
        @Override
        public void initProjectRanges(IIIFManifest manifest, File root, RangePath top) {
                super.initProjectRanges(manifest, root, top);
                containers = RangePath.makeRangePath(manifest, "ZZContainers", "Containers");
                if (showFolderRanges()) {
                        top.addChildRange(containers);
                }
                dirPaths.put(root.getAbsolutePath(), containers);
        }

        @Override
        public RangePath makeRangePath(IIIFManifest manifest, File f) {
                RangePath rp = RangePath.makeRangePath(manifest, getRelPath(f), rangeTranslate(f.getName()));
                RangePath lastrp = rp;
                for(File parent = f.getParentFile(); parent != null; parent = parent.getParentFile()) {
                        if (dirPaths.containsKey(parent.getAbsolutePath())) {
                                RangePath parrp = dirPaths.get(parent.getAbsolutePath());
                                parrp.addChildRange(lastrp);
                                lastrp.setParent(parrp);
                                break;
                        }
                        
                        RangePath parrp = RangePath.makeRangePath(manifest, getRelPath(parent), rangeTranslate(parent.getName()));
                        parrp.addChildRange(lastrp);
                        lastrp.setParent(parrp);
                        dirPaths.put(parent.getAbsolutePath(), parrp);
                        lastrp = parrp;
                }
                return rp;
        }
        
}
