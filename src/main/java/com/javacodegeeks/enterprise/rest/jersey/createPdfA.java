package com.javacodegeeks.enterprise.rest.jersey;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import javax.ws.rs.core.HttpHeaders;
import com.sun.jersey.core.util.Base64;

import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import javax.ws.rs.Produces;

@Path("/filepdf")
public class createPdfA {

	public static final String FONT = "c:/windows/fonts/arial.ttf";
	public static final String PROFILE = "resources/img/sRGB.profile";

	@POST
	@Path("/convert")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)

	public Response convertFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws Exception, DocumentException {

		String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();
		//String dest = "/tmp/changed.pdf";
		//String dest = uploadedFileLocation;

		try {

			// WRITE FILE TO HEROKU DISK
			writeToFile(uploadedInputStream, uploadedFileLocation);
			
			// TRANSFORM PDF TO ANOTHER CONVERTED IN PDF/A1-B
			transform(uploadedFileLocation);

			// CHECK ALL IS DONE
			File varTmpSourceFile = new File(uploadedFileLocation);
			boolean existsSource = varTmpSourceFile.exists();
			
			//File varTmpDestFile = new File(dest);
			//boolean existsDest = varTmpDestFile.exists();
			
			System.out.println(">> convertFile - existsSource:" + existsSource );
			
			
			javax.ws.rs.core.Response.ResponseBuilder response = Response
					.ok((Object) FileUtils.readFileToByteArray(varTmpSourceFile));
			response.header("Content-Disposition", "attachment; filename=changed.pdf");
			return response.build();

		} catch (DocumentException e) {

			System.out.println(">> createPdfA - Error");
			throw new DocumentException("DocumentException thrown: " + e);
		} catch (Exception e) {

			System.out.println(">> createPdfA - Error");
			throw new Exception("Execption thrown :" + e);
		}

	}

	public void transform(String filename) throws IOException, DocumentException {

		//String filename = tmpFile.getName();
		System.out.println(">> createPdfA - transform - Begin : " + filename);
		Document document = new Document();
		PdfAWriter writer = PdfAWriter.getInstance(document, new FileOutputStream(filename),
				PdfAConformanceLevel.PDF_A_1B);
		
		writer.createXmpMetadata();
		System.out.println(">> createPdfA - transform - HERE1");
		document.open();

		byte[] fontByte = IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream("Helvetica.ttf"));
		BaseFont baseFontHelvetica = BaseFont.createFont("helvetica.ttf", BaseFont.CP1252, BaseFont.EMBEDDED, true,
				fontByte, null);
		Font fontHelvetica = new Font(baseFontHelvetica, 12);

		document.add(new Paragraph("Hello World", fontHelvetica));

		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		ICC_Profile icc = ICC_Profile.getInstance(classloader.getResourceAsStream("sRGB_CS_profile.icm"));// new
																											// FileInputStream("sRGB_CS_profile.icm"));
		writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
		document.close();
		System.out.println(">> createPdfA - transform - End");

	}

	// save uploaded file to new location
	private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation)
			throws IOException, DocumentException {

		try {

			System.out.println(">> createPdfA - writeToFile - Begin - uploadedFileLocation:" + uploadedFileLocation);
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];
			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			System.out.println(">> createPdfA - writeToFile - End");
/*
			System.out.println(">> createPdfA - writeToFile - Stamping Begin");
			PdfReader reader = new PdfReader(uploadedFileLocation);
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
			stamper.close();
			reader.close();
			System.out.println(">> createPdfA - writeToFile - Stamping End");
*/
			

		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}

}
