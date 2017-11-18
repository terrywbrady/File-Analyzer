package edu.georgetown.library.fileAnalyzer.filetest.iiif;

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
        
        public static enum IIIFType {
                typeManifest("sc:Manifest"),
                typeRange("sc:Range"),
                typeSequence("sc:Sequence"),
                typeCanvas("sc:Canvas"),
                typeImage("dctypes:Image"),
                typeAnnotation("oa:Annotation");                

                String val;
                IIIFType(String val) {
                        this.val = val;
                }
                String getValue() {
                        return val;
                }
        }
        
        public static enum IIIFArray {
                metadata,
                structures,
                sequences,
                canvases,
                images,
                ranges;                
                String getLabel() {
                        return name();
                }
        }
        
        public static enum IIIFProp {
                label,
                value,
                format,
                id{
                        String getLabel() {
                                return "@id";
                        }

                },
                height,
                width,
                type {
                        String getLabel() {
                                return "@type";
                        }
                },
                context{
                        String getLabel() {
                                return "@context";
                        }
                        String getDefault() {
                                return "http://iiif.io/api/presentation/2/context.json";
                        }
                },
                logo,
                profile {
                        String getDefault() {
                                return "http://iiif.io/api/image/2/level2.json";
                        }
                };

                String val;
                IIIFProp() {
                        this.val = name();
                }
                IIIFProp(String val) {
                        this.val = val;
                }
                String getLabel() {
                        return name();
                }
                String getDefault() {
                        return "";
                }

        }
        
        
        
        JSONObject top;
        protected MetadataInputFile inputMetadata;
        ManifestDimensions dimensions;
        
        public enum ManifestDimensions {
                PORTRAIT(1000,700), LANDSCAPE(750,1000);
                int height;
                int width;
                private ManifestDimensions(int height, int width) {
                        this.height = height;
                        this.width = width;
                }                
                String height() {
                        return Integer.toString(height);
                }
                String width() {
                        return Integer.toString(width);
                }
        }

        public ManifestDimensions getDimensions() {
                return ManifestDimensions.LANDSCAPE;
        }
        
        public void setProperty(JSONObject json, IIIFProp prop) {
                json.put(prop.getLabel(), prop.getDefault());
        }
        public void setProperty(JSONObject json, IIIFProp prop, String value) {
                json.put(prop.getLabel(), value);
        }
        public void setProperty(JSONObject json, IIIFType type) {
                json.put(IIIFProp.type.getLabel(), type.getValue());
        }

        public JSONArray addArray(JSONObject obj, IIIFArray iiifarr) {
                String arrlabel = iiifarr.getLabel();
                JSONArray arr = null;
                if (obj.has(arrlabel)) {
                        arr = obj.getJSONArray(arrlabel);
                } else {
                        arr = new JSONArray();
                        obj.put(arrlabel, arr);
                }
                return arr;
        }

        public void addMetadata(JSONObject json, String label, String value) {
                JSONArray metadata = addArray(json, IIIFArray.metadata);
                Map<String,String> m = new HashMap<>();
                m.put(IIIFProp.label.name(), label);
                m.put(IIIFProp.value.name(), value);
                metadata.put(m);
        }
       
        public IIIFManifest(MetadataInputFile inputMetadata, String iiifRootPath, File manifestFile, boolean isCollectionManifest) throws IOException {
                checkManifestFile(manifestFile);
                file = manifestFile;
                dimensions = getDimensions();
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                this.inputMetadata = inputMetadata;
                xp = XMLUtil.xf.newXPath();
                
                setProperty(jsonObject, IIIFProp.context);
                setProperty(jsonObject, IIIFType.typeManifest);
                //TODO - make param
                setProperty(jsonObject, IIIFProp.logo, "https://repository.library.georgetown.edu/themes/Mirage2/images/digitalgeorgetown-logo-small-inverted.png");

                top = makeRangeObject("Finding Aid","id","Document Type").put("viewingHint", "top");
                seq = addSequence(jsonObject);
                setProperty(jsonObject, IIIFProp.id,"https://repository-dev.library.georgetown.edu/xxx");
        }       
        
        public void addManifestToCollection(IIIFManifest itemManifest) {
                //TODO
        }
        
        public File getManifestFile() {
                return file;
        }

        public File getComponentManifestFile(File f, String identifier) {
                return new File(file.getParentFile(), identifier);
        }

        public void checkManifestFile(File manFile) throws IOException {
                try(FileWriter fw = new FileWriter(manFile)){
                        fw.write("");
                } catch (IOException e) {
                        throw e;
                }
                if (!manFile.canWrite()) {
                        throw new IOException(String.format("Cannot write to manifest file [%s]", manFile.getName()));
                }
        }
        
        
        public void set2Page() {
                seq.put("viewingHint", "paged");
        }
        
        
        public void addDirLink(File f, IIIFArray iiifarr, String id) {
                File pfile = f.getParentFile();
                if (pfile == null) {
                        return;
                }
                JSONObject parent = ranges.get(pfile);
                if (parent != null) {
                        addArray(parent, iiifarr).put(id);
                } else {
                        //System.err.println(pfile.getAbsolutePath()+" not found");
                }
        }

        public String translateLabel(String label) {
                return label;
        }
        
        public JSONObject makeRange(File dir) {
                return top;
        }
        
        public JSONObject makeRange(File dir, String label, String id, boolean isTop) {
                return top;
        }       

        public JSONObject makeRangeObject(String label, String id, String labelLabel) {
                JSONObject obj = new JSONObject();
                label = translateLabel(label);
                setProperty(obj, IIIFProp.label, label);
                setProperty(obj, IIIFProp.id, id);
                setProperty(obj, IIIFType.typeRange);
                this.addArray(obj, IIIFArray.ranges);
                addArray(jsonObject, IIIFArray.structures).put(obj);
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
        
        public JSONObject addSequence(JSONObject parent) {
                JSONArray arr = addArray(parent, IIIFArray.sequences);
                JSONObject obj = new JSONObject();
                arr.put(obj);
                setProperty(obj, IIIFProp.id, "https://repository-dev.library.georgetown.edu/seq");
                setProperty(obj, IIIFType.typeSequence);
                addArray(obj, IIIFArray.canvases);
                return obj;
        }
        public JSONObject addFile(File f) {
                return addCanvas(f.getName(), f);
        }
        public JSONObject addFile(String key, File f) {
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
                setProperty(canvas, IIIFProp.label, label);
                addMetadata(canvas, "name", f.getName());
        }
        
        public void addCanvasToManifest(JSONObject canvas) {
                JSONArray arr = addArray(seq, IIIFArray.canvases);
                arr.put(canvas);
        }
        
        public JSONObject addCanvas(String key, File f) {
                String iiifpath = getIIIFPath(key, f);
                String canvasid = "https://repository-dev.library.georgetown.edu/loris/Canvas/"+f.getName();
                String imageid = "https://repository-dev.library.georgetown.edu/loris/Image/"+f.getName();
                String resid = iiifpath + "/full/full/0/default.jpg";
                
                JSONObject canvas = new JSONObject();
                setProperty(canvas, IIIFProp.id, canvasid);
                setProperty(canvas, IIIFType.typeCanvas); 
                setProperty(canvas, IIIFProp.height, dimensions.height());
                setProperty(canvas, IIIFProp.width, dimensions.width());
                addCanvasMetadata(canvas, f);
                addCanvasToManifest(canvas);
                JSONArray imarr = addArray(canvas, IIIFArray.images);
                JSONObject image = new JSONObject();
                imarr.put(image);
                setProperty(image, IIIFProp.context);
                setProperty(image, IIIFProp.id, imageid); 
                setProperty(image, IIIFType.typeAnnotation);
                image.put("motivation", "sc:painting"); 
                image.put("on", "https://repository-dev.library.georgetown.edu/ead");
                JSONObject resource = new JSONObject();
                image.put("resource", resource);
                setProperty(resource, IIIFProp.id, resid); 
                setProperty(resource, IIIFType.typeImage);
                setProperty(resource, IIIFProp.format, "image/jpeg");
                setProperty(resource, IIIFProp.height, dimensions.height());
                setProperty(resource, IIIFProp.width, dimensions.width());                      
                JSONObject service = new JSONObject();
                resource.put("service", service);
                setProperty(service, IIIFProp.context); 
                setProperty(service, IIIFProp.id, iiifpath); 
                setProperty(service, IIIFProp.profile);    
                
                linkCanvas(f, canvasid);
                return canvas;
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
