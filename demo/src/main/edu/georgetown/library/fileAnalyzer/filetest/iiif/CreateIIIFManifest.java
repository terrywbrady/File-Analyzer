package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.JpegFileTestFilter;
import gov.nara.nwts.ftapp.filter.TiffFileTestFilter;
import gov.nara.nwts.ftapp.filter.TiffJpegFileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifer;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileException;

public class CreateIIIFManifest extends DefaultFileTest {
        protected static enum Type {Folder, Image;}
        protected static enum Status {NA, Skip, Complete, NoMetadata, NoImage, Error;}
        static enum IIIFStatsItems implements StatsItemEnum {
                Key(StatsItem.makeStringStatsItem("Key", 300)),
                Path(StatsItem.makeStringStatsItem("Path", 300)),
                Status(StatsItem.makeEnumStatsItem(Status.class, "Status")),
                Width(StatsItem.makeIntStatsItem("Width").setWidth(60)),
                Height(StatsItem.makeIntStatsItem("Height").setWidth(60)),
                Identifier(StatsItem.makeStringStatsItem("Identifier")),
                Title(StatsItem.makeStringStatsItem("Title", 200)),
                Sequence(StatsItem.makeStringStatsItem("Sequence")),
                ParentRange(StatsItem.makeStringStatsItem("Parent Range")),
                Note(StatsItem.makeStringStatsItem("Note", 200))
                ;

                StatsItem si;

                IIIFStatsItems(StatsItem si) {
                        this.si = si;
                }

                public StatsItem si() {
                        return si;
                }
        }
        
        public static enum Generator implements StatsGenerator {
                INSTANCE;

                public Stats create(String key) {
                        return new Stats(details, key);
                }
        }

        public static StatsItemConfig details = StatsItemConfig.create(IIIFStatsItems.class);

        public static final String IIIFROOT      = "iiifroot";
        public static final String MANIFEST      = "manifest";
        public static final String INITMETADATA  = "initMetadata";
        public static final String MAKECOLL      = "makecoll";
        public static final String METHOD_ID     = "method-id";
        public static final String METHOD_META   = "method-meta";
        public static final String TRANSLATE     = "translate";
        public static final String LOGOURL       = "logo-url";
        IIIFManifest manifest;
        ManifestProjectTranslate manifestProjectTranslate;
        MetadataInputFile inputMetadata;

        public FTProp addProp(String name, String label) {
                FTProp prop = new FTPropString(dt, this.getClass().getSimpleName(), name, name, label, ""); 
                ftprops.add(prop);
                return prop;
        }
        public FTProp addPropEnum(String name, String label, Object[] vals, Object def) {
                FTProp prop = new FTPropEnum(dt, this.getClass().getSimpleName(), name, name, label, vals, def); 
                ftprops.add(prop);
                return prop;
        }
        
        MetadataInputFileBuilder metaBuilder = new MetadataInputFileBuilder();
        public void addProjectTranslator() {
                addPropEnum(TRANSLATE, "Project Value Translator", DefaultManifestProjectTranslate.values(), DefaultManifestProjectTranslate.Default);                
        }
        
        public CreateIIIFManifest(FTDriver dt) {
                super(dt);
                addProp(IIIFROOT, "IIIF Server Root Path");
                addProp(MANIFEST, "Output Path for Manifest File");
                addProp(INITMETADATA, "Input File used to populate manifest metadata");
                addPropEnum(MAKECOLL, "Make collection manifest of manifests", YN.values(), YN.N);
                addPropEnum(METHOD_ID, "Method to determine item identifiers", MethodIdentifer.values(), MethodIdentifer.FolderName);
                addPropEnum(METHOD_META, "Method to determine item metadata", MethodMetadata.values(), MethodMetadata.None);
                addProp(LOGOURL, "URL to logo to embed in the manifest file");
                addProjectTranslator();
        }

        public InitializationStatus init() {
                //InitializationStatus is = new InitializationStatus();
                InitializationStatus is = super.init();
                manifestProjectTranslate = (ManifestProjectTranslate)getProperty(TRANSLATE);
                String sInit = this.getProperty(INITMETADATA).toString(); 
                try {
                        inputMetadata = metaBuilder.identifyFile(this.getRoot(), sInit);
                } catch (InputFileException e) {
                        is.addMessage(e.getMessage());
                        return is;
                }
                
                File manFile = new File(this.getProperty(MANIFEST).toString());
                try {
                        manifest = new IIIFManifest(inputMetadata, this.getProperty(IIIFROOT).toString(), manFile, hasCollectionManifest());
                        manifest.setProjectTranslate(manifestProjectTranslate);
                        manifest.init();
                        manifest.setLogoUrl(getProperty(LOGOURL, IIIFManifest.EMPTY).toString());
                } catch (IOException e) {
                        is.addMessage(e);
                        return is;
                }
                lastParent = null;
                curmanifest = null;
                currentMetadataFile = null;
                
                return is;
        }
        
        public void refineResults() {
                try {
                        manifest.write();
                } catch (JSONException e) {
                         e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        
        public String toString() {
                return "Create IIIF Manifest";
        }

        public String getKey(File f) {
                return getRelPath(f);
        }

        public String getShortName() {
                return "IIIF";
        }
        
        public boolean hasCollectionManifest() {
                return (this.getProperty(MAKECOLL) == YN.Y);
        }

        public IIIFManifest getCurrentManifest(File f) throws IOException {
                if (!hasCollectionManifest()) {
                        return manifest;
                }
                
                File curfile = manifest.getComponentManifestFile(f, getIdentifier(IIIFType.typeManifest, f));
                inputMetadata.setCurrentKey(getKey(f));
                IIIFManifest itemManifest = new IIIFManifest(inputMetadata, this.getProperty(IIIFROOT).toString(), curfile, false);
                manifest.setProjectTranslate(manifestProjectTranslate);
                manifest.init();
                manifest.addManifestToCollection(itemManifest);
                return itemManifest;
        }
        
        public String getIdentifier(IIIFType type, File f) {
                MethodIdentifer methId = (MethodIdentifer)getProperty(METHOD_ID);
                String ret = f.getName();
                if (methId == MethodIdentifer.MetadataFile) {
                        inputMetadata.setCurrentKey(f.getName());
                        ret = inputMetadata.getValue(IIIFLookup.Identifier, "NA");
                }
                return manifestProjectTranslate.translate(type, IIIFProp.identifier, ret);
        }
        
        
        File lastParent;
        IIIFManifest curmanifest;
        MetadataInputFile currentMetadataFile;
        
        public Object fileTest(File f) {
                Stats s = getStats(f);
                File parent = f.getParentFile();
                
                try {
                        if (!parent.equals(lastParent)) {
                                lastParent = parent;
                                curmanifest = getCurrentManifest(parent);
                                //TODO: evaluate parameter to set this
                                currentMetadataFile = metaBuilder.findMetadataFile(parent, inputMetadata);
                        }
                        s.setVal(IIIFStatsItems.Path, s.key);
                        if (manifestProjectTranslate.includeItem(currentMetadataFile)) {
                                s.setVal(IIIFStatsItems.Status, Status.Complete); //TODO - evaluate
                                JSONObject range = curmanifest.makeRange(s.key, f, currentMetadataFile);
                                s.setVal(IIIFStatsItems.ParentRange, range.get(IIIFProp.label.getLabel())); 
                                
                                String canvasKey = curmanifest.addFile(s.key, f, currentMetadataFile);
                                JSONObject canvas = curmanifest.getCanvas(canvasKey);
                                curmanifest.linkRangeToCanvas(range, canvas);
                                
                                s.setVal(IIIFStatsItems.Height, canvas.getInt(IIIFProp.height.getLabel())); 
                                s.setVal(IIIFStatsItems.Width, canvas.getInt(IIIFProp.width.getLabel())); 
                                s.setVal(IIIFStatsItems.Identifier, canvas.get(IIIFProp.id.getLabel())); 
                                s.setVal(IIIFStatsItems.Title, canvas.get(IIIFProp.label.getLabel())); 
                                s.setVal(IIIFStatsItems.Sequence, canvasKey); 
                        } else {
                                s.setVal(IIIFStatsItems.Status, Status.Skip); 
                        }
                } catch (IOException | InputFileException e) {
                        s.setVal(IIIFStatsItems.Status, Status.Error); 
                        s.setVal(IIIFStatsItems.Note, e.getMessage());                         
                }
                return s;
        }

        public Stats createStats(String key) {
                return Generator.INSTANCE.create(key);
        }

        public StatsItemConfig getStatsDetails() {
                return details;
        }

        public void initFilters() {
                filters.add(new TiffJpegFileTestFilter());
                filters.add(new TiffFileTestFilter());
                filters.add(new JpegFileTestFilter());
        }

        public String getDescription() {
                return "Create IIIF Manifest for files";
        }

}
