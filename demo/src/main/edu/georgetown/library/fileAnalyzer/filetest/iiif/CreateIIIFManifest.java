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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodIdentifer;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileException;
import edu.georgetown.library.fileAnalyzer.util.InvalidFilenameException;

/**
 * @author TBrady
 *
 */
public class CreateIIIFManifest extends DefaultFileTest {
        protected static enum Type {Folder, Image;}
        static enum IIIFStatsItems implements StatsItemEnum {
                Key(StatsItem.makeStringStatsItem("Path", 400)),
                Type(StatsItem.makeEnumStatsItem(Type.class, "Type")),
                Name(StatsItem.makeStringStatsItem("Name", 400)), 
                InfoPath(StatsItem.makeStringStatsItem("InfoPath", 400));

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
        }

        public InitializationStatus init() {
                //InitializationStatus is = new InitializationStatus();
                InitializationStatus is = super.init();
                manifestProjectTranslate = (ManifestProjectTranslate)getProperty(TRANSLATE);
                String sInit = this.getProperty(INITMETADATA).toString(); 
                try {
                        inputMetadata = new MetadataInputFileBuilder().identifyFile(sInit);
                } catch (InputFileException e) {
                        is.addMessage(e.getMessage());
                        return is;
                }
                
                File manFile = new File(this.getProperty(MANIFEST).toString());
                try {
                        manifest = new IIIFManifest(inputMetadata, this.getProperty(IIIFROOT).toString(), manFile);
                } catch (IOException e) {
                        is.addMessage(e);
                        return is;
                }
                
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

        public IIIFManifest getCurrentManifest(File f) {
                if (this.getProperty(MAKECOLL) != YN.Y) {
                        return manifest;
                }
                
                File curfile = manifest.getComponentManifestFile(f, getIdentifier(f));
                return manifest;
        }
        
        public String getIdentifier(File f) {
                MethodIdentifer methId = (MethodIdentifer)getProperty(METHOD_ID);
                String ret = f.getName();
                if (methId == MethodIdentifer.MetadataFile) {
                        inputMetadata.setCurrentKey(f.getName());
                        ret = inputMetadata.getValue("identifier", "NA");
                }
                return manifestProjectTranslate.translate(ManifestProjectTranslate.IDENTIFIER, ret);
        }
        
        public Object fileTest(File f) {
                Stats s = getStats(f);
                File parent = f.getParentFile();
                
                IIIFManifest curmanifest = manifest;
                if (this.getProperty(MAKECOLL) == YN.Y) {
                        
                }
                
                manifest.makeRange(parent, parent.getName(), parent.getName(), false);
                
                s.setVal(IIIFStatsItems.Name, f.getName());
                s.setVal(IIIFStatsItems.Type, Type.Image);
                s.setVal(IIIFStatsItems.InfoPath, manifest.addFile(s.key, f));                        
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
