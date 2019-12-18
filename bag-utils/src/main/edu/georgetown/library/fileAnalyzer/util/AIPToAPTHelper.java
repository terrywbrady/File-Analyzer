package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AIPToAPTHelper {
    public static final String AIPEXTRACT = "aipextract_";
    public static final String METSXML = "mets.xml";
    StringBuilder errorMessage = new StringBuilder();
    int unique = 0;
    public AIPToAPTHelper(){}
    public static String APTFILE_CHAR_REGEX = "[^a-zA-Z0-9\\.\\-_]";
    public static String APTFILE_REGEX = String.format("^.*%s.*$", APTFILE_CHAR_REGEX);
    public static int APTFILE_MAX = 255;
    
    abstract public int fillBag(File f, APTrustHelper aptHelper) throws FileNotFoundException, IOException, InvalidMetadataException, InvalidFilenameException;
    public int bag(File f, APTrustHelper aptHelper) throws IOException, IncompleteSettingsException, InvalidMetadataException, InvalidFilenameException {
        int count = fillBag(f, aptHelper);
        aptHelper.createBagFile();
        aptHelper.generateBagInfoFiles();
        aptHelper.saveChecksums();
        aptHelper.writeBagFile();
        cleanup();
        return count;
    }
    public void cleanup(){
        //no action by default
    };

    public File testForAptCompliantFilenames(File f, boolean rename) throws InvalidFilenameException {
        this.errorMessage.setLength(0);
        f = testFile(f.getParentFile(), f, rename);
        if (errorMessage.length() != 0) {
            throw new InvalidFilenameException(errorMessage.toString());
        }
        return f;
    }

    
    private File rename(File f) {
        File fnew = new File(f.getParentFile(), f.getName().replaceAll(APTFILE_CHAR_REGEX, "_"));
        if (fnew.exists()) {
            fnew = makeUnique(fnew, 0);
        }
        return fnew;
    }
    
    public static Pattern pExt = Pattern.compile("^(.*)(\\.[^\\.]*)$");
    private File makeUnique(File f, int seq) {
        String name = f.getName();
        Matcher m = pExt.matcher(name);
        if (m.matches()){
            name = String.format("%s%d%s", m.group(1), seq, m.group(2));
        } else {
            name = String.format("%s%d", name, seq);
        }
        File newf = new File(f.getParentFile(), name);
        if (!newf.exists()) {
            return newf;
        }
        return makeUnique(f, ++seq);
    }
    
    private File testFile(File root, File f, boolean rename) {
        Path p = root.toPath().resolve(f.toPath());
        if (p.toString().length() > APTFILE_MAX) {
            errorMessage.append(String.format("File Path too long: [%s]; \n", p.toString()));
        }
        if (f.getName().matches(APTFILE_REGEX)) {
            if (rename) {
                File newf = this.rename(f);
                if (f.renameTo(newf)) {
                    f = newf;                    
                } else {
                    errorMessage.append(String.format("Error renaming [%s] to [%s]", f.getName(), newf.getName()));
                }
            } else {
                errorMessage.append(String.format("Invalid Path:[%s]; \n", f.getName()));                
            }
        }
        if (f.isDirectory()) {
            for(File cf: f.listFiles()) {
                testFile(root, cf, rename);
            }
        } 
        return f;
    }
}
