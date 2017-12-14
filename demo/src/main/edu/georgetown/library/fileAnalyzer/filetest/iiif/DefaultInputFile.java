package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

abstract class DefaultInputFile extends DefaultInput {
        File file;
        @Override
        public File getFile() {
                return file;
        }
        DefaultInputFile(File file) {
                this.file = file;
        }
 }
