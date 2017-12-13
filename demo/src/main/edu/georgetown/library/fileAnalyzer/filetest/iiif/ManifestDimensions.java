package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TIFF;
import org.apache.tika.parser.image.ImageMetadataExtractor;
import org.xml.sax.SAXException;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.DefaultDimensions;

public class ManifestDimensions {
        int height;
        int width;
        ManifestDimensions(int height, int width) {
                this.height = height;
                this.width = width;
        }                
        ManifestDimensions(File f) throws IOException, SAXException, TikaException {
                Metadata metadata = new Metadata();
                ImageMetadataExtractor ime = new ImageMetadataExtractor(metadata);
                
                this.height = DefaultDimensions.PORTRAIT.dimensions.height;
                this.width = DefaultDimensions.PORTRAIT.dimensions.width;
                if (f.getName().toLowerCase().endsWith("tif") || f.getName().toLowerCase().endsWith("tiff")) {
                        ime.parseTiff(f);
                } else if (f.getName().toLowerCase().endsWith("jpg") || f.getName().toLowerCase().endsWith("jpeg")) {
                        ime.parseJpeg(f);
                } 
                int h = metadata.getInt(TIFF.IMAGE_LENGTH);
                int w = metadata.getInt(TIFF.IMAGE_WIDTH);
                if (h == 0 || w == 0) {
                        return;
                }
                if (h >= w) {
                        this.width = (w * 1000) / h;
                        this.height = 1000;
                } else {
                        this.height = (h * 1000) / w;
                        this.width = 1000;
                }
        }                
        String height() {
                return Integer.toString(height);
        }
        String width() {
                return Integer.toString(width);
        }
}
