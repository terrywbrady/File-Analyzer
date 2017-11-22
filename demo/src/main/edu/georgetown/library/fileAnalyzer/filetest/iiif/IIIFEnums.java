package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public final class IIIFEnums {
        public enum MethodIdentifer {
                FolderName, MetadataFile;
        }
        public enum MethodMetadata {
                None, MetadataFile, RestAPI;
        }

        public static enum IIIFType {
                typeManifest("sc:Manifest"),
                typeRange("sc:Range"),
                typeSequence("sc:Sequence"),
                typeCanvas("sc:Canvas"),
                typeImage("dctypes:Image"),
                typeAnnotation("oa:Annotation");                

                String val;
                IIIFType(String val) {
                        this.val = val;
                }
                String getValue() {
                        return val;
                }
        }
        
        public static enum IIIFArray {
                metadata,
                structures,
                sequences,
                canvases,
                images,
                ranges;                
                String getLabel() {
                        return name();
                }
        }
        
        public static enum IIIFProp {
                label,
                attribution,
                value,
                format,
                id("@id"),
                height,
                width,
                type("@type"),
                context("@context") {
                        String getDefault() {
                                return "http://iiif.io/api/presentation/2/context.json";
                        }
                },
                logo,
                profile {
                        String getDefault() {
                                return "http://iiif.io/api/image/2/level2.json";
                        }
                },
                dateCreated("Date Created", true), 
                creator("Creator", true), 
                description("Description", true),
                subject("Subject(s)", true),
                rights("Rights", true), 
                permalink("Permanent URL", true);

                String val;
                boolean isMetadata = false;
                IIIFProp() {
                        this.val = name();
                }
                IIIFProp(String val) {
                        this.val = val;
                }
                IIIFProp(String val, boolean isMetadata) {
                        this.val = val;
                        this.isMetadata = isMetadata;
                }
                String getLabel() {
                        return val;
                }
                String getDefault() {
                        return "";
                }

        }
        
        public static enum IIIFLookup {
                Title("title", "//dim:field[@element='title']",""),
                Attribution("attribution", null, null),
                Identifier("identifier",null, null),
                DateCreated("Date Created", "//dim:field[@element='date'][@qualifier='created']"), 
                Creator("Creator", "//dim:field[@element='creator']"), 
                Description("Description", "//dim:field[@element='description'][not(@qualifier)]"),
                Subject("Subject(s)", "//dim:field[@element='subject']"),
                Rights("Rights", "//dim:field[@element='rights']"), 
                Permalink("Permanent URL", "//dim:field[@element='identifier'][@qualifier='uri']");
                String property = null;
                String metsXpath = null; 
                String eadXPath = null;
                IIIFLookup(String property) {
                        this(property, null, null);
                }
                IIIFLookup(String property, String metsXpath) {
                        this(property, metsXpath, null);
                }
                IIIFLookup(String property, String metsXpath, String eadXPath) {
                        this.property = property;
                        this.metsXpath = metsXpath;
                        this.eadXPath = eadXPath;
                }
                String getFileTypeKey(InputFileType fileType) {
                        if (fileType == InputFileType.METS) {
                                return this.metsXpath;
                        } else if (fileType == InputFileType.EAD) {
                                return this.eadXPath;
                        }
                        return property;
                }
        }
        
        public enum DefaultDimensions {
                PORTRAIT(1000,700), LANDSCAPE(750,1000);
                
                ManifestDimensions dimensions;
                DefaultDimensions(int height, int width) {
                        dimensions = new ManifestDimensions(height, width);
                }
                
        }

}
