package edu.georgetown.library.fileAnalyzer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.FileUtils;

public class TarUtil {
	public static File tarFolder(File folder) throws FileNotFoundException, IOException {
		File tarout = new File(folder.getParentFile(), folder.getName() + ".tar");
		try(TarArchiveOutputStream tar = new TarArchiveOutputStream(new FileOutputStream(tarout))) {
		    tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			TarArchiveEntry arch = new TarArchiveEntry(folder.getName() + "/");
			tar.putArchiveEntry(arch);
	        TarUtil.tarSubDirectory(folder.getName()+"/", folder, tar);			
		}
		return tarout;
	}

	public static File tarFolderAndDeleteFolder(File folder) throws FileNotFoundException, IOException {
		File out = tarFolder(folder);
		FileUtils.deleteDirectory(folder);
		return out;
	}
	
	public static void tarSubDirectory(String basePath, File dir, TarArchiveOutputStream tar) throws IOException {
	    byte[] buffer = new byte[4096];
	    File[] files = dir.listFiles();
	    for (File file : files) {
	        if (file.isDirectory()) {
	            String path = basePath + file.getName() + "/";
	            TarArchiveEntry arch = new TarArchiveEntry(path); 
	            tar.putArchiveEntry(arch);
	            tarSubDirectory(path, file, tar);
	        } else {
	        	String path = basePath + file.getName();
	            TarArchiveEntry arch = new TarArchiveEntry(path); 
	            arch.setSize(file.length());
	            tar.putArchiveEntry(arch);
	            try(FileInputStream fin = new FileInputStream(file)) {
		            int length;
		            while ((length = fin.read(buffer)) > 0) {
		                tar.write(buffer, 0, length);
		            }	            	
	            }
                tar.closeArchiveEntry();
	        }
	    }
	}
	
	public static File untar(File f) throws IOException {
		Path temp = Files.createTempDirectory(f.getName());
		temp.toFile().deleteOnExit();
		TarArchiveInputStream taris = new TarArchiveInputStream(new FileInputStream(f));
		
		for(TarArchiveEntry entry = taris.getNextTarEntry(); entry != null; entry =  taris.getNextTarEntry()){
			Path fentry = temp.resolve(entry.getName());
			if (entry.isDirectory()) {
		        Files.createDirectory(fentry);
			} else {
			    if (fentry.getParent() != null) {
			        Files.createDirectories(fentry.getParent());
			    }
		        Files.copy(taris, fentry);
			}
			fentry.toFile().deleteOnExit();
		}
		return temp.toFile();
	}
}