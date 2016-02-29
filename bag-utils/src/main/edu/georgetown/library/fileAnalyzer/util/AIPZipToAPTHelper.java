package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import gov.loc.repository.bagit.Bag;

public class AIPZipToAPTHelper extends AIPToAPTHelper {

    File outdir;
    public AIPZipToAPTHelper(File outdir) {
        this.outdir = outdir;
    }
    
    public static File createTempDir() throws IOException {
        Path outpath = Files.createTempDirectory(AIPToAPTHelper.AIPEXTRACT);
        File outdir = outpath.toFile();
        outdir.deleteOnExit();
        return outdir;        
    }

    @Override public int fillBag(File f, APTrustHelper aptHelper) throws FileNotFoundException, IOException, InvalidMetadataException{
        byte[] buf = new byte[4096];
        File zout = outdir;
        Bag bag = aptHelper.getBag();
        int count = 0;

        try(
            ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
        ) {
            for(ZipEntry ze = zis.getNextEntry(); ze != null; ze = zis.getNextEntry()){
                if (ze.getName().startsWith("__MACOSX")) continue;
                if (ze.getName().endsWith(".DS_Store")) continue;
                
                File ztemp = new File(ze.getName());
                File zeout = new File(zout, ztemp.getName());
                if (ze.isDirectory()) continue;
                
                try(FileOutputStream fos = new FileOutputStream(zeout)) {
                    for(int i=zis.read(buf); i > -1; i=zis.read(buf)) {
                        fos.write(buf, 0, i);
                    }
                }
                
                if (ze.getName().equals(METSXML)) {
                    aptHelper.parseMetsFile(zeout);
                }
                
                bag.addFileToPayload(zeout);
                count++;
            }
        }
        return count;
    }

}
