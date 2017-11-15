package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;

public class MetadataInputFileBuilder {
        public class InputFileException extends Exception {
                private static final long serialVersionUID = -8244295017254682738L;

                InputFileException(String s) {
                        super(s);
                }
        }

        public enum InputFileType {
                NA, Property, CSV, DC, METS, EAD;
        }
        public MetadataInputFile identifyFile(String s) throws InputFileException {
                if (s.isEmpty()) throw new InputFileException("No Metadata Input File Specified");
                return identifyFile(new File(s));
        }
        public MetadataInputFile identifyFile(File f) throws InputFileException {
                if (f == null) throw new InputFileException("Null Input File"); 
                if (f.getName().toLowerCase().endsWith(".xml")) {
                        return new XMLInputFile(f);
                } else if (f.getName().toLowerCase().endsWith(".csv")) {
                        
                }
                return new UnidentifiedInputFile(f);
        }
        
        
                
        abstract class DefaultInputFile implements MetadataInputFile {
                File file;
                InputFileType fileType;
                public File getFile() {
                        return file;
                }
                public abstract String getValue(String key, String def);
                public InputFileType getInputFileType() {
                        return fileType;
                }
                public void setCurrentKey(String s) {
                        //no action except for CSV
                }
                DefaultInputFile(File file) {
                        this.file = file;
                }
        }
        
        class UnidentifiedInputFile extends DefaultInputFile {
                UnidentifiedInputFile(File file) {
                        super(file);
                        fileType = InputFileType.NA;
                }

                @Override
                public String getValue(String key, String def) {
                        return def;
                }
        }
        
        class XMLInputFile extends DefaultInputFile {
                Document d;
                XPath xp = XMLUtil.xf.newXPath();
                XMLInputFile(File file) throws InputFileException {
                        super(file);
                        try {
                                d = XMLUtil.dbf_ns.newDocumentBuilder().parse(file);
                                SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
                                
                                //For mets.xml
                                nsContext.add("dim", "http://www.dspace.org/xmlns/dspace/dim");
                                nsContext.add("dim", "http://www.loc.gov/METS/");
                                
                                //For EAD files
                                nsContext.add("ead", "urn:isbn:1-931666-22-9");
                                nsContext.add("ns2", "http://www.w3.org/1999/xlink");

                                xp.setNamespaceContext(nsContext);
                                
                                String ns = d.getNamespaceURI();
                                if (ns.equals("urn:isbn:1-931666-22-9")) {
                                        fileType = InputFileType.EAD;
                                } else if (ns.equals("http://www.loc.gov/METS/")) {
                                        fileType = InputFileType.METS;
                                } else if (d.getDocumentElement().getTagName().equals("dublin_core")) {
                                        fileType = InputFileType.DC;
                                } else {                                        
                                        throw new InputFileException("Cannot identify XML file");
                                }
                        } catch (SAXException | IOException | ParserConfigurationException e) {
                                throw new InputFileException(e.getMessage());
                        }
                }
                
                @Override
                public String getValue(String key, String def) {
                        return "TBD";
                }
        }
        
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
                
                public String getValue(String col, String def) {
                        if (currentRow == null) {
                                return def;
                        }
                        if (cols.containsKey(col)) {
                                return currentRow.get(cols.get(col));
                        }
                        return def;
                }

        }
}
