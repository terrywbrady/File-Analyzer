package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static void zipFolder(File folder) throws FileNotFoundException, IOException {
		File zip = new File(folder.getParentFile(), folder.getName() + ".zip");
		try(ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(zip))) {
	        ZipUtil.zipSubDirectory("", folder, zout);			
		}
	}
	/*
	 * From http://stackoverflow.com/questions/2403830/recursively-zip-a-directory-containing-any-number-of-files-and-subdirectories-in
	 */
	public static void zipSubDirectory(String basePath, File dir, ZipOutputStream zout) throws IOException {
	    byte[] buffer = new byte[4096];
	    File[] files = dir.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            String path = basePath + file.getName() + "/";
	            zout.putNextEntry(new ZipEntry(path));
	            zipSubDirectory(path, file, zout);
	            zout.closeEntry();
	        } else {
	            FileInputStream fin = new FileInputStream(file);
	            zout.putNextEntry(new ZipEntry(basePath + file.getName()));
	            int length;
	            while ((length = fin.read(buffer)) > 0) {
	                zout.write(buffer, 0, length);
	            }
	            zout.closeEntry();
	            fin.close();
	        }
	    }
	}

}
