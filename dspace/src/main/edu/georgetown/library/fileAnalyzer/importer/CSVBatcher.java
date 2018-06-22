package edu.georgetown.library.fileAnalyzer.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.NumberFormat;
import java.util.TreeMap;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.YN;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.importer.DefaultImporter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

/**
 * Abstract class handling the import of a character-delimited text file allowing for individual values to be wrapped by quotation marks.
 * @author TBrady
 *
 */
public class CSVBatcher extends DefaultImporter {
	private static enum BatcherStatsItems implements StatsItemEnum {
		File(StatsItem.makeStringStatsItem("File Name").setWidth(200)),
		Count(StatsItem.makeIntStatsItem("Num items"))
		;
		
		StatsItem si;
		BatcherStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

	public static enum Generator implements StatsGenerator {
		INSTANCE;
		public Stats create(String key) {return new Stats(details, key);}
	}
	static StatsItemConfig details = StatsItemConfig.create(BatcherStatsItems.class);
	public enum SIZE {
		B5(5),
		B10(10),
		B25(25),
		B50(50),
		B100(100),
		B250(250),
		B500(500),
		B1000(1000),
		B2000(2000),
		B5000(5000),
		B10000(10000);
		
		int size;
		SIZE(int x) {
			this.size = x;
		}
	}
	public static final String HEADROW = "HeadRow";
	public static final String BATCHSIZE = "BatchSize";
	public CSVBatcher(FTDriver dt) {
		super(dt);
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), HEADROW, HEADROW,
				"Treat first row as header", YN.values(), YN.Y));
		this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), BATCHSIZE, BATCHSIZE,
				"Batch Size", SIZE.values(), SIZE.B1000));
	}
	boolean forceKey;
	
	public static String KEY = "key";

	public ActionResult importFile(File selectedFile) throws IOException {
		boolean  header = (YN)getProperty(HEADROW) == YN.Y;
		int batchSize = ((SIZE)getProperty(BATCHSIZE)).size;
		BufferedReader br = new BufferedReader(new FileReader(selectedFile));
		String headRow = header ? br.readLine() + "\n" : "";
		int outFileCount = 0;
		int rec = 0;
		int currec = 0;
		
		Timer timer = new Timer();
		TreeMap<String,Stats> types = new TreeMap<String,Stats>();

		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(4);
		nf.setGroupingUsed(false);
    	BufferedWriter bw = null;
		String outFileName = "";
		
		for(String line = br.readLine(); line!=null; line=br.readLine()) {
			if (rec % batchSize == 0) {
				outFileCount++;
				if (bw!=null) {
					bw.close();
					Stats stats = Generator.INSTANCE.create(outFileName);
					stats.setVal(BatcherStatsItems.Count, currec);
					types.put(outFileName, stats);
				}
				outFileName = selectedFile.getName()+"_"+nf.format(outFileCount)+".csv";
				File outFile = new File(selectedFile.getParent(), outFileName);
				currec = 0;
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
				if (header) bw.write(headRow);
			}
			bw.write(line);
			bw.write("\n");
			rec++;
			currec++;
		}
		br.close();
		if (bw!=null) {
			bw.close();
			Stats stats = Generator.INSTANCE.create(outFileName);
			stats.setVal(BatcherStatsItems.Count, currec);
			types.put(outFileName, stats);
		}
		
		return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
	}
	
	public String toString() {
		return "Batch CSV File";
	}
	public String getDescription() {
		return "This rule will break a CSV file into appropriate sized batches while retaining the header row.";
	}
	public String getShortName() {
		return "BatchCSV";
	}
}
