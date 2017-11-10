package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;


public class IIIFManifest {
        private File file;
        protected JSONObject jsonObject;
        protected String iiifRootPath;
        protected JSONObject seq;
        protected XPath xp;
        
        protected HashMap<File,JSONObject> ranges = new HashMap<>();
        
        public static final String METADATA = "metadata";
        public static final String STRUCTURES = "structures";
        public static final String SEQUENCES = "sequences";
        public static final String CANVASES = "canvases";
        public static final String IMAGES = "images";
        public static final String RANGES = "ranges";
        
        JSONObject top;
        protected File root;
        ManifestDimensions dimensions;
        
        public enum ManifestDimensions {
                PORTRAIT(1000,700), LANDSCAPE(750,1000);
                int height;
                int width;
                private ManifestDimensions(int height, int width) {
                        this.height = height;
                        this.width = width;
                }                
        }

        public ManifestDimensions getDimensions() {
                return ManifestDimensions.LANDSCAPE;
        }
        
        public IIIFManifest(File root, String iiifRootPath, File manifestFile) {
                file = manifestFile;
                dimensions = getDimensions();
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                this.root = root;
                xp = XMLUtil.xf.newXPath();
                
                jsonObject.put("@context", "http://iiif.io/api/presentation/2/context.json");
                jsonObject.put("@type","sc:Manifest");
                jsonObject.put("logo", "https://repository.library.georgetown.edu/themes/Mirage2/images/digitalgeorgetown-logo-small-inverted.png");

                top = makeRangeObject("Finding Aid","id","Document Type").put("viewingHint", "top");
                seq = addSequence(jsonObject, SEQUENCES);
                jsonObject.put("@id","https://repository-dev.library.georgetown.edu/xxx");
        }       
        
        public void set2Page() {
                seq.put("viewingHint", "paged");
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
                        //System.err.println(pfile.getAbsolutePath()+" not found");
                }
        }

        public String translateLabel(String label) {
                return label;
        }
        
        public JSONObject makeRange(File dir, String label, String id, boolean isTop) {
                return top;
        }       

        public JSONObject makeRangeObject(String label, String id, String labelLabel) {
                JSONObject obj = new JSONObject();
                label = translateLabel(label);
                obj.put("label", label);
                //addMetadata(obj, METADATA, labelLabel, label);
                obj.put("@id", id);
                obj.put("@type", "sc:Range");
                addArray(obj, "ranges");
                addArray(jsonObject, STRUCTURES).put(obj);
                return obj;
        }       
        
        public void refine()  {
                //No op - meant to be overridden
        }
        public void write() throws IOException {
                refine();
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
                return label;
        }
        
        public String getIIIFPath(String key, File f) {
                return String.format("%s/%s", iiifRootPath, key.replaceAll("\\\\",  "/").replaceFirst("^/*", ""));
        }
       
        public void addCanvasMetadata(JSONObject canvas, File f) {
                String label = translateItemLabel(f.getName());
                canvas.put("label", label);
                addMetadata(canvas, METADATA, "name", f.getName());
        }
        
        public void addCanvasToManifest(JSONObject canvas) {
                JSONArray arr = addArray(seq, CANVASES);
                arr.put(canvas);
        }
        
        public String addCanvas(String key, File f) {
                String iiifpath = getIIIFPath(key, f);
                String canvasid = "https://repository-dev.library.georgetown.edu/loris/Canvas/"+f.getName();
                String imageid = "https://repository-dev.library.georgetown.edu/loris/Image/"+f.getName();
                String resid = iiifpath + "/full/full/0/default.jpg";
                
                JSONObject canvas = new JSONObject();
                canvas.put("@id", canvasid);
                canvas.put("@type", "sc:Canvas"); 
                canvas.put("height", dimensions.height);
                canvas.put("width", dimensions.width);
                addCanvasMetadata(canvas, f);
                addCanvasToManifest(canvas);
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
                resource.put("height", dimensions.height);
                resource.put("width", dimensions.width);                      
                JSONObject service = new JSONObject();
                resource.put("service", service);
                service.put("@context", "http://iiif.io/api/image/2/context.json"); 
                service.put("@id", iiifpath); 
                service.put("profile", "http://iiif.io/api/image/2/level2.json");    
                
                linkCanvas(f, canvasid);
                return canvasid;
        }
        
        public void linkCanvas(File f, String canvasid) {
                //no action if canvases only appear in the sequences
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

}
