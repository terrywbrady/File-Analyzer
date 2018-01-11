package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import java.util.TreeSet;

import javax.xml.xpath.XPath;

import org.apache.tika.exception.TikaException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.CollectionMode;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.DefaultDimensions;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookupEnum;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFMetadataProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

public class IIIFManifest extends IIIFJSONWrapper {
        private File file;
        protected IIIFJSONWrapper seq;
        protected XPath xp;
        
        protected HashMap<File,JSONObject> ranges = new HashMap<>();
        
        TreeSet<IIIFCanvasWrapper> orderedCanvases = new TreeSet<>();
        //TODO: store range ref inside of canvas
        HashMap<String,RangePath> parentRangeForCanvas = new HashMap<>();
        
        RangePath top;
        protected MetadataInputFile inputMetadata;
        ManifestGeneratePropFile manifestGen;
        private boolean isCollectionManifest = false;
        private String manifestRoot = "";

       
        public IIIFManifest(MetadataInputFile inputMetadata, ManifestGeneratePropFile manifestGen) throws IOException, InputFileException {
                this(inputMetadata, manifestGen, manifestGen.getManifestOutputFile(), manifestGen.getCreateCollectionManifest());
        }      

        public IIIFManifest(MetadataInputFile inputMetadata, ManifestGeneratePropFile manifestGen, File compFile) throws IOException, InputFileException {
                this(inputMetadata, manifestGen, compFile, false);
        }

        public IIIFManifest(MetadataInputFile inputMetadata, ManifestGeneratePropFile manifestGen, File compFile, boolean isCollectionManifest) throws IOException, InputFileException {
                super(manifestGen.getIIIFRoot());
                this.manifestGen = manifestGen;
                this.manifestRoot = manifestGen.getManifestRoot();
                file = compFile;
                checkManifestFile(file);
                this.inputMetadata = inputMetadata;
                xp = XMLUtil.xf.newXPath();
                this.isCollectionManifest = isCollectionManifest;
                if (!isCollectionManifest) {
                        seq = addSequence();
                        if (manifestGen.getSet2PageView()) {
                                this.set2Page();
                        }
                }
        }      

        /*
         * Call this after the proper translator object has been set
         */
        public void init(File root, String path) {
                setProperty(IIIFType.typeManifest, IIIFStandardProp.context);
                setProperty(isCollectionManifest ? IIIFType.typeCollection : IIIFType.typeManifest);
                
                String def = "";
                def = manifestGen.getProperty(IIIFLookupEnum.Title.getLookup());
                String label = inputMetadata.getValue(IIIFLookupEnum.Title.getLookup(), def);
                if (manifestGen.getCreateCollectionMode() == CollectionMode.ManyItemsPerFolder) {
                        label = this.getManifestProjectTranslate().translate(IIIFType.typeManifest, IIIFStandardProp.label, path);
                }
                if (label.isEmpty()) {
                        label = "(no label)";
                }
                setProperty(IIIFType.typeManifest, IIIFStandardProp.label, label); 
                def = manifestGen.getProperty(IIIFLookupEnum.DateCreated.getLookup());
                setProperty(IIIFType.typeManifest, IIIFStandardProp.attribution, inputMetadata.getValue(IIIFLookupEnum.DateCreated.getLookup(), def));
                def = manifestGen.getProperty(IIIFLookupEnum.Creator.getLookup());
                setProperty(IIIFType.typeManifest, IIIFStandardProp.attribution, inputMetadata.getValue(IIIFLookupEnum.Creator.getLookup(), def));
                def = manifestGen.getProperty(IIIFLookupEnum.Description.getLookup());
                setProperty(IIIFType.typeManifest, IIIFStandardProp.attribution, inputMetadata.getValue(IIIFLookupEnum.Description.getLookup(), def));
                def = manifestGen.getProperty(IIIFLookupEnum.Attribution.getLookup());
                setProperty(IIIFType.typeManifest, IIIFStandardProp.attribution, inputMetadata.getValue(IIIFLookupEnum.Attribution.getLookup(), def));
                setLogoUrl(manifestGen.getManifestLogoURL());

                if (!isCollectionManifest) {
                        initRanges(root);
                }
                
                setProperty(IIIFType.typeManifest, IIIFStandardProp.id,String.format("%s/%s", this.manifestRoot, file.getName()));
        }

        public IIIFJSONWrapper addSequence() {
                IIIFJSONWrapper obj = new IIIFJSONWrapper(this.iiifRootPath, this.getManifestProjectTranslate());
                JSONArray arr = getArray(IIIFArray.sequences);
                arr.put(obj.getJSONObject());
                obj.setProperty(IIIFType.typeSequence, IIIFStandardProp.id, String.format("%s/seq", this.iiifRootPath));
                obj.setProperty(IIIFType.typeSequence);
                obj.getArray(IIIFArray.canvases);
                return obj;
        }

        public void initRanges(File root) {
                top = new RangePath(this, "__toprange","Top Range");
                top.getJSONObject().put("viewingHint", "top");
                if (manifestProjectTranslate.processInitRanges()) {
                        inputMetadata.getInitRanges(this, top, manifestProjectTranslate);
                }
                manifestProjectTranslate.initProjectRanges(this, root, top);
        }
        
        
        public void setLogoUrl(String s) {
                if (!s.equals(EMPTY)) {
                        setProperty(IIIFType.typeManifest, IIIFStandardProp.logo, s);
                }
        }
        
        public void addManifestToCollection(IIIFManifest itemManifest) {
                this.getArray(IIIFArray.manifests).put(itemManifest.getMinimalJSONObject());
        }
        
        public File getManifestFile() {
                return file;
        }

        public File getComponentManifestFile(File f, String identifier) {
                return new File(file.getParentFile(), String.format("%s.json",identifier));
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
                seq.getJSONObject().put("viewingHint", "paged");
        }
        
        public RangePath makeRange(String key, File parent, MetadataInputFile currentMetadataFile) {
                return manifestProjectTranslate.getPrimaryRangePath(this, key, parent, currentMetadataFile);
        }
        
        public void linkRangeToCanvas(RangePath rangePath, IIIFJSONWrapper canvas) {
                String canvasid = canvas.getProperty(IIIFStandardProp.id, EMPTY);
                if (!canvasid.isEmpty()) {
                        rangePath.addCanvasId(canvasid);
                }
        }
        
        public void refine()  {
                for(IIIFCanvasWrapper canvasWrap: orderedCanvases) {
                        addCanvasToManifest(canvasWrap);
                        String canvasid = canvasWrap.getProperty(IIIFStandardProp.id, EMPTY);
                        RangePath rangePath = parentRangeForCanvas.get(canvasid);
                        if (rangePath != null) {
                                rangePath.getArray(IIIFArray.canvases).put(canvasid);
                        }
                }
                if (top != null) {
                        for(RangePath rp: top.getDescendants()) {
                                for(RangePath chRange: rp.getOrderedChildren()) {
                                        rp.getArray(IIIFArray.ranges).put(chRange.getID());
                                }
                                int count = 0;
                                for(String canvasId: rp.getCanvasIds()) {
                                        rp.getArray(IIIFArray.canvases).put(canvasId);
                                        count++;
                                }
                                if (count > 0) {
                                        rp.setProperty(IIIFType.typeRange, IIIFStandardProp.label, String.format("%s (%d)", rp.displayPath, count));
                                }
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
        
        public String getIIIFPath(String key, File f) {
                String slash = manifestGen.getDirSeparator();
                String s = key.replaceAll("\\\\",  "/").replaceFirst("^/*", "").replaceAll("/", slash);
                return String.format("%s%s%s", iiifRootPath, slash, s);
        }
       
        
        public void addCanvasToManifest(IIIFCanvasWrapper canvasWrap) {
                seq.getArray(IIIFArray.canvases).put(canvasWrap.getJSONObject());
        }

        public IIIFCanvasWrapper addCanvas(String key, File f, MetadataInputFile itemMeta) {
                String iiifpath = getIIIFPath(key, f);
                IIIFCanvasWrapper canvasWrap = new IIIFCanvasWrapper(this, iiifpath, getDimensions(f), f.getName());
                String canvasKey = manifestProjectTranslate.getSequenceValue(orderedCanvases.size(), itemMeta);
                canvasWrap.setSortName(canvasKey);
                orderedCanvases.add(canvasWrap);
                
                return canvasWrap;
        }
        
        public ManifestDimensions getDimensions(File f) {
                try {
                        return new ManifestDimensions(f);
                } catch (IOException | SAXException | TikaException e) {
                        e.printStackTrace();
                } 
                return DefaultDimensions.PORTRAIT.dimensions;
        }
        public IIIFCanvasWrapper addEadCanvas(String imageUrl, Node eadNode, XMLInputFile itemMeta) {
                String slash = manifestGen.getDirSeparator();
                String iiifpath = imageUrl.replaceAll("/",  slash).replaceFirst("^IIIFRoot", iiifRootPath);
                String[] paths = iiifpath.split("/");
                String name = paths[paths.length-1];
                String infoJson = String.format("%s/info.json", iiifpath);
                IIIFCanvasWrapper canvasWrap = new IIIFCanvasWrapper(this, iiifpath, getDimensions(infoJson), name);
                String title = itemMeta.getXPathValue(eadNode, "ead:daodesc//text()", "");
                canvasWrap.setProperty(IIIFType.typeCanvas, IIIFStandardProp.label, title);
                canvasWrap.setProperty(IIIFType.typeCanvas, IIIFMetadataProp.title, title);

                String canvasKey = manifestProjectTranslate.getSequenceValue(orderedCanvases.size(), itemMeta);
                canvasWrap.setSortName(canvasKey);
                orderedCanvases.add(canvasWrap);
                
                return canvasWrap;
        }
        

        public ManifestDimensions getDimensions(String infojson) {
                return DefaultDimensions.PORTRAIT.dimensions;
        }

        public ManifestProjectTranslate getManifestProjectTranslate() {
                return this.manifestProjectTranslate;
        }
        
        public void setProjectTranslate(ManifestProjectTranslate manifestProjectTranslate) {
                this.manifestProjectTranslate = manifestProjectTranslate;
        }

}


