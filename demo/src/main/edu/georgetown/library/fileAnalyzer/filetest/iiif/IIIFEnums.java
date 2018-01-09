package edu.georgetown.library.fileAnalyzer.filetest.iiif;

public final class IIIFEnums {
        public enum MethodIdentifer {
                FolderName, FileName, ItemMetadataFile;
        }
        public enum MethodMetadata {
                None, ItemMetadataFile, ManifestMetadataFile, RestAPI;
        }

        public static enum IIIFType {
                typeCollection("sc:Collection"),
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
                ranges,
                manifests;
                String getLabel() {
                        return name();
                }
        }
        
        
        
        public static enum IIIFStandardProp implements IIIFProp {
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
                        public String getDefault() {
                                return "http://iiif.io/api/presentation/2/context.json";
                        }
                },
                logo,
                profile {
                        public String getDefault() {
                                return "http://iiif.io/api/image/2/level2.json";
                        }
                },
                motivation {
                        public String getDefault() {
                                return "sc:painting";
                        }
                },
                on,
                resource,
                service;

                private String val;
                IIIFStandardProp() {
                        this.val = name();
                }
                IIIFStandardProp(String val) {
                        this.val = val;
                }
               public String getLabel() {
                        return val;
                }
                public String getDefault() {
                        return "";
                }

                public boolean isMetadata() {
                        return false;
                }
        }

        public static enum IIIFMetadataProp implements IIIFProp {
                title("Title"), 
                dateCreated("Date Created"), 
                creator("Creator"), 
                description("Description"),
                subject("Subject(s)"),
                rights("Rights"), 
                permalink("Permanent URL");

                private String val;
                IIIFMetadataProp() {
                        this.val = name();
                }
                IIIFMetadataProp(String val) {
                        this.val = val;
                }
                public String getLabel() {
                        return val;
                }
                public String getDefault() {
                        return "";
                }

                public boolean isMetadata() {
                        return true;
                }
        }
 
        public static enum IIIFLookupEnum {
                Title(
                        new IIIFLookup(
                                "Title", 
                                "title",
                                null,
                                "/ead:ead/ead:archdesc/ead:did/ead:unittitle"
                        )
                ),
                Attribution(new IIIFLookup("Attribution")),
                Identifier(new IIIFLookup("identifier")),
                DateCreated(
                        new IIIFLookup(
                                "Date Created", 
                                "date",
                                "created",
                                "/ead:ead/ead:archdesc/ead:did/ead:unitdate"
                        )
                ),
                Creator(new IIIFLookup("Creator", "creator")), 
                Description(new IIIFLookup("Description", "description")),
                Subject(new IIIFLookup("Subject(s)", "subject")),
                SubjectOther(new IIIFLookup("Subject Other", "subject", "other")),
                SubjectLcsh(new IIIFLookup("Subject(s)", "subject", "lcsh")),
                Rights(new IIIFLookup("Rights", "rights")), 
                Permalink(new IIIFLookup("Permanent URL", "identifier", "uri"));
                
                private IIIFLookup lookup;
                IIIFLookupEnum(IIIFLookup lookup) {
                        this.lookup = lookup;
                }
                
                public IIIFLookup getLookup() {
                        return lookup;
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
