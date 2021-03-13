package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import gov.nara.nwts.ftapp.YN;


import edu.ucop.cdluc3.fileAnalyzer.filetest.MerrittZipEntry.FileType;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class ZipInventory extends DefaultFileTest { 
	private static enum ZipStatsItems implements StatsItemEnum {
		Key(StatsItem.makeStringStatsItem("Key", 300)),
		Type(StatsItem.makeEnumStatsItem(FileType.class, "Type")),
		Include(StatsItem.makeEnumStatsItem(YN.class, "Include")),
		Ark(StatsItem.makeStringStatsItem("Ark", 200)),
		Version(StatsItem.makeIntStatsItem("Ver").setWidth(30)),
		Filename(StatsItem.makeStringStatsItem("File", 200)),
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
	
	private static String DEFKEY = "";
	public String getKey(File f) {
		return DEFKEY;
	}
	
	@Override
    public void refineResults() {
        this.dt.types.remove(DEFKEY);
    }
	
	@Override
	public Object fileTest(File f) {
		//String relpath = this.getRelPath(f);
		try(
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
		) {
			
			for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
				if (ze.isDirectory()) continue;
				MerrittZipEntry mze = new MerrittZipEntry(ze.getName());
				String key = mze.getOutputPath();
				Stats stats = Generator.INSTANCE.create(key);
				dt.types.put(key, stats);
				stats.setVal(ZipStatsItems.Type, mze.getFileType());
				stats.setVal(ZipStatsItems.Ark, mze.getArk());
				stats.setVal(ZipStatsItems.Version, mze.getVersion());
				stats.setVal(ZipStatsItems.Filename, mze.getFilename());
				stats.setVal(ZipStatsItems.Include, mze.includeInOutput() ? YN.Y : YN.N);
				
	            MessageDigest md = MessageDigest.getInstance("MD5");
				byte[] dataBytes = new byte[1204];
				int nread = 0;
				while((nread = zis.read(dataBytes)) != -1){
					md.update(dataBytes, 0, nread);
				}
				byte[] mdbytes = md.digest();
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<mdbytes.length; i++){
					sb.append(Integer.toString((mdbytes[i] & 0xFF) + 0x100, 16).substring(1));
				}
				stats.setVal(ZipStatsItems.Checksum, sb.toString());
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
