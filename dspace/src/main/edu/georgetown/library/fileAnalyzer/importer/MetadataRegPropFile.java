package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.ftprop.FTPropFile;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;

public class MetadataRegPropFile extends FTPropFile {

    public static final String P_METAREG = "metadata-reg";
    HashMap<String,Field> fields = new HashMap<String,Field>();
    public MetadataRegPropFile(FTDriver ft) {
        super(ft, "MetadataRegPropFile", P_METAREG, P_METAREG, "Metadata Registry Export File (JSON format), Optional", "");
    }

    @Override public InitializationStatus initValidation(File refFile) {
        InitializationStatus iStat = new InitializationStatus();
        if (getFile() != null) {
            try(FileReader fr = new FileReader(getFile())){
                Schema[] data = new Gson().fromJson(fr, Schema[].class);
                fields = Schema.getFields(data);
                if (fields.size() == 0) {
                    iStat.addFailMessage("No fields defined in the Metadata Registry");
                } else {
                    //iStat.addMessage(data.length + " schemas in registry. " + fields.size() + " fields in the Metadata Registry.");                    
                }
            } catch (IOException|RuntimeException e) {
                iStat.addFailMessage("Cannot Parse the Metadata Registry File");
                iStat.addFailMessage(e.getMessage());
            }            
        }
        return iStat;
    }
    
    public boolean isFieldInRegistry(String name) {
        if (fields.isEmpty()) return true;
        return fields.containsKey(name);
    }

}
