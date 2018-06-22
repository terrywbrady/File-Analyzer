package edu.georgetown.library.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

/*
 * Importer for outsourced MARC records
 * 
 * @author wz24
 * @version 1.0
 *  
 */

public class OutsourcedMarcRecValidator extends DefaultImporter
{
	
	// name and description of the outsourced marc validator
	public String toString()
	{
		return "Outsourced MARC Validation";
	}
	
	public String getDescription()
	{
		return "The purpose of this rule is to validate the contents of MARC files created by a cataloging outsourcing company. This rule will allow the user to count records by cataloging agencies and to validate the presence of required elements, including MARC leader, OCLC number, title, and various item fields.";
	}
	
	public String getShortName()
	{
		return "Outsourced";
	}
	
	
	// runtime parameters, to validate local 949 overlay field
	public static final String LOCALCAT = "Local Cataloging Agencies";
	public static final String OVERLAY = "Require 949 Overlay Field";
	
	public OutsourcedMarcRecValidator(FTDriver dt)
	{
		super(dt);
		this.ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(), LOCALCAT, LOCALCAT, "Please submit a list of local cataloging agencies, separated by comma\n(default values are Georgetown University Libraries)", "DGU, DGT, KIE, GTU, GUL"));
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getSimpleName(), OVERLAY, OVERLAY, "Validate 949 Overlay Field - III Libraries Only", YN.values(), YN.Y));
		
	}
		
	
	// resulting information to display
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static
	{
		nf.setMinimumIntegerDigits(4);
		nf.setGroupingUsed(false);
	}
	
	public static enum STAT {Valid, Invalid};
	public static enum VALIDATE {Missing, No_Occurrence, One_Occurrence, Multi_Occurrence, Not_Applicable};
	public static enum CONTENT {Correct, Error}
	public static enum AGENCY {LOCAL, LC_NLM, Member};
	public static enum CODE {MARC8, Unicode}
	
	
	private static enum OutsourcedStatsItems implements StatsItemEnum
	{
		Item_Num(StatsItem.makeStringStatsItem("Item No.", 100)),
		Stat(StatsItem.makeEnumStatsItem(STAT.class, "Status").setWidth(100)),
		
		Leader(StatsItem.makeStringStatsItem("Leader", 250)),
		Coding(StatsItem.makeEnumStatsItem(CODE.class, "Coding Scheme").setWidth(100)),
		OCLC_STAT(StatsItem.makeEnumStatsItem(VALIDATE.class, "Has OCLC Number").setWidth(150)),
		OCLC(StatsItem.makeStringStatsItem("OCLC Number (001)", 200)),
		Title(StatsItem.makeStringStatsItem("Title (245)", 250)),
		Source_STAT(StatsItem.makeEnumStatsItem(AGENCY.class, "Cataloging Agency Category").setWidth(200)),
		Source(StatsItem.makeStringStatsItem("Cataloging Agency (040)", 200).makeFilter(true)),
		Language(StatsItem.makeStringStatsItem("Language", 100).makeFilter(true)),
		
		Has_f949_Overlay(StatsItem.makeEnumStatsItem(VALIDATE.class, "Has 949 Overlay").setWidth(200)),
		f949_Overlay(StatsItem.makeStringStatsItem("949 Overlay", 200)),
		f949_Overlay_Bib(StatsItem.makeStringStatsItem("Overlay Bib No.", 150)),
		Has_f949(StatsItem.makeEnumStatsItem(VALIDATE.class, "Has 949 Item").setWidth(200)),
		f949_i(StatsItem.makeStringStatsItem("item barcode (949$i)", 150)),
		f949_z(StatsItem.makeStringStatsItem("item call no.(949$z)", 150)),
		f949_a(StatsItem.makeStringStatsItem("item call no.(949$a)", 150)),
		f949_b(StatsItem.makeStringStatsItem("item call no.(949$b)", 150));
		
		StatsItem si;
		
		OutsourcedStatsItems(StatsItem si)
		{
			this.si = si;
		}
		
		public StatsItem si()
		{
			return si;
		}
	}
	
	public static enum Generator implements StatsGenerator
	{
		INSTANCE;
		public Stats create(String key)
		{
			return new Stats(details, key);
		}
	}
	
	public static StatsItemConfig details = StatsItemConfig.create(OutsourcedStatsItems.class);
	
/*	
    public OutsourcedMarcRecValidator(FTDriver dt)
	{
		super(dt);
	}
*/
	
	// file import rule
	public void setFieldCount(Stats stat, int count, OutsourcedStatsItems osi, boolean required)
	{
		if (count == 0)
		{
			if (required)
			{
				stat.setVal(osi, VALIDATE.Missing);
				stat.setVal(OutsourcedStatsItems.Stat, STAT.Invalid);
			} else
			{
				stat.setVal(osi, VALIDATE.No_Occurrence);
			}
			
		} else if (count == 1)
		{
			stat.setVal(osi, VALIDATE.One_Occurrence);
		} else if (count > 1)
		{
			stat.setVal(osi, VALIDATE.Multi_Occurrence);
		}
		
	}
	
	/* not in use in this rule
	public void setFieldError(Stats stat, String s, OutsourcedStatsItems osiMsg, OutsourcedStatsItems osi)
	{
		stat.setVal(osiMsg, s);
		
		if (! s.isEmpty())
		{
			stat.setVal(OutsourcedStatsItems.Stat, STAT.Invalid);
			stat.setVal(osi, CONTENT.Error);			
		}
	}
	*/
	
	public static void statSubfield(Stats stat, OutsourcedStatsItems osi, DataField df, char f)
	{
		stat.setVal(osi, getSubfield(df, f));
	}
	
	public static void statSubfield(Stats stat, OutsourcedStatsItems osi, ControlField cf, int start, int end)
	{
		stat.setVal(osi, getSubfield(cf, start, end));
	}
	
	public static String getSubfield(DataField df, char f)
	{
		Subfield sf = df.getSubfield(f);
		if (sf == null) return "";
		return sf.getData();
	}
	
	public static String getSubfield(ControlField cf, int start, int end)
	{
		String cfdata = cf.getData();
		if (cfdata == null) return "";
		return cfdata.substring(start, end);
	}
	
	
	public ActionResult importFile(File selectedFile) throws IOException
	{
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
				
		InputStream in = new FileInputStream(selectedFile);
		MarcReader reader = new MarcPermissiveStreamReader(in, true, false);
		
		InputStream in_utf = new FileInputStream(selectedFile);
		MarcReader reader_uft = new MarcPermissiveStreamReader(in_utf, true, true);
		
		String localcat = this.getProperty(LOCALCAT,"").toString();
		List<String> localcatlist = Arrays.asList(localcat.split("\\s*,\\s*"));
		
		boolean ov = (YN)getProperty(OVERLAY) == YN.Y;
		
		int i = 1;
		
		while (reader_uft.hasNext() && reader.hasNext())
		{
			String key = nf.format(i++);
			Stats stat = Generator.INSTANCE.create(key);
			stat.setVal(OutsourcedStatsItems.Stat, STAT.Valid);
			types.put(stat.key, stat);
						
			Record record_utf = reader_uft.next();
			Record record = reader.next();
						
			int count_001 = 0;
			int count_949_1 = 0;
			int count_949_2 = 0;
			
			// marc leader is from the original file
			Leader leader = record.getLeader();
			if (leader.toString().equals(""))
			{
				stat.setVal(OutsourcedStatsItems.Stat, STAT.Invalid);				
			} else
			{
				stat.setVal(OutsourcedStatsItems.Leader, leader.toString());
			}
			
			if (leader.getCharCodingScheme() == ' ')
			{
				stat.setVal(OutsourcedStatsItems.Coding, CODE.MARC8);
			} else if (leader.getCharCodingScheme() == 'a')
			{
				stat.setVal(OutsourcedStatsItems.Coding, CODE.Unicode);
			}
			
			// other control fields and data fields are converted to utf
			for (ControlField cf : record_utf.getControlFields())
			{
				String ctag = cf.getTag();
				
				if (ctag.equals("001"))
				{
					statSubfield(stat, OutsourcedStatsItems.OCLC, cf, 0, cf.getData().length()-1);
					count_001 ++;
				} else if (ctag.equals("008"))
				{
					statSubfield(stat, OutsourcedStatsItems.Language, cf, 35, 38);
				}
			}
			setFieldCount(stat, count_001, OutsourcedStatsItems.OCLC_STAT, true);			
			
			
			for (DataField df : record_utf.getDataFields())
			{
							
				String tag = df.getTag();
				char ind1 = df.getIndicator1();
				char ind2 = df.getIndicator2();
				
				
				if (tag.equals("245"))
				{
					statSubfield(stat, OutsourcedStatsItems.Title, df, 'a');
				} else if (tag.equals("040"))
				{
					statSubfield(stat, OutsourcedStatsItems.Source, df, 'a');
					
					if( localcatlist.contains(df.getSubfield('a').getData()) )
					{
						stat.setVal(OutsourcedStatsItems.Source_STAT, AGENCY.LOCAL);
					} else if(df.getSubfield('a').getData().startsWith("DLC") || df.getSubfield('a').getData().equals("NLM") )
					{
						stat.setVal(OutsourcedStatsItems.Source_STAT, AGENCY.LC_NLM);
					} else
					{
						stat.setVal(OutsourcedStatsItems.Source_STAT, AGENCY.Member);
					}
				} else if (tag.equals("949") && ind1 == ' ' && ind2 == ' ')
				{
					if (ov)
					{
						statSubfield(stat, OutsourcedStatsItems.f949_Overlay,
								df, 'a');
						count_949_1++;
						String sub_data = df.getSubfield('a').getData();
						String bib = sub_data.substring(sub_data
								.indexOf("ov=.") + 4, sub_data.length() - 1);
						if (bib.length() != 1)
						{
							stat.setVal(OutsourcedStatsItems.f949_Overlay_Bib,
									bib);
						} else
						{
							stat.setVal(OutsourcedStatsItems.f949_Overlay_Bib,
									"Bib No. Missing");
						}
					} else
					{
						stat.setVal(OutsourcedStatsItems.f949_Overlay, VALIDATE.Not_Applicable);
						stat.setVal(OutsourcedStatsItems.f949_Overlay_Bib, "Not Applicable");
					}
					
				} else if (tag.equals("949") && ind1 == ' ' && ind2 =='1')
				{
					statSubfield(stat, OutsourcedStatsItems.f949_i, df, 'i');
					statSubfield(stat, OutsourcedStatsItems.f949_z, df, 'z');
					statSubfield(stat, OutsourcedStatsItems.f949_a, df, 'a');
					statSubfield(stat, OutsourcedStatsItems.f949_b, df, 'b');
					count_949_2 ++;
				}
				
			}
			
			if (ov)
			{
				setFieldCount(stat, count_949_1,
						OutsourcedStatsItems.Has_f949_Overlay, true);
			} else
			{
				stat.setVal(OutsourcedStatsItems.Has_f949_Overlay, VALIDATE.Not_Applicable);
				stat.setVal(OutsourcedStatsItems.f949_Overlay, VALIDATE.Not_Applicable);
				stat.setVal(OutsourcedStatsItems.f949_Overlay_Bib, "Not Applicable");
			}
			setFieldCount(stat, count_949_2, OutsourcedStatsItems.Has_f949, true);
									
		}
		
		details.createFilters(types);
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
}