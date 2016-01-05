package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.XmlFilter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import gov.nara.nwts.ftapp.util.XMLUtil;

public class ProquestQC extends DefaultFileTest {
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMinimumIntegerDigits(4);
		nf.setGroupingUsed(false);
	}

	public enum OVERALL_STAT {
		INIT,
		PASS,
		FAIL,
		REVIEW
	}

	public enum TYPE {
		ProQuest,
		DublinCore,
		MarcXMLCollection,
		MarcXMLRecord,
		Other
	}

	public static enum ProquestQCStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Path").setWidth(300)),
		File(StatsItem.makeStringStatsItem("File").setWidth(200)),
		OverallStat(StatsItem.makeEnumStatsItem(OVERALL_STAT.class, "Status", OVERALL_STAT.INIT).setWidth(40)),
		Type(StatsItem.makeEnumStatsItem(TYPE.class, "Status", TYPE.Other).setWidth(120)),
		Creator(StatsItem.makeStringStatsItem("Creator",200)),
		Suffix(StatsItem.makeStringStatsItem("Suffix",50)),
		Title(StatsItem.makeStringStatsItem("Title",350)),
		TitleAlt(StatsItem.makeStringStatsItem("Alt Title",150)),
		URL(StatsItem.makeStringStatsItem("URL",250)),
		Created(StatsItem.makeStringStatsItem("Created")),
		Pages(StatsItem.makeStringStatsItem("Pages")),
		Degree(StatsItem.makeStringStatsItem("Degree")),
		College(StatsItem.makeStringStatsItem("College")),
		Dept(StatsItem.makeStringStatsItem("Dept")),
		Advisor(StatsItem.makeStringStatsItem("Advisor")),
		CatCode(StatsItem.makeStringStatsItem("CatCode")),
		CatStr(StatsItem.makeStringStatsItem("CatString")),
		CatDesc(StatsItem.makeStringStatsItem("CatDesc")),
		Keyword(StatsItem.makeStringStatsItem("Keyword")),
		Abstract(StatsItem.makeStringStatsItem("Abstract")),
		;
		
		StatsItem si;
		ProquestQCStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}
	
	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {
			return new Stats(details, key);
		}
	}
	static StatsItemConfig details = StatsItemConfig.create(ProquestQCStatsItems.class);
	
	public enum XPATH {
		pq_title("//DISS_title"),
		dc_title("//dcvalue[@element='title']"),
		m_title(".//marc:datafield[@tag='245']/marc:subfield[@code='a']"),
		
		dc_url("//dcvalue[@element='identifier'][@qualifier='uri']"),
		m_url(".//marc:datafield[@tag='856']/marc:subfield[@code='u']"),

		pq_atitle("//DISS_supp_title"),
		dc_atitle("//dcvalue[@element='title'][@qualifier='alternative']"),
		m_atitle(".//marc:datafield[@tag='246']/marc:subfield[@code='a']"),
		
		pq_creator("concat(//DISS_author/DISS_name/DISS_surname/text(),', ',//DISS_author/DISS_name/DISS_fname/text(),' ',//DISS_author/DISS_name/DISS_middle/text(),' ',//DISS_author/DISS_name/DISS_suffix/text())", XPathConstants.STRING),
		dc_creator("//dcvalue[@element='creator']"),
		m_creator(".//marc:datafield[@tag='100']/marc:subfield[@code='a']"),

		pq_suffix("//DISS_author/DISS_name/DISS_suffix"),
		m_suffix(".//marc:datafield[@tag='100']/marc:subfield[@code='c']"),

		pq_created("//DISS_comp_date"),
		dc_created("//dcvalue[@element='date'][@qualifier='created']"),
		m_created(".//marc:datafield[@tag='264']/marc:subfield[@code='c']"),

		pq_pages("//DISS_description/@page_count"),
		dc_pages("//dcvalue[@element='format'][@qualifier='extent']"),
		m_pages(".//marc:datafield[@tag='300']/marc:subfield[@code='a']"),

		pq_degree("//DISS_degree"),
		dc_degree("//dcvalue[@element='description'][@qualifier='none']"),
		m_degree(".//marc:datafield[@tag='502']/marc:subfield[@code='b']"),

		pq_college("//DISS_inst_name"),
		dc_college("//dcvalue[@element='source'][@qualifier='none'][1]"),
		m_college("substring-before(.//marc:datafield[@tag='502']/marc:subfield[@code='c'], ', ')", XPathConstants.STRING),

		pq_dept("//DISS_inst_contact"),
		dc_dept("//dcvalue[@element='source'][@qualifier='none'][2]"),
		m_dept("substring-after(.//marc:datafield[@tag='502']/marc:subfield[@code='c'],', ')", XPathConstants.STRING),

		pq_advisor("//DISS_advisor/DISS_name"),
		dc_advisor("//dcvalue[@element='contributor'][@qualifier='advisor']"),
		m_advisor(".//marc:datafield[@tag='700']/marc:subfield[@code='a']"),

		pq_catcode("//DISS_cat_code"),

		dc_catstr("//dcvalue[@element='subject'][@qualifier='lcsh']"),
		m_catstr(".//marc:datafield[@tag='650'][@ind2='0']/marc:subfield"),

		pq_catdesc("//DISS_cat_desc"),
		dc_catdesc("//dcvalue[@element='subject'][@qualifier='other']"),
		m_catdesc(".//marc:datafield[@tag='650'][@ind2='4']/marc:subfield"),
		
		pq_catkey("//DISS_keyword"),
		dc_catkey("//dcvalue[@element='subject'][@qualifier='none']"),
		m_catkey(".//marc:datafield[@tag='653']/marc:subfield[@code='a']"),
		
		pq_abs("substring(//DISS_abstract/DISS_para,1,50)", XPathConstants.STRING),
		dc_abs("substring(//dcvalue[@element='description'][@qualifier='abstract'],1,50)", XPathConstants.STRING),
		m_abs("substring(.//marc:datafield[@tag='520']/marc:subfield[@code='a'],1,50)", XPathConstants.STRING),
		
		;

		XPathExpression ex;
		QName rt;
		XPATH(String s) {
			this(s, XPathConstants.NODESET);
		}
		XPATH(String s, QName rt) {
			try {
				this.rt = rt;
				XPath xpath = XMLUtil.xf.newXPath();
				
				xpath.setNamespaceContext(new NamespaceContext(){
					public String getNamespaceURI(String prefix) {
						if (prefix.equals("marc")) return "http://www.loc.gov/MARC21/slim";
						return null;
					}

					public String getPrefix(String namespaceURI) {
						if (namespaceURI.equals("http://www.loc.gov/MARC21/slim")) return "marc";
						return null;
					}

					@SuppressWarnings("rawtypes")
					public Iterator getPrefixes(String namespaceURI) {
						Vector<String> v = new Vector<String>();
						String s = getPrefix(namespaceURI);
						if (s != null) {
							v.add(s);
						}
						return v.iterator();
					}
				});
				ex = xpath.compile(s);
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
		}
		
		public Object getObject(Node d) {
			StringBuffer buf = new StringBuffer();
			try {
				Object obj = ex.evaluate(d, rt);
				if (rt == XPathConstants.STRING) {
					buf.append(obj.toString());
				} else {
					NodeList ns = (NodeList)obj;
					for(int i=0; i<ns.getLength(); i++) {
						if (i > 0) buf.append("; ");
						buf.append(ns.item(i).getTextContent().trim().replaceAll("\\s\\s+", " "));
					}					
				}
					
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			return buf.toString();
		}
		
		public Object setVal(ProquestQCStatsItems key, Stats stats, Node d) {
			if (d == null) return null;
			Object obj = getObject(d);
			if (obj == null) return null;
			obj = prepVal(obj);
			if (obj == null) return null;
			stats.setVal(key, obj);
			return obj;			
		}
		
		public Object prepVal(Object obj) {
			return obj;
		}
		public Object prepIntVal(Object obj) {
			return ((Double)obj).intValue();
		}
		public Object prepDistinctVal(Object obj) {
			NodeList nl = (NodeList) obj;
			TreeSet<String> ts = new TreeSet<String>();
			for(int i=0; i<nl.getLength();i++) {
				Text t = (Text)nl.item(i);
				ts.add(t.getTextContent().trim().intern());
			}
			return ts.size();
		}
		
	}
	
	public ProquestQC(FTDriver dt) {
		super(dt);
	}
	
	
	public String getDescription() {
		return "QC Files created by the Proquest Ingest Process";
	}
	public String getKey(File f) {
		return f.getAbsolutePath().substring(getRoot().getAbsolutePath().length());
	}
	
	public void doMarcRec(Stats stats, Node d){
		stats.setVal(ProquestQCStatsItems.Type, TYPE.MarcXMLRecord);
		XPATH.m_title.setVal(ProquestQCStatsItems.Title, stats, d);
		XPATH.m_atitle.setVal(ProquestQCStatsItems.TitleAlt, stats, d);
		XPATH.m_url.setVal(ProquestQCStatsItems.URL, stats, d);
		XPATH.m_creator.setVal(ProquestQCStatsItems.Creator, stats, d);
		XPATH.m_suffix.setVal(ProquestQCStatsItems.Suffix, stats, d);
		XPATH.m_created.setVal(ProquestQCStatsItems.Created, stats, d);
		XPATH.m_pages.setVal(ProquestQCStatsItems.Pages, stats, d);
		XPATH.m_degree.setVal(ProquestQCStatsItems.Degree, stats, d);
		XPATH.m_college.setVal(ProquestQCStatsItems.College, stats, d); 		
		XPATH.m_dept.setVal(ProquestQCStatsItems.Dept, stats, d); 		
		XPATH.m_advisor.setVal(ProquestQCStatsItems.Advisor, stats, d); 		
		XPATH.m_catstr.setVal(ProquestQCStatsItems.CatStr, stats, d); 		
		XPATH.m_catdesc.setVal(ProquestQCStatsItems.CatDesc, stats, d); 		
		XPATH.m_catkey.setVal(ProquestQCStatsItems.Keyword, stats, d); 		
		XPATH.m_abs.setVal(ProquestQCStatsItems.Abstract, stats, d); 		
	}
	
	
	public Object fileTest(File f) {
		String key = getKey(f);
		Stats stats = (Stats)this.getStats(key);
		stats.setVal(ProquestQCStatsItems.File, f.getName());
		
		try {
			Document d = XMLUtil.db_ns.parse(f);
			Element root = d.getDocumentElement();
			if (root.getTagName().equals("DISS_submission")) {
				stats.setVal(ProquestQCStatsItems.Type, TYPE.ProQuest);
				XPATH.pq_title.setVal(ProquestQCStatsItems.Title, stats, d);
				XPATH.pq_atitle.setVal(ProquestQCStatsItems.TitleAlt, stats, d);
				XPATH.pq_creator.setVal(ProquestQCStatsItems.Creator, stats, d);
				XPATH.pq_suffix.setVal(ProquestQCStatsItems.Suffix, stats, d);
				XPATH.pq_created.setVal(ProquestQCStatsItems.Created, stats, d);
				XPATH.pq_pages.setVal(ProquestQCStatsItems.Pages, stats, d);
				XPATH.pq_degree.setVal(ProquestQCStatsItems.Degree, stats, d);
				XPATH.pq_college.setVal(ProquestQCStatsItems.College, stats, d); 		
				XPATH.pq_dept.setVal(ProquestQCStatsItems.Dept, stats, d); 		
				XPATH.pq_advisor.setVal(ProquestQCStatsItems.Advisor, stats, d); 		
				XPATH.pq_catcode.setVal(ProquestQCStatsItems.CatCode, stats, d); 		
				XPATH.pq_catdesc.setVal(ProquestQCStatsItems.CatDesc, stats, d); 		
				XPATH.pq_catkey.setVal(ProquestQCStatsItems.Keyword, stats, d); 		
				XPATH.pq_abs.setVal(ProquestQCStatsItems.Abstract, stats, d); 		
			} else if (root.getTagName().equals("dublin_core")) {
				stats.setVal(ProquestQCStatsItems.Type, TYPE.DublinCore);
				XPATH.dc_title.setVal(ProquestQCStatsItems.Title, stats, d);
				XPATH.dc_atitle.setVal(ProquestQCStatsItems.TitleAlt, stats, d);
				XPATH.dc_url.setVal(ProquestQCStatsItems.URL, stats, d);
				XPATH.dc_creator.setVal(ProquestQCStatsItems.Creator, stats, d);
				XPATH.dc_created.setVal(ProquestQCStatsItems.Created, stats, d);
				XPATH.dc_pages.setVal(ProquestQCStatsItems.Pages, stats, d);
				XPATH.dc_degree.setVal(ProquestQCStatsItems.Degree, stats, d);
				XPATH.dc_college.setVal(ProquestQCStatsItems.College, stats, d); 		
				XPATH.dc_dept.setVal(ProquestQCStatsItems.Dept, stats, d); 		
				XPATH.dc_advisor.setVal(ProquestQCStatsItems.Advisor, stats, d); 		
				XPATH.dc_catstr.setVal(ProquestQCStatsItems.CatStr, stats, d); 		
				XPATH.dc_catdesc.setVal(ProquestQCStatsItems.CatDesc, stats, d); 		
				XPATH.dc_catkey.setVal(ProquestQCStatsItems.Keyword, stats, d); 		
				XPATH.dc_abs.setVal(ProquestQCStatsItems.Abstract, stats, d); 		
			} else if (root.getTagName().equals("marc:collection")) {
				stats.setVal(ProquestQCStatsItems.Type, TYPE.MarcXMLCollection);
				NodeList nl = d.getElementsByTagName("marc:record");
				for(int i=0; i<nl.getLength(); i++){
					String mekey = key + "_" + nf.format(i);
					Stats mestats = this.createStats(mekey);
					mestats.setVal(ProquestQCStatsItems.File, nf.format(i));
					mestats.setVal(ProquestQCStatsItems.OverallStat, OVERALL_STAT.PASS);
					doMarcRec(mestats, nl.item(i));
					this.dt.types.put(mekey, mestats);
				}
			} else if (root.getTagName().equals("marc:record")) {
				doMarcRec(stats, d);
			} 
			
			
			stats.setVal(ProquestQCStatsItems.OverallStat, OVERALL_STAT.PASS);
		} catch (SAXException e) {
			stats.setVal(ProquestQCStatsItems.OverallStat, OVERALL_STAT.FAIL);
		} catch (IOException e) {
			stats.setVal(ProquestQCStatsItems.OverallStat, OVERALL_STAT.FAIL);
		}
		
		return "";
	}

	public String getShortName() {
		return "ProQuestQC";
	}

  	public void initFilters() {
		filters.add(new XmlFilter());
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details;
    }

	public String toString() {
		return "ProQuest Metadata QC";
	}

}
