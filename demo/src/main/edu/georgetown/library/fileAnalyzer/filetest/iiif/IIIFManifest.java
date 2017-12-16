package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPath;

import org.apache.tika.exception.TikaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.DefaultDimensions;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

public class IIIFManifest {
        private File file;
        protected JSONObject jsonObject;
        protected String iiifRootPath;
        protected JSONObject seq;
        protected XPath xp;
        
        protected HashMap<File,JSONObject> ranges = new HashMap<>();
        
        public static final String EMPTY = "";
        
        ManifestProjectTranslate manifestProjectTranslate;
        TreeMap<String,JSONObject> orderedCanvases = new TreeMap<>();
        HashMap<String,RangePath> parentRangeForCanvas = new HashMap<>();
        
        RangePath top;
        protected MetadataInputFile inputMetadata;
        ManifestGeneratePropFile manifestGen;

        public void setProperty(JSONObject json, IIIFType type, IIIFProp prop) {
                setProperty(json, type, prop, prop.getDefault());
        }
        public void setProperty(JSONObject json, IIIFType type, IIIFProp prop, String value) {
                value = manifestProjectTranslate.translate(type, prop, value);
                
                if (value.equals(EMPTY)) {
                        return;
                }
                
                if (prop == IIIFProp.title && type == IIIFType.typeManifest) {
                        addMetadata(json, prop.getLabel(), value);
                        json.put(prop.getLabel(), value);                        
                } else if (prop.isMetadata) {
                        addMetadata(json, prop.getLabel(), value);
                } else {
                        json.put(prop.getLabel(), value);
                }
        }
        public static void setProperty(JSONObject json, IIIFType type) {
                json.put(IIIFProp.type.getLabel(), type.getValue());
        }

        public static String getProperty(JSONObject json, IIIFProp prop, String defValue) {
                String ret = null;
                if (prop.isMetadata) {
                        JSONArray jarr = json.getJSONArray(IIIFArray.metadata.getLabel());
                        if (jarr == null) {
                                return defValue;
                        }
                        for(int i = 0; i < jarr.length(); i++) {
                                JSONObject obj = jarr.getJSONObject(i);
                                if (prop.getLabel().equals(obj.getString(IIIFProp.label.getLabel()))) {
                                        ret = obj.getString(IIIFProp.value.getLabel());
                                }
                        }
                } else {
                        if (json.has(prop.getLabel())) {
                                ret = json.getString(prop.getLabel());
                        }
                }
                return ret == null ? defValue : ret;
        }

        public static int getIntProperty(JSONObject json, IIIFProp prop, int defValue) {
                if (json.has(prop.getLabel())) {
                        return json.getInt(prop.getLabel());
                }
                return defValue;
        }

        
        public static JSONArray getArray(JSONObject obj, IIIFArray iiifarr) {
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

        public static void addMetadata(JSONObject json, String label, String value) {
                JSONArray metadata = getArray(json, IIIFArray.metadata);
                Map<String,String> m = new HashMap<>();
                m.put(IIIFProp.label.name(), label);
                m.put(IIIFProp.value.name(), value);
                metadata.put(m);
        }
       
        public IIIFManifest(MetadataInputFile inputMetadata, ManifestGeneratePropFile manifestGen, boolean isCollectionManifest) throws IOException, InputFileException {
                this.manifestGen = manifestGen;
                File manifestFile = manifestGen.getManifestOutputFile();
                String iiifRootPath = manifestGen.getIIIFRoot();
                checkManifestFile(manifestFile);
                file = manifestFile;
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                this.inputMetadata = inputMetadata;
                this.manifestProjectTranslate = DefaultManifestProjectTranslateEnum.Default.getTranslator();
                xp = XMLUtil.xf.newXPath();
        }      
        
        /*
         * Call this after the proper translator object has been set
         */
        public void init(File root) {
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.context);
                setProperty(jsonObject, IIIFType.typeManifest);
                
                String def = "";
                def = manifestGen.getProperty(IIIFLookup.Title);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.label, inputMetadata.getValue(IIIFLookup.Title, def)); 
                def = manifestGen.getProperty(IIIFLookup.DateCreated);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.attribution, inputMetadata.getValue(IIIFLookup.DateCreated, def));
                def = manifestGen.getProperty(IIIFLookup.Creator);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.attribution, inputMetadata.getValue(IIIFLookup.Creator, def));
                def = manifestGen.getProperty(IIIFLookup.Description);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.attribution, inputMetadata.getValue(IIIFLookup.Description, def));
                def = manifestGen.getProperty(IIIFLookup.Attribution);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.attribution, inputMetadata.getValue(IIIFLookup.Attribution, def));
                setLogoUrl(manifestGen.getManifestLogoURL());

                initRanges(root);
                
                seq = addSequence(jsonObject);
                setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.id,String.format("%s/misc", this.iiifRootPath));
        }
        
        public void initRanges(File root) {
                top = new RangePath("__toprange","Top Range");
                makeRangeObject(top).put("viewingHint", "top");
                if (manifestProjectTranslate.processInitRanges()) {
                        List<RangePath> rangePaths = inputMetadata.getInitRanges(this, top, manifestProjectTranslate);
                        for(RangePath rangePath: rangePaths) {
                                makeRangeObject(rangePath);
                        }
                }
                manifestProjectTranslate.initProjectRanges(root, top);
        }
        
        
        public void setLogoUrl(String s) {
                if (!s.equals(EMPTY)) {
                        setProperty(jsonObject, IIIFType.typeManifest, IIIFProp.logo, s);
                }
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
        
        public RangePath makeRange(String key, File parent, MetadataInputFile currentMetadataFile) {
                return manifestProjectTranslate.getPrimaryRangePath(key, parent, currentMetadataFile);
        }
        
        public void linkRangeToCanvas(RangePath rangePath, JSONObject canvas) {
                String canvasid = getProperty(canvas, IIIFProp.id, EMPTY);
                if (!canvasid.isEmpty()) {
                        if (!rangePath.hasObject()) {
                                makeRangeObject(rangePath);
                        }
                        rangePath.addCanvasId(canvasid);
                }
        }
        
        public JSONObject makeRangeObject(RangePath rangePath) {
                if (rangePath.hasObject()) {
                        return rangePath.getRangeObject();
                }
                JSONObject obj = new JSONObject();
                String label = manifestProjectTranslate.translate(IIIFType.typeRange, IIIFProp.label, rangePath.displayPath);
                setProperty(obj, IIIFType.typeRange, IIIFProp.label, label);
                setProperty(obj, IIIFType.typeRange, IIIFProp.id, rangePath.getID());
                setProperty(obj, IIIFType.typeRange);
                getArray(obj, IIIFArray.ranges);
                getArray(jsonObject, IIIFArray.structures).put(obj);
                rangePath.setRangeObject(obj);
                return obj;
        }       
        
        public void refine()  {
                for(String canvasKey: orderedCanvases.keySet()) {
                        JSONObject canvas = orderedCanvases.get(canvasKey);
                        addCanvasToManifest(canvas);
                        String canvasid = getProperty(canvas, IIIFProp.id, EMPTY);
                        RangePath rangePath = parentRangeForCanvas.get(canvasid);
                        if (rangePath != null) {
                                if (!rangePath.hasObject()) {
                                        makeRangeObject(rangePath);
                                }
                                JSONObject range = rangePath.getRangeObject();
                                if (range != null) {
                                        getArray(range, IIIFArray.canvases).put(canvasid);
                                }
                        }
                }
                for(RangePath rp: top.getDescendants()) {
                        if (!rp.hasObject()) {
                                makeRangeObject(rp);
                        }
                        JSONObject range = rp.getRangeObject();
                        for(RangePath chRange: rp.getOrderedChildren()) {
                                getArray(range, IIIFArray.ranges).put(chRange.getID());
                        }
                        int count = 0;
                        for(String canvasId: rp.getCanvasIds()) {
                                getArray(range, IIIFArray.canvases).put(canvasId);
                                count++;
                        }
                        if (count > 0) {
                                setProperty(range, IIIFType.typeRange, IIIFProp.label, String.format("%s (%d)", rp.displayPath, count));
                        }
                }
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
                JSONArray arr = getArray(parent, IIIFArray.sequences);
                JSONObject obj = new JSONObject();
                arr.put(obj);
                setProperty(obj, IIIFType.typeSequence, IIIFProp.id, String.format("%s/seq", this.iiifRootPath));
                setProperty(obj, IIIFType.typeSequence);
                getArray(obj, IIIFArray.canvases);
                return obj;
        }
        public String getIIIFPath(String key, File f) {
                return String.format("%s/%s", iiifRootPath, key.replaceAll("\\\\",  "/").replaceFirst("^/*", ""));
        }
       
        public void addCanvasMetadata(JSONObject canvas, File f, MetadataInputFile itemMeta) {
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.label, itemMeta.getValue(IIIFLookup.Title, f.getName()));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.title, itemMeta.getValue(IIIFLookup.Title, EMPTY));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.dateCreated, itemMeta.getValue(IIIFLookup.DateCreated, EMPTY));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.creator, itemMeta.getValue(IIIFLookup.Creator, EMPTY));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.description, itemMeta.getValue(IIIFLookup.Description, EMPTY));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.subject, itemMeta.getValue(IIIFLookup.Subject, EMPTY));
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.rights, itemMeta.getValue(IIIFLookup.Rights, EMPTY));
                String uri = itemMeta.getValue(IIIFLookup.Permalink, EMPTY);
                if (!uri.isEmpty()) {
                        uri = String.format("<a href='%s'>%s</a>", uri, uri);
                }
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.permalink, uri);
        }
        
        public void addCanvasToManifest(JSONObject canvas) {
                JSONArray arr = getArray(seq, IIIFArray.canvases);
                arr.put(canvas);
        }

        public JSONObject addCanvas(String key, File f, MetadataInputFile itemMeta) {
                String iiifpath = getIIIFPath(key, f);
                
                JSONObject canvas = createCanvas(iiifpath, getDimensions(f), f.getName());
                String canvasKey = manifestProjectTranslate.getSequenceValue(orderedCanvases.size(), itemMeta);
                orderedCanvases.put(canvasKey, canvas);
                
                return canvas;
        }
        
        public ManifestDimensions getDimensions(File f) {
                try {
                        return new ManifestDimensions(f);
                } catch (IOException | SAXException | TikaException e) {
                        e.printStackTrace();
                } 
                return DefaultDimensions.PORTRAIT.dimensions;
        }
        public JSONObject addEadCanvas(String imageUrl, Node eadNode, XMLInputFile itemMeta) {
                String iiifpath = imageUrl.replaceFirst("^IIIFRoot", iiifRootPath);
                String[] paths = iiifpath.split("/");
                String name = paths[paths.length-1];
                String infoJson = String.format("%s/info.json", iiifpath);
                JSONObject canvas = createCanvas(iiifpath, getDimensions(infoJson), name);
                String title = itemMeta.getXPathValue(eadNode, "ead:daodesc//text()", "");
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.label, title);
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.title, title);

                String canvasKey = manifestProjectTranslate.getSequenceValue(orderedCanvases.size(), itemMeta);
                orderedCanvases.put(canvasKey, canvas);
                
                return canvas;
        }
        

        public ManifestDimensions getDimensions(String infojson) {
                return DefaultDimensions.PORTRAIT.dimensions;
        }

        public JSONObject createCanvas(String iiifpath, ManifestDimensions dim, String qualifier) {
                String canvasid = String.format("%s/Canvas/%s", this.iiifRootPath, qualifier);

                JSONObject canvas = new JSONObject();
                setProperty(canvas, IIIFType.typeCanvas, IIIFProp.id, canvasid);
                setProperty(canvas, IIIFType.typeCanvas); 
                canvas.put(IIIFProp.height.val, dim.height());
                canvas.put(IIIFProp.width.val, dim.width());

                JSONObject image = createImage(iiifpath, dim, qualifier);
                getArray(canvas, IIIFArray.images).put(image);
                return canvas;
        }

        public JSONObject createImage(String iiifpath, ManifestDimensions dim, String qualifier) {
                String imageid = String.format("%s/Image/%s", this.iiifRootPath, qualifier);
                JSONObject image = new JSONObject();
                setProperty(image, IIIFType.typeImage, IIIFProp.context);
                setProperty(image, IIIFType.typeImage, IIIFProp.id, imageid); 
                setProperty(image, IIIFType.typeImageAnnotation);
                setProperty(image, IIIFType.typeImage, IIIFProp.motivation);
                setProperty(image, IIIFType.typeImage, IIIFProp.on, String.format("%s/img", this.iiifRootPath));
                image.put(IIIFProp.resource.val, createImageResource(iiifpath, dim));
                return image;
        }

        public JSONObject createImageResource(String iiifpath, ManifestDimensions dim) {
                String resid = iiifpath + "/full/full/0/default.jpg";
                JSONObject resource = new JSONObject();
                setProperty(resource, IIIFType.typeImageResource, IIIFProp.id, resid); 
                setProperty(resource, IIIFType.typeImageResource);
                setProperty(resource, IIIFType.typeImageResource, IIIFProp.format, "image/jpeg");
                resource.put(IIIFProp.height.val, dim.height());
                resource.put(IIIFProp.width.val, dim.width());
                resource.put(IIIFProp.service.val, createImageService(iiifpath));
                return resource;
        }

        public JSONObject createImageService(String iiifpath) {
                JSONObject service = new JSONObject();
                setProperty(service, IIIFType.typeImageResourceService, IIIFProp.context); 
                setProperty(service, IIIFType.typeImageResourceService, IIIFProp.id, iiifpath); 
                setProperty(service, IIIFType.typeImageResourceService, IIIFProp.profile);    
                return service;
        }
        
        public void setProjectTranslate(ManifestProjectTranslate manifestProjectTranslate) {
                this.manifestProjectTranslate = manifestProjectTranslate;
        }
        public JSONObject getCanvas(String canvasKey) {
                return orderedCanvases.get(canvasKey);
        }

}


