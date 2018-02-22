package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.util.ArrayList;
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
        ArrayList<String> childCanvases = new ArrayList<>();
        boolean hasMetadata = false;
        
        public RangePath(IIIFManifest manifest, String orderedPath, String displayPath) {
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

        public void addCanvasId(String canvasid) {
                this.childCanvases.add(canvasid);
        }
        
        public List<String> getCanvasIds() {
                return this.childCanvases;
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
