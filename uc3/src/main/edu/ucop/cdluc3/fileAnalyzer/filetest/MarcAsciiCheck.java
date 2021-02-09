package edu.ucop.cdluc3.fileAnalyzer.filetest;

import gov.nara.nwts.ftapp.ActionResult;
import gov.nara.nwts.ftapp.FTDriver;
import gov.nara.nwts.ftapp.Timer;
import gov.nara.nwts.ftapp.filetest.DefaultFileTest;
import gov.nara.nwts.ftapp.filter.MrcFilter;
import gov.nara.nwts.ftapp.ftprop.FTPropString;
import gov.nara.nwts.ftapp.ftprop.InitializationStatus;
import gov.nara.nwts.ftapp.importer.Importer;
import gov.nara.nwts.ftapp.stats.Stats;
import gov.nara.nwts.ftapp.stats.StatsGenerator;
import gov.nara.nwts.ftapp.stats.StatsItem;
import gov.nara.nwts.ftapp.stats.StatsItemConfig;
import gov.nara.nwts.ftapp.stats.StatsItemEnum;
import gov.nara.nwts.ftapp.YN;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.marc4j.marc.VariableField;

public class MarcAsciiCheck extends DefaultFileTest implements Importer
{
    public String toString()
    {
        return "MARC Ascii Check";
    }
    
    public String getDescription()
    {
        return "Check which fields can be encoded to ascii";
        
    }
    
    public String getShortName()
    {
        return "MarcAscii";
    }

    
    // resulting information to display
    private static enum InventoryStatsItem implements StatsItemEnum
    {
        Key(StatsItem.makeStringStatsItem("Key", 100)),
        File(StatsItem.makeStringStatsItem("File", 100).makeFilter(true)),
        Item_ID(StatsItem.makeStringStatsItem("Item Num", 100).makeFilter(true)),
        Tag(StatsItem.makeStringStatsItem("Tag", 100).makeFilter(true)),
        Ascii(StatsItem.makeEnumStatsItem(YN.class, "Ascii")),
        Text(StatsItem.makeStringStatsItem("Text", 300)),
        NonAscii(StatsItem.makeStringStatsItem("NonAscii")),
        ;
        
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
    
    public MarcAsciiCheck(FTDriver dt)
    {
        super(dt);
    }

    int ftni = 0;
    
    // file import rules
    public ActionResult importFile(File selectedFile) throws IOException
    {
        Timer timer = new Timer();
        TreeMap<String, Stats> types = new TreeMap<String, Stats>();
        ftni = 0;
        processFile(selectedFile, types);
        
        return new ActionResult(selectedFile, selectedFile.getName(), this.toString(), details, types, true, timer.getDuration());
        
    }
    
    public Stats createTagRecord(TreeMap<String, Stats> types, File selectedFile, String item, String tag, String text) {
        String key = String.format("%s %s", item, tag);
        Stats stat = Generator.INSTANCE.create(key);
        types.put(stat.key, stat);
        stat.setVal(InventoryStatsItem.File, selectedFile.getName());
        stat.setVal(InventoryStatsItem.Item_ID, item);
        stat.setVal(InventoryStatsItem.Tag, tag);
        stat.setVal(InventoryStatsItem.Text, text);
        boolean b = Charset.forName("US-ASCII").newEncoder().canEncode(text);
        stat.setVal(InventoryStatsItem.Ascii, b ? YN.Y : YN.N);        
        stat.setVal(InventoryStatsItem.NonAscii, b ? "" : text.replaceAll("[\\x00-\\x7F]", ""));        
        
    	return stat;
    }

    public void processFile(File selectedFile, TreeMap<String, Stats> types) throws IOException
    {
        InputStream in = new FileInputStream(selectedFile);
        MarcReader reader = new MarcPermissiveStreamReader(in, true, true);
        
        while( reader.hasNext() )
        {
            Record record = reader.next();
            
            String item_id = "ni"+(ftni++);
            
            DataField df20 = (DataField) record.getVariableField("020");
            
            if (df20 != null) {
                Subfield df20a = df20.getSubfield('a');
                if (df20a != null) {
                    item_id = df20a.getData();
                }
            }
                 
            for(DataField df: record.getDataFields()) {
            	for(Subfield sf: df.getSubfields()) {
                    String tag = String.format("%s %s", df.getTag(), sf.getCode());
                    createTagRecord(types, selectedFile, item_id, tag, sf.getData());
            	}
            }
            
           
        } // end while loop.
    }

    @Override
    public Object fileTest(File f) {
        try {
            processFile(f, this.dt.types);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void refineResults() {
        this.dt.types.remove(DEFKEY);
		getStatsDetails().createFilters(this.dt.types);
    }
    @Override
    public InitializationStatus initValidate(File refFile) {
        return init();
    }

    @Override
    public boolean allowForceKey() {
        return false;
    }
    
    public void initFilters() {
        filters.add(new MrcFilter());
    }
    public StatsItemConfig getStatsDetails() {
        return MarcAsciiCheck.details;
    }
    
    public static final String DEFKEY = "";
    public String getKey(File f) {
        return DEFKEY;
    }

    public static void main(String[] arg) {
    	String in = "Hello Piñatas €";
    	System.out.println(in.replaceAll("[\\x00-\\x7F]", ""));
    }
    
    
}
