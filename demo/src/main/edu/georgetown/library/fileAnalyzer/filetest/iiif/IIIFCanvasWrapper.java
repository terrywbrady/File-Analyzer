package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.File;

import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFLookup;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class IIIFCanvasWrapper extends IIIFJSONWrapper implements Comparable<IIIFCanvasWrapper> {
        private String name;
        private String sortName;
        private RangePath parentRange;
        private RangePath displayRange;

        IIIFCanvasWrapper(IIIFManifest manifest, String iiifpath, ManifestDimensions dim, String qualifier) {
                super(manifest.iiifRootPath, manifest.manifestProjectTranslate);
                String canvasid = String.format("%s/Canvas/%s", manifest.iiifRootPath, qualifier);

                setProperty(IIIFType.typeCanvas, IIIFProp.id, canvasid);
                setProperty(IIIFType.typeCanvas); 
                getJSONObject().put(IIIFProp.height.val, dim.height());
                getJSONObject().put(IIIFProp.width.val, dim.width());

                JSONObject image = createImage(iiifpath, dim, qualifier);
                getArray(IIIFArray.images).put(image);
        }

        public String getName() {
                return name;
        }
        public void setName(String name) {
                this.name = name;
        }
        
        public String getSortName() {
                return (sortName == null)  ? name : sortName;
        }

        public void setSortName(String sortName) {
                this.sortName = sortName;
        }
        
        public RangePath getParentRange() {
                return parentRange;
        }
        
        public void setRangePath(RangePath parent) {
                parentRange = parent;
        }
        
        public RangePath getDsiplayRange() {
                return (displayRange == null) ?  parentRange : displayRange;
        }
        
        public void setDisplayRange(RangePath disp) {
                displayRange = disp;
        }

        @Override
        public int compareTo(IIIFCanvasWrapper arg0) {
                return getSortName().compareTo(arg0.getSortName());
        }

        public void addCanvasMetadata(File f, MetadataInputFile itemMeta) {
                setProperty(IIIFType.typeCanvas, IIIFProp.label, itemMeta.getValue(IIIFLookup.Title, f.getName()));
                setProperty(IIIFType.typeCanvas, IIIFProp.title, itemMeta.getValue(IIIFLookup.Title, EMPTY));
                setProperty(IIIFType.typeCanvas, IIIFProp.dateCreated, itemMeta.getValue(IIIFLookup.DateCreated, EMPTY));
                setProperty(IIIFType.typeCanvas, IIIFProp.creator, itemMeta.getValue(IIIFLookup.Creator, EMPTY));
                setProperty(IIIFType.typeCanvas, IIIFProp.description, itemMeta.getValue(IIIFLookup.Description, EMPTY));
                setProperty(IIIFType.typeCanvas, IIIFProp.subject, itemMeta.getValue(IIIFLookup.Subject, EMPTY));
                setProperty(IIIFType.typeCanvas, IIIFProp.rights, itemMeta.getValue(IIIFLookup.Rights, EMPTY));
                String uri = itemMeta.getValue(IIIFLookup.Permalink, EMPTY);
                if (!uri.isEmpty()) {
                        uri = String.format("<a href='%s'>%s</a>", uri, uri);
                }
                setProperty(IIIFType.typeCanvas, IIIFProp.permalink, uri);
        }

        public JSONObject createImage(String iiifpath, ManifestDimensions dim, String qualifier) {
                String imageid = String.format("%s/Image/%s", this.iiifRootPath, qualifier);
                IIIFJSONWrapper image = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                image.setProperty(IIIFType.typeImage, IIIFProp.context);
                image.setProperty(IIIFType.typeImage, IIIFProp.id, imageid); 
                image.setProperty(IIIFType.typeImageAnnotation);
                image.setProperty(IIIFType.typeImage, IIIFProp.motivation);
                image.setProperty(IIIFType.typeImage, IIIFProp.on, String.format("%s/img", this.iiifRootPath));
                image.getJSONObject().put(IIIFProp.resource.val, createImageResource(iiifpath, dim));
                return image.getJSONObject();
        }

        public JSONObject createImageResource(String iiifpath, ManifestDimensions dim) {
                String resid = iiifpath + "/full/full/0/default.jpg";
                IIIFJSONWrapper resource = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                resource.setProperty(IIIFType.typeImageResource, IIIFProp.id, resid); 
                resource.setProperty(IIIFType.typeImageResource);
                resource.setProperty(IIIFType.typeImageResource, IIIFProp.format, "image/jpeg");
                resource.getJSONObject().put(IIIFProp.height.val, dim.height());
                resource.getJSONObject().put(IIIFProp.width.val, dim.width());
                resource.getJSONObject().put(IIIFProp.service.val, createImageService(iiifpath));
                return resource.getJSONObject();
        }

        public JSONObject createImageService(String iiifpath) {
                IIIFJSONWrapper service = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                service.setProperty(IIIFType.typeImageResourceService, IIIFProp.context); 
                service.setProperty(IIIFType.typeImageResourceService, IIIFProp.id, iiifpath); 
                service.setProperty(IIIFType.typeImageResourceService, IIIFProp.profile);    
                return service.getJSONObject();
        }

}
