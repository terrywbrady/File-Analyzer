package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

abstract class DefaultInput implements MetadataInputFile {
        InputFileType fileType;
        @Override
        public String getValue(IIIFLookup key, String def){
                return def;
        }
        @Override
        public File getFile() {
                return null;
        }
        @Override
        public InputFileType getInputFileType() {
                return fileType;
        }
        @Override
        public void setCurrentKey(String s) {
                //no action except for CSV
        }
        @Override
        public List<RangePath> getInitRanges(IIIFManifest manifest, RangePath parent, ManifestProjectTranslate manifestTranslate) {
                return new ArrayList<RangePath>();
        }
 }

