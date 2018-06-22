package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.xpath.XPath;

import org.apache.tika.exception.TikaException;
import org.json.JSONArray;
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
        
        TreeSet<IIIFCanvasWrapper> orderedCanvases = new TreeSet<>();

        HashMap<String,RangePath> parentRangeForCanvas = new HashMap<>();
        TreeMap<String,IIIFManifest> collectionManifests = new TreeMap<>();
        
        private RangePath top;
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
                setProperty(IIIFType.typeManifest, IIIFMetadataProp.dateCreated, inputMetadata.getValue(IIIFLookupEnum.DateCreated.getLookup(), def));
                def = manifestGen.getProperty(IIIFLookupEnum.Creator.getLookup());
                setProperty(IIIFType.typeManifest, IIIFMetadataProp.creator, inputMetadata.getValue(IIIFLookupEnum.Creator.getLookup(), def));
                def = manifestGen.getProperty(IIIFLookupEnum.ManifestDescription.getLookup());
                setProperty(IIIFType.typeManifest, IIIFMetadataProp.manifestDescription, def);
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
                top = RangePath.makeRangePath(this, "__toprange","Top Range");
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
        
        public void addManifestToCollection(IIIFManifest itemManifest, String seq) {
                if (seq.isEmpty()) {
                        seq = String.format("%-5d", collectionManifests.size());
                }
                collectionManifests.put(seq, itemManifest);
        }

        public File getManifestFile() {
                return file;
        }

        public File getComponentManifestFile(File f, String identifier) {
                return new File(file.getParentFile(), String.format("%s.json",identifier));
        }

        public void checkManifestFile(File manFile) throws IOException {
                try(OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(manFile), "UTF-8")) {
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
        
        public void linkRangeToCanvas(RangePath rangePath, IIIFCanvasWrapper canvas) {
                String canvasid = canvas.getProperty(IIIFStandardProp.id, EMPTY);
                if (canvasid == null) {
                        return;
                }
                if (!canvasid.isEmpty()) {
                        rangePath.addCanvas(canvas);
                }
        }
        
        public void refine()  {
                for(String seq: collectionManifests.keySet()) {
                        IIIFManifest cman = collectionManifests.get(seq);
                        this.getArray(IIIFArray.manifests).put(cman.getMinimalJSONObject());
                }
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
                try(BufferedWriter fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))){
                        fw.write(jsonObject.toString());
                }
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
                String canvasKey = manifestProjectTranslate.getSequenceValue(key, itemMeta);
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

                String canvasKey = manifestProjectTranslate.getSequenceValue(iiifpath, itemMeta);
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

        public void addItemMetadata(IIIFJSONWrapper obj, IIIFType type, File f, MetadataInputFile itemMeta) {
                obj.setProperty(type, IIIFMetadataProp.title, itemMeta.getValue(IIIFLookupEnum.Title.getLookup(), EMPTY));
                obj.setProperty(type, IIIFMetadataProp.dateCreated, itemMeta.getValue(IIIFLookupEnum.DateCreated.getLookup(), EMPTY));
                obj.setProperty(type, IIIFMetadataProp.creator, itemMeta.getValue(IIIFLookupEnum.Creator.getLookup(), EMPTY));
                obj.setProperty(type, IIIFMetadataProp.description, itemMeta.getValue(IIIFLookupEnum.Description.getLookup(), EMPTY));
                obj.setProperty(type, IIIFMetadataProp.type, itemMeta.getValue(IIIFLookupEnum.Type.getLookup(), EMPTY));
                obj.setProperty(type, IIIFMetadataProp.language, itemMeta.getValue(IIIFLookupEnum.Language.getLookup(), EMPTY));
                ArrayList<IIIFLookup> subjectLookups = new ArrayList<>();
                subjectLookups.add(IIIFLookupEnum.Subject.getLookup());
                subjectLookups.add(IIIFLookupEnum.SubjectLcsh.getLookup());
                subjectLookups.add(IIIFLookupEnum.SubjectOther.getLookup());
                obj.setProperty(type, IIIFMetadataProp.subject, itemMeta.getValue(subjectLookups, EMPTY, "; ").replaceAll("\\|\\|", "; "));
                obj.setProperty(type, IIIFMetadataProp.rights, itemMeta.getValue(IIIFLookupEnum.Rights.getLookup(), EMPTY));
                String uri = itemMeta.getValue(IIIFLookupEnum.Permalink.getLookup(), EMPTY);
                if (!uri.isEmpty()) {
                        uri = String.format("<a href='%s'>%s</a>", uri, uri);
                }
                obj.setProperty(type, IIIFMetadataProp.permalink, uri);
        }

        public boolean isCollectionManifest() {
                return isCollectionManifest;
        }
        
}


