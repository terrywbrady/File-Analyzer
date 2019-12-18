package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.List;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public interface MetadataInputFile {
        public File getFile();
        public String getValue(IIIFLookup key, String def);
        public String getValue(List<IIIFLookup> keyList, String def, String sep);
        public InputFileType getInputFileType();
        public void setCurrentKey(String key);
        public List<RangePath> getInitRanges(IIIFManifest manifest, RangePath parent, ManifestProjectTranslate manifestTranslate);

}
