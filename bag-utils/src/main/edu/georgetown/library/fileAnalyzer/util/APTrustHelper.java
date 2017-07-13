package edu.georgetown.library.fileAnalyzer.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagFile;
import gov.loc.repository.bagit.BagInfoTxt;
import gov.loc.repository.bagit.Manifest.Algorithm;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil.SimpleNamespaceContext;

public class APTrustHelper extends TarBagHelper {
    public static final String P_INSTID = "inst-id";
    public static final String P_ITEMUID = "item-uid";
	public static final String P_SRCORG = "source-org";
    public static final String P_BAGTOTAL = "bag-total";
    public static final String P_BAGCOUNT = "bag-count";
    public static final String P_INTSENDDESC = "internal-sender-desc";
    public static final String P_INTSENDID = "internal-sender-id";
    public static final String P_TITLE = "title";
    public static final String P_ACCESS = "access";
    public static final String P_ALLOW_RENAME = "allow-rename";
    
    public enum STAT {
        VALID,
        INVALID, 
        ERROR
    }
    public enum Access {Consortia, Restricted, Institution;}

    File aptinfo;
    private Map<String,String> checksums = new HashMap<>();
    boolean allowSourceRename;
    
    public APTrustHelper(File parent, boolean allowSourceRename) {
    	super(parent);
    	this.allowSourceRename = allowSourceRename;
    }
    
    private String instId = null;
    private String itemUid = null;
    private Integer ibagCount = null;
    private Integer ibagTotal = null;
    private Access access = null;
    private String srcOrg = null;
    private String intSendDesc = null;
    private String intSendId = null;
    private String title = null;
    private String parent = null;
    
	public void setInstitutionId(String instId) {
		this.instId = instId;
	}

	public void setItemIdentifer(String itemUid) {
		this.itemUid = itemUid;
	}

	public void setBagCount(Integer ibagCount) {
		this.ibagCount = ibagCount;
	}

	public void setBagTotal(Integer ibagTotal) {
		this.ibagTotal = ibagTotal;
	}

	public void setAccessType(Access access) {
		this.access = access;
	}

	public void setSourceOrg(String srcOrg) {
		this.srcOrg = srcOrg;
	}

	public void setInstitutionalSenderDesc(String intSendDesc) {
	    //bagit seems to have an issue with trailing periods in this field
		this.intSendDesc = intSendDesc.replaceAll("\\.+\\s*$", "");
	}

	public void setInstitutionalSenderId(String intSendId) {
		this.intSendId = intSendId;
	}

	public void setTitle(String title) {
		this.title = title;
	}    
    public void setParent(String parent) {
        this.parent = parent;
    }    
   
    @Override public void validateImpl(StringBuilder sb) throws IncompleteSettingsException {
    	if (instId == null) {
    		sb.append("Institution Id cannot be null. \n");
    	} else if (instId.isEmpty()) {
    		sb.append("Institution Id cannot be empty. \n");
    	}
    	
    	if (itemUid == null) {
    		sb.append("Item Identifier cannot be null. \n");
    	} else if (itemUid.isEmpty()) {
    		sb.append("Item Identifier cannot be empty. \n");
    	}
    	
    	if (ibagCount == null) {
    		sb.append("Bag count must be set. \n");
    	}
    	if (ibagTotal == null) {
    		sb.append("Bag total must be set. \n");
    	}
    	if (access == null) {
    		sb.append("Access type must be set. \n");
    	}
    	
    	if (srcOrg == null) {
    		sb.append("Source Organization cannot be null. \n");
    	} else if (srcOrg.isEmpty()) {
    		sb.append("Source Organization cannot be empty. \n");
    	}
    	if (intSendDesc == null) {
    		sb.append("Institution Sender Description cannot be null. \n");
    	}
    	//if (intSendDesc.isEmpty()) sb.append("Institution Sender Description cannot be empty. \n");
    	if (intSendId == null) {
    		sb.append("Institution Sender Id cannot be null. \n");
    	} else if (intSendId.isEmpty()) {
    		sb.append("Institution Sender Id cannot be empty. \n");
    	}
    	
    	if (title == null) {
    		sb.append("Title cannot be null. \n");
    	} else if (title.isEmpty()) {
    		sb.append("Title cannot be empty. \n");
    	}
    }
        
    @Override public void createBagFile() throws IncompleteSettingsException {
    	validate();
		String bagCount = String.format("%03d", ibagCount);
        String bagTotal = String.format("%03d", ibagTotal);
		
		StringBuilder sb = new StringBuilder();
		sb.append(instId);
		sb.append(".");
        sb.append(itemUid);
        if ((ibagCount > 1) || (ibagTotal > 1)) {
            sb.append(".b");
            sb.append(bagCount);
            sb.append(".of");
            sb.append(bagTotal);            
        }

		data.newBag = new File(data.parent, sb.toString());
    }
    
    public Bag getBag() {
    	return data.bag;
    }
    
    @Override public void generateBagInfoFiles() throws IOException, IncompleteSettingsException {
    	validate();
    	if (data.newBag == null) throw new IncompleteSettingsException("Bag File must be created - call createBagFile()");
        aptinfo = new File(data.parent, "aptrust-info.txt");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aptinfo),"UTF-8"));
        bw.write(String.format("Title: %s%n", title));
        bw.write(String.format("Access: %s%n", access));
        bw.close();
        data.bag.addFileAsTag(aptinfo);
	    
        super.generateBagInfoFiles();

	    BagInfoTxt bit = data.bag.getBagInfoTxt();
        bit.addSourceOrganization(srcOrg);
	    bit.addInternalSenderDescription(intSendDesc);
	    bit.addInternalSenderIdentifier(intSendId);
	    bit.addBagGroupIdentifier(parent);
	    bit.setBagCount(String.format("%d of %d", ibagCount, ibagTotal));
    }
    
    @Override public void writeBagFile() throws IOException, IncompleteSettingsException {
    	if (aptinfo == null) throw new IncompleteSettingsException("Aptinfo File must be created - call generateBagInfoFiles()");
    	super.writeBagFile();
	    aptinfo.delete();
    }

    public void parseMetsFile(File zeout) throws IOException, InvalidMetadataException {
        try {
            Document doc = XMLUtil.db_ns.parse(zeout);
            String id = doc.getDocumentElement().getAttribute("OBJID");
            if (id == null) throw new InvalidMetadataException("mets.xml root element must have an OBJID field");
            if (id.isEmpty()) throw new InvalidMetadataException("mets.xml root element must not have an empty OBJID field");
            setInstitutionalSenderId(id);
            
            setItemIdentifer(id.replaceFirst("hdl:", "").replaceFirst("/", "_"));
            
            XPath xp = XMLUtil.xf.newXPath();
            SimpleNamespaceContext nsContext = new XMLUtil().new SimpleNamespaceContext();
            nsContext.add("mods", "http://www.loc.gov/mods/v3");
            nsContext.add("mets", "http://www.loc.gov/METS/");
            nsContext.add("xlink", "http://www.w3.org/1999/xlink");
            xp.setNamespaceContext(nsContext);
            
            String title = xp.evaluate("/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/mods:mods/mods:titleInfo/mods:title", doc);
            if (title == null || title.isEmpty()) {
                title = "No title found";
            }
            setTitle(title.replaceAll("\\s+", " "));                
            String parent = xp.evaluate("/mets:mets/mets:structMap[@LABEL='Parent']/mets:div[@TYPE='AIP Parent Link']/mets:mptr/@xlink:href", doc);
            if (parent == null || parent.isEmpty()) {
                parent = "N/A";
            }
            setParent(parent);
            setInstitutionalSenderDesc("See the bag manifest for the contents of the bag");
        } catch (SAXException e) {
            throw new InvalidMetadataException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new InvalidMetadataException(e.getMessage());
        }
    }

    public void parseEadFile(File zeout) throws IOException, InvalidMetadataException {
        try {
	    //namespace is opaque when coming out of AS
            Document doc = XMLUtil.db.parse(zeout);
		
            XPath xp = XMLUtil.xf.newXPath();
            String id = xp.evaluate("/ead/archdesc/did/unitid/text()", doc);
            if (id == null) throw new InvalidMetadataException("The ead must have a unitid");
            if (id.isEmpty()) throw new InvalidMetadataException("The ead must not have an empty unitid");
            setInstitutionalSenderId(id);
	    setItemIdentifer(id);
            
            String title = xp.evaluate("/ead/archdesc/did/unittitle", doc);
            if (title == null || title.isEmpty()) {
                title = "No title found";
            }
            setTitle(title.replaceAll("\\s+", " "));                
            String parent = "N/A";
            setParent(parent);
            setInstitutionalSenderDesc("See the bag manifest for the contents of the bag");
        } catch (SAXException e) {
           throw new InvalidMetadataException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new InvalidMetadataException(e.getMessage());
        }
    }

    public void parseHierarchyFile(File zeout) throws IOException, InvalidMetadataException {
        try {
            //namespace is opaque when coming out of AS
            Document doc = XMLUtil.db.parse(zeout);
            NodeList nl = doc.getDocumentElement().getElementsByTagName("handle");
            if (nl.getLength() == 0) {
                    throw new InvalidMetadataException("The hierarchy file must have a root handle");
            }
            String id = nl.item(0).getTextContent();
            if (id == null) throw new InvalidMetadataException("The hierarchy handle must exist");
            if (id.isEmpty()) throw new InvalidMetadataException("The hierarchy handle cannot be empty");
                    
            setInstitutionalSenderId(id);
            setItemIdentifer(id);
            setTitle("DSpace hierarchy for repository "+id);            
            setInstitutionalSenderDesc("This is a generated export of the DSpace hierarchy");
        } catch (SAXException e) {
            throw new InvalidMetadataException(e.getMessage());
        }
    }

    public void saveChecksums() {
        for(BagFile bf: data.bag.getPayload()){
            checksums.put(bf.getFilepath(), data.bag.getChecksums(bf.getFilepath()).get(Algorithm.MD5));
        }
    }
    
    public Map<String, String> compareChecksums(File checksumFile) throws FileNotFoundException, IOException {
        Map<String, String> errors = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new FileReader(checksumFile))){
            for(String line = br.readLine(); line != null; line = br.readLine()) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String csum = parts[0];
                String name = parts[1];
                
                String bagCsum = checksums.get(name);
                if (bagCsum == null) {
                    String s = String.format("No checksum found for path: %s", name);
                    for(String tkey: checksums.keySet()) {
                        if (checksums.get(tkey).equals(csum)) {
                            s = String.format("No checksum found for path: %s; A match found in the bag with path: %s", name, tkey);
                        }
                    }
                    errors.put(name, s);
                } else if (!bagCsum.equals(csum)) {
                    errors.put(name, String.format("Checksum Mismatch for %s [Compare File:%s; Bag:%s]", name, csum, bagCsum));                    
                }
            }
        }
        
        return errors;
    }

}
