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
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;

public class MarcInventory extends DefaultImporter
{
	// name and description of the Marc Inventory Importer
	public String toString()
	{
		return "MARC Inventory";
	}
	
	public String getDescription()
	{
		return "The MARC Inventory Importer will report Bib IDs and MD5 hash of marc records in a MARC file (.mrc or .dat)";
		
	}
	
	public String getShortName()
	{
		return "Inventory";
	}

	
	// resulting information to display
	private static enum InventoryStatsItem implements StatsItemEnum
	{
		Bib_ID(StatsItem.makeStringStatsItem("Bib IDs", 100)),
		Hash_Code(StatsItem.makeStringStatsItem("Hash Code (MD5)", 500));
		
		StatsItem si;
		
		InventoryStatsItem (StatsItem si)
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

	
	public static StatsItemConfig details = StatsItemConfig.create(InventoryStatsItem.class);
		
	public MarcInventory(FTDriver dt)
	{
		super(dt);
	}

	
	// checksum with MD5
	public String getChecksum(String recString)
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(recString.getBytes());
			
			byte[] mdbytes = md.digest();
			
			StringBuffer sb =new StringBuffer();
			for (int i=0; i < mdbytes.length; i++)
			{
				sb.append(Integer.toString((mdbytes[i] & 0xFF) + 0x100, 16).substring(1));
			}
			
			return sb.toString();
		
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	// file import rules
	public ActionResult importFile(File selectedFile) throws IOException
	{
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		
		InputStream in = new FileInputStream(selectedFile);
		MarcReader reader = new MarcPermissiveStreamReader(in, true, true);
		
		while( reader.hasNext() )
		{
			Record record = reader.next();
			
			String bib_id = "";
			DataField df907 = (DataField) record.getVariableField("907");
			
			if (df907 != null) {
			    Subfield df907a = df907.getSubfield('a');
			    if (df907a != null) {
			        bib_id = df907a.getData();
			    }
			}
			if (bib_id.startsWith(".b")) {bib_id=bib_id.substring(2, 9);}
				
			String key = bib_id;
			Stats stat = Generator.INSTANCE.create(key);
			types.put(stat.key, stat);
			
			String checksum = getChecksum(record.toString());
			stat.setVal(InventoryStatsItem.Hash_Code, checksum);
			
		} // end while loop
		
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
		
	}
	

}
