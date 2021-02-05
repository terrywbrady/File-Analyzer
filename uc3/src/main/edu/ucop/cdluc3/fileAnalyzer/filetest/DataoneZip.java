package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.ZipFilter;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
		InFiles(StatsItem.makeIntStatsItem("Input Files")),
		OutFiles(StatsItem.makeIntStatsItem("Output Files")),
		InSize(StatsItem.makeLongStatsItem("Input Bytes")),
		OutSize(StatsItem.makeLongStatsItem("Output Bytes"))
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
    	return f.getName().toLowerCase().endsWith(".zip");
    }

	public void initFilters() {
		filters.add(new ZipFilter());
	}
	
	private File outdir;
	
	public InitializationStatus init() {
		outdir = new File(dt.getRoot().getParentFile(), dt.getRoot().getName() + "_output");
		outdir.mkdirs();
		return super.init();
	}
	

	@Override
	public Object fileTest(File f) {
		String key = getKey(f);
		Stats stats = getStats(key);

		byte[] buf = new byte[4096];
		//long bytes = 0;
		
		File outf = new File(outdir, f.getName());
		stats.setVal(DataoneStatsItems.Output, outf.getPath());
		int vcount = 0;
		
		try(
			ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
			ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outf));
		) {
			
			for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
				if (ze.isDirectory()) continue;
				stats.sumVal(DataoneStatsItems.InFiles, 1);
				MerrittZipEntry mze = new MerrittZipEntry(ze.getName());
				if (!mze.includeInOutput()) continue;
				stats.sumVal(DataoneStatsItems.OutFiles, 1);
				vcount = Math.max(vcount, mze.getVersion());
				
				zos.putNextEntry(new ZipEntry(mze.getOutputPath()));
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
		stats.setVal(DataoneStatsItems.OutSize, outf.length());
		stats.setVal(DataoneStatsItems.InSize, f.length());

		return null;
	}

    public Stats createStats(String key){
    	return Generator.INSTANCE.create(key);
    }

    public StatsItemConfig getStatsDetails() {
    	return details;
    }

}
