package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.CollectionMode;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifier;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.ftprop.InvalidInputException;

public class ManifestGeneratePropFile extends FTPropString {
        final String PROP_IIIFRoot                  = "IIIFRoot";
        final String PROP_ManifestRoot              = "ManifestRoot";
        final String PROP_ManifestOuputDir          = "ManifestOuputDir";
        final String PROP_ManifestOuputFile         = "ManifestOuputFile";
        final String PROP_CreateCollectionManifest  = "CreateCollectionManifest";
        final String PROP_ManifestLogoURL           = "ManifestLogoURL";
        final String PROP_ManifestMetadataInputFile = "ManifestMetadataInputFile";
        final String PROP_GetItemIdentifier         = "GetItemIdentifier";
        final String PROP_GetItemMetadata           = "GetItemMetadata";
        final String PROP_ManifestProject           = "ManifestProject";
        final String PROP_DirSeparator              = "DirectorySeparator";
        final String PROP_Set2PageView              = "Set2PageView";
        
        final String VAL_ManifestOuputFile          = "manifest.json";
        final String VAL_ItemFolder                 = "FolderName";
        final String VAL_ItemFile                   = "FileName";
        final String VAL_ItemMetadata               = "ItemMetadataFile";
        final String VAL_ManifestMetadata           = "ManifestMetadataFile";
        final String VAL_ItemREST                   = "RESTAPI";
        final String VAL_None                       = "None";
        final String VAL_NoCollection               = "NoCollection";
        final String VAL_OneItemPerFolder           = "OneItemPerFolder";
        final String VAL_ManyItemsPerFolder         = "ManyItemsPerFolder";
                        
        
        final String RX_tf                          = "(true|false)?";
        final String RX_ItemId                      = "(FolderName|FileName|ItemMetadataFile)";
        final String RX_ItemMeta                    = "(None|RESTAPI|ItemMetadataFile|ManifestMetadataFile)";
        final String RX_Collection                  = "(NoCollection|OneItemPerFolder|ManyItemsPerFolder)";
        
        Properties prop = new Properties();
        File propFile;
        File inMeta;
        private MethodIdentifier myMethodIdentifier;
        private MethodMetadata   myMethodMetadata;
        private CollectionMode   myCollectionMode;
        
        
        ManifestGeneratePropFile(FTDriver dt, String prefix) {
            super(dt, prefix, CreateIIIFManifest.MANGEN, CreateIIIFManifest.MANGEN,
                    "Manifest Generation Property Filename", "manifestGenerate.prop");
        }
        @Override public InitializationStatus initValidation(File refFile) {
            InitializationStatus iStat = new InitializationStatus();
            try {
                    propFile = new File(ft.root, this.getValue().toString());
                    readPropertyFile(propFile);
                    File outdir = getManifestOutputDir();
                    File manfile = getManifestOutputFile();
                    if (manfile.exists()) {
                            if (!manfile.canWrite()) {
                                    throw new InputFileException(String.format("Existing Manifest File [%s] must be writeable", manfile.getAbsolutePath()));
                            } else if (!outdir.canWrite()) {
                                    throw new InputFileException(String.format("New Manifest File [%s] must be writeable", manfile.getAbsolutePath()));
                            }
                    }
                    inMeta = getManifestInputFile(propFile);
                    if (inMeta == null) {
                    } else if (!inMeta.exists()) {
                            throw new InputFileException(String.format("Metadata File [%s] must exist", inMeta.getAbsolutePath()));
                    }
                    setItemIdentifierMethod();
                    setItemMetadataMethod();
                    setCreateCollectionMode();
            } catch (InvalidInputException| IOException | InputFileException e) {
                    iStat.addFailMessage(e.getMessage());
            }
            return iStat;
        }
        
        MetadataInputFile getMetadataInputFile(MetadataInputFileBuilder mifBuild) throws InputFileException {
                if (inMeta == null) {
                        return null;
                }
                return mifBuild.identifyFile(inMeta);
        }
        
        public File getManifestGenPropFile() {
                return propFile;
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

        public String getManifestRoot() throws edu.georgetown.library.fileAnalyzer.filetest.iiif.InputFileException {
                return prop.getProperty(PROP_ManifestRoot, "");
        }

        /*
         * Default to current dir if empty
         */
        public File getManifestOutputDir(){
                String dir = prop.getProperty(PROP_ManifestOuputDir, "");
                if (dir.isEmpty()) {
                        return ft.root;
                }
                return new File(dir);
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
                return new File(ft.root, fname);
        }

        public CollectionMode getCreateCollectionMode() {
                return this.myCollectionMode;
        }
        public void setCreateCollectionMode() throws InputFileException {
                String s = prop.getProperty(PROP_CreateCollectionManifest, VAL_NoCollection);
                if (!Pattern.compile(RX_Collection).matcher(s).matches()) {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_Collection, s));
                } else if (s.equals(VAL_NoCollection)) {
                        this.myCollectionMode = CollectionMode.NoCollection;
                } else if (s.equals(VAL_OneItemPerFolder)) {
                        this.myCollectionMode = CollectionMode.OneItemPerFolder;
                } else if (s.equals(VAL_ManyItemsPerFolder)) {
                        this.myCollectionMode = CollectionMode.ManyItemsPerFolder;
                } else {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_Collection, s));
                }
        }
        public boolean getCreateCollectionManifest() throws InputFileException {
                return myCollectionMode != CollectionMode.NoCollection;
        }

        public MethodIdentifier getItemIdentifierMethod()  {
                return this.myMethodIdentifier;
        }
        public void setItemIdentifierMethod() throws InputFileException {
                String s = prop.getProperty(PROP_GetItemIdentifier, "");
                if (!Pattern.compile(RX_ItemId).matcher(s).matches()) {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemId, s));
                }
                if (s.equals(VAL_ItemFolder)) {
                        this.myMethodIdentifier = MethodIdentifier.FolderName;
                } else if (s.equals(VAL_ItemFile)) {
                        this.myMethodIdentifier = MethodIdentifier.FileName;
                } else if (s.equals(VAL_ItemMetadata)) {
                        this.myMethodIdentifier = MethodIdentifier.ItemMetadataFile;
                } else {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemId, s));
                }
        }
        public MethodMetadata getItemMetadataMethod() {
                return this.myMethodMetadata;
        }
        public void setItemMetadataMethod() throws InputFileException {
                String s = prop.getProperty(PROP_GetItemMetadata, "");
                if (!Pattern.compile(RX_ItemMeta).matcher(s).matches()) {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemMeta, s));
                } else if (s.equals(VAL_ManifestMetadata)) {
                        this.myMethodMetadata = MethodMetadata.ManifestMetadataFile;
                } else if (s.equals(VAL_ItemMetadata)) {
                        this.myMethodMetadata = MethodMetadata.ItemMetadataFile;
                } else if (s.equals(VAL_ItemREST)) {
                        this.myMethodMetadata = MethodMetadata.RestAPI;
                } else if (s.equals(VAL_None)) {
                        this.myMethodMetadata = MethodMetadata.None;
                } else {
                        throw new InputFileException(String.format("%s must be '%s'. [%s] found", PROP_GetItemIdentifier, RX_ItemMeta, s));
                }
        }

        public String getManifestLogoURL() {
                return prop.getProperty(PROP_ManifestLogoURL);
        }
        
        public ManifestProjectTranslate getManifestProject(Object[] vals) {
                String s = prop.getProperty(PROP_ManifestProject);
                ManifestProjectTranslateEnum translateEnum = DefaultManifestProjectTranslateEnum.Default;
                for(Object val: vals) {
                        if (val instanceof ManifestProjectTranslateEnum && val instanceof Enum) {
                                if (((Enum<?>)val).name().equals(s)) {
                                        translateEnum = (ManifestProjectTranslateEnum)val;
                                        break;
                                }
                        }
                }
                return translateEnum.getTranslator();
        }
        
        public String getProperty(IIIFLookup lookup) {
                return prop.getProperty(lookup.getProperty(), "");
        }
        
        /*
         * Return "/" by defualt.  "%2F" for Cantaloupe.
         */
        public String getDirSeparator() {
                return prop.getProperty(PROP_DirSeparator, "/");
        }

        public boolean getSet2PageView() {
                return prop.getProperty(PROP_Set2PageView, "false").equals("true");
        }

}


