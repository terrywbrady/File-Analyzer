package edu.ucop.cdluc3.fileAnalyzer.filetest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract all metadata fields from a TIF or JPG using categorized tag defintions.
 * @author TBrady
 *
 */
class MerrittZipEntry { 
	private static Pattern pZipEntry = Pattern.compile("^([^/]+)/(\\d+)/(.*)");
	private static Pattern pZipInput = Pattern.compile("^([^\\/]+)/(\\d+)/(producer|system)/(.*)");
	private static Pattern pManifest = Pattern.compile("^([^\\/]+)/(manifest.xml)");

	
	private String ze;
	private String ark = "";
	private FileType ft = FileType.OTHER;
	private String filename = "";
	private String normFilename = "";
	private int ver;

	public enum FileType {SYSTEM, PRODUCER, OTHER}
	
	public static String normalizeArk(String s) {
		return s.replaceAll("[\\+=]+", "_").replaceFirst("ark_", "ark:/").replaceAll("_", "/");
	}
	
	public MerrittZipEntry(String entry) {
		this.ze = entry;
		Matcher m = pZipInput.matcher(entry);
		if (m.matches()) {
			ark = normalizeArk(m.group(1));
			ver = Integer.parseInt(m.group(2));
			if (m.group(3).equals("system")) {
				ft = FileType.SYSTEM;
			} else if (m.group(3).equals("producer")) {
				ft = FileType.PRODUCER;
			} else {
				ft = FileType.OTHER;
			}
			normFilename = m.group(4);
			filename = String.format("%s/%s", m.group(3), m.group(4));
			return;
		} 
		m = pManifest.matcher(entry);
		if (m.matches()) {
			ark = normalizeArk(m.group(1));
			ver = 0;
			ft = FileType.OTHER;
			filename = m.group(2);
			normFilename = m.group(2);
			return;
		} 
		m = pZipEntry.matcher(entry);
		if (m.matches()) {
			ark = normalizeArk(m.group(1));
			ver = Integer.parseInt(m.group(2));
			ft = ver == 0 ? FileType.OTHER : FileType.PRODUCER;
			filename = m.group(3);
			normFilename = m.group(3);			
		}
	}

	public FileType getFileType() {
		return ft;
	}

	public String getArk() {
		return ark;
	}

	public int getVersion() {
		return ver;
	}

	public String getFilename() {
		return filename;
	}

	public String toString() {
		return ze;
	}
	
	public boolean includeInOutput() {
		if (ft == FileType.SYSTEM) {
			return normFilename.equals("mrt-dataone-map.rdf");
		}
		return true;
	}
	
	public String getOutputPath() {
		return String.format(
			"%s/%d/%s", 
			ark.replaceAll("[:/]+", "_"), 
			ver, 
			includeInOutput() ? normFilename : filename
		);
	}
}
