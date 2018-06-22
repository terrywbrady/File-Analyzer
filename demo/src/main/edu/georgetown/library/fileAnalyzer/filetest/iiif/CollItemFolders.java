package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookupEnum;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class CollItemFolders extends DefaultManifestProjectTranslate {
        public static Pattern pImage = Pattern.compile(".*\\.(tiff?|jpg|jpeg|jp2)$");
        public static FilenameFilter filter = new FilenameFilter(){
                @Override
                public boolean accept(File arg0, String arg1) {
                        return pImage.matcher(arg1.toLowerCase()).matches();
                }
        };
        @Override
        public File getCollComponentRootAncestor(File root, File f) {
                return f;
        }
        
        @Override
        public String getCollComponentRootLabel(File root, File f, MetadataInputFile curMeta) {
                String s = curMeta.getValue(IIIFLookupEnum.Title.getLookup(), f.getParentFile().getName());
                return this.translate(IIIFType.typeCollection, IIIFStandardProp.label, s);
        }

        @Override
        public String getCollManifestLabel(File root, File f, MetadataInputFile curMeta) {
                String s = curMeta.getValue(IIIFLookupEnum.Title.getLookup(), f.getParentFile().getName());
                return this.translate(IIIFType.typeCollection, IIIFStandardProp.label, s);
        }

        @Override
        public RangePath getPrimaryRangePath(IIIFManifest manifest, String key, File f, MetadataInputFile itemMeta) {
                return RangePath.makeEmptyRangePath(manifest);
        }

}
