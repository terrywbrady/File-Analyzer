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

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookupEnum;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFMetadataProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifier;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;

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
                DateCreated(StatsItem.makeStringStatsItem("Date Created")),
                ParentRange(StatsItem.makeStringStatsItem("Parent Range", 200)),
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
        File lastParent;
        File lastAncestor;
        IIIFManifest curmanifest;
        MetadataInputFile currentMetadataFile;
        
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
        
        MetadataInputFileBuilder metaBuilder;
        public ManifestProjectTranslateEnum[] getProjectTranslatorValues() {
                return DefaultManifestProjectTranslateEnum.values();                
        }
        
        ManifestGeneratePropFile manifestGen;
        public CreateIIIFManifest(FTDriver dt) {
                super(dt);
                manifestGen = new ManifestGeneratePropFile(dt, this.getClass().getSimpleName());
                this.ftprops.add(manifestGen);
                ManifestProjectTranslateEnum[] vals = getProjectTranslatorValues();
                ManifestProjectTranslateEnum val = vals.length == 0 ? DefaultManifestProjectTranslateEnum.Default: vals[0];
                addPropEnum(TRANSLATE, "Project Value Translator: if set to Default, use the value in the property file", vals, val);                
        }

        public InitializationStatus init() {
                InitializationStatus is = super.init();
                
                metaBuilder = new MetadataInputFileBuilder(manifestGen);
                
                ManifestProjectTranslateEnum manifestProjectTranslateEnum = (ManifestProjectTranslateEnum)getProperty(TRANSLATE);
                manifestProjectTranslate = manifestProjectTranslateEnum.getTranslator();
                
                //Since a custom enum may exist for a project, compare on enum name
                if (manifestProjectTranslateEnum.name() == DefaultManifestProjectTranslateEnum.Default.name()) {
                        manifestProjectTranslate = manifestGen.getManifestProject(getProjectTranslatorValues()); 
                }
                File metadataInputFile = manifestGen.getManifestInputFile(manifestGen.getManifestGenPropFile());
                try {
                        inputMetadata = metaBuilder.identifyFile(metadataInputFile);
                } catch (InputFileException e) {
                        is.addMessage(e.getMessage());
                        return is;
                }
                
                try {
                        manifest = new IIIFManifest(inputMetadata, manifestGen);
                        manifest.setProjectTranslate(manifestProjectTranslate);
                        manifest.init(dt.getRoot(), "");
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
                        if (curmanifest != null && curmanifest != manifest) {
                                curmanifest.write();
                                curmanifest = null;
                        }
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
        
        public IIIFManifest getCurrentManifest(File parent, MetadataInputFile currentInput) throws IOException, InputFileException {
                if (manifestGen.getCreateCollectionManifest()) {
                        File mf = manifest.getComponentManifestFile(parent, getIdentifier(IIIFType.typeManifest, parent));
                        inputMetadata.setCurrentKey(getKey(parent));
                        if (curmanifest != null) {
                                curmanifest.write();
                                curmanifest = null;
                        }
                        curmanifest = new IIIFManifest(currentInput, manifestGen, mf);
                        curmanifest.setProjectTranslate(manifestProjectTranslate);
                        return curmanifest;
                }
                return manifest;
        }
        
        public String getIdentifier(IIIFType type, File f) {
                String ret = f.getName();
                if (type == IIIFType.typeManifest) {
                        ret = f.getName();
                } else {
                        MethodIdentifier methId = manifestGen.getItemIdentifierMethod();
                        if (methId == MethodIdentifier.ItemMetadataFile) {
                                inputMetadata.setCurrentKey(f.getName());
                                ret = inputMetadata.getValue(IIIFLookupEnum.Identifier.getLookup(), "NA");
                        } else if (methId == MethodIdentifier.FileName) {
                                ret = f.getName();
                                inputMetadata.setCurrentKey(ret);
                        } else if (methId == MethodIdentifier.FolderName) {
                                ret = f.getParentFile().getName();
                                inputMetadata.setCurrentKey(ret);
                        }
                }
                return manifestProjectTranslate.translate(type, IIIFStandardProp.identifier, ret);
        }
        
        public File getRootAncestor(File f) {
                for(File cf=f; cf != null; cf = cf.getParentFile()) {
                        if (dt.getRoot().equals(cf.getParentFile())) {
                                return cf;
                        }
                }
                return f;
        }
        
        public Object fileTest(File f) {
                Stats s = getStats(f);
                File parent = f.getParentFile();
                File ancestor = getRootAncestor(parent);
                
                try {
                        if (!parent.equals(lastParent)) {
                                lastParent = parent;
                                if (manifestGen.getItemMetadataMethod() == MethodMetadata.RestAPI) {
                                        currentMetadataFile = new RESTResponseInputFile(manifestGen.getDSpaceRestUrl());
                                } else if (manifestGen.getItemMetadataMethod() == MethodMetadata.ItemMetadataFile) {
                                        currentMetadataFile = metaBuilder.findMetadataFile(parent, inputMetadata);
                                } else if (manifestGen.getItemMetadataMethod() == MethodMetadata.ManifestMetadataFile) {
                                        currentMetadataFile = manifestGen.getMetadataInputFile(metaBuilder);
                                } else {
                                        currentMetadataFile = metaBuilder.emptyInputFile(); 
                                }
                                if (!ancestor.equals(lastAncestor)) {
                                        lastAncestor = ancestor;
                                        curmanifest = getCurrentManifest(ancestor, currentMetadataFile);
                                        currentMetadataFile.setCurrentKey(getIdentifier(IIIFType.typeCanvas, f));
                                        if (manifestGen.getCreateCollectionManifest()) {
                                                curmanifest.init(dt.getRoot(), getRelPath(parent));
                                                manifest.addManifestToCollection(curmanifest);
                                        }
                                } else {
                                        currentMetadataFile.setCurrentKey(getIdentifier(IIIFType.typeCanvas, f));
                                }

                        }
                        s.setVal(IIIFStatsItems.Path, s.key);
                        if (manifestProjectTranslate.includeItem(currentMetadataFile)) {
                                s.setVal(IIIFStatsItems.Status, Status.Complete); //TODO - evaluate
                                
                                RangePath rangePath = curmanifest.makeRange(s.key, parent, currentMetadataFile);
                                
                                s.setVal(IIIFStatsItems.ParentRange, rangePath.getFullPath()); 
                                
                                IIIFCanvasWrapper canvasWrap = curmanifest.addCanvas(s.key, f, currentMetadataFile);
                                canvasWrap.addCanvasMetadata(f, currentMetadataFile);
                                curmanifest.linkRangeToCanvas(rangePath, canvasWrap);
                                
                                s.setVal(IIIFStatsItems.Height, canvasWrap.getIntProperty(IIIFStandardProp.height, 0)); 
                                s.setVal(IIIFStatsItems.Width, canvasWrap.getIntProperty(IIIFStandardProp.width, 0)); 
                                s.setVal(IIIFStatsItems.Identifier, canvasWrap.getProperty(IIIFStandardProp.id, IIIFManifest.EMPTY)); 
                                s.setVal(IIIFStatsItems.Title, canvasWrap.getProperty(IIIFStandardProp.label, IIIFManifest.EMPTY)); 
                                s.setVal(IIIFStatsItems.DateCreated, canvasWrap.getProperty(IIIFMetadataProp.dateCreated, IIIFManifest.EMPTY)); 
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
