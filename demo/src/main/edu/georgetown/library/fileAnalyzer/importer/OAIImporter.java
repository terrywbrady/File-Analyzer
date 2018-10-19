package edu.georgetown.library.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.TreeMap;

import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

public class OAIImporter extends DefaultImporter {
    private enum Verbs {ListMetadataFormats,ListSets,ListRecords;}
    private static enum DemoStatsItems implements StatsItemEnum {
        Key(StatsItem.makeStringStatsItem("Key", 300)),
        Value(StatsItem.makeStringStatsItem("Value", 400)),
        ;
        
        StatsItem si;
        DemoStatsItems(StatsItem si) {this.si=si;}
        public StatsItem si() {return si;}
    }
	public static enum Generator implements StatsGenerator {
		INSTANCE;

		public Stats create(String key) {return new Stats(details, key);}
	}
	
	public static StatsItemConfig details = StatsItemConfig.create(DemoStatsItems.class);

    public static final String OAI_URL = "oai-url";
    public static final String VERB = "oai-verb";
    public static final String SET = "oai-set";
    public static final String FORMAT = "oai-format";
	public OAIImporter(FTDriver dt) {
		super(dt);
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  OAI_URL, OAI_URL,
                "OAI URL", ""));
        ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),  VERB, VERB, "OAI Verb", Verbs.values(), Verbs.ListSets));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  SET, SET,
                "OAI Set", ""));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  FORMAT, FORMAT,
                "OAI Format", "oai_dc"));
	}

	public ActionResult importFile(File selectedFile) throws IOException {
        Timer timer = new Timer();
	    TreeMap<String,Stats> types = new TreeMap<String,Stats>();
	    
	    String url = this.getProperty(OAI_URL).toString();
        Verbs verb = (Verbs)this.getProperty(VERB);
        
        if (!url.isEmpty()) {
            for(String resumptionToken = ""; resumptionToken != null; resumptionToken = processRequest(types, url, resumptionToken));
        }
        
		return new ActionResult(selectedFile, verb.toString(), this.toString(), details, types, true, timer.getDuration());
	}
	
	public String processRequest(TreeMap<String,Stats> types, String url, String resumptionToken) {
        Verbs verb = (Verbs)this.getProperty(VERB);
        String set = this.getProperty(SET).toString();
        String format = this.getProperty(FORMAT).toString();
	    
        try {
            URIBuilder urib = new URIBuilder(url).addParameter("verb", verb.toString());
            if (verb == Verbs.ListRecords) {
                if (!resumptionToken.isEmpty()) {
                    urib.addParameter("resumptionToken", resumptionToken);
                } else {
                    if (!set.isEmpty()) urib.addParameter("set", set);
                    if (!format.isEmpty()) urib.addParameter("metadataPrefix", format);                    
                }
            }
            URI uri = urib.build();
            System.out.println(uri);
            HttpURLConnection con= (HttpURLConnection) uri.toURL().openConnection();
            
            Document d = XMLUtil.db_ns.parse(con.getInputStream());
            if (verb == Verbs.ListSets) {
                NodeList nl = d.getElementsByTagName("setSpec");
                for(int i=0; i< nl.getLength(); i++) {
                    String val = ((Element)nl.item(i)).getTextContent();
                    types.put(val, Generator.INSTANCE.create(val));
                }
            } else if (verb == Verbs.ListMetadataFormats) {
                NodeList nl = d.getElementsByTagName("metadataPrefix");
                for(int i=0; i< nl.getLength(); i++) {
                    String val = ((Element)nl.item(i)).getTextContent();
                    types.put(val, Generator.INSTANCE.create(val));
                }
                
            } else {
                NodeList nl = d.getElementsByTagName("record");
                for(int i=0; i< nl.getLength(); i++) {
                    Element rec = (Element)nl.item(i);
                    NodeList nt = rec.getElementsByTagNameNS("http://purl.org/dc/elements/1.1/", "title");
                    NodeList nid = rec.getElementsByTagName("identifier");
                    String t = nt.getLength() > 0 ? ((Element)nt.item(0)).getTextContent() : "";
                    String id = nid.getLength() > 0 ? ((Element)nid.item(0)).getTextContent() : "";
                    Stats s = Generator.INSTANCE.create(id);
                    s.setVal(DemoStatsItems.Value, t);
                    types.put(id, s);
                    System.out.println(types.size() + "\t"+id+"\t"+t);
                }
                nl = d.getElementsByTagName("resumptionToken");
                if (nl.getLength() > 0) {
                    String rt = ((Element)nl.item(0)).getTextContent();
                    System.out.println(rt);
                    return rt.isEmpty() ? null : rt;
                }
            }
        } catch (URISyntaxException|IOException | SAXException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	
	public String toString() {
		return "OAI Importer";
	}
	public String getDescription() {
		return "Interactive call to OAI Service.  Input file does not matter.";
	}
	public String getShortName() {
		return "OAI";
	}
}
