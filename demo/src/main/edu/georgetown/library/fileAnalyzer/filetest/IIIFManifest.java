package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;

public class IIIFManifest {
        private File file;
        private JSONObject jsonObject;
        private String iiifRootPath;
        private JSONObject seq;
        
        private HashMap<File,JSONObject> ranges = new HashMap<>();
        
        public static final String METADATA = "metadata";
        public static final String STRUCTURES = "structures";
        public static final String SEQUENCES = "sequences";
        public static final String CANVASES = "canvases";
        public static final String IMAGES = "images";
        public static final String RANGES = "ranges";
        
        private XPath xp;
        JSONObject top;
        private File root;
        
        HashMap<FolderIndex,JSONObject> folderRanges = new HashMap<>();
        class FolderIndex {
                String box = "";
                String folderStart = "";
                String folderEnd = "";
                Pattern p = Pattern.compile("^(.*)-(.*)$");
                Pattern pdir = Pattern.compile("^b(.*)_f(.*)$");
                FolderIndex(String box, String folder) {
                        this.box = normalize(box);
                        Matcher m = p.matcher(folder);
                        if (m.matches()) {
                                folderStart = normalize(m.group(1));
                                folderEnd = normalize(m.group(2));
                        } else {
                                folderStart = normalize(folder);
                                folderEnd = normalize(folder);                                
                        }
                }
                
                public String normalize(String s) {
                        try {
                                int i = Integer.parseInt(s);
                                return String.format("%06d", i);
                        } catch(NumberFormatException e) {
                                return s;
                        }
                }
                public boolean inRange(String dirName) {
                        Matcher m = pdir.matcher(dirName);
                        if (m.matches()) {
                                if (normalize(m.group(1)).equals(box)) {
                                        String f = normalize(m.group(2));
                                        if (f.compareTo(folderStart) >= 0 && f.compareTo(folderEnd) <= 0) {
                                                return true;
                                        }
                                }
                        }
                        return false;
                }
        }
        
        public IIIFManifest(File root, String iiifRootPath, File manifestFile) {
                file = manifestFile;
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                this.root = root;
                
                xp = XMLUtil.xf.newXPath();
                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                nsContext.add("ead", "urn:isbn:1-931666-22-9");
                nsContext.add("ns2", "http://www.w3.org/1999/xlink");
                xp.setNamespaceContext(nsContext);

                jsonObject.put("@context", "http://iiif.io/api/presentation/2/context.json");
                jsonObject.put("@type","sc:Manifest");
                jsonObject.put("logo", "https://repository.library.georgetown.edu/themes/Mirage2/images/digitalgeorgetown-logo-small-inverted.png");

                jsonObject.put("description","desc");
                top = makeRangeObject("Finding Aid","id","Document Type").put("viewingHint", "top");
                seq = addSequence(jsonObject, SEQUENCES);
                jsonObject.put("attribution", "Georgetown Law Library");
                jsonObject.put("@id","https://repository-dev.library.georgetown.edu/xxx");
        }       
        
        public void setEAD(Document d) {
                if (d == null) {
                        return;
                }
                setXPathValue(jsonObject, "label", d, "concat(/ead:ead/ead:archdesc/ead:did/ead:unitid,': ',/ead:ead/ead:archdesc/ead:did/ead:unittitle)");
                makeEADRanges(top, d, "//ead:c01");
                JSONObject fs = makeRange(root, "All Boxes and Folders","file-system", true);
                addArray(top, RANGES).put(fs.getString("@id"));
        }
        
        public void makeEADRanges(JSONObject parent, Node n, String xq) {
                try {
                        NodeList nl = (NodeList)xp.evaluate(xq, n, XPathConstants.NODESET);
                        for(int i=0; i<nl.getLength(); i++) {
                                Element c0 = (Element)nl.item(i);
                                JSONObject range = makeRangeObject(getXPathValue(c0, "ead:did/ead:unittitle","label"), c0.getAttribute("id"), "Container Title");
                                addArray(parent, RANGES).put(range.getString("@id"));
                                addMetadata(range, METADATA, "level", c0.getAttribute("level"));
                                makeEADRanges(range, c0, "ead:c02");
                                String box = getXPathValue(c0, "ead:did/ead:container[@type='Box']","");
                                String folder = getXPathValue(c0, "ead:did/ead:container[@type='Folder']","");
                                String boxlab = "";
                                if (!box.isEmpty()) {
                                        boxlab = String.format("Box %s; ", box);
                                        if (!folder.isEmpty()) {
                                                boxlab += String.format("Folder %s", folder);
                                        }
                                }
                                if (!boxlab.isEmpty()) {
                                        addMetadata(range, METADATA, "Container", String.format("Box %s; Folder %s", box, folder));
                                        FolderIndex folderIndex = new FolderIndex(box, folder);
                                        folderRanges.put(folderIndex, range);
                                }
                        }
                } catch (XPathExpressionException e) {
                        e.printStackTrace();
                }
                
        }
        
        public void setXPathValue(JSONObject obj, String label, Node d, String xq) {
                try { 
                    obj.put(label, xp.evaluate(xq, d));
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
        }

        public String getXPathValue(Node d, String xq, String def) {
                try { 
                    return xp.evaluate(xq, d);
                } catch (XPathExpressionException e) {
                    e.printStackTrace();
                }
                return def;
        }
        
        public JSONArray addArray(JSONObject obj, String arrlabel) {
                JSONArray arr = null;
                if (obj.has(arrlabel)) {
                        arr = obj.getJSONArray(arrlabel);
                } else {
                        arr = new JSONArray();
                        obj.put(arrlabel, arr);
                }
                return arr;
        }
        public void addMetadata(JSONObject obj, String arrlabel, String label, String value) {
                JSONArray arr = addArray(obj, arrlabel);
                Map<String,String> m = new HashMap<>();
                m.put("label", label);
                m.put("value", value);
                arr.put(m);
        }
        
        public void addDirLink(File f, String arrlabel, String id) {
                File pfile = f.getParentFile();
                if (pfile == null) {
                        return;
                }
                JSONObject parent = ranges.get(pfile);
                if (parent != null) {
                        addArray(parent, arrlabel).put(id);
                } else {
                        System.err.println(pfile.getAbsolutePath()+" not found");
                }
        }

        public String translateLabel(String label) {
                return label
                        .replaceAll("box_","Box ")
                        .replaceAll("^b", "Box ")
                        .replaceAll("_f", " Folder ")
                        ;
        }
        
        public JSONObject makeRange(File dir, String label, String id, boolean isTop) {
                if (ranges.containsKey(dir)) {
                        return ranges.get(dir);
                }
                if (!isTop) {
                        File pfile = dir.getParentFile();
                        if (!ranges.containsKey(pfile)) {
                                //root path will always be found
                                makeRange(pfile, pfile.getName(), pfile.getName(), false);
                        }                        
                }
                
                JSONObject obj = makeRangeObject(label, id, "Directory");
                addDirLink(dir, RANGES, id);
                for(FolderIndex folderIndex: folderRanges.keySet()) {
                        if (folderIndex.inRange(label)) {
                                JSONObject range = folderRanges.get(folderIndex);
                                addArray(range, RANGES).put(id);
                        }
                }
                ranges.put(dir, obj);
                return obj;
        }       

        public JSONObject makeRangeObject(String label, String id, String labelLabel) {
                JSONObject obj = new JSONObject();
                label = translateLabel(label);
                obj.put("label", label);
                addMetadata(obj, METADATA, labelLabel, label);
                obj.put("@id", id);
                obj.put("@type", "sc:Range");
                addArray(obj, "ranges");
                addArray(jsonObject, STRUCTURES).put(obj);
                return obj;
        }       
        
        public void write() throws IOException {
                FileWriter fw = new FileWriter(file);
                jsonObject.write(fw);
                fw.close();
        }

        public String serialize() {
                return jsonObject.toString(2);
        }
        
        public JSONObject addSequence(JSONObject parent, String arrlabel) {
                JSONArray arr = addArray(parent, SEQUENCES);
                JSONObject obj = new JSONObject();
                arr.put(obj);
                obj.put("@id", "https://repository-dev.library.georgetown.edu/seq"); 
                obj.put("@type", "sc:Sequence");
                addArray(obj, CANVASES);
                return obj;
        }
        public String addFile(File f) {
                return addCanvas(f.getName(), f);
        }
        public String addFile(String key, File f) {
                return addCanvas(key, f);
        }
        public String translateItemLabel(String label) {
                return label
                        .replaceAll(".*_item_", "");
        }
        public String addCanvas(String key, File f) {
                String iiifpath = String.format("%s/%s", iiifRootPath, key.replaceAll("\\\\",  "/").replaceFirst("^/*", ""));
                String canvasid = "https://repository-dev.library.georgetown.edu/loris/Canvas/"+f.getName();
                String imageid = "https://repository-dev.library.georgetown.edu/loris/Image/"+f.getName();
                String resid = iiifpath + "/full/full/0/default.jpg";
                
                JSONArray arr = addArray(seq, CANVASES);
                JSONObject canvas = new JSONObject();
                arr.put(canvas);
                canvas.put("@id", canvasid);
                canvas.put("@type", "sc:Canvas"); 
                canvas.put("height", 1536);
                canvas.put("width", 2048);
                String label = translateItemLabel(f.getName());
                canvas.put("label", label);
                addMetadata(canvas, METADATA, "name", f.getName());
                JSONArray imarr = addArray(canvas, IMAGES);
                JSONObject image = new JSONObject();
                imarr.put(image);
                image.put("@context", "http://iiif.io/api/presentation/2/context.json");
                image.put("@id", imageid); 
                image.put("@type", "oa:Annotation");
                image.put("motivation", "sc:painting"); 
                image.put("on", "https://repository-dev.library.georgetown.edu/ead");
                JSONObject resource = new JSONObject();
                image.put("resource", resource);
                resource.put("@id", resid); 
                resource.put("@type", "dctypes:Image");
                resource.put("format", "image/jpeg");
                resource.put("height", 1536);
                resource.put("width", 2048);                      
                JSONObject service = new JSONObject();
                resource.put("service", service);
                service.put("@context", "http://iiif.io/api/image/2/context.json"); 
                service.put("@id", iiifpath); 
                service.put("profile", "http://iiif.io/api/image/2/level2.json");    
                
                addDirLink(f, CANVASES, canvasid);
                return canvasid;
            }
        
}
