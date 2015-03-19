package edu.georgetown.library.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.ftprop.FTProp;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.ftprop.InvalidInputException;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import gov.nara.nwts.ftapp.util.XMLUtil;

import edu.georgetown.library.fileAnalyzer.filetest.IngestValidate.Generator.DSpaceStats;
import edu.georgetown.library.fileAnalyzer.importer.MetadataRegPropFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
public class IngestValidate extends DefaultFileTest { 
	
	Pattern pOther = Pattern.compile("^metadata_(.+)\\.xml$");
	
	public enum OVERALL_STAT {
		SKIP,
		VALID,
		INVALID,
		EXTRA
	}
	public enum CONTENTS {
		ORIGINAL,
		THUMBNAIL
	}
	public enum CONTENTS_STAT {
		MISSING,
		VALID,
		OTHER,
		TAB_MISSING,
		UNLISTED,
		INVALID
	}
	public enum DC_STAT {
		MISSING,
		VALID,
		INVALID,
		UNKNOWN_FIELD
	}

	public enum OTHER_STAT {
		NA,
		VALID,
		INVALID
	}

	public enum THUMBNAIL_STAT { 
		NA,
		VALID,
		INVALID_FILENAME
	}

	public static enum DSpaceStatsItems implements StatsItemEnum {
		ItemFolder(StatsItem.makeStringStatsItem("Item Folder", 200)),
		OverallStat(StatsItem.makeEnumStatsItem(OVERALL_STAT.class, "Status", OVERALL_STAT.INVALID).setWidth(80)),
		NumFiles(StatsItem.makeIntStatsItem("Num Items").setWidth(80)),
		ContentsStat(StatsItem.makeEnumStatsItem(CONTENTS_STAT.class, "Contents File", CONTENTS_STAT.MISSING).setWidth(80)),
		ContentFileCount(StatsItem.makeIntStatsItem("Num ContentF Files").setWidth(80)),
		DublinCoreStat(StatsItem.makeEnumStatsItem(DC_STAT.class, "Dublin Core File", DC_STAT.MISSING).setWidth(80)),
		OtherSchemas(StatsItem.makeStringStatsItem("Other Schemas")),
		OtherMetadataFileStats(StatsItem.makeEnumStatsItem(OTHER_STAT.class, "Other Metadata File Status").setWidth(120)),
		PrimaryBitstream(StatsItem.makeStringStatsItem("PrimaryBitstream", 150)),
		Thumbnail(StatsItem.makeStringStatsItem("Thumbnail", 150)),
		ThumbnailStatus(StatsItem.makeEnumStatsItem(THUMBNAIL_STAT.class, "Thumbnail Filename")),
		License(StatsItem.makeStringStatsItem("License", 150)),
		Text(StatsItem.makeStringStatsItem("TextBitstream", 150)),
		Other(StatsItem.makeStringStatsItem("OtherBitstream", 150)),
        Notes(StatsItem.makeStringStatsItem("Error Details", 150)),
		;
		
		StatsItem si;
		DSpaceStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public class DSpaceStats extends Stats {
			
			public DSpaceStats(String key) {
				super(details, key);
			}

			public void setInfo(DSpaceInfo info) {
				setVal(DSpaceStatsItems.OverallStat, info.getOverallStat());
				setVal(DSpaceStatsItems.NumFiles, info.fileCount);
				setVal(DSpaceStatsItems.ContentsStat, info.contents_stat);
				setVal(DSpaceStatsItems.ContentFileCount, info.contentsList.size());
				setVal(DSpaceStatsItems.DublinCoreStat, info.dc_stat);
				setVal(DSpaceStatsItems.OtherSchemas, info.otherSchemas);
				setVal(DSpaceStatsItems.OtherMetadataFileStats, info.other_stat);
				setVal(DSpaceStatsItems.PrimaryBitstream,info.primary);
				setVal(DSpaceStatsItems.Thumbnail,info.thumbnail);
				setVal(DSpaceStatsItems.ThumbnailStatus,info.thumbnail_stat);
				setVal(DSpaceStatsItems.License,info.license);
				setVal(DSpaceStatsItems.Text,info.text);
				setVal(DSpaceStatsItems.Other,info.other);
                setVal(DSpaceStatsItems.Notes,info.notes);
				
				for(String tag: info.metadata.keySet()) {
					ArrayList<String>vals = info.metadata.get(tag);
					String sep = "";
					for(String val: vals) {
						appendKeyVal(details.getByKey(tag), sep+val);
						sep=",\n";
					}
				}
			}

		}
		public DSpaceStats create(String key) {return new DSpaceStats(key);}
	}
	public static StatsItemConfig details = StatsItemConfig.create(DSpaceStatsItems.class);
    @Override public InitializationStatus init() {
    	details = StatsItemConfig.create(DSpaceStatsItems.class);
    	for(FTProp prop: ftprops) {
    		if (prop.getValue().equals("NA")) continue;
            if (prop.getName().equals(MetadataRegPropFile.P_METAREG)) continue;
    		details.addStatsItem(prop.getValue(), StatsItem.makeStringStatsItem(prop.getValue().toString()));
    	}
    	return super.init();
    }
	
	public class DSpaceInfo {
		public File contents = null;
		public File dc = null;
		public File[] files;
		public int fileCount = 0;
		public ArrayList<String> contentsList;
		public HashMap<String,ArrayList<String>> metadata;
		
		public CONTENTS_STAT contents_stat = CONTENTS_STAT.MISSING;
		public DC_STAT dc_stat = DC_STAT.MISSING;
		public OTHER_STAT other_stat = OTHER_STAT.NA;
		public String otherSchemas = "";
		public THUMBNAIL_STAT thumbnail_stat = THUMBNAIL_STAT.NA;
		
		public String primary ="";
		public String thumbnail ="";
		public String license ="";
		public String text ="";
		public String other ="";
		public String notes = "";
		
		public DSpaceInfo(File f) {
			files = f.listFiles();
			contentsList = new ArrayList<String>();
			metadata = new HashMap<String,ArrayList<String>>();
			
			for(File file : files) {
				if (!file.isDirectory()) fileCount++;
				if (file.getName().equals("contents")) {
					contents = file;
					contents_stat = CONTENTS_STAT.VALID;
					readContents(file);
				} else if (file.getName().equals("dublin_core.xml")) {
					dc = file;
					try {
						Document d = XMLUtil.db.parse(dc);
						if (d!=null) {
							loadMetadata(d);
						}
						dc_stat = DC_STAT.VALID;
					} catch (SAXException e) {
						dc_stat = DC_STAT.INVALID;
					} catch (IOException e) {
						dc_stat = DC_STAT.INVALID;
                    } catch (InvalidInputException e) {
                        dc_stat = DC_STAT.UNKNOWN_FIELD;
                        notes = e.getMessage();
					}
				} else {
					Matcher m = pOther.matcher(file.getName());
					if (!m.matches()) continue;
					otherSchemas += m.group(1) + " ";
					if (m.matches()) {
						try {
							Document d = XMLUtil.db.parse(file);
							if (d!=null) {
								loadMetadata(d);
							}
							if (other_stat != OTHER_STAT.INVALID) {
								other_stat = OTHER_STAT.VALID;
							} 
						} catch (SAXException e) {
							other_stat = OTHER_STAT.INVALID;
						} catch (IOException e) {
							other_stat = OTHER_STAT.INVALID;
	                    } catch (InvalidInputException e) {
	                        dc_stat = DC_STAT.UNKNOWN_FIELD;
	                        notes = e.getMessage();
						}
					}
				}
			}
			for(File file : files) {
				if (isExpectedFile(file)) continue;
				String name = file.getName();
				boolean found = false;
				for(String s: contentsList) {
					if (name.equals(s)) found = true;
				}
				if (!found) {
					if (contents_stat == CONTENTS_STAT.VALID) {
						contents_stat = CONTENTS_STAT.UNLISTED;						
					}
					other += name + "; ";
				}
			}
			
			if (thumbnail.isEmpty()) {
			} else if (thumbnail.equals(primary + ".jpg")) {
				thumbnail_stat = THUMBNAIL_STAT.VALID;
			} else {
				thumbnail_stat = THUMBNAIL_STAT.INVALID_FILENAME;				
			}
			
		}
		
		
		public OVERALL_STAT getOverallStat() {
			if (fileCount == 0)
				return OVERALL_STAT.SKIP;
			if (dc_stat != DC_STAT.VALID) 
				return OVERALL_STAT.INVALID;
			if (other_stat == OTHER_STAT.INVALID) 
				return OVERALL_STAT.INVALID;
			if (thumbnail_stat == THUMBNAIL_STAT.INVALID_FILENAME)
				return OVERALL_STAT.INVALID;
			if (contents_stat == CONTENTS_STAT.VALID)
				return OVERALL_STAT.VALID;
			else if (contents_stat == CONTENTS_STAT.OTHER)
				return OVERALL_STAT.EXTRA;
			return OVERALL_STAT.INVALID;
		}
		
		public void loadMetadata(Document d) throws InvalidInputException {
			String schema = d.getDocumentElement().getAttribute("schema");
			NodeList nl = d.getDocumentElement().getElementsByTagName("dcvalue");
			for(int i=0; i<nl.getLength();i++) {
				Element elem = (Element)nl.item(i);
				String tag = getMetadataKey(schema, elem);
				if (!metadataPropFile.isFieldInRegistry(tag)) {
				    throw new InvalidInputException(tag + " is not in the Metadata Registry");
				}
				String text = elem.getTextContent();
				if (text.isEmpty()) continue;
				ArrayList<String> vals = metadata.get(tag);
				if (vals == null) {
					vals = new ArrayList<String>();
					metadata.put(tag, vals);
				}
				vals.add(text);
			}
		}
		
		public String getMetadataKey(String s, String e, String q) {
			StringBuffer buf = new StringBuffer();
			buf.append(s.isEmpty() ? "dc" : s);
			buf.append(".");
			buf.append(e);

			if (q == null) {
			} else if (q.isEmpty()) {
			} else if (q.equals("none")) {
			} else {
				buf.append(".");
				buf.append(q);
			}
			return buf.toString();
		}
		
		public String getMetadataKey(String s, String e) {
			return getMetadataKey(s, e, "");
		}

		
		public String getMetadataKey(String s, Element elem) {
			return getMetadataKey(s, elem.getAttribute("element"), elem.getAttribute("qualifier"));
		}
		
		public String getTextVal(String key, String def) {
			ArrayList<String> vals = metadata.get(key);
			if (vals == null) return def;
			if (vals.size() == 1) return vals.get(0);
			return def;
		}
		
		public ArrayList<String> getTextVals(String key) {
			ArrayList<String> vals = metadata.get(key);
			if (vals == null) return new ArrayList<String>(0);
			return vals;
		}

		public void readContents(File f) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(f));
				for(String s=br.readLine(); s!=null;s=br.readLine()) {
					if (s.trim().equals("")) continue;
					Pattern p = Pattern.compile("^([^\\t]+)\\t(.+)\\s*$");
					Matcher m = p.matcher(s);
					if (m.matches()) {
						String fname = m.group(1);
						contentsList.add(fname);
						String type = m.group(2).trim();
						if (type.equals("bundle:ORIGINAL")) {
							primary = fname;
						} else if (type.equals("bundle:THUMBNAIL")) {
							thumbnail = fname;
						} else if (type.equals("bundle:LICENSE")) {
							license = fname;
						} else if (type.equals("bundle:TEXT")) {
							text = fname;
						} else {
							other += type + "; ";
							contents_stat = CONTENTS_STAT.OTHER;
						}
					} else if (s.indexOf("\t", 0) == -1) {
						contents_stat = CONTENTS_STAT.TAB_MISSING;
						other = "[" + s + "]";
					} else {
						contents_stat = CONTENTS_STAT.INVALID;
						other = "[" + s + "]";
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	public String[] getMETA() {return IngestInventory.META;}
	public static final int COUNT = 8;

	MetadataRegPropFile metadataPropFile;
	long counter = 1000000;
	
	public int getStdPropCount() {return 1;}
	
	public IngestValidate(FTDriver dt) {
		super(dt);
        metadataPropFile = new MetadataRegPropFile(dt);
        this.ftprops.add(metadataPropFile);
		for(int i=1+getStdPropCount(); i<=1+getStdPropCount(); i++) {
			this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(),  "metadata "+i, "m"+i,
					"field to display for each item found", getMETA(), "dc.title"));			
		}
		for(int i=2+getStdPropCount(); i<=2+getStdPropCount(); i++) {
			this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(),  "metadata "+i, "m"+i,
					"field to display for each item found", getMETA(), "dc.date.created"));			
		}
		for(int i=3+getStdPropCount(); i<=COUNT; i++) {
			this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(),  "metadata "+i, "m"+i,
					"field to display for each item found", getMETA(), "NA"));			
		}
	}
	
	public String toString() {
		return "Ingest Validate";
	}
	public String getKey(File f) {
		return f.getName();
	}
	
    public String getShortName(){return "Ingest Validate";}

	public boolean isExpectedFile(File file) {
		String name = file.getName();
		if (name.equals("contents")) return true;
		if (name.equals("dublin_core.xml")) return true;
		if (name.equals("Thumbs.db")) return true;
		if (pOther.matcher(name).matches()) return true;
		return false;
	}
	public Object fileTest(File f) {
		DSpaceStats stats = (DSpaceStats)this.getStats(getKey(f));
		DSpaceInfo info = new DSpaceInfo(f);
		stats.setInfo(info);
		if (info.contents_stat != CONTENTS_STAT.VALID) return info.contents_stat;
		if (info.dc_stat != DC_STAT.VALID) return info.dc_stat;
		return info.files.length;
	}

    public DSpaceStats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details;
    }

    public boolean isTestFiles() {return false;}
    public boolean isTestDirectory() {return true;}

    public String getDescription() {
		return "Analyzes an ingest folder to be uploaded into DSpace.\n\n" +
				"This rule should be pointed at a folder of subfolders where each sub-folder conforms to DSpace's ingest folder conventions.\n" +
				"- An item file should be present\n" +
				"- A thumbnail file may be present.  If present, it must match the item file name + '.jpg'\n" +
				"- An optional license file or text file may be present\n" +
				"- A dublin core metadata file 'dublin_core.xml' is required.  This file will be scanned for required metadata.\n" +
				"- A contents file 'contents' is required.  This file must contain a list of all other required files.\n" +
				"";
	}

}
