package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.proquestXsl.GUProquestURIResolver;
import edu.georgetown.library.fileAnalyzer.proquestXsl.MarcUtil;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;

/**
 * Importer for tab delimited files
 * 
 * @author TBrady
 * 
 */
public class DSpaceMetadata2Marc extends DefaultImporter {

	public static Pattern phead = Pattern
			.compile("^dc\\.([a-z]+)(\\.([a-z]+))?(\\[[a-zA-Z_]*\\])?$");
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMinimumIntegerDigits(4);
		nf.setGroupingUsed(false);
	}
	
	public static enum STAT {
		CONVERTED,
		SKIPPED,
		COMP_DATE_FORMAT
	}

	public static enum DSpace2MarcStatsItems implements StatsItemEnum {
		Handle(StatsItem.makeStringStatsItem("Handle", 100)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Status").setWidth(150)),
		Url(StatsItem.makeStringStatsItem("URL", 250)), 
		AccessionDate(StatsItem.makeStringStatsItem("Accession", 100)),
		CompDate(StatsItem.makeStringStatsItem("Comp Date", 100)),
		Author(StatsItem.makeStringStatsItem("Author", 250)), 
		Title(StatsItem.makeStringStatsItem("Title", 250)), 
		EmbargoTerms(StatsItem.makeStringStatsItem("Embargo Terms", 100)), 
		EmbargoLift(StatsItem.makeStringStatsItem("Embargo Lift", 100)), ;

		StatsItem si;

		DSpace2MarcStatsItems(StatsItem si) {
			this.si = si;
		}

		public StatsItem si() {
			return si;
		}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {
			return new Stats(details, key);
		}
	}

	public static StatsItemConfig details = StatsItemConfig
			.create(DSpace2MarcStatsItems.class);
	public static String P_DC = "Generate DC";
	public static String P_START = "Accession Start";

	public DSpaceMetadata2Marc(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),
				P_DC, P_DC,
				"Generate a DC record for QC/troubleshooting purposes", YN
						.values(), YN.N));
		this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),
				P_START, P_START,
				"Accession Start Date in YYYY-MM-DD format", "2013-03-01"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_UNIV_NAME, MarcUtil.P_UNIV_NAME,
				"University Name", "My University"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_UNIV_LOC, MarcUtil.P_UNIV_LOC,
				"University Location", "My University Location"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_SCHEMA, MarcUtil.P_EMBARGO_SCHEMA,
				"Embargo Schema Prefix", "local"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_ELEMENT, MarcUtil.P_EMBARGO_ELEMENT,
				"Embargo Element", "embargo"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_TERMS, MarcUtil.P_EMBARGO_TERMS,
				"Embargo Policy Qualifier", "terms"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_CUSTOM, MarcUtil.P_EMBARGO_CUSTOM,
				"Embargo Custom Date Qualifier", "custom-date"));
		ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  MarcUtil.P_EMBARGO_LIFT, MarcUtil.P_EMBARGO_LIFT,
				"Embargo Lift Date Qualifier", "lift-date"));
	}

	public String toString() {
		return "Convert DSpace Metadata to MARC";
	}

	public String getDescription() {
		return "This rule will take exported metadata from DSpace and generate MARC records.  This rule is intended for use on items that were ingested from Proquest.";
	}

	public String getShortName() {
		return "Dspace2Marc";
	}

	public void makeElement(Element parent, String header, String content) {
		Document d = parent.getOwnerDocument();
		Matcher m = phead.matcher(header);
		if (!m.matches()) {
			return;
		}
		String[] contarr = content.split("\\|\\|");
		for (String contline : contarr) {
			Element elem = d.createElement("dcvalue");
			parent.appendChild(elem);
			elem.setAttribute("element", m.group(1));
			String q = m.group(3);
			elem.setAttribute("qualifier", q == null ? "none" : q);
			elem.appendChild(d.createTextNode(contline));
		}
	}

	private static Pattern p1 = Pattern.compile("^.*hdl.handle.net/(.*)$");
	private static Pattern p2 = Pattern.compile("^.*handle/(.*)$");
	private static Pattern pComp  = Pattern.compile("^\\d\\d\\d\\d$");
	public String getKey(String url, int seq) {
		Matcher m = p1.matcher(url);
		if (m.matches()) return m.group(1);
		m = p2.matcher(url);
		if (m.matches()) return m.group(1);
		return "" + seq;
	}
	
	public String get(Vector<String> cols, Vector<Integer> nums) {
		for(int n: nums) {
			String s = cols.get(n);
			if (!s.isEmpty()) return s;
		}
		return "";
	}
	
	public ActionResult importFile(File selectedFile) throws IOException {
		String accStart = (String)this.getProperty(P_START);
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		DelimitedFileReader dfr = new DelimitedFileReader(selectedFile, ",");
		HashMap<Integer, DSpace2MarcStatsItems> colMap = new HashMap<Integer, DSpace2MarcStatsItems>();

		Document collDoc = XMLUtil.db.newDocument();		
		Element collDocRoot = collDoc.createElementNS("http://www.loc.gov/MARC21/slim","marc:collection");
		collDocRoot.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", "http://www.loc.gov/MARC21/slim http://www.loc.gov/standards/marcxml/schema/MARC21slim.xsd");
		collDoc.appendChild(collDocRoot);

		Vector<String> headers = dfr.getRow();
		Vector<Integer>urlCol = new Vector<Integer>();
		Vector<Integer>accCol = new Vector<Integer>();
		Vector<Integer>compCol = new Vector<Integer>();
		
		String embargo_element = this.getProperty(MarcUtil.P_EMBARGO_SCHEMA) + "." + this.getProperty(MarcUtil.P_EMBARGO_ELEMENT) + ".";
		
		for (int i = 0; i < headers.size(); i++) {
			String head = headers.get(i);
			if (head.startsWith("dc.creator"))
				colMap.put(i, DSpace2MarcStatsItems.Author);
			else if (head.startsWith("dc.title"))
				colMap.put(i, DSpace2MarcStatsItems.Title);
			else if (head.startsWith(embargo_element + this.getProperty(MarcUtil.P_EMBARGO_TERMS)))
				colMap.put(i, DSpace2MarcStatsItems.EmbargoTerms);
			else if (head.startsWith(embargo_element + this.getProperty(MarcUtil.P_EMBARGO_LIFT)))
				colMap.put(i, DSpace2MarcStatsItems.EmbargoLift);
			else if (head.startsWith("dc.identifier.uri")) {
				colMap.put(i, DSpace2MarcStatsItems.Url);
				urlCol.add(i);
			} else if (head.startsWith("dc.date.accession")) {
				accCol.add(i);
			} else if (head.startsWith("dc.date.created")) {
				compCol.add(i);
			}
		}

		for (Vector<String> cols = dfr.getRow(); cols != null; cols = dfr
				.getRow()) {
			String url = get(cols, urlCol);
			String accDate = get(cols, accCol);
			String compDate = get(cols, compCol);
			String key = getKey(url, types.size());
			Stats stats = Generator.INSTANCE.create(key);
			types.put(key, stats);
			stats.setVal(DSpace2MarcStatsItems.Url, url);
			stats.setVal(DSpace2MarcStatsItems.AccessionDate, accDate);
			stats.setVal(DSpace2MarcStatsItems.CompDate, compDate);
			boolean bCompDate = pComp.matcher(compDate).matches();
			
			stats.setVal(DSpace2MarcStatsItems.Stat, bCompDate ? STAT.CONVERTED :  STAT.COMP_DATE_FORMAT);
			if (accDate.compareTo(accStart) < 0) {
				stats.setVal(DSpace2MarcStatsItems.Stat, STAT.SKIPPED);
				continue;
			}

			Document d = XMLUtil.db.newDocument();
			Element root = d.createElement("dublin_core");
			root.setAttribute("schema", "dc");
			d.appendChild(root);

			for (int i = 0; i < cols.size(); i++) {
				//Commenting out, unsure why this was suppressed
				//if (urlCol.contains(i) || accCol.contains(i)) continue;
				String col = cols.get(i);
				if (col == null) continue;
				if (col.trim().isEmpty()) continue;
				makeElement(root, headers.get(i), col);
				DSpace2MarcStatsItems dmsi = colMap.get(i);
				if (dmsi != null) {
					String s = stats.getVal(dmsi).toString();
					s = (s == null) ? col : s + col; 
					stats.setVal(dmsi, col);
				}
				
			}

			File f = new File(selectedFile.getParentFile(), "marc_"
					+ key.replace("/", "_") + ".xml");
			try {
				XMLUtil.doTransform(d, f, GUProquestURIResolver.INSTANCE,
						"dc2marc.xsl", MarcUtil.getXslParm(this.ftprops));
				Document md = XMLUtil.db.parse(f);
				collDocRoot.appendChild(collDoc.importNode(md.getDocumentElement(), true));
			} catch (TransformerException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
			if (this.getProperty(P_DC).equals(YN.Y)) {
				File f1 = new File(selectedFile.getParentFile(), "dc_"
						+ key.replace("/", "_") + ".xml");
				XMLUtil.serialize(d, f1);
			}
		}

		File collFile = new File(selectedFile.getParentFile(), "collection.xml");
		XMLUtil.serialize(collDoc, collFile);
		return new ActionResult(selectedFile, selectedFile.getName(),
				this.toString(), details, types, true, timer.getDuration());
	}
}
