package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class IIIFManifest {
        private File file;
        private JSONObject jsonObject;
        private String iiifRootPath;
        private JSONObject seq;
        
        public static final String METADATA = "metadata";
        public static final String STRUCTURES = "structures";
        public static final String SEQUENCES = "sequences";
        public static final String CANVASES = "canvases";
        public static final String IMAGES = "images";
        
        public IIIFManifest(File root, String iiifRootPath) {
                file = new File(root, "manifest.json");
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                
                jsonObject.put("@context", "http://iiif.io/api/presentation/2/context.json");
                jsonObject.put("@type","sc:Manifest");
                jsonObject.put("logo", "https://repository.library.georgetown.edu/themes/Mirage2/images/digitalgeorgetown-logo-small-inverted.png");
                jsonObject.put("label","label");
                jsonObject.put("description","desc");
                addMetadata(jsonObject, METADATA, "aa","bb");
                //JSONArray structures = addArray(jsonObject, STRUCTURES).put(makeRange("rangelabel","idlabel", true));
                //structures.toString().getClass();//TBD
                seq = addSequence(jsonObject, SEQUENCES);
                jsonObject.put("attribution", "attrib");
                jsonObject.put("@id","https://repository-dev.library.georgetown.edu/xxx");
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
        
        public JSONObject makeRange(String label, String id, boolean isTop) {
                JSONObject obj = new JSONObject();
                obj.put("label", label);
                obj.put("@id", id);
                if (isTop) {
                        obj.put("viewingHint", "top");                        
                }
                obj.put("@type", "sc:Range");
                addArray(obj, "ranges");
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
        
        public static final void main(String[] args) {
                IIIFManifest manifest = new IIIFManifest(new File("."),"http://");
                manifest.addFile(new File("foo.bar"));
                System.out.println(manifest.serialize());
                
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
        public String addCanvas(String key, File f) {
                String iiifpath = String.format("%s/%s", iiifRootPath, key.replaceAll("\\\\",  "/").replaceFirst("^/*", ""));
                
                JSONArray arr = addArray(seq, CANVASES);
                JSONObject canvas = new JSONObject();
                arr.put(canvas);
                canvas.put("@id", "https://repository-dev.library.georgetown.edu/loris/"+f.getName());
                canvas.put("@type", "sc:Canvas"); 
                canvas.put("height", 1536);
                canvas.put("width", 2048);
                canvas.put("label", f.getName());
                JSONArray imarr = addArray(canvas, IMAGES);
                JSONObject image = new JSONObject();
                imarr.put(image);
                image.put("@context", "http://iiif.io/api/presentation/2/context.json");
                image.put("@id", "https://repository-dev.library.georgetown.edu/loris"); 
                image.put("@type", "oa:Annotation");
                image.put("motivation", "sc:painting"); 
                image.put("on", "https://repository-dev.library.georgetown.edu/ead");
                JSONObject resource = new JSONObject();
                image.put("resource", resource);
                resource.put("@id", iiifpath + "/full/full/0/default.jpg"); 
                resource.put("@type", "dctypes:Image");
                resource.put("format", "image/jpeg");
                resource.put("height", 1536);
                resource.put("width", 2048);                      
                JSONObject service = new JSONObject();
                resource.put("service", service);
                service.put("@context", "http://iiif.io/api/image/2/context.json"); 
                service.put("@id", iiifpath); 
                service.put("profile", "http://iiif.io/api/image/2/level2.json");                
                return iiifpath;
            }
        
}
