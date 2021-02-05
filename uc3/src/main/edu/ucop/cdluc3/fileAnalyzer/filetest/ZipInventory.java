package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class ZipInventory extends DefaultFileTest { 
	private static enum ZipStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Key", 200)),
		File(StatsItem.makeStringStatsItem("File", 200)),
		ZipEntry(StatsItem.makeStringStatsItem("Zip Entry", 200)),
		Checksum(StatsItem.makeStringStatsItem("Checksum", 200))
		;
		
		StatsItem si;
		ZipStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}

    public static enum Generator implements StatsGenerator {
        INSTANCE;

        public Stats create(String key) {return new Stats(details, key);}
    }
	public static StatsItemConfig details = StatsItemConfig.create(ZipStatsItems.class);

    public ZipInventory(FTDriver dt) {
        super(dt);
    }

	public String getKey(File f) {
		return f.getName().replaceAll("\\.zip$", "");
	}
    public String toString() {
        return "Zip Inventory";
    }
    public String getShortName(){return "ZipInv";}
    
    public String getDescription() {
        return "Inventory the contents of a set of zip files";
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
    	return f.getName().toLowerCase().endsWith(".zip");
    }

	public void initFilters() {
		filters.add(new ZipFilter());
	}
	
	@Override
	public Object fileTest(File f) {
		try(
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		) {
			
			for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
				if (ze.isDirectory()) continue;
				String key = String.format("%s:%s", f.getName(), ze.getName());
				Stats stats = Generator.INSTANCE.create(key);
				dt.types.put(key, stats);
				stats.setVal(ZipStatsItems.File, f.getName());
				stats.setVal(ZipStatsItems.ZipEntry, ze.getName());
				
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            DigestInputStream dis = new DigestInputStream(zis, md);
	            byte[] buffer = new byte[1024];
	            int read = dis.read(buffer);
	            while (read > -1) {
	                read = dis.read(buffer);
	            }
				stats.setVal(ZipStatsItems.Checksum, Arrays.toString(dis.getMessageDigest().digest()));
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}

		return null;
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }

    public StatsItemConfig getStatsDetails() {
    	return details;
    }

}
