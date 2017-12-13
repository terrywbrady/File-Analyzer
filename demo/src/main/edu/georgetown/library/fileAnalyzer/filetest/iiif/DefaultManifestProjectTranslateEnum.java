package edu.georgetown.library.fileAnalyzer.filetest.iiif;


public enum DefaultManifestProjectTranslateEnum implements ManifestProjectTranslateEnum {
        Default,
        ByCreationDate {
                public ManifestProjectTranslate getTranslator() {
                        return new CreateDateProjectTranslate();
                }
        },
        ByFolderName {
                public ManifestProjectTranslate getTranslator() {
                        return new FileSystemProjectTranslate();
                }
        },
        EADFolderMap {
                public ManifestProjectTranslate getTranslator() {
                        return new EADFolderTranslate();
                }
        }
        ;

        @Override
        public ManifestProjectTranslate getTranslator() {
                return new DefaultManifestProjectTranslate();
        }
}
