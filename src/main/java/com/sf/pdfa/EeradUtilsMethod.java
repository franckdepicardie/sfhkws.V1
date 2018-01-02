package com.sf.pdfa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Properties;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.xml.xmp.XmpWriter;


import java.io.File;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import com.itextpdf.text.DocumentException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import sun.misc.BASE64Decoder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.HashMap;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.xml.xmp.XmpWriter;
import com.itextpdf.text.pdf.PdfDocument;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.Document;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.itextpdf.text.pdf.PdfCopy;
import java.util.ArrayList;
import java.nio.file.*;
import com.sf.pdfa.*;


public class EeradUtilsMethod {
	
	
	static String pdfPageCount(File file) throws IOException {
		
		System.out.println("pdfPageCount - HERE1");
	    RandomAccessFile raf = new RandomAccessFile(file, "r");
	    System.out.println("pdfPageCount - HERE2");
	    RandomAccessFileOrArray pdfFile = new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(raf));
	    System.out.println("pdfPageCount - HERE3");
	    PdfReader reader = new PdfReader(pdfFile, new byte[0]);
	    System.out.println("pdfPageCount - HERE4");
	    Integer pages = reader.getNumberOfPages();
	    System.out.println("pdfPageCount - HERE5");
	    reader.close();
	    System.out.println("pdfPageCount - pages.toString():" + pages.toString());
	    return pages.toString();
	 }
	
	
	 static void copyFileUsingStream(InputStream tmpIn, File dest) throws IOException {

		OutputStream os = null;

		try {
			os = new FileOutputStream(dest);
			byte[] buffer = new byte[1024];
			int length;

			while ((length = tmpIn.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			tmpIn.close();
			os.close();
		}
	}
	
	static void setPdfMetadata(String uploadedPreFileLocation, String uploadedFileLocation, String ZoneSign) throws IOException, DocumentException {
	
		System.out.println("setPdfMetadata ------------------------------------------------ ");
		System.out.println("setPdfMetadata - uploadedPreFileLocation: " + uploadedPreFileLocation);
		System.out.println("setPdfMetadata - uploadedFileLocation: " + uploadedFileLocation);
		System.out.println("setPdfMetadata - ZoneSign: " + ZoneSign);
		
		try {
			
				// read existing pdf document
		        PdfReader reader = new PdfReader(uploadedPreFileLocation);
		        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(uploadedFileLocation));

		        // get and edit meta-data
		        HashMap<String, String> info = reader.getInfo();
		        info.put("Title", uploadedFileLocation);
		        info.put("Creator", ZoneSign);

		        // add updated meta-data to pdf
		        stamper.setMoreInfo(info);

		        // update xmp meta-data
		        ByteArrayOutputStream baos = new ByteArrayOutputStream();
		        XmpWriter xmp = new XmpWriter(baos, info);
		        xmp.close();
		        stamper.setXmpMetadata(baos.toByteArray());
		        stamper.close();   
		        
		        
		        // CHECK ALL IS DONE
				File fileRef = new File(uploadedFileLocation);
				System.out.println(">> setPdfMetadata - uploadedFileLocation - exists : " + fileRef.exists());
		        
		        System.out.println("setPdfMetadata - finished");

		} catch (IOException e) {

			e.printStackTrace();
		}
	}
	
	
	// ZONE SIGNATURE
	static String getSignatureZone(String fileName) throws IOException {
		
		System.out.println("getSignatureZone ------------------------------------------------ ");
		System.out.println("getSignatureZone - fileName: " + fileName);
		
		String docType = "";
		
		if (fileName.contains("CONVENTION_PREUVE")) docType = "CONVENTION_PREUVE";
		if (fileName.contains("SYNTHESE_ECHANGE")) docType = "SYNTHESE_ECHANGE";
		if (fileName.contains("PRODUIT_CG")) docType = "PRODUIT_CG";
		if (fileName.contains("PRODUIT_CP")) docType = "PRODUIT_CP";
		if (fileName.contains("CRS")) docType = "CRS";
		if (fileName.contains("BAD_CG")) docType = "BAD_CG";
		if (fileName.contains("BAD_CP")) docType = "BAD_CP";
		if (fileName.contains("BROCHURE_TARIFAIRE")) docType = "BROCHURE_TARIFAIRE";
		
		Properties prop = new Properties();
		String propFileName = "ZONE_SIGNATURE.properties";
		InputStream tmpIs = EeradUtilsMethod.class.getClassLoader().getResourceAsStream(propFileName);
		
		System.out.println("getSignatureZone - docType: " + docType);
		
		if (docType != null) {
		
			System.out.println("getSignatureZone with filename - docType: " + fileName + " - " + docType);
			
			try {
					if (tmpIs != null) {
						prop.load(tmpIs);
					} else {
						throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
					}
			
					if (fileName.contains("_CP") || fileName.contains("SYNTHESE_ECHANGE") ) { // Dynamics Sign Zone cases
						
						System.out.println("getSignatureZone - generating DYNAMIC_SIGN_ZONE");
						String count = EeradUtilsMethod.pdfPageCount(new File(fileName));
						System.out.println("getSignatureZone - count: " + count);
						
						String hauteurSignZone = "";
						String dynamicSignZone = "";
						if (fileName.contains("BAD_CP")) {
							hauteurSignZone = prop.getProperty("HAUTEUR_BAD_CP");
							dynamicSignZone = prop.getProperty("DYNAMIC_SIGN_ZONE_CP");
						}
						if (fileName.contains("PRODUIT_CP")) {
							hauteurSignZone = prop.getProperty("HAUTEUR_PRODUIT_CP");
							dynamicSignZone = prop.getProperty("DYNAMIC_SIGN_ZONE_CP");
						}
						if (fileName.contains("SYNTHESE_ECHANGE")) {
							hauteurSignZone = prop.getProperty("HAUTEUR_SYNTHESE_ECHANGE");
							dynamicSignZone = prop.getProperty("DYNAMIC_SIGN_ZONE_SY");
						}
						
						System.out.println("getSignatureZone - hauteurSignZone: " + hauteurSignZone);
						
						dynamicSignZone = dynamicSignZone.replace("S", count);
						dynamicSignZone = dynamicSignZone.replace("H", hauteurSignZone);
						return dynamicSignZone;
					}
					
					// Elsewhere, we are in a fixed Sign Zone cases (fixed nnumber of page)
					System.out.println("getSignatureZone - prop.getProperty(docType): " + prop.getProperty(docType));
					
					return prop.getProperty(docType);
					
				}
			catch (Exception e) {
				System.out.println("Exception: " + e);
			}
		}
		
		return null;
	}
	
	static void writeToFile(InputStream uploadedInputStream, String uploadedPreFileLocation)
			throws IOException, DocumentException {

		try {

			OutputStream out = new FileOutputStream(new File(uploadedPreFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
			out.flush();
			out.close();
		
			System.out.println(">> createPdfA - writeToFile - End");

		} catch (IOException e) {

			e.printStackTrace();
		}

	}
		
	static void writeLog(String log) {
		
		System.out.println(">> ----------------------- log: " + log);
	}
	
	static boolean isUserAuthenticated(String authString) throws IOException {
        
		writeLog(authString);
		
        String decodedAuth = "";
        String[] authParts = authString.split("\\s+");
        String authInfo = authParts[1];
        
        writeLog(authInfo);
        
        byte[] bytes = null;
        try {
            bytes = ( new BASE64Decoder().decodeBuffer(authInfo));
        } catch (IOException e) {
            
            e.printStackTrace();
        }
        decodedAuth = new String(bytes);
        String[] tab = decodedAuth.split(":");
        String u = tab[0];
        String p = tab[1];
        
        InputStream tmpIs = EeradUtilsMethod.class.getClassLoader().getResourceAsStream("securite.properties");
		Properties prop = new Properties();
		prop.load(tmpIs);
		String sfu = prop.getProperty("SFU");
		String sfp = prop.getProperty("SFP");
                 
        writeLog(decodedAuth);
        /*
        writeLog(u);
        writeLog(p);
        writeLog(sfu);
        writeLog(sfp);*/
        if (u.equals(sfu) && p.equals(sfp)) {
        	return true;
        }
        writeLog("HK : Error Authent");
        return false;
    }
	
}


