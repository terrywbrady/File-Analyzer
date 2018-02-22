package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class IIIFCanvasWrapper extends IIIFJSONWrapper implements Comparable<IIIFCanvasWrapper> {
        private String name;
        private String sortName;
        private RangePath parentRange;
        private RangePath displayRange;

        IIIFCanvasWrapper(IIIFManifest manifest, String iiifpath, ManifestDimensions dim, String qualifier) {
                super(manifest.iiifRootPath, manifest.manifestProjectTranslate);
                String canvasid = String.format("%s/Canvas/%s", manifest.iiifRootPath, qualifier);

                setProperty(IIIFType.typeCanvas, IIIFStandardProp.id, canvasid);
                setProperty(IIIFType.typeCanvas); 
                getJSONObject().put(IIIFStandardProp.height.getLabel(), dim.height());
                getJSONObject().put(IIIFStandardProp.width.getLabel(), dim.width());

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

       public JSONObject createImage(String iiifpath, ManifestDimensions dim, String qualifier) {
                String imageid = String.format("%s/Image/%s", this.iiifRootPath, qualifier);
                IIIFJSONWrapper image = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                image.setProperty(IIIFType.typeImage, IIIFStandardProp.context);
                image.setProperty(IIIFType.typeImage, IIIFStandardProp.id, imageid); 
                image.setProperty(IIIFType.typeImageAnnotation);
                image.setProperty(IIIFType.typeImage, IIIFStandardProp.motivation);
                image.setProperty(IIIFType.typeImage, IIIFStandardProp.on, String.format("%s/img", this.iiifRootPath));
                image.getJSONObject().put(IIIFStandardProp.resource.getLabel(), createImageResource(iiifpath, dim));
                return image.getJSONObject();
        }

        public JSONObject createImageResource(String iiifpath, ManifestDimensions dim) {
                String resid = iiifpath + "/full/full/0/default.jpg";
                IIIFJSONWrapper resource = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                resource.setProperty(IIIFType.typeImageResource, IIIFStandardProp.id, resid); 
                resource.setProperty(IIIFType.typeImageResource);
                resource.setProperty(IIIFType.typeImageResource, IIIFStandardProp.format, "image/jpeg");
                resource.getJSONObject().put(IIIFStandardProp.height.getLabel(), dim.height());
                resource.getJSONObject().put(IIIFStandardProp.width.getLabel(), dim.width());
                resource.getJSONObject().put(IIIFStandardProp.service.getLabel(), createImageService(iiifpath));
                return resource.getJSONObject();
        }

        public JSONObject createImageService(String iiifpath) {
                IIIFJSONWrapper service = new IIIFJSONWrapper(this.iiifRootPath, this.manifestProjectTranslate);
                service.setProperty(IIIFType.typeImageResourceService, IIIFStandardProp.context); 
                service.setProperty(IIIFType.typeImageResourceService, IIIFStandardProp.id, iiifpath); 
                service.setProperty(IIIFType.typeImageResourceService, IIIFStandardProp.profile);    
                return service.getJSONObject();
        }

}
