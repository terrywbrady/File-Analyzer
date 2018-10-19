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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.utils.URIBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

public class OAIImporter extends DefaultImporter {
    private enum Verbs {ListMetadataFormats,ListSets,ListRecords,ListIdentifiers;}
    private static enum DemoStatsItems implements StatsItemEnum {
        Key(StatsItem.makeStringStatsItem("Key", 300)),
        Title(StatsItem.makeStringStatsItem("Title", 300)),
        Identifier(StatsItem.makeStringStatsItem("Identifier")),
        Repo(StatsItem.makeStringStatsItem("Repo").makeFilter(true)),
        ObjType(StatsItem.makeStringStatsItem("ObjType").makeFilter(true)),
        ;
        
        StatsItem si;
        DemoStatsItems(StatsItem si) {this.si=si;}
        public StatsItem si() {return si;}
    }
	public static enum Generator implements StatsGenerator {
		INSTANCE;

		public Stats create(String key) {
		    Stats s = new Stats(details, key);
		    //This is currently hard-coded for the ArchivesSpace OAI Service
	        Matcher m = Pattern.compile(".*/repositories/([^/]+)/([^/]+)/.*").matcher(key);
	        if (m.matches()) {
	            s.setVal(DemoStatsItems.Repo, m.group(1));
                s.setVal(DemoStatsItems.ObjType, m.group(2));
	        }
		    return s;
		}
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
        
        details.createFilters(types);
		return new ActionResult(selectedFile, verb.toString(), this.toString(), details, types, true, timer.getDuration());
	}
	
    public String getText(Node n) {
        return getText(n, "");
    }
	public String getText(Node n, String def) {
	    return n instanceof Element ? ((Element)n).getTextContent() : def;
	}
	
    public String getText(NodeList nl, String def) {
        return nl.getLength() > 0 ? getText(nl.item(0), def) : def;
    }

    public String getText(NodeList nl) {
        return getText(nl, "");
    }
    
    public Stats addStats(TreeMap<String,Stats> types, String val) {
        Stats stat = Generator.INSTANCE.create(val);
        types.put(val, stat);
        return stat;
    }
    
    public NodeList getNodes(Document d, Verbs verb) {
        if (verb == Verbs.ListSets) {
            return d.getElementsByTagName("setSpec");
        } else if (verb == Verbs.ListMetadataFormats) {
            return d.getElementsByTagName("metadataPrefix");
        } else if (verb == Verbs.ListRecords) {
            return d.getElementsByTagName("record");
        } else if (verb == Verbs.ListIdentifiers) {
            return d.getElementsByTagName("identifier");
        } else {
            return d.getElementsByTagName("xxxemptyxxx");            
        }
        
    }

	public String processRequest(TreeMap<String,Stats> types, String url, String resumptionToken) {
        Verbs verb = (Verbs)this.getProperty(VERB);
        String set = this.getProperty(SET).toString();
        String format = this.getProperty(FORMAT).toString();
        String DC = "http://purl.org/dc/elements/1.1/";
	    
        try {
            URIBuilder urib = new URIBuilder(url).addParameter("verb", verb.toString());
            if (!resumptionToken.isEmpty()) {
                urib.addParameter("resumptionToken", resumptionToken);
            } else {
                if (!format.isEmpty()) urib.addParameter("metadataPrefix", format);
                if (!set.isEmpty()) urib.addParameter("set", set);
            }
            URI uri = urib.build();
            System.out.println(uri);
            HttpURLConnection con= (HttpURLConnection) uri.toURL().openConnection();
            
            Document d = XMLUtil.db_ns.parse(con.getInputStream());
            NodeList nl = getNodes(d, verb);
            
            if (verb == Verbs.ListRecords) {
                for(int i=0; i< nl.getLength(); i++) {
                    Element rec = (Element)nl.item(i);
                    String t = getText(rec.getElementsByTagNameNS(DC, "title"));
                    String id = getText(rec.getElementsByTagName("identifier"));
                    String dcid = getText(rec.getElementsByTagNameNS(DC, "identifier"));
                    Stats s = this.addStats(types, id);
                    s.setVal(DemoStatsItems.Title, t);
                    s.setVal(DemoStatsItems.Identifier, dcid);
                    System.out.println(types.size() + "\t"+id+"\t"+t);
                }                
            } else {
                for(int i=0; i< nl.getLength(); i++) {
                    Stats s = addStats(types, getText(nl.item(i)));
                    if (verb == Verbs.ListIdentifiers) {
                    }
                }                
            }
            String rt = getText(d.getElementsByTagName("resumptionToken"));
            return rt.isEmpty() || rt.endsWith("==") ? null : rt;
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
