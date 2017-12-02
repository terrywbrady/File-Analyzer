package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import gov.nara.nwts.ftapp.FTDriver;
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

        public static final String MANGEN        = "manifest-gen-prop";
        public static final String TRANSLATE     = "translate";
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
        public ManifestProjectTranslate[] getProjectTranslatorValues() {
                return DefaultManifestProjectTranslate.values();                
        }
        
        ManifestGeneratePropFile manifestGen;
        public CreateIIIFManifest(FTDriver dt) {
                super(dt);
                manifestGen = new ManifestGeneratePropFile(dt);
                this.ftprops.add(manifestGen);
                ManifestProjectTranslate[] vals = getProjectTranslatorValues();
                ManifestProjectTranslate val = vals.length == 0 ? DefaultManifestProjectTranslate.Default: vals[0];
                addPropEnum(TRANSLATE, "Project Value Translator", vals, val);                
        }

        public InitializationStatus init() {
                InitializationStatus is = super.init();
                
                manifestProjectTranslate = (ManifestProjectTranslate)getProperty(TRANSLATE);
                
                //Since a custom enum may exist for a project, compare on enum name
                if (manifestProjectTranslate.name() == DefaultManifestProjectTranslate.Default.name()) {
                        manifestProjectTranslate = manifestGen.getManifestProject(getProjectTranslatorValues()); 
                }
                File metadataInputFile = manifestGen.getManifestInputFile(manifestGen.getManifestGenPropFile());
                try {
                        inputMetadata = metaBuilder.identifyFile(metadataInputFile);
                } catch (InputFileException e) {
                        is.addMessage(e.getMessage());
                        return is;
                }
                
                File manFile = manifestGen.getManifestOutputFile();
                try {
                        manifest = new IIIFManifest(inputMetadata, manifestGen.getIIIFRoot(), manFile, manifestGen.getCreateCollectionManifest());
                        manifest.setProjectTranslate(manifestProjectTranslate);
                        manifest.init();
                        manifest.setLogoUrl(manifestGen.getManifestLogoURL());
                } catch (IOException | InputFileException e) {
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
                try {
                        return manifestGen.getCreateCollectionManifest();
                } catch (InputFileException e) {
                        //validation should have already been applied
                        return false;
                }
        }
        
        public IIIFManifest getCurrentManifest(File f) throws IOException, InputFileException {
                if (!hasCollectionManifest()) {
                        return manifest;
                }
                
                File curfile = manifest.getComponentManifestFile(f, getIdentifier(IIIFType.typeManifest, f));
                inputMetadata.setCurrentKey(getKey(f));
                IIIFManifest itemManifest = new IIIFManifest(inputMetadata, manifestGen.getIIIFRoot(), curfile, false);
                manifest.setProjectTranslate(manifestProjectTranslate);
                manifest.init();
                manifest.addManifestToCollection(itemManifest);
                return itemManifest;
        }
        
        public String getIdentifier(IIIFType type, File f) {
                MethodIdentifer methId;
                try {
                        methId = manifestGen.getItemIdentifierMethod();
                } catch (InputFileException e) {
                        //validation should have already been applied
                        methId = MethodIdentifer.FolderName;
                }
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
                                String rangePath = curmanifest.makeRangePath(s.key, parent, currentMetadataFile);
                                //JSONObject range = curmanifest.getRangeByName(rangePath);
                                //String rangeName = curmanifest.getRangeLabel(rangePath);
                                
                                s.setVal(IIIFStatsItems.ParentRange, rangePath); 
                                
                                String canvasKey = curmanifest.addFile(s.key, f, currentMetadataFile);
                                JSONObject canvas = curmanifest.getCanvas(canvasKey);
                                curmanifest.linkRangeToCanvas(rangePath, canvas);
                                
                                
                                s.setVal(IIIFStatsItems.Height, IIIFManifest.getIntProperty(canvas, IIIFProp.height, 0)); 
                                s.setVal(IIIFStatsItems.Width, IIIFManifest.getIntProperty(canvas, IIIFProp.width, 0)); 
                                s.setVal(IIIFStatsItems.Identifier, IIIFManifest.getProperty(canvas, IIIFProp.id, IIIFManifest.EMPTY)); 
                                s.setVal(IIIFStatsItems.Title, IIIFManifest.getProperty(canvas, IIIFProp.label, IIIFManifest.EMPTY)); 
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
