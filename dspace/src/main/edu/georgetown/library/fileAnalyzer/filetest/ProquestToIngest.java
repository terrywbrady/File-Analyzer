package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.proquestXsl.GUProquestURIResolver;
import edu.georgetown.library.fileAnalyzer.proquestXsl.MarcUtil;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import edu.georgetown.library.fileAnalyzer.util.XMLUtil;
import edu.georgetown.library.fileAnalyzer.util.ZipUtil;

public class ProquestToIngest extends DefaultFileTest {
	
	File outdir;

	public enum OVERALL_STAT {
		INIT,
		PASS,
		FAIL,
		REVIEW
	}

	public static enum ProquestStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Zip File Name").setWidth(400)),
		OverallStat(StatsItem.makeEnumStatsItem(OVERALL_STAT.class, "Status", OVERALL_STAT.INIT).setWidth(40)),
		Dept(StatsItem.makeStringStatsItem("Department").setWidth(150).makeFilter(true)),
		Items(StatsItem.makeIntStatsItem("Num Items")),
		Size(StatsItem.makeLongStatsItem("Total Size")),
		XmlStat(StatsItem.makeEnumStatsItem(OVERALL_STAT.class, "XML Status", OVERALL_STAT.INIT).setWidth(40)),
        EmbargoStat(StatsItem.makeEnumStatsItem(YN.class, "Embargo?", YN.N).setWidth(80)),
		EmbargoTerms(StatsItem.makeStringStatsItem("Embargo Terms",130).makeFilter(true)),
		EmbargoCustom(StatsItem.makeStringStatsItem("Embargo Custom",80)),
        OtherRestriction(StatsItem.makeStringStatsItem("Other Restriction")),
		Orcid(StatsItem.makeStringStatsItem("ORCID", 140)),
        Author(StatsItem.makeStringStatsItem("Author",180)),
		Title(StatsItem.makeStringStatsItem("Title",350)),
		Message(StatsItem.makeStringStatsItem("Status Note", 300)),
		;
		
		StatsItem si;
		ProquestStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}
	
	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {
			return new Stats(details, key);
		}
	}
	static StatsItemConfig details = StatsItemConfig.create(ProquestStatsItems.class);
	static String P_MARC = "Generate MARC";
	static String P_FOLDERS = "Separate Folders per Dept";
	static String PS_FOLDERS = "folders";
	static String P_ZIP = "zip ingest";
	
    MarcUtil marcUtil;
    
    public MarcUtil getMarcUtil() {
        return new MarcUtil();
    }
	
	public ProquestToIngest(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),  P_MARC, P_MARC,
				"Generate a MARC XML record for QC/troubleshooting purposes", YN.values(), YN.N));	
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),  P_FOLDERS, PS_FOLDERS,
				"Group ETD's into folders by academic department", YN.values(), YN.Y));	
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(),  P_ZIP, P_ZIP,
				"Create zip files for ingest folders", YN.values(), YN.N));
		marcUtil = getMarcUtil();
		marcUtil.addProps(dt, ftprops);
	}
	
	public static final String PQEXTRACT = "pqextract_";
	
	private Vector<File> outdirs = new Vector<>();
	
	public InitializationStatus init() {
        marcUtil.setProps(ftprops);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
		outdir = new File(getRoot(), PQEXTRACT+df.format(new Date()));
		outdir.mkdir();		
		outdirs.clear();
		if (this.getProperty(P_FOLDERS).equals(YN.N)) {
			outdirs.add(outdir);
		}
		return super.init();
	}

	public String getDescription() {
		return "Prepare Proquest Files for DSpace Ingest";
	}
	public String getKey(File f) {
		return f.getAbsolutePath().substring(getRoot().getAbsolutePath().length());
	}
	
	public boolean isTestable(File f) {
		return !getKey(f).contains("\\"+PQEXTRACT);
	}
	
	public boolean hasEmbargoInXml(Document d) {
        if (d.getDocumentElement().hasAttribute("embargo_code")) {
            String s = d.getDocumentElement().getAttribute("embargo_code");
            return !s.equals("0");
        }
        return false;    
	}
	
	//To be overridden
	public boolean evaluateEmbargoInFileList() {return false;}
    public void writeFileListEmbargo(File zout, Stats stats) {}
	
	public String getLocalXslName() {return "proquest2ingest-local.xsl";}
	
	File getLocalSchemaFile(File zout) {
	    return new File(zout, "metadata_" + marcUtil.getLocalSchema() + ".xml");
	}
	private void setLocalMetadata(Document d, File zout, Stats stats) throws TransformerException, IOException, SAXException {
        File gu = getLocalSchemaFile(zout);
        XMLUtil.doTransform(d, gu, GUProquestURIResolver.INSTANCE, getLocalXslName(), marcUtil.getXslParm());   
        Document gd =  XMLUtil.db.parse(gu);
        NodeList nl = gd.getElementsByTagName("dcvalue");
        for(int i=0; i<nl.getLength(); i++) {
            Element elem = (Element)nl.item(i);
            if (!elem.getAttribute("element").equals("embargo")) continue;
            if (elem.getAttribute("qualifier").equals("terms")) {
                stats.setVal(ProquestStatsItems.EmbargoTerms, elem.getTextContent());
                stats.setVal(ProquestStatsItems.EmbargoStat, YN.Y);
            } else if (elem.getAttribute("qualifier").equals("custom-date")) {
                stats.setVal(ProquestStatsItems.EmbargoCustom, elem.getTextContent());
            }
        }
	}
	
	
	public boolean checkSkipFile(ZipEntry ze) {
        if (ze.getName().startsWith("__MACOSX")) return true;
        if (ze.getName().endsWith(".DS_Store")) return true;
        return false;
	}
	
	public boolean isCheckThirdPartySearchEnabled() {
	    return true;
	}

	public void checkThirdPartySearch(Document d, Stats stats) {
        if (d.getDocumentElement().hasAttribute("third_party_search")) {
            String tpsearch = d.getDocumentElement().getAttribute("third_party_search");
            if (tpsearch.equals("Y")) {
                stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.REVIEW);
                stats.setVal(ProquestStatsItems.OtherRestriction, "Restrict from 3rd Party Search");                                             
            }
        }
	}

    public void extractMetadata(Document d, Stats stats) {
        NodeList nl = d.getElementsByTagName("DISS_title");
        if (nl.getLength() == 1) {
            Element elem = (Element)nl.item(0);
            stats.setVal(ProquestStatsItems.Title, elem.getTextContent());
        }
        XPath xp = XMLUtil.xf.newXPath();
        try {
            stats.setVal(ProquestStatsItems.Author, "--");
            String author = xp.evaluate("concat(/DISS_submission/DISS_authorship/DISS_author/DISS_name/DISS_surname/text(),', ',/DISS_submission/DISS_authorship/DISS_author/DISS_name/DISS_fname/text())", d);
            stats.setVal(ProquestStatsItems.Author, author);
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        nl = d.getElementsByTagName("DISS_orcid");
        if (nl.getLength() == 1) {
            Element elem = (Element)nl.item(0);
            stats.setVal(ProquestStatsItems.Orcid, elem.getTextContent());
        }
    }

    public String getDept(Document d, Stats stats) {
        NodeList nl = d.getElementsByTagName("DISS_inst_contact");
        String dept = "TBD";
        
        if (nl.getLength() == 1) {
            Element elem = (Element)nl.item(0);
            dept = elem.getTextContent();
            stats.setVal(ProquestStatsItems.Dept, dept);
        }
        return dept;
    }

	
	public Object fileTest(File f) {
		String key = getKey(f);
		Stats stats = (Stats)this.getStats(key);
		File zout = new File(outdir, f.getName().replaceAll("\\.[zZ][iI][pP]$", ""));
		File revzout = new File(outdir, "review_"+f.getName().replaceAll("\\.[zZ][iI][pP]$", ""));
		zout.mkdir();
		int zcount = 0;
		long bytes = 0;
		byte[] buf = new byte[4096];
		String dept = "TBD";
		boolean bXmlFound = false;
		
		File contents = new File(zout, "contents");
		
		try(
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
			BufferedWriter br = new BufferedWriter(new FileWriter(contents));
		) {
			
			for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
			    if (checkSkipFile(ze)) continue;
				File ztemp = new File(ze.getName());
				File zeout = new File(zout, ztemp.getName());
				if (ze.isDirectory()) continue;
				
				try(FileOutputStream fos = new FileOutputStream(zeout)) {
					for(int i=zis.read(buf); i > -1; i=zis.read(buf)) {
						fos.write(buf, 0, i);
						bytes += i;
					}
				}
				
				if (ze.getName().toLowerCase().endsWith(".xml") && (!ze.getName().contains("/"))) {
					try {
						bXmlFound = true;
						Document d = XMLUtil.db.parse(zeout);
						stats.setVal(ProquestStatsItems.XmlStat, OVERALL_STAT.PASS);
						
						File dc = new File(zout, "dublin_core.xml");
						XMLUtil.doTransform(d, dc, GUProquestURIResolver.INSTANCE, "proquest2ingest-dc.xsl", marcUtil.getXslParm());

						if (this.getProperty(P_MARC).equals(YN.Y)) {
							File marc = new File(zout, "marc.xml");
							XMLUtil.doTransform(d, marc, GUProquestURIResolver.INSTANCE, "proquest2marc.xsl", marcUtil.getXslParm());							
						}
						extractMetadata(d, stats);
                        dept = getDept(d, stats);
						
						if (hasEmbargoInXml(d)) {
						    this.setLocalMetadata(d, zout, stats);
						}
                        if (isCheckThirdPartySearchEnabled()) {
                            this.checkThirdPartySearch(d, stats);
                        }
					} catch (SAXException e) {
						stats.setVal(ProquestStatsItems.XmlStat, OVERALL_STAT.FAIL);
						stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
						stats.setVal(ProquestStatsItems.Message, e.getMessage());					
					} catch (TransformerException e) {
						stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
						stats.setVal(ProquestStatsItems.Message, e.getMessage());					
					}
				} else {
					br.write(ztemp.getName());
					br.write("\t");
					br.write("bundle:ORIGINAL\n");
				}

				zcount++;

			}
	        if (evaluateEmbargoInFileList()) {
	            writeFileListEmbargo(zout, stats);
	        }
		} catch (ZipException e) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
			stats.setVal(ProquestStatsItems.Message, e.getMessage());					
		} catch (IOException e) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
			stats.setVal(ProquestStatsItems.Message, e.getMessage());					
		} catch (IllegalArgumentException e) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
			stats.setVal(ProquestStatsItems.Message, "Error unzipping the file perhaps due to unexpected characters in file name.\nProcess manually.");					
		}
		
		
		stats.setVal(ProquestStatsItems.Items, zcount);
		stats.setVal(ProquestStatsItems.Size, bytes);
		
		if (!bXmlFound) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.FAIL);
			stats.setVal(ProquestStatsItems.Message, "ProQuest XML File not found in root directory");	
		}
		
		if (stats.getVal(ProquestStatsItems.OverallStat) == OVERALL_STAT.FAIL) {
			for (File c : zout.listFiles()) {
				if (!c.delete()) {
					System.err.println("Cannot delete "+c.getAbsolutePath());
				}
			}
			if (!zout.delete()){
				System.err.println("Cannot delete "+zout.getAbsolutePath());
			}
			return zcount;		
		}

		if (bytes > 25000000 && stats.getVal(ProquestStatsItems.OverallStat) == OVERALL_STAT.INIT) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.REVIEW);					
		}
		
		if (zcount > 2 && stats.getVal(ProquestStatsItems.OverallStat) == OVERALL_STAT.INIT) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.REVIEW);					
		} 

		if (zcount == 2 && stats.getVal(ProquestStatsItems.OverallStat) == OVERALL_STAT.INIT) {
			stats.setVal(ProquestStatsItems.OverallStat, OVERALL_STAT.PASS);					
		} 
		
		if (stats.getVal(ProquestStatsItems.OverallStat) == OVERALL_STAT.REVIEW) {
			zout.renameTo(revzout);
			zout = revzout;
		}

		if (this.getProperty(P_FOLDERS).equals(YN.Y)) {
			dept = dept.replaceAll("[^a-zA-Z0-9]", "_").replaceAll("__+","_");
			File deptdir = new File(outdir, dept);
			deptdir.mkdir();
			File newName = new File(deptdir, zout.getName()); 
			zout.renameTo(newName);
			if (!outdirs.contains(deptdir)) {
				outdirs.add(deptdir);				
			}
		}
		
		if (getProperty(P_ZIP) == YN.Y) {
			for(File ingestFolder: outdirs) {
				try {
					ZipUtil.zipFolder(ingestFolder);
				} catch (IOException e) {
					System.err.println(e.getMessage() + " " + ingestFolder.getAbsolutePath());
				} catch (Exception e) {
					System.err.println(e.getMessage() + " " + ingestFolder.getAbsolutePath());
				}
			}
		}
		return zcount;
	}

    public void refineResults() {
        getStatsDetails().createFilters(this.dt.types);
    }
	public String getShortName() {
		return "ProQuest";
	}

  	public void initFilters() {
		filters.add(new ZipFilter());
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details;
    }

	public String toString() {
		return "ProQuest to DSpace Ingest Folder";
	}

}
