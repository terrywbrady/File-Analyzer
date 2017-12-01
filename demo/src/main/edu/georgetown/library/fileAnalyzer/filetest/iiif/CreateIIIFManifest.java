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
import gov.nara.nwts.ftapp.ftprop.InvalidInputException;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifer;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileException;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;
import edu.georgetown.library.fileAnalyzer.importer.OutputToBursar;
import edu.georgetown.library.fileAnalyzer.importer.OutputToBursar.Patron;
import edu.georgetown.library.fileAnalyzer.importer.OutputToBursar.PatronPropFile;

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
        public void addProjectTranslator() {
                addPropEnum(TRANSLATE, "Project Value Translator", DefaultManifestProjectTranslate.values(), DefaultManifestProjectTranslate.Default);                
        }
        
        ManifestGeneratePropFile manifestGen;
        class ManifestGeneratePropFile extends FTPropString {
                final String PROP_IIIFRoot                  = "IIIFRoot";
                final String PROP_ManifestOuputDir          = "ManifestOuputDir";
                final String PROP_ManifestOuputFile         = "ManifestOuputFile";
                final String PROP_CreateCollectionManifest  = "CreateCollectionManifest";
                final String PROP_ManifestLogoURL           = "ManifestLogoURL";
                final String PROP_ManifestMetadataInputFile = "ManifestMetadataInputFile";
                final String PROP_GetItemIdentifier         = "GetItemIdentifier";
                final String PROP_GetItemMetadata           = "GetItemMetadata";
                
                final String VAL_ManifestOuputFile          = "manifest.json";
                final String VAL_ItemFolder                 = "FolderName";
                final String VAL_ItemMetadata               = "MetadataFile";
                final String VAL_ItemREST                   = "RESTAPI";
                final String VAL_None                       = "None";
                final String VAL_true                       = "true";
                
                final String RX_tf                          = "(true|false)?";
                final String RX_ItemId                      = "(FolderName|MetadataFile)";
                final String RX_ItemMeta                    = "(None|RESTAPI|MetadataFile)";
                
                
                Properties prop = new Properties();
                
                ManifestGeneratePropFile(FTDriver dt) {
                    super(dt, CreateIIIFManifest.this.getClass().getName(), MANGEN, MANGEN,
                            "Manifest Generation Filename", "manifestGenerate.prop");
                }
                @Override public InitializationStatus initValidation(File refFile) {
                    InitializationStatus iStat = new InitializationStatus();
                    try {
                            File f = new File(refFile.getParentFile(), this.getValue().toString());
                            readPropertyFile(f);
                            File outdir = getManifestOutputDir();
                            File manfile = getManifestOutputFile();
                            if (manfile.exists()) {
                                    if (!manfile.canWrite()) {
                                            throw new InputFileException(String.format("Existing Manifest File [%s] must be writeable", manfile.getAbsolutePath()));
                                    } else if (!outdir.canWrite()) {
                                            throw new InputFileException(String.format("New Manifest File [%s] must be writeable", manfile.getAbsolutePath()));
                                    }
                            }
                            File inMeta = getManifestInputFile(f);
                            if (!inMeta.exists()) {
                                    throw new InputFileException(String.format("Metadata File [%s] must exist", inMeta.getAbsolutePath()));
                            }
                            this.getItemIdentifierMethod();
                            this.getItemMetadataMethod();
                            this.getCreateCollectionManifest();
                    } catch (InvalidInputException| IOException | InputFileException e) {
                            iStat.addFailMessage(e.getMessage());
                    }
                    return iStat;
                }
                public void readPropertyFile(File selectedFile) throws IOException, InvalidInputException, InputFileException {
                        try {
                                prop.load(new FileReader(selectedFile));
                        } catch (Exception e) {
                                throw new InputFileException("Property Parsing Error "+e.getMessage());
                        }
                }
                
                public String getIIIFRoot() throws edu.georgetown.library.fileAnalyzer.filetest.iiif.InputFileException {
                        String s = prop.getProperty(PROP_IIIFRoot, "");
                        if (s.isEmpty()) {
                                throw new InputFileException(String.format("%s cannot be empty", PROP_IIIFRoot));
                        }
                        return s;
                }

                /*
                 * Default to current dir if empty
                 */
                public File getManifestOutputDir(){
                        String dir = prop.getProperty(PROP_IIIFRoot, "");
                        File root = CreateIIIFManifest.this.dt.root;
                        if (dir.isEmpty()) {
                                return root;
                        }
                        return new File(root, dir);
                }

                public File getManifestOutputFile(){
                        String file = prop.getProperty(PROP_ManifestOuputFile, VAL_ManifestOuputFile);
                        return new File(getManifestOutputDir(), file);
                }
                
                public File getManifestInputFile(File defFile){
                        String fname = prop.getProperty(PROP_ManifestMetadataInputFile, "");
                        if (fname.isEmpty()) {
                                return defFile;
                        }
                        File root = CreateIIIFManifest.this.dt.root;
                        return new File(root, fname);
                }

                public boolean getCreateCollectionManifest() throws InputFileException {
                        String s = prop.getProperty(PROP_CreateCollectionManifest, "");
                        if (!Pattern.compile("(true|false)?").matcher(s).matches()) {
                                throw new InputFileException(String.format("%s must be blank, 'true', or 'false'. [%s] found", PROP_CreateCollectionManifest, s));
                        }
                        return s.equals(VAL_true);
                }

                public MethodIdentifer getItemIdentifierMethod() throws InputFileException {
                        String s = prop.getProperty(PROP_GetItemIdentifier, "");
                        if (!Pattern.compile(RX_ItemId).matcher(s).matches()) {
                                throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemId, s));
                        }
                        if (s.equals(VAL_ItemFolder)) {
                                return MethodIdentifer.FolderName;
                        }
                        if (s.equals(VAL_ItemMetadata)) {
                                return MethodIdentifer.MetadataFile;
                        }
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemId, s));
                }
                public MethodMetadata getItemMetadataMethod() throws InputFileException {
                        String s = prop.getProperty(PROP_GetItemMetadata, "");
                        if (!Pattern.compile(RX_ItemMeta).matcher(s).matches()) {
                                throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemMeta, s));
                        }
                        if (s.equals(VAL_ItemMetadata)) {
                                return MethodMetadata.MetadataFile;
                        }
                        if (s.equals(VAL_ItemREST)) {
                                return MethodMetadata.RestAPI;
                        }
                        if (s.equals(VAL_None)) {
                                return MethodMetadata.None;
                        }
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemMeta, s));
                }

                public String getManifestLogoURL() {
                        return prop.getProperty(PROP_ManifestLogoURL);
                }
        }

        public CreateIIIFManifest(FTDriver dt) {
                super(dt);
                manifestGen = new ManifestGeneratePropFile(dt);
                this.ftprops.add(manifestGen);
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
                                JSONObject range = curmanifest.makeRange(s.key, parent, currentMetadataFile);
                                
                                s.setVal(IIIFStatsItems.ParentRange, IIIFManifest.getProperty(range, IIIFProp.label, IIIFManifest.EMPTY)); 
                                
                                String canvasKey = curmanifest.addFile(s.key, f, currentMetadataFile);
                                JSONObject canvas = curmanifest.getCanvas(canvasKey);
                                curmanifest.linkRangeToCanvas(range, canvas);
                                
                                
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
