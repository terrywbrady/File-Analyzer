package edu.georgetown.library.fileAnalyzer.filetest;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import edu.georgetown.library.fileAnalyzer.filetest.SwapDelimitedColumns.Generator.ColStats;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.CSVFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropEnum;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;

/*
 * This code is included for illustrative purposes.  It is not currently active in the application.
 * More robust file overwriting controls are needed.
 */
public class SwapDelimitedColumns extends DefaultFileTest {
        static final String DELIM = "delim";
        static final String COLA = "ColA";
        static final String COLB = "ColB";
        String cNameA = "";
        String cNameB = "";
        
        public static enum Sep{CSV, TSV;}
        public static enum Status{Updated, Failed, ColNotFound;}
        private static enum ColStatsItems implements StatsItemEnum {
                Key(StatsItem.makeStringStatsItem("File", 200)),
                Status(StatsItem.makeEnumStatsItem(Status.class, "Status")),
                Note(StatsItem.makeStringStatsItem("Message", 200)),
                ;
                
                StatsItem si;
                ColStatsItems(StatsItem si) {this.si=si;}
                public StatsItem si() {return si;}
        }

        public static enum Generator implements StatsGenerator {
                INSTANCE;
                class ColStats extends Stats {
                        public ColStats(String key) {
                                super(details, key);
                        }

                }
                public ColStats create(String key) {return new ColStats(key);}
        }
        public static StatsItemConfig details = StatsItemConfig.create(ColStatsItems.class);
        
        public SwapDelimitedColumns(FTDriver dt) {
                super(dt);
                this.ftprops.add(new FTPropEnum(dt, this.getClass().getName(), DELIM, DELIM,
                                "Delimiter character separating fields", Sep.values(), Sep.CSV));
                ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  COLA, COLA,
                                "Column A to Swap", ""));
                ftprops.add(new FTPropString(dt, this.getClass().getSimpleName(),  COLB, COLB,
                                "Column B to Swap", ""));
        }

        
        
        @Override
        public InitializationStatus init() {
                InitializationStatus is = super.init();
                cNameA = getProperty(COLA, "").toString();
                cNameB = getProperty(COLB, "").toString();
                if (cNameA.isEmpty() || cNameB.isEmpty()) {
                        is.addFailMessage("ColA and ColB cannot be blank");
                        return is;
                }
                if (cNameA.equals(cNameB)) {
                        is.addFailMessage("ColA and ColB should not be equal");
                }
                return is;
        }

        
        @Override
        public String getDescription() {
                return "Swap Delimited File Columns\n\nTHIS RULE WILL OVERWRITE FILES.  MAKE A BACKUP.";
        }

        @Override
        public String toString() {
                return "Swap CSV Columns";
        }

        @Override
        public Object fileTest(File f) {
                Stats stats = getStats(f);
                Sep sep = (Sep)getProperty(DELIM);
                CSVFormat csvf = (sep == Sep.TSV) ? CSVFormat.TDF : CSVFormat.DEFAULT;
                try(FileReader fr = new FileReader(f)) {
                        CSVParser cp = CSVParser.parse(f, Charset.forName("UTF-8"), csvf.withHeader());
                        Map<String,Integer> headers = cp.getHeaderMap();
                        Integer cola = headers.get(cNameA);
                        if (cola == null) {
                                stats.setVal(ColStatsItems.Status, Status.ColNotFound);
                                stats.setVal(ColStatsItems.Note, String.format("Col [%s] not found", cNameA));
                                return Status.ColNotFound;
                        }
                        Integer colb = headers.get(cNameB);
                        if (colb == null) {
                                stats.setVal(ColStatsItems.Status, Status.ColNotFound);
                                stats.setVal(ColStatsItems.Note, String.format("Col [%s] not found", cNameB));
                                return Status.ColNotFound;
                        }

                        cp = CSVParser.parse(f, Charset.forName("UTF-8"), csvf);
                        ArrayList<ArrayList<String>> data = new ArrayList<>();
                        ArrayList<String> row = new ArrayList<>();
                        
                        for(CSVRecord rec: cp.getRecords()) {
                                row = new ArrayList<>();
                                data.add(row);
                                for(int i=0; i<rec.size(); i++) {
                                        int col = i;
                                        if (col == cola) {
                                                col = colb;
                                        } else if (col == colb) {
                                                col = cola;
                                        }
                                        row.add(rec.get(col));
                                }
                        }
                        
                        try(
                                FileWriter fw = new FileWriter(f);
                                CSVPrinter cprint = new CSVPrinter(fw, csvf);
                        ) {
                                
                                cprint.printRecords(data);
                        }
                        stats.setVal(ColStatsItems.Status, Status.Updated);
                        stats.setVal(ColStatsItems.Note, String.format("Record Count [%06d]", data.size()));
                        
                } catch (IOException e) {
                        stats.setVal(ColStatsItems.Status, Status.Failed);
                        stats.setVal(ColStatsItems.Note, e.getMessage());
                } 
                
                return Status.Updated;
        }

        @Override
        public ColStats createStats(String key){ 
                return Generator.INSTANCE.create(key);
        }

        @Override
        public StatsItemConfig getStatsDetails() {
                return details; 
        }

        @Override
        public String getShortName() {
                return "SwapCol";
        }

        public void initFilters() {
                filters.add(new CSVFilter());
        }

        @Override
        public String getKey(File f) {
                return f.getPath();
        }
}
