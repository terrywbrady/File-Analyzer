package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class RangePath extends IIIFJSONWrapper implements Comparable<RangePath> {
        String orderedPath;
        String displayPath;
        TreeSet<RangePath> childRanges = new TreeSet<>();
        RangePath parentRange;
        TreeSet<IIIFCanvasWrapper> childCanvases = new TreeSet<>();
        private static HashMap<String, RangePath> cache = new HashMap<>();
        boolean hasMetadata = false;
        
        public static RangePath makeRangePath(IIIFManifest manifest, String orderedPath, String displayPath) {
                RangePath rp = cache.get(orderedPath);
                if (rp == null) {
                        rp = new RangePath(manifest, orderedPath, displayPath);
                        cache.put(orderedPath, rp);
                }
                return rp;
        }

        public static RangePath makeEmptyRangePath(IIIFManifest manifest) {
                return makeRangePath(manifest, "", "");
        }
        

        public static void clearCache() {
                cache.clear();
        }
        
        private RangePath(IIIFManifest manifest, String orderedPath, String displayPath) {
                super(manifest.iiifRootPath, manifest.getManifestProjectTranslate());
                this.orderedPath = orderedPath;
                this.displayPath = displayPath;

                String label = manifestProjectTranslate.translate(IIIFType.typeRange, IIIFStandardProp.label, displayPath);
                setProperty(IIIFType.typeRange, IIIFStandardProp.label, label);
                setProperty(IIIFType.typeRange, IIIFStandardProp.id, getID());
                setProperty(IIIFType.typeRange);
                if (!orderedPath.isEmpty()) {
                        manifest.getArray(IIIFArray.structures).put(getJSONObject());                        
                }
        }
        
        public boolean hasMetadata() {
                return hasMetadata;
        }
        
        public void setHasMetadata(boolean b) {
                hasMetadata = b;
        }
        
        public void setDisplayPath(String s) {
                displayPath = s;
        }
        
        @Override
        public int compareTo(RangePath rp) {
                return orderedPath.compareTo(rp.orderedPath);
        }
        
        public boolean isEmpty() {
                return orderedPath.isEmpty();
        }
        
        public String getID() {
                return "r" + orderedPath.replaceAll("[ \\|]", "");
        }
        
        public void addChildRange(RangePath child) {
                this.childRanges.add(child);
        }
        
        public Iterable<RangePath> getOrderedChildren() {
                return this.childRanges;
        }
        
        public void setParent(RangePath parent) {
                this.parentRange = parent;
        }
        
        public List<RangePath> getDescendants() {
                ArrayList<RangePath> list = new ArrayList<>();
                appendDescendants(list);
                return list;
        }

        private void appendDescendants(List<RangePath> list) {
                list.add(this);
                for(RangePath child: getOrderedChildren()) {
                        child.appendDescendants(list);
                }
        }

        public void addCanvas(IIIFCanvasWrapper cw) {
                this.childCanvases.add(cw);
        }
        
        public List<String> getCanvasIds() {
                ArrayList<String> childIds = new ArrayList<>();
                for(IIIFCanvasWrapper icw: childCanvases) {
                        childIds.add(icw.getId());
                }
                return childIds;
        }
        
        public String getFullPath() {
                StringBuilder sb = new StringBuilder();
                for(RangePath rp = this; rp != null; rp = rp.parentRange) {
                        sb.insert(0, "/");
                        sb.insert(0, rp.displayPath);
                }
                return sb.toString();
        }
}
