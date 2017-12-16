package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

public final class IIIFEnums {
        public enum MethodIdentifer {
                FolderName, FileName, ItemMetadataFile;
        }
        public enum MethodMetadata {
                None, ItemMetadataFile, ManifestMetadataFile, RestAPI;
        }

        public static enum IIIFType {
                typeManifest("sc:Manifest"),
                typeRange("sc:Range"),
                typeSequence("sc:Sequence"),
                typeCanvas("sc:Canvas"),
                typeImage("na"),
                typeImageResource("dctypes:Image"),
                typeImageResourceService("na"),
                typeImageAnnotation("oa:Annotation");                

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
                identifier,
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
                title("Title", true), 
                dateCreated("Date Created", true), 
                creator("Creator", true), 
                description("Description", true),
                subject("Subject(s)", true),
                rights("Rights", true), 
                permalink("Permanent URL", true),
                motivation {
                        String getDefault() {
                                return "sc:painting";
                        }
                },
                on,
                resource,
                service;

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
                Title(
                        "Title", 
                        "title",
                        null,
                        "/ead:ead/ead:archdesc/ead:did/ead:unittitle"),
                Attribution("Attribution"),
                Identifier("identifier"),
                DateCreated(
                        "Date Created", 
                        "date",
                        "created",
                        "/ead:ead/ead:archdesc/ead:did/ead:unitdate"
                ),
                Creator(
                        "Creator", 
                        "creator"
                ), 
                Description(
                        "Description", 
                        "description"
                ),
                Subject(
                        "Subject(s)",
                        "subject",
                        "*"
                ),
                Rights(
                        "Rights", 
                        "rights"
                ), 
                Permalink(
                        "Permanent URL", 
                        "identifier",
                        "uri"
                );
                String property = null;
                String dc = null;
                String metsXpath = null; 
                String eadXPath = null;
                String dcXPath = null;
                IIIFLookup(String property) {
                        this(property, null, null);
                }
                IIIFLookup(String property, String dcelem) {
                        this(property, dcelem, null, null);
                }
                IIIFLookup(String property, String dcelem, String dcqual) {
                        this(property, dcelem, dcqual, null);
                }
                IIIFLookup(String property, String dcelem, String dcqual, String eadXPath) {
                        this.property  = property;
                        dcelem = (dcelem == null) ? "" : dcelem;
                        dcqual = (dcqual == null) ? "" : dcqual;
                        if (!dcelem.isEmpty()) {
                                if (dcqual.isEmpty()) {
                                        this.dc = String.format("dc.%s", dcelem);
                                        this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s'][@qualifier='none']", dcelem);
                                        this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s'][not(@qualifier)]", dcelem);
                                } else if (dcqual.equals("*")) {
                                        this.dc = String.format("dc.%s", dcelem);
                                        this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s']", dcelem);
                                        this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s']", dcelem);
                                } else {
                                        this.dc = String.format("dc.%s.%s", dcelem, dcqual);
                                        this.dcXPath = String.format("/dublin_core/dcvalue[@element='%s'][@qualifier='%s']", dcelem, dcqual);
                                        this.metsXpath = String.format("//mets:mdWrap[@OTHERMDTYPE='DIM']//dim:field[@element='%s'][@qualifier='%s']", dcelem, dcqual);
                                }
                        }
                        this.eadXPath  = eadXPath;
                }
                String getFileTypeKey(InputFileType fileType) {
                        if (fileType == InputFileType.METS) {
                                return this.metsXpath;
                        } else if (fileType == InputFileType.EAD) {
                                return this.eadXPath;
                        } else if (fileType == InputFileType.DC) {
                                return this.dcXPath;
                        } else if (fileType == InputFileType.CSV) {
                                return this.dc;
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
