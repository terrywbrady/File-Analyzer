package edu.georgetown.library.fileAnalyzer.importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;
import java.util.Vector;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.ftprop.FTPropFile;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

public class MarcSerializer extends DefaultImporter
{
	// name and description of the Marc Serializer importer
	public String toString()
	{
		return "MARC Serializer";
	}
	
	public String getDescription()
	{
		return "This importer will serialize individual bib records from a marc file into one text file per bib. The bib id number will be used to name the output file.\n\nOptionally, a file of bib ids may be provided to the importer to select only specific bibs to be serialized.  If no file is provided, all bibs will be output.";
	}
	
	public String getShortName()
	{
		return "Serializer";
	}

	
	// runtime input file
	public static final String IDFILE = "Bib ID File";
	FTPropFile pBibID;
	
	public MarcSerializer(FTDriver dt)
	{
		super(dt);
		pBibID = new FTPropFile(dt, this.getClass().getSimpleName(), IDFILE, IDFILE, "A file containing a list of bibs (one per line) to serialize (optional):", "");
		this.ftprops.add(pBibID);
	}
	
	
	// read Bib ID file
	public Vector<Vector<String>> readBibIDFile() throws IOException
	{
		File pFile = pBibID.getFile();
		if (pFile == null) return null;
		Vector<Vector<String>> data = DelimitedFileReader.parseFile(pFile, ",", false);
		return data;
	}
	
	
	// resulting information to display
	public static enum SERIALIZED {Bib_Serialized, Bib_Skipped, No_File_Present}
	
	private static enum SerializerStatsItem implements StatsItemEnum
	{
		Bib_ID(StatsItem.makeStringStatsItem("Bib IDs", 100)),
		Title(StatsItem.makeStringStatsItem("Title", 300)),
		Serialized(StatsItem.makeEnumStatsItem(SERIALIZED.class, "Serialized?").setWidth(100)),
		File_Created(StatsItem.makeStringStatsItem("File Created", 600));
		
		StatsItem si;
		
		SerializerStatsItem (StatsItem si)
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
	
	public static StatsItemConfig details = StatsItemConfig.create(SerializerStatsItem.class);
	
	
	// output MARC records as Text
	public void WriteMarcText (String filenamebasetrim, String bibid, Record rec, Stats stat_ser)
	{	
		String filename = filenamebasetrim + "." + bibid + ".txt";
		stat_ser.setVal(SerializerStatsItem.File_Created, "File Created: " + filename);
		
		try
		{
			File file = new File(filename);
			if (!file.exists()) {file.createNewFile();}
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write(rec.toString().trim().replaceAll("\n", "\r\n"));
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
		
	// file import rules
	public ActionResult importFile(File selectedFile) throws IOException
	{
		Timer timer = new Timer();
		TreeMap<String, Stats> types = new TreeMap<String, Stats>();
		
		Vector<Vector<String>> bib_id_vec_from_file = new Vector<Vector<String>>();
		bib_id_vec_from_file = readBibIDFile();
				
		String filename_base = selectedFile.getAbsolutePath();
		String filename_base_trim = filename_base.substring(0, filename_base.lastIndexOf("."));
		
		InputStream in = new FileInputStream(selectedFile);
		MarcReader reader = new MarcPermissiveStreamReader(in, true, true);
			
		if (bib_id_vec_from_file == null)
		{
			while (reader.hasNext())
			{
				Record record = reader.next();
				
				DataField df907 = (DataField) record.getVariableField("907");
				String bib_id = df907.getSubfield('a').getData();
				if (bib_id.startsWith(".b") && bib_id.length()==10) {bib_id=bib_id.substring(2, bib_id.length()-1);}
					
				String key = bib_id;
				Stats stat = Generator.INSTANCE.create(key);
				types.put(stat.key, stat);
				
				DataField df245 = (DataField) record.getVariableField("245");
				String title = df245.getSubfield('a').getData();
				
				stat.setVal(SerializerStatsItem.Title, title);
				stat.setVal(SerializerStatsItem.Serialized, SERIALIZED.No_File_Present);
								
				WriteMarcText(filename_base_trim, bib_id, record, stat);
			
			}  // end of while loop
		}  // end of if
		
		else if (bib_id_vec_from_file != null)
		{
			while(reader.hasNext())
			{
				Record record = reader.next();
				
				DataField df907 = (DataField) record.getVariableField("907");
				String bib_id = df907.getSubfield('a').getData();
				if (bib_id.startsWith(".b") && bib_id.length()==10) {bib_id=bib_id.substring(2, bib_id.length()-1);}
				
				DataField df245 = (DataField) record.getVariableField("245");
				String title = df245.getSubfield('a').getData();
				
				String key = bib_id;
				Stats stat = Generator.INSTANCE.create(key);
				types.put(stat.key, stat);
				
				stat.setVal(SerializerStatsItem.Title, title);
				stat.setVal(SerializerStatsItem.Serialized, SERIALIZED.Bib_Skipped);
				stat.setVal(SerializerStatsItem.File_Created, "No File Created");
				
				for (Vector<String> row : bib_id_vec_from_file)
				{
					String bib_id_from_file = (String) row.get(0);
					if (bib_id_from_file.startsWith(".b") && bib_id_from_file.length()==10) {bib_id_from_file = bib_id_from_file.substring(2, bib_id_from_file.length()-1);}
					
					if (bib_id_from_file.equals(bib_id))
					{
						stat.setVal(SerializerStatsItem.Serialized, SERIALIZED.Bib_Serialized);
						WriteMarcText(filename_base_trim, bib_id, record, stat);
						break;
					}
				}
								
			}  // end of while loop
		}  // end of else if
	
		
	return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());

	}  // end of ActionResult

}