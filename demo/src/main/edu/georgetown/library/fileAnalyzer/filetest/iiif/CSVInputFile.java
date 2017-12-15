package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;

public class CSVInputFile extends DefaultInputFile {
        HashMap<String,Integer> cols = new HashMap<>(); 
        ArrayList<String> keys = new ArrayList<>();
        HashMap<String,Vector<String>> values = new HashMap<>();
        Vector<String> currentRow = null;
        CSVInputFile(File file) throws InputFileException {
                super(file);
                fileType = InputFileType.CSV;
                try {
                        Vector<Vector<String>> data = DelimitedFileReader.parseFile(file, ",");
                        Vector<String> header = new Vector<>();
                        if (data.size() > 0) {
                                header = data.get(0);
                                for(int i=1; i<header.size(); i++) {
                                        cols.put(header.get(i), i);
                                }
                        }
                        for(int r=1; r<data.size(); r++) {
                                Vector<String> row = data.get(r);
                                if (header.size() == row.size()) {
                                        keys.add(row.get(0));
                                        values.put(row.get(0), row);
                                }
                                
                        }
                } catch (IOException e) {
                        throw new InputFileException("CSV Parsing Error "+e.getMessage());
                }
        }
        
        @Override
        public void setCurrentKey(String key) {
                currentRow = values.containsKey(key) ? values.get(key) : null;
        }
        
        @Override
        public String getValue(IIIFLookup key, String def) {
                if (currentRow == null) {
                        return def;
                }
                String col = key.getFileTypeKey(fileType);
                if (col == null) {
                        return def;
                }
                if (cols.containsKey(col)) {
                        return currentRow.get(cols.get(col));
                }
                return def;
        }

}
