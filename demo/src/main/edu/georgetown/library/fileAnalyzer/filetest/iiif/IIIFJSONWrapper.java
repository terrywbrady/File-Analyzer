package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFArray;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFStandardProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFMetadataProp;
import edu.georgetown.library.fileAnalyzer.filetest.iiif.IIIFEnums.IIIFType;

public class IIIFJSONWrapper {
        protected JSONObject jsonObject;
        protected String iiifRootPath;
        public static final String EMPTY = "";
        protected ManifestProjectTranslate manifestProjectTranslate;
        
        public ManifestProjectTranslate getManifestProjectTranslate() {
                return manifestProjectTranslate;
        }
        
        public JSONObject getJSONObject() {
                return jsonObject;
        }
        
        public JSONObject getMinimalJSONObject() {
                JSONObject j = new JSONObject();
                j.put(IIIFStandardProp.id.getLabel(), jsonObject.get(IIIFStandardProp.id.getLabel()));
                j.put(IIIFStandardProp.type.getLabel(), jsonObject.get(IIIFStandardProp.type.getLabel()));
                j.put(IIIFStandardProp.label.getLabel(), jsonObject.get(IIIFStandardProp.label.getLabel()));
                return j;
        }
        
        IIIFJSONWrapper(String iiifRootPath) {
                this(iiifRootPath, DefaultManifestProjectTranslateEnum.Default.getTranslator());
        }
        IIIFJSONWrapper(String iiifRootPath, ManifestProjectTranslate manifestProjectTranslate) {
                jsonObject = new JSONObject();
                this.iiifRootPath = iiifRootPath;
                this.manifestProjectTranslate = manifestProjectTranslate;
        }
       
        public void setProperty(IIIFType type, IIIFProp prop) {
                setProperty(type, prop, prop.getDefault());
        }
        public void setProperty(IIIFType type, IIIFProp prop, String value) {
                value = manifestProjectTranslate.translate(type, prop, value);
                
                if (value.equals(EMPTY)) {
                        return;
                }
                
                if (prop == IIIFMetadataProp.title && type == IIIFType.typeManifest) {
                        addMetadata(prop.getLabel(), value);
                        jsonObject.put(prop.getLabel(), value);                        
                } else if (prop.isMetadata()) {
                        addMetadata(prop.getLabel(), value);
                } else {
                        jsonObject.put(prop.getLabel(), value);
                }
        }
        public void setProperty(IIIFType type) {
                jsonObject.put(IIIFStandardProp.type.getLabel(), type.getValue());
        }

        public String getProperty(IIIFProp prop, String defValue) {
                String ret = null;
                if (prop.isMetadata()) {
                        JSONArray jarr = getArray(IIIFArray.metadata);
                        if (jarr == null) {
                                return defValue;
                        }
                        for(int i = 0; i < jarr.length(); i++) {
                                if (jarr.get(i) instanceof JSONObject) {
                                        JSONObject obj = jarr.getJSONObject(i);
                                        if (prop.getLabel().equals(obj.getString(IIIFStandardProp.label.getLabel()))) {
                                                ret = obj.getString(IIIFStandardProp.value.getLabel());
                                        }
                                } else if (jarr.get(i) instanceof HashMap){
                                        HashMap<String,String> map = (HashMap<String,String>)jarr.get(i);
                                        for(String key: map.keySet()) {
                                                if (key.equals(IIIFStandardProp.label.getLabel())) {
                                                        ret = map.get(key);
                                                        break;
                                                }
                                        }
                                }
                        }
                } else {
                        if (jsonObject.has(prop.getLabel())) {
                                ret = jsonObject.getString(prop.getLabel());
                        }
                }
                return ret == null ? defValue : ret;
        }

        public int getIntProperty(IIIFProp prop, int defValue) {
                if (jsonObject.has(prop.getLabel())) {
                        return jsonObject.getInt(prop.getLabel());
                }
                return defValue;
        }

        
        public JSONArray getArray(IIIFArray iiifarr) {
                String arrlabel = iiifarr.getLabel();
                JSONArray arr = null;
                if (jsonObject.has(arrlabel)) {
                        arr = jsonObject.getJSONArray(arrlabel);
                } else {
                        arr = new JSONArray();
                        jsonObject.put(arrlabel, arr);
                }
                return arr;
        }

        public void addMetadata(String label, String value) {
                JSONArray metadata = getArray(IIIFArray.metadata);
                Map<String,String> m = new HashMap<>();
                m.put(IIIFStandardProp.label.name(), label);
                m.put(IIIFStandardProp.value.name(), value);
                metadata.put(m);
        }

}
