package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.MethodMetadata;

public class MetadataInputFileBuilder {

        public static final String NA = "NA.txt";
        public enum InputFileType {
                NA, Property, CSV, DC, METS, EAD, REST;
        }
        private ArrayList<FilenameFilter> filters = new ArrayList<>();
        
        public MetadataInputFileBuilder(ManifestGeneratePropFile manGen) {
                if (manGen.getItemMetadataMethod() == MethodMetadata.ItemMetadataFile) {
                        filters.add(new FilenameFilter(){
                                public boolean accept(File dir, String name) {
                                        return name.toLowerCase().equals("mets.xml");
                                }
                        });
                        filters.add(new FilenameFilter(){
                                public boolean accept(File dir, String name) {
                                        return name.toLowerCase().equals("dublin_core.xml");
                                }
                        });
                }
        }
        
        public MetadataInputFile identifyFile(File parent, String s) throws InputFileException {
                if (s.isEmpty()) throw new InputFileException("No Metadata Input File Specified");
                return identifyFile(new File(parent, s));
        }

        public MetadataInputFile findMetadataFile(File parent, MetadataInputFile manifestMeta) throws InputFileException {
                MetadataInputFile returnFile = identifyFile(parent, NA);
                for(FilenameFilter ff: filters) {
                        String[] matches = parent.list(ff);
                        if (matches.length > 0) {
                                returnFile = identifyFile(parent, matches[0]);
                                if (returnFile.getInputFileType() == InputFileType.NA) {
                                        return manifestMeta;
                                }
                                return returnFile;
                        }
                }
                return returnFile;
        }
        
        public MetadataInputFile identifyFile(File f) throws InputFileException {
                if (f == null) throw new InputFileException("Null Input File"); 
                if (f.getName().toLowerCase().endsWith(".xml")) {
                        return new XMLInputFile(f);
                } else if (f.getName().toLowerCase().endsWith(".prop")) {
                        return new PropertyFile(f);
                } else if (f.getName().toLowerCase().endsWith(".csv")) {
                        return new CSVInputFile(f);
                }
                return emptyInputFile();
        }
        
        public MetadataInputFile emptyInputFile() {
                return new UnidentifiedInputFile();
        }
                
        
        class UnidentifiedInputFile extends DefaultInput {

                @Override
                public InputFileType getInputFileType() {
                        return InputFileType.NA;
                }
       }
}
