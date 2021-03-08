package edu.ucop.cdluc3.fileAnalyzer.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tika.parser.txt.CharsetDetector;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

public class ZipAnalyzer extends DefaultImporter
{
	// name and description of the Marc Serializer importer
	public String toString()
	{
		return "Zip Analyzer";
	}
	
	public String getDescription()
	{
		return "Zip file TOC.";
	}
	
	public String getShortName()
	{
		return "Zip";
	}

	
	public ZipAnalyzer(FTDriver dt)
	{
		super(dt);
	}
	
	// resulting information to display
	public static enum EntryProp {A, B;}
	
	private static enum ZipStatsItem implements StatsItemEnum
	{
		Key(StatsItem.makeStringStatsItem("Key", 300)),
		Size(StatsItem.makeLongStatsItem("Size")),
		Date(StatsItem.makeStringStatsItem("Date")),
		Comment(StatsItem.makeStringStatsItem("Comment")),
		Charset(StatsItem.makeStringStatsItem("Charset")),
		Extra(StatsItem.makeStringStatsItem("Extra")),
		Property(StatsItem.makeEnumStatsItem(EntryProp.class, "Prop").setWidth(100));
		
		StatsItem si;
		
		ZipStatsItem (StatsItem si)
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
	
	public static StatsItemConfig details = StatsItemConfig.create(ZipStatsItem.class);
	
	
	// file import rules
	public ActionResult importFile(File selectedFile) throws IOException
	{
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		DateFormat df = DateFormat.getDateTimeInstance();
		
		try(
				ZipInputStream zis = new ZipInputStream(new FileInputStream(selectedFile));
			) {
				
				for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
					if (ze.isDirectory()) continue;
					String key = ze.getName();
					Stats stats = Generator.INSTANCE.create(key);
					stats.setVal(ZipStatsItem.Size, ze.getSize());
					stats.setVal(ZipStatsItem.Comment, ze.getComment());
					stats.setVal(ZipStatsItem.Extra, ze.getExtra());
					Date d = new Date(ze.getTime());
					stats.setVal(ZipStatsItem.Date, df.format(d));
					CharsetDetector detector = new CharsetDetector();
					detector.setText(key.getBytes());
					stats.setVal(ZipStatsItem.Charset, detector.detect().toString());
					types.put(key, stats);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}  // end of ActionResult

}