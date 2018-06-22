package edu.georgetown.library.fileAnalyzer.importer;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.marc.ControlField;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;


public class EncodingCheck extends DefaultImporter
{
	// name and description of the encoding validator
	public String toString()
	{
		return "MARC Encoding Check";
	}
	
	public String getDescription()
	{
		return "Check character encoding of MARC file (records), and split them if necessary."; 
	}
	
	public String getShortName()
	{
		return "Encoding";
	}
	
	
	// resulting information to display
	public static NumberFormat nf = NumberFormat.getNumberInstance();
	static
	{
		nf.setMinimumIntegerDigits(5);
		nf.setGroupingUsed(false);
	}
	
	public static enum STATUS{Valid, Invalid};
	public static enum CODE{MARC8, Unicode};
	
	private static enum EncodingStatsItem implements StatsItemEnum
	{
		Item_Num(StatsItem.makeStringStatsItem("Item Number", 100)),
		Status(StatsItem.makeEnumStatsItem(STATUS.class, "Status").setWidth(100)),
		Leader(StatsItem.makeStringStatsItem("MARC LEADER", 200)),
		Encoding(StatsItem.makeEnumStatsItem(CODE.class, "Character Encoding").setWidth(200)),
		OCLC(StatsItem.makeStringStatsItem("OCLC Number", 200)),
		Title(StatsItem.makeStringStatsItem("Title (245)", 300));
		
		StatsItem si;
		
		EncodingStatsItem (StatsItem si)
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
	
	public static StatsItemConfig details = StatsItemConfig.create(EncodingStatsItem.class);
	
	public EncodingCheck(FTDriver dt)
	{
		super(dt);
	}
	
	
	// file import rules
	public ActionResult importFile(File selectedFile) throws IOException
	{
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		
		InputStream in = new FileInputStream(selectedFile);
		MarcReader reader = new MarcPermissiveStreamReader(in, true, false);
		
		InputStream in_utf = new FileInputStream(selectedFile);
		MarcReader reader_utf = new MarcPermissiveStreamReader(in_utf, true, true);
		
		int i = 0;
		int count_marc8 = 0;
		int count_utf = 0;
		
		List<Record> rec_marc8 = new ArrayList<Record>();
		List<Record> rec_utf = new ArrayList<Record>();
		
				
		while(reader.hasNext() && reader_utf.hasNext())
		{
			String key = nf.format(++i);
			Stats stat = Generator.INSTANCE.create(key);
			stat.setVal(EncodingStatsItem.Status, STATUS.Valid);
			types.put(stat.key, stat);
			
			Record record = reader.next();
			Record record_utf = reader_utf.next();
			
			// marc leader comes from the original reader
			Leader leader = record.getLeader();
			char code = leader.getCharCodingScheme();
			if (leader.toString().equals(""))
			{
				stat.setVal(EncodingStatsItem.Status, STATUS.Invalid);
				stat.setVal(EncodingStatsItem.Leader, "MARC Leader Missing");
			} else
			{
				stat.setVal(EncodingStatsItem.Leader, leader.toString());
				if (code == ' ')
				{
					stat.setVal(EncodingStatsItem.Encoding, CODE.MARC8);
					rec_marc8.add(record);
					count_marc8 ++;
				} else if (code == 'a')
				{
					stat.setVal(EncodingStatsItem.Encoding, CODE.Unicode);
					rec_utf.add(record_utf);
					count_utf ++;
				}
			}
			
			// OCLC number and title come from utf reader
			ControlField cf001 = (ControlField) record_utf.getVariableField("001");
			String oclc;
			if (cf001 == null)
			{
				stat.setVal(EncodingStatsItem.Status, STATUS.Invalid);
				stat.setVal(EncodingStatsItem.OCLC, "OCLC Missing");
			} else
			{
				oclc = cf001.getData();
				stat.setVal(EncodingStatsItem.OCLC, oclc);				
			}
			
			DataField df245 = (DataField) record_utf.getVariableField("245");
			String title;
			if (df245 == null)
			{
				stat.setVal(EncodingStatsItem.Status, STATUS.Invalid);
				stat.setVal(EncodingStatsItem.Title, "Title Missing");
			} else
			{
				title = df245.getSubfield('a').getData();
				stat.setVal(EncodingStatsItem.Title, title);
			}
						
			
		} // end while loop
		
		// System.out.println("count_marc8 = " + count_marc8);
		// System.out.println("count_utf = " + count_utf);
		
		
		// if mixed records, separate records and create two marc files
		if (count_marc8 != 0 && count_utf != 0)
		{
			String absPath = selectedFile.getAbsolutePath();
			String basePath = absPath.substring(0, absPath.lastIndexOf(File.separator));
			String file_marc8 = basePath + "\\MARC8-" + selectedFile.getName();
			String file_utf = basePath + "\\UNICODE-" + selectedFile.getName();
			
			try (OutputStream out_marc8 = new FileOutputStream(file_marc8))
			{
				for(Record rec : rec_marc8)
				{
					MarcWriter writer = new MarcStreamWriter(out_marc8);
					writer.write(rec);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			try (OutputStream out_utf = new FileOutputStream(file_utf))
			{
				for (Record rec : rec_utf)
				{
					MarcWriter writer = new MarcStreamWriter(out_utf);
					writer.write(rec);
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
		}
		
		
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
			
}
