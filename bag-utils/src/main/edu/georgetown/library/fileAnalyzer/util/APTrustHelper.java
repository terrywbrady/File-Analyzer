package edu.georgetown.library.fileAnalyzer.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import gov.loc.repository.bagit.Bag;
import gov.loc.repository.bagit.BagInfoTxt;
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
    
    public enum STAT {
        VALID,
        INVALID, 
        ERROR
    }
    public enum Access {Consortia, Restricted, Institution;}

    File aptinfo;
    
    public APTrustHelper(File parent) {
    	super(parent);
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
		this.intSendDesc = intSendDesc;
	}

	public void setInstitutionalSenderId(String intSendId) {
		this.intSendId = intSendId;
	}

	public void setTitle(String title) {
		this.title = title;
	}    
   
    @Override public void validateImpl(StringBuilder sb) throws IncompleteSettingsException {
    	if (instId == null) sb.append("Institution Id cannot be null. \n");
    	if (instId.isEmpty()) sb.append("Institution Id cannot be empty. \n");
    	if (itemUid == null) sb.append("Item Identifier cannot be null. \n");
    	if (itemUid.isEmpty()) sb.append("Item Identifier cannot be empty. \n");
    	if (ibagCount == null) sb.append("Bag count must be set. \n");
    	if (ibagTotal == null) sb.append("Bag total must be set. \n");
    	if (access == null) sb.append("Access type must be set. \n");
    	if (srcOrg == null) sb.append("Source Organization cannot be null. \n");
    	if (srcOrg.isEmpty()) sb.append("Source Organization cannot be empty. \n");
    	if (intSendDesc == null) sb.append("Institution Sender Description cannot be null. \n");
    	//if (intSendDesc.isEmpty()) sb.append("Institution Sender Description cannot be empty. \n");
    	if (intSendId == null) sb.append("Institution Sender Id cannot be null. \n");
    	if (intSendId.isEmpty()) sb.append("Institution Sender Id cannot be empty. \n");
    	if (title == null) sb.append("Title cannot be null. \n");
    	if (title.isEmpty()) sb.append("Title cannot be empty. \n");
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(aptinfo));
        bw.write(String.format("Title: %s%n", title));
        bw.write(String.format("Access: %s%n", access));
        bw.close();
        data.bag.addFileAsTag(aptinfo);
	    
        super.generateBagInfoFiles();

	    BagInfoTxt bit = data.bag.getBagInfoTxt();
        bit.addSourceOrganization(srcOrg);
	    bit.addInternalSenderDescription(intSendDesc);
	    bit.addInternalSenderIdentifier(intSendId);
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
            xp.setNamespaceContext(nsContext);
            
            String title = xp.evaluate("//mods:title", doc);
            setTitle(title);
            String intSendDesc = xp.evaluate("//mods:abstract", doc);
            setInstitutionalSenderDesc(intSendDesc);
        } catch (SAXException e) {
            throw new InvalidMetadataException(e.getMessage());
        } catch (XPathExpressionException e) {
            throw new InvalidMetadataException(e.getMessage());
        }
    }
}
