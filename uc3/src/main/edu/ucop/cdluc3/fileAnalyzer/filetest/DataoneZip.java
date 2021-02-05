package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import edu.georgetown.library.fileAnalyzer.filetest.IngestInventory.Generator;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class DataoneZip extends DefaultFileTest { 
	private static enum DataoneStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Input", 200)),
		Output(StatsItem.makeStringStatsItem("Output", 200)),
        VerCount(StatsItem.makeIntStatsItem("Ver Count")),
		FileCount(StatsItem.makeIntStatsItem("File Count"))
		;
		
		StatsItem si;
		DataoneStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

    public static enum Generator implements StatsGenerator {
        INSTANCE;

        public Stats create(String key) {return new Stats(details, key);}
    }
	public static StatsItemConfig details = StatsItemConfig.create(DataoneStatsItems.class);

    public DataoneZip(FTDriver dt) {
        super(dt);
    }

	public String getKey(File f) {
		return f.getName().replaceAll("\\.zip$", "");
	}
    public String toString() {
        return "Data One Zip Preparation";
    }
    public String getShortName(){return "DataOne";}
    
    public String getDescription() {
        return "This rule will process a DataOne Merritt Object download for ingest into Dryad";
    }
    
    @Override public boolean isTestDirectory() {
    	return false;
    }
    @Override public boolean processRoot() {
        return false;
    }

    @Override public boolean isTestFiles() {
        return true; 
    }

    @Override public boolean isTestable(File f) {
    	if (f.getName().startsWith("output.")) {
    		return false;
    	}
    	return f.getName().toLowerCase().endsWith(".zip");
    }

	public void initFilters() {
		filters.add(new ZipFilter());
	}
	
	public static Pattern zipEntryParser() {
		return Pattern.compile("^([^\\/]+)/(\\d+)/(producer|system)/(.*)");
	}
	public static Pattern zipEntryParserManifest() {
		return Pattern.compile("^([^\\/]+)/(manifest.xml)");
	}

	public Matcher manifestMatch(String ze) {
		return DataoneZip.zipEntryParserManifest().matcher(ze);
	}

	public Matcher match(String ze) {
		return DataoneZip.zipEntryParser().matcher(ze);
	}
	
	public boolean includeInOutput(String ze) {
		Matcher m = match(ze);
		if (m.matches()) {
			if (m.group(3).equals("producer")) {
				return true;
			}
			if (m.group(4).equals("mrt-dataone-map.rdf")) {
				return true;
			}
		}
		return manifestMatch(ze).matches();
	}
	
	public String outputPath(String ze) {
		Matcher m = match(ze);
		if (m.matches()) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.group(1).replaceAll("[\\+=]+", "_"));
			sb.append("/");
			sb.append(m.group(2));
			sb.append("/");
			sb.append(m.group(4));
			return sb.toString();
		}
		m = manifestMatch(ze);
		if (m.matches()) {
			StringBuilder sb = new StringBuilder();
			sb.append(m.group(1).replaceAll("[\\+=]+", "_"));
			sb.append("/");
			sb.append(m.group(2));
			return sb.toString();
		}
		return "na";
	}

	public int ver(String ze) {
		Matcher m = match(ze);
		if (m.matches()) {
			return Integer.parseInt(m.group(2));
		}
		return 0;
	}

	public String status(String ze) {
		return String.format("%s\t%s\t%b", ze, outputPath(ze), includeInOutput(ze));
	}
	

	@Override
	public Object fileTest(File f) {
		String key = getKey(f);
		Stats stats = getStats(key);

		File zout = new File("/tmp", key);
		zout.mkdir();
		byte[] buf = new byte[4096];
		//long bytes = 0;
		
		String outname = "output." + f.getName();
		stats.setVal(DataoneStatsItems.Output, outname);
		File outf = new File(f.getParentFile(), outname);
		int fcount = 0;
		int vcount = 0;
		
		try(
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outf));
		) {
			
			for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
				if (ze.isDirectory()) continue;
				String zn = ze.getName();
				if (!includeInOutput(zn)) continue;
				fcount++;
				vcount = Math.max(vcount, ver(zn));
				
				zos.putNextEntry(new ZipEntry(outputPath(zn)));
				int length;
	            while ((length = zis.read(buf)) > 0) {
	                zos.write(buf, 0, length);
	            }
	            zos.closeEntry();
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		stats.setVal(DataoneStatsItems.VerCount, vcount);
		stats.setVal(DataoneStatsItems.FileCount, fcount);

		return null;
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }

    public StatsItemConfig getStatsDetails() {
    	return details;
    }

}
