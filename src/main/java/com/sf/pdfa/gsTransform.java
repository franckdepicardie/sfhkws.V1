package com.sf.pdfa;

import java.io.File;
import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import com.itextpdf.text.DocumentException;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import com.sun.research.ws.wadl.Request;

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

@Path("/gsfilepdf")
public class gsTransform {

	public static final String PROFILE = "resources/img/sRGB.profile";
	
	// CONVERSION TO PDF/A1b FORMAT
	@POST
	@Path("/gsconvert")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)

	public Response convertFile(@FormDataParam("file") InputStream uploadedInputStream,
								@FormDataParam("file") FormDataContentDisposition fileDetail,
								@FormDataParam("filecase") String tmpCaseId, 
								@HeaderParam("authorization") String authString)
			throws Exception, InterruptedException, IOException {

		System.out.println(">> convertFile - Starting...");

		if (EeradUtilsMethod.isUserAuthenticated(authString)) {
		
			String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();
			String uploadedPreFileLocation = "/tmp/" + "pre" + fileDetail.getFileName();
			String outputPdf = "/tmp/output_" + fileDetail.getFileName();
			String refNewPathPDFA = "/tmp/PDFA_def.ps";
			String refNewPathICC = "/tmp/sRGB2014.icc";
	
			// WRITE FILE TO HEROKU DISK
			EeradUtilsMethod.writeToFile(uploadedInputStream, uploadedPreFileLocation);
			//EeradUtilsMethod.writeToFile(uploadedInputStream, uploadedFileLocation);
			
			// Get PDFA_def.ps 
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("PDFA_def.ps");
			EeradUtilsMethod.copyFileUsingStream(in, new File(refNewPathPDFA));
			in.close();
	
			// Get sRGB2014.icc
			InputStream inicc = this.getClass().getClassLoader().getResourceAsStream("sRGB2014.icc");
			EeradUtilsMethod.copyFileUsingStream(inicc, new File(refNewPathICC));
			inicc.close();
			
			// CHECK ALL IS DONE
			File fileRef = new File(refNewPathPDFA);
			System.out.println(">> convertFile - refNewPathPDFA - exists : " + fileRef.exists());
	
			// SET PDF SIGN ZONE
			EeradUtilsMethod.setPdfMetadata(uploadedPreFileLocation, uploadedFileLocation, EeradUtilsMethod.getSignatureZone(uploadedPreFileLocation));
			
			// MAKE CONVERSION		
			try {
				
				File varTmpDestFile;
	
				if ((new File(uploadedPreFileLocation)).exists()) {
					
					System.out.println(">> convertFile - executing script PART I ...");
	
					Process awk = new ProcessBuilder("/usr/bin/gs", "-dPDFA=2", "-dNOOUTERSAVE",
							"-sColorConversionStrategy=RGB", "-sProcessColorModel=DeviceRGB", "-sDEVICE=pdfwrite", "-o",
							outputPdf, "-dPDFACompatibilityPolicy=1", "-dEmbedAllFonts=true", "-dDetectDuplicateImages=true", "-r300" ,
							 refNewPathPDFA , uploadedFileLocation).start();
					
					awk.waitFor();
					String line;
					BufferedReader inb = new BufferedReader(new InputStreamReader(awk.getInputStream()));
					StringBuilder builder = new StringBuilder();
					
					while ((line = inb.readLine()) != null) {
						builder.append(line);
						builder.append(System.getProperty("line.separator"));
	
					}
					System.out.println(">> [Command1]" + builder.toString());
					inb.close();
					System.out.println(">> convertFile - end executing script PART I");
	
					varTmpDestFile = new File(outputPdf);
					
					javax.ws.rs.core.Response.ResponseBuilder response = Response
							.ok((Object) FileUtils.readFileToByteArray(varTmpDestFile));
					response.header("Content-Disposition", "attachment; filename=output.pdf");
	
					return response.build();
				}
	
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (Exception e) {
	
				System.out.println(">> createPdfA - Error");
				throw new Exception("Execption thrown :" + e);
			}
	
			return null;
		}
		
		javax.ws.rs.core.Response.ResponseBuilder response = Response.ok(("HK : Error Authent"));

		return response.build();
	}

	

}
