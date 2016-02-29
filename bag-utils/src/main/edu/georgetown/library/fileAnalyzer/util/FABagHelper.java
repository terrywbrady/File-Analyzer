package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFactory;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.transformer.impl.DefaultCompleter;
import gov.loc.repository.bagit.writer.Writer;
import gov.loc.repository.bagit.writer.impl.FileSystemWriter;

public class FABagHelper {
	public enum STAT {
		VALID,
		INVALID, 
		ERROR
	}
	
    public static final String P_INSTID = "inst-id";
    public static final String P_ITEMUID = "item-uid";
	public static final String P_SRCORG = "source-org";
    public static final String P_BAGTOTAL = "bag-total";
    public static final String P_BAGCOUNT = "bag-count";
    public static final String P_BAGCOUNTSTR = "bag-count-str";
    public static final String P_INTSENDDESC = "internal-sender-desc";
    public static final String P_INTSENDID = "internal-sender-id";
    public static final String P_TITLE = "title";
    public static final String P_ACCESS = "access";

    FABagHelperData data = new FABagHelperData();
    public static Pattern pBagCountStr = Pattern.compile("^(\\d+) of (\\d+|\\?)$");
    
    public FABagHelper(File source) {
    	this.data.source = source;
    	this.data.parent = source.getParentFile();
    	data.bf = new BagFactory();
    	data.bag = data.bf.createBag();
    }
    
    public void validate() throws IncompleteSettingsException {
    	StringBuilder sb = new StringBuilder();
    	validateImpl(sb);
    	if (sb.length() > 0) {
    		throw new IncompleteSettingsException(sb.toString());
    	}
    }
    public void validateImpl(StringBuilder sb) throws IncompleteSettingsException {
    }
        
    public void createBagFile() throws IncompleteSettingsException {
    	validate();
		data.newBag = new File(data.parent, data.source.getName() + "_bag");
    }
    
    public Bag getBag() {
    	return data.bag;
    }
    
    public void generateBagInfoFiles() throws IOException, IncompleteSettingsException {
    	validate();
    	if (data.newBag == null) throw new IncompleteSettingsException("Bag File must be created - call createBagFile()");
	    
	    DefaultCompleter comp = new DefaultCompleter(data.bf);
		
		comp.setGenerateBagInfoTxt(true);
		comp.setUpdateBaggingDate(true);
		comp.setUpdateBagSize(true);
		comp.setUpdatePayloadOxum(true);
		comp.setGenerateTagManifest(false);
		
		data.bag = comp.complete(data.bag);
    }
    
    public void writeBagFile() throws IOException, IncompleteSettingsException {
    	validate();
    	if (data.newBag == null) throw new IncompleteSettingsException("Bag File must be created - call createBagFile()");
	    Writer writer = new FileSystemWriter(data.bf); 
	    data.bag.write(writer, data.newBag);
	    data.bag.close();
    }
    
    public String getFinalBagName() throws IncompleteSettingsException {
    	validate();
    	if (data.newBag == null) throw new IncompleteSettingsException("Bag File must be created - call createBagFile() and writeBagFile()");
    	return data.newBag.getName();
    }

    public void setBagCountStr(String countStr) throws IncompleteSettingsException {
        BagInfoTxt bit = data.bag.getBagInfoTxt();
        if (bit == null) {
            throw new IncompleteSettingsException("Bag info file must be generated before the count can be set");
        }
        bit.setBagCount(countStr);
    }
    
    public static void validateBagCount(String bagNum, String bagTot) throws IncompleteSettingsException {
        int ibagNum = 0;
        try {
            ibagNum = Integer.parseInt(bagNum);            
        } catch(NumberFormatException e) {
            throw new IncompleteSettingsException(String.format("Bag Number (%s) must be numeric", bagNum));
        }
        if (ibagNum < 1) {
            throw new IncompleteSettingsException(String.format("Bag Number (%d) must be 1 or larger", ibagNum));
        }
        if (!bagTot.equals("?")) {
            try {
                int ibagTot = Integer.parseInt(bagTot);
                if (ibagTot < 1) {
                    throw new IncompleteSettingsException(String.format("Bag Total (%d) must be 1 or larger", ibagTot));
                }
                if (ibagNum > ibagTot) {                        
                    throw new IncompleteSettingsException(String.format("Bag Num (%d) cannot be larger than Bag Total(%d)", ibagNum, ibagTot));            
                }                
            } catch(NumberFormatException e) {
                throw new IncompleteSettingsException(String.format("Bag Total (%s) must be '?' or numeric", bagTot));
            }
        }

    }
}
