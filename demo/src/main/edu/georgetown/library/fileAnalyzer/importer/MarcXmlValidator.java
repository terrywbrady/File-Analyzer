package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.TreeMap;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import gov.nara.nwts.ftapp.util.XMLUtil;

/**
 * Importer for tab delimited files
 * 
 * @author TBrady
 * 
 */
public class MarcXmlValidator extends DefaultImporter {

	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static {
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
	}
	
	public static enum STAT {
		VALID,
		INVALID,
	}

	public static enum COUNT {
		MISSING,
		NOT_PRESENT,
		PRESENT_ONCE,
		MULTIPLE_COPIES,
		CONTENT_ERR
	}
	
	public static enum MarcStatsItems implements StatsItemEnum {
		ItemNum(StatsItem.makeStringStatsItem("ItemNum", 100)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Status").setWidth(150)),
		Author(StatsItem.makeStringStatsItem("Author", 250)), 
		Title(StatsItem.makeStringStatsItem("Title", 250)),

		f949i(StatsItem.makeStringStatsItem("949 i").setWidth(150)),
		f949l(StatsItem.makeStringStatsItem("949 l").setWidth(50)),
		f949s(StatsItem.makeStringStatsItem("949 s").setWidth(50)),
		f949t(StatsItem.makeStringStatsItem("949 t").setWidth(50)),
		f949z(StatsItem.makeStringStatsItem("949 z").setWidth(50)),
		f949a(StatsItem.makeStringStatsItem("949 a").setWidth(100)),
		f949b(StatsItem.makeStringStatsItem("949 b").setWidth(100)),
		f949(StatsItem.makeEnumStatsItem(COUNT.class, "949").setWidth(120)),
		Stat949(StatsItem.makeStringStatsItem("Status 949").setWidth(350)),

		f980(StatsItem.makeEnumStatsItem(COUNT.class, "980").setWidth(120)),
		Stat980(StatsItem.makeStringStatsItem("Status 980").setWidth(350)),

		f981(StatsItem.makeEnumStatsItem(COUNT.class, "981").setWidth(120)),
		f981b(StatsItem.makeStringStatsItem("981 b").setWidth(50)),
		Stat981(StatsItem.makeStringStatsItem("Status 981").setWidth(350)),

		f935(StatsItem.makeEnumStatsItem(COUNT.class, "935").setWidth(120)),
		f935a(StatsItem.makeStringStatsItem("935 a").setWidth(50)),
		Stat935(StatsItem.makeStringStatsItem("Status 935").setWidth(350)),
		;

		StatsItem si;

		MarcStatsItems(StatsItem si) {
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
			.create(MarcStatsItems.class);

	public MarcXmlValidator(FTDriver dt) {
		super(dt);
	}

	public String toString() {
		return "MARC XML Validator";
	}

	public String getDescription() {
		return "Look for common issues when importing Marc Records.";
	}

	public String getShortName() {
		return "Marc";
	}

	public void setFieldCount(Stats stat, String s, MarcStatsItems msi, boolean required) {
		if (s.equals("1")) {
			stat.setVal(msi, COUNT.PRESENT_ONCE);
		} else {
			if (s.equals("0")) {
				if (required) {
					stat.setVal(msi, COUNT.MISSING);
				} else {
					stat.setVal(msi, COUNT.NOT_PRESENT);
					return;
				}
			} else {
				stat.setVal(msi, COUNT.MULTIPLE_COPIES);						
			}
			stat.setVal(MarcStatsItems.Stat, STAT.INVALID);						
		}
		
	}
	
	public void setFieldError(Stats stat, String s, MarcStatsItems msiMsg, MarcStatsItems msi) {
		stat.setVal(msiMsg, s);
		if (!s.isEmpty()) {
			stat.setVal(MarcStatsItems.Stat, STAT.INVALID);						
			stat.setVal(msi, COUNT.CONTENT_ERR);
		}
	}
	
	
	public ActionResult importFile(File selectedFile) throws IOException {
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		
		try {
			Document d = XMLUtil.db_ns.parse(selectedFile);
			Document dout = (Document)XMLUtil.doTransformToDom(d, "edu/georgetown/library/fileAnalyzer/importer/marc.xsl");
			NodeList nl = dout.getElementsByTagName("result");
			
			for(int i=0; i < nl.getLength(); i++) {
				String key = nf.format(i+1);
				Stats stat = Generator.INSTANCE.create(key);
				stat.setVal(MarcStatsItems.Stat, STAT.VALID);
				types.put(key, stat);
				Element el = (Element)nl.item(i);
				stat.setVal(MarcStatsItems.Author, el.getAttribute("author"));
				stat.setVal(MarcStatsItems.Title, el.getAttribute("title"));
				stat.setVal(MarcStatsItems.f949i, el.getAttribute("f949i"));
				stat.setVal(MarcStatsItems.f949l, el.getAttribute("f949l"));
				stat.setVal(MarcStatsItems.f949s, el.getAttribute("f949s"));
				stat.setVal(MarcStatsItems.f949t, el.getAttribute("f949t"));
				stat.setVal(MarcStatsItems.f949z, el.getAttribute("f949z"));
				stat.setVal(MarcStatsItems.f949a, el.getAttribute("f949a"));
				stat.setVal(MarcStatsItems.f949b, el.getAttribute("f949b"));
				setFieldCount(stat, el.getAttribute("f949"), MarcStatsItems.f949, true);						
				setFieldError(stat, el.getAttribute("f949err"), MarcStatsItems.Stat949, MarcStatsItems.f949);						

				setFieldCount(stat, el.getAttribute("f980"), MarcStatsItems.f980, true);						
				setFieldError(stat, el.getAttribute("f980err"), MarcStatsItems.Stat980, MarcStatsItems.f980);						
				
				setFieldCount(stat, el.getAttribute("f981"), MarcStatsItems.f981, true);						
				stat.setVal(MarcStatsItems.f981b, el.getAttribute("f981b"));
				setFieldError(stat, el.getAttribute("f981err"), MarcStatsItems.Stat981, MarcStatsItems.f981);						
				
				setFieldCount(stat, el.getAttribute("f935"), MarcStatsItems.f935, false);						
				stat.setVal(MarcStatsItems.f935a, el.getAttribute("f935a"));
				setFieldError(stat, el.getAttribute("f350err"), MarcStatsItems.Stat935, MarcStatsItems.f935);						
				
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return new ActionResult(selectedFile, selectedFile.getName(),
				this.toString(), details, types, true, timer.getDuration());
	}
	
	public void doLine(String line) {
		
	}
	
}
