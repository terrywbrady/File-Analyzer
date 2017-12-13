package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifer;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.ftprop.InvalidInputException;

public class ManifestGeneratePropFile extends FTPropString {
        final String PROP_IIIFRoot                  = "IIIFRoot";
        final String PROP_ManifestOuputDir          = "ManifestOuputDir";
        final String PROP_ManifestOuputFile         = "ManifestOuputFile";
        final String PROP_CreateCollectionManifest  = "CreateCollectionManifest";
        final String PROP_ManifestLogoURL           = "ManifestLogoURL";
        final String PROP_ManifestMetadataInputFile = "ManifestMetadataInputFile";
        final String PROP_GetItemIdentifier         = "GetItemIdentifier";
        final String PROP_GetItemMetadata           = "GetItemMetadata";
        final String PROP_ManifestProject           = "ManifestProject";
        
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
        File propFile;
        
        ManifestGeneratePropFile(FTDriver dt, String prefix) {
            super(dt, prefix, CreateIIIFManifest.MANGEN, CreateIIIFManifest.MANGEN,
                    "Manifest Generation Filename", "manifestGenerate.prop");
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
                    File inMeta = getManifestInputFile(propFile);
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
}
