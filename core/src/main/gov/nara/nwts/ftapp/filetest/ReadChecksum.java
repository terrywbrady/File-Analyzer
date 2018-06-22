package gov.nara.nwts.ftapp.filetest;

import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.NameChecksum.KEYTYPE;
import gov.nara.nwts.ftapp.filter.DefaultFileTestFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.importer.DelimitedFileReader;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

/**
 * Abstract class for rules that report on checksum values; derived versions of this class will provide a specific hashing algorithm from the Java core library.
 * @author TBrady
 *
 */
public class ReadChecksum extends DefaultFileTest {
	
	private static enum ChecksumStatsItems implements StatsItemEnum {
		File(StatsItem.makeStringStatsItem("File", 200)),
		Checksum(StatsItem.makeStringStatsItem("Checksum",300)),
		;
		
		StatsItem si;
		ChecksumStatsItems(StatsItem si) {this.si=si;}
		public StatsItem si() {return si;}
	}
	public static enum Generator implements StatsGenerator {
		INSTANCE;
		class ChecksumStats extends Stats {
			public ChecksumStats(String key) {
				super(details, key);
			}

		}
		public ChecksumStats create(String key) {return new ChecksumStats(key);}

	}
	public static StatsItemConfig details = StatsItemConfig.create(ChecksumStatsItems.class);
	
	class Cache {
		File f;
		String key;
		String checksum;
		
		void cache(File f) {
			if (!f.equals(this.f)) {
				this.f = f;
				try {
				    String sep = ReadChecksum.this.getProperty(SEPARATOR, " *").toString();
					Vector<Vector<String>> data = DelimitedFileReader.parseFile(f, sep);
					if (data.size() > 0) {
						if (data.get(0).size() > 1) {
							checksum = data.get(0).get(0);
							String filekey = data.get(0).get(1);
						    if (((KEYTYPE)getProperty(NameChecksum.KEY)) == KEYTYPE.NAME) {
						        key = filekey;
						    } else {
	                            key = getRelPath(new File(f.getParentFile(), filekey));						        
						    }
						}
					}
				} catch (IOException e) {
				}
			}
		}
	}
	
	Cache cache;
	
	public static final String SEPARATOR = "separator";
	
	public ReadChecksum(FTDriver dt) {
		super(dt);
        this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), NameChecksum.KEY, NameChecksum.KEY,
                "Result Key", KEYTYPE.values(), KEYTYPE.PATH));
        ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  SEPARATOR, SEPARATOR,
                "Separator string between the checksum and the file name", " *"));
		cache = new Cache();
	}

	public String toString() {
		return "Read Checksum";
	}
	public String getKey(File f) {
		cache.cache(f);
		return cache.key;
	}
	
    public String getShortName(){return "Read Checksum";}

	public Object fileTest(File f) {
		Stats stat = getStats(f);
		cache.cache(f);
		stat.setVal(ChecksumStatsItems.Checksum, cache.checksum);
		return cache.checksum;
	}
    public Stats createStats(String key){ 
    	return Generator.INSTANCE.create(key);
    }
    public StatsItemConfig getStatsDetails() {
    	return details; 

    }
    
    class ChecksumFilter extends DefaultFileTestFilter {
    	public String getSuffix() {
    		return ".*\\.(md5|sha1)$";
    	}
    	public boolean isReSuffix() {
    		return true;
    	}
        public String getName(){return "Checksum";}

    	
    }
    
	public void initFilters() {
		filters.add(new ChecksumFilter());
	}

	public String getDescription() {
		return "Read and import checksums from checksum files. Assuems checksum files are formatted as follows\n" +
				"checksum *filename\n" +
				"where <space>* is the delimiter";
	}

}
