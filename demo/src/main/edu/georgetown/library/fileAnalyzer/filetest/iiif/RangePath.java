package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.json.JSONObject;

public class RangePath implements Comparable<RangePath> {
        String orderedPath;
        String displayPath;
        JSONObject range = null;
        TreeSet<RangePath> childRanges = new TreeSet<>();
        RangePath parentRange;
        ArrayList<String> childCanvases = new ArrayList<>();
        
        public RangePath(String orderedPath, String displayPath) {
                this.orderedPath = orderedPath;
                this.displayPath = displayPath;
        }
        
        public void setRangeObject(JSONObject range) {
                this.range = range;
        }
        
        public JSONObject getRangeObject() {
                return range;
        }

        public boolean hasObject() {
                return this.range != null;
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
}
