package edu.georgetown.library.fileAnalyzer.filetest.iiif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.georgetown.library.fileAnalyzer.filetest.iiif.MetadataInputFileBuilder.InputFileType;

class RESTResponseInputFile extends DefaultInput {
        private String url = "";
        private HashMap<String,String> currentMetadata = new HashMap<>();;
        public RESTResponseInputFile(String url) throws InputFileException {
                this.url = url;
                if (url.isEmpty()) throw new InputFileException("The DSpace REST API cannot be null when REST lookup is configured");
        }
        
        @Override
        public InputFileType getInputFileType() {
                return InputFileType.REST;
        }
        
        @Override
        public void setCurrentKey(String s) {
                currentMetadata = new HashMap<>();
                String url = String.format("%s/handle/%s", this.url, s.replace('-', '/'));
                StringBuilder sb = new StringBuilder();
                try {
                        URI uri = new URIBuilder(url).addParameter("expand", "metadata").build();
                        //System.out.println(uri.toString());
                        HttpURLConnection con= (HttpURLConnection) uri.toURL().openConnection();
                        
                        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()))){
                                for(String line=br.readLine(); line != null; line = br.readLine()) {
                                        sb.append(line);
                                }
                        }
                } catch (URISyntaxException|IOException e) {
                        e.printStackTrace();
                }
                try {
                        if (!sb.toString().isEmpty()) {
                                JSONObject jresp = new JSONObject(sb.toString());
                                if (jresp.get("metadata") instanceof JSONArray){
                                        JSONArray arr = (JSONArray)jresp.get("metadata");
                                        for(int i=0; i<arr.length(); i++) {
                                                JSONObject jobj = arr.getJSONObject(i);
                                                String k = jobj.getString("key");
                                                String v = jobj.getString("value");
                                                if (k != null && v != null) {
                                                        String cv = currentMetadata.get(k);
                                                        cv = (cv == null) ? v : String.format("%s; %s", cv, v);
                                                        currentMetadata.put(k, cv);
                                                }
                                                
                                        }
                                }
                        }
                } catch (JSONException e) {
                        e.printStackTrace();
                }  
        }
        

        @Override
        public String getValue(IIIFLookup key, String def){
                String skey = key.getFileTypeKey(getInputFileType());
                String val = currentMetadata.get(skey);
                if (val == null) {
                        return def;
                }
                return val.isEmpty() ? def : val;
        }

}
