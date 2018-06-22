package edu.georgetown.library.fileAnalyzer.cmd;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.georgetown.library.fileAnalyzer.util.AIPDirToAPTHelper;
import edu.georgetown.library.fileAnalyzer.util.AIPToAPTHelper;
import edu.georgetown.library.fileAnalyzer.util.AIPZipToAPTHelper;
import edu.georgetown.library.fileAnalyzer.util.APTrustHelper;
import edu.georgetown.library.fileAnalyzer.util.APTrustHelper.Access;
import edu.georgetown.library.fileAnalyzer.util.IncompleteSettingsException;
import edu.georgetown.library.fileAnalyzer.util.InvalidFilenameException;
import edu.georgetown.library.fileAnalyzer.util.InvalidMetadataException;

public class AIPToAPTCmd {
    public AIPToAPTCmd(){}
    
    public static final void fail(String message) {
        System.err.println(message);
        System.exit(FAIL);
    }
    
    private static enum CONVTYPE {ZIP,DIR;}
    public static final String CMD = "AIPToAPTCmd";
    public static final int FAIL = 100;
    
    public static File testInputFile(CONVTYPE convType, String name) {
        File input = new File(name);
        if (!input.exists()) {
            fail(String.format("File (%s) does not exist", name));            
        }

        if (convType == CONVTYPE.ZIP) {
            if (!name.toLowerCase().endsWith(".zip")) {
                usage();
                fail(String.format("AIP_Zip file (%s) must end with .zip", name));
            }
            if (!input.isFile()) {
                usage();
                fail(String.format("AIP_Zip File (%s) must be a regular file", name));            
            }            
        } else {
            if (!input.isDirectory()) {
                usage();
                fail(String.format("AIP_Dir (%s) must be a directory", name));            
            }                        
        }
        return input;
    }
    
    public static final int convertCommand(CommandLine cmdLine) throws IOException, IncompleteSettingsException, InvalidMetadataException, InvalidFilenameException {
        CONVTYPE convType;
        AIPToAPTHelper aipHelper = null;
        if (cmdLine.hasOption("zip")) {
            convType = CONVTYPE.ZIP;
            File temp = AIPZipToAPTHelper.createTempDir();
            aipHelper = new AIPZipToAPTHelper(temp);
        } else {
            convType = CONVTYPE.DIR;
            aipHelper = new AIPDirToAPTHelper();
        }
        Access access = Access.Institution;
        if (cmdLine.hasOption("c")) {
            access = Access.Consortia;
        }
        if (cmdLine.hasOption("i")) {
            access = Access.Institution;
        }
        if (cmdLine.hasOption("r")) {
            access = Access.Restricted;
        }
        String srcOrg = cmdLine.getOptionValue("srcorg","SrcOrg");
        String sendId = cmdLine.getOptionValue("srcorg","SendId");
        String minstr = cmdLine.getOptionValue("min", "1");
        boolean allowRename = cmdLine.hasOption("rename");
        
        if (cmdLine.getArgs().length == 0){
            usage();
            fail("Specify the name of the AIP file/folder");            
        }
        
        File input = testInputFile(convType, cmdLine.getArgs()[0]);
        APTrustHelper aptHelper = new APTrustHelper(input, allowRename);
        aptHelper.setAccessType(access);
        aptHelper.setInstitutionId(sendId);
        aptHelper.setSourceOrg(srcOrg);
        aptHelper.setBagCount(1);
        aptHelper.setBagTotal(1);
        
        int minCount = 1;
        try {
            minCount = Integer.parseInt(minstr);            
        } catch(NumberFormatException e) {
            fail("Option -min must be numberic");
        }

        int count = aipHelper.bag(input, aptHelper);
        if (count == 0) {
            fail(String.format("No items written to bag file (%s)", aptHelper.getFinalBagName()));
        } else if (count < minCount) {
            fail(String.format("Bag file (%s) must have at least (%d) files", aptHelper.getFinalBagName(), minCount));
        }
        System.out.println(String.format("Bag Complete: %d item(s) written to bag (%s)", count, aptHelper.getFinalBagName()));
        
        if (cmdLine.hasOption("compareFile")) {
            File compareFile = new File(cmdLine.getOptionValue("compareFile"));
            Map<String, String> errors = aptHelper.compareChecksums(compareFile);
            if (!errors.isEmpty()) {
                StringBuilder buf = new StringBuilder();
                buf.append("Checksum comparison failure.\n");
                for(String s: errors.values()) {
                    buf.append(s);
                    buf.append(".\n");
                }
                fail(buf.toString());
            } else {
                System.out.println(String.format("Checksum Compare File Successful using file %s", compareFile.getAbsolutePath())); 
            }
        }
        return count;
    }
    
    
    public static final void main(String[] args) {
        CommandLine cmdLine = parseAipCommandLine(CMD, args);
        try {
            convertCommand(cmdLine);
        } catch (IOException | IncompleteSettingsException | InvalidMetadataException | InvalidFilenameException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void usage() {
        System.out.println(String.format("%s -dir (-consortia|-institution|-restricted) -srcorg SrcOrg [-min 1] [-rename] [-compareFile <file>] <AIP_Dir>", CMD));
        System.out.println(String.format("%s -zip (-consortia|-institution|-restricted) -srcorg SrcOrg [-min 1] [-rename] [-compareFile <file>] <AIP_Zip>", CMD));
    }
    
    public static CommandLine parseAipCommandLine(String main, String[] args) {
        DefaultParser clParse = new DefaultParser();
        Options opts = new Options();
        OptionGroup optGrp = new OptionGroup();
        optGrp.addOption(new Option("consortia","Access=Consortia"));
        optGrp.addOption(new Option("institution","Access=Institution"));
        optGrp.addOption(new Option("restricted","Access=Restricted"));
        optGrp.setRequired(true);
        opts.addOptionGroup(optGrp);
        OptionGroup optGrp2 = new OptionGroup();
        optGrp2.addOption(new Option("dir","Bag AIP Directory"));
        optGrp2.addOption(new Option("zip","Bag AIP Zip"));
        optGrp2.setRequired(true);
        opts.addOptionGroup(optGrp2);
        opts.addOption("srcorg", true, "Src Organization");
        opts.getOption("srcorg").setRequired(true);
        opts.addOption("min", true, "Min number of files requried");
        opts.addOption("rename", false, "Allow source files to be renamed");
        opts.addOption("compareFile", true, "Optional Checksum Compare File.  Comma Separated: MD5,path");
        opts.addOption("h", false, "Help Info");
        
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmdLine = clParse.parse(opts, args);
            if (cmdLine.hasOption("h")) {
                usage();
                formatter.printHelp(CMD, opts);
                System.exit(0);
            }
            return cmdLine;
        } catch (ParseException e) {
            usage();
            formatter.printHelp(CMD, opts);
            fail("Invalid Options");
        }
        return null;
    }
}
