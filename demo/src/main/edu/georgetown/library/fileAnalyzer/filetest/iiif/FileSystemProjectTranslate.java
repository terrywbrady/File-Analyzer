package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.TreeMap;

public class FileSystemProjectTranslate extends DefaultManifestProjectTranslate {
        File root;
        RangePath containers;
        TreeMap<String,RangePath> dirPaths = new TreeMap<>();
        
        @Override
        public void initProjectRanges(File root, RangePath top) {
                this.root = root;
                containers = new RangePath("ZZContainers", "Containers");
                top.addChildRange(containers);
                dirPaths.put(root.getAbsolutePath(), containers);
        }

        public String getRelPath(File f) {
                return f.getAbsolutePath().substring(root.getAbsolutePath().length()).replaceAll("[\\\\\\/]", "_");
        }
        
        public RangePath makeRangePath(File f) {
                RangePath rp = new RangePath(getRelPath(f), f.getName());
                dirPaths.put(f.getAbsolutePath(), rp);
                return rp;
        }
        
        @Override
        public RangePath getPrimaryRangePath(String key, File f, MetadataInputFile itemMeta) {
                if (dirPaths.containsKey(f.getAbsolutePath())) {
                        return dirPaths.get(f.getAbsolutePath());
                }
                RangePath rp = makeRangePath(f);
                RangePath lastrp = rp;
                for(File parent = f.getParentFile(); parent != null; parent = parent.getParentFile()) {
                        if (dirPaths.containsKey(parent.getAbsolutePath())) {
                                dirPaths.get(parent.getAbsolutePath()).addChildRange(lastrp);
                                break;
                        }
                        
                        RangePath parrp = makeRangePath(parent);
                        parrp.addChildRange(lastrp);
                        dirPaths.put(parent.getAbsolutePath(), parrp);
                        lastrp = parrp;
                }
                return rp;
        }

}
