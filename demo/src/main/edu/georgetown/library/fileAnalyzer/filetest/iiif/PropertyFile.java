package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public class PropertyFile extends DefaultInputFile {
        Properties prop = new Properties();
        PropertyFile(File file) throws InputFileException {
                super(file);
                fileType = InputFileType.Property;
                try {
                        prop.load(new FileReader(file));
                } catch (Exception e) {
                        throw new InputFileException("Property Parsing Error "+e.getMessage());
                }
        }
        
        @Override
        public String getValue(IIIFLookup key, String def) {
                String propkey = key.getFileTypeKey(fileType);
                return prop.getProperty(propkey, def);
        }

}
