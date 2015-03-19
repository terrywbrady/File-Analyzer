package gov.nara.nwts.ftapp.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLUtil {
	public static DocumentBuilderFactory dbf;
	public static DocumentBuilder db;
	public static DocumentBuilderFactory dbf_ns;
	public static DocumentBuilder db_ns;
	public static TransformerFactory tf;
	public static XPathFactory xf;
	public static XPath xp;
	
	static {
		try {
			dbf = DocumentBuilderFactory.newInstance();
			db = dbf.newDocumentBuilder();
			dbf_ns = DocumentBuilderFactory.newInstance();
			dbf_ns.setNamespaceAware(true);
			db_ns = dbf_ns.newDocumentBuilder();
			tf = TransformerFactory.newInstance();
			xf = XPathFactory.newInstance();
			xp = xf.newXPath();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	public static void serialize(Document d, File f) {
		try {
			doSerialize(d, f);
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void doSerialize(Document d, File f) throws TransformerException, IOException {
		Transformer t = tf.newTransformer();
		FileOutputStream fos = new FileOutputStream(f);
		StreamResult sr = new StreamResult(fos);
		t.transform(new DOMSource(d), sr);
		fos.close();
	}

	public static void doTransform(Document d, File f, InputStream is) throws TransformerException, IOException {
		doTransform(d,f,is,new HashMap<String,Object>());
	}
	public static void doTransform(Document d, File f, InputStream is, HashMap<String,Object> pmap) throws TransformerException, IOException {
		Transformer t = tf.newTransformer(new StreamSource(is));
		try(FileOutputStream fos = new FileOutputStream(f)){
			StreamResult sr = new StreamResult(fos);
			for(String s:pmap.keySet()) {
				t.setParameter(s, pmap.get(s));
			}
			t.transform(new DOMSource(d), sr);			
		}
	}

	public static void doTransform(Document d, File f, String xsl) throws TransformerException, IOException {
		doTransform(d, f, FileAnalyzerURIResolver.INSTANCE, xsl, new HashMap<String,Object>());		
	}
	public static void doTransform(Document d, File f, String xsl, HashMap<String,Object> pmap) throws TransformerException, IOException {
		doTransform(d, f, FileAnalyzerURIResolver.INSTANCE, xsl, pmap);		
	}
	public static Node doTransformToDom(Document d, String xsl) throws TransformerException, IOException {
		return doTransformToDom(d, xsl, new HashMap<String,Object>());
	}
	public static Node doTransformToDom(Document d, String xsl, HashMap<String,Object> pmap) throws TransformerException, IOException {
		TransformerFactory tfres = TransformerFactory.newInstance();

		tfres.setURIResolver(FileAnalyzerURIResolver.INSTANCE);

		Transformer t = tfres.newTransformer(FileAnalyzerURIResolver.INSTANCE.resolve(xsl, ""));

		DOMResult dr = new DOMResult();
		for(String s:pmap.keySet()) {
			t.setParameter(s, pmap.get(s));
		}
		t.transform(new DOMSource(d), dr);
		return dr.getNode();
	}

	public static void doTransform(Document d, File f, URIResolver urir, String xsl) throws TransformerException, IOException {
		doTransform(d, f, urir, xsl, new HashMap<String,Object>());		
	}
	public static void doTransform(Document d, File f, URIResolver urir, String xsl, HashMap<String,Object> pmap) throws TransformerException, IOException {
		TransformerFactory tfres = TransformerFactory.newInstance();

		tfres.setURIResolver(urir);

		Transformer t = tfres.newTransformer(urir.resolve(xsl, ""));
		try(FileOutputStream fos = new FileOutputStream(f)){
		    StreamResult sr = new StreamResult(fos);
		    for(String s:pmap.keySet()) {
			    t.setParameter(s, pmap.get(s));
		    }
		    t.transform(new DOMSource(d), sr);
		}
	}
}
