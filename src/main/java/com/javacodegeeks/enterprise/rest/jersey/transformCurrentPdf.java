package com.javacodegeeks.enterprise.rest.jersey;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

import com.itextpdf.text.DocumentException;
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

import java.io.IOException;
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

@Path("/file")
public class transformCurrentPdf {

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response uploadFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) {

		System.out.println(">> transformCurrentPdf - uploadFile - fileDetail:" + fileDetail);
		String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();
		String dest = "/tmp/changed.pdf";

		try {

			// save it
			File resFile = writeToFile(uploadedInputStream, uploadedFileLocation, dest);

			String output = "File name : " + fileDetail.getFileName();
			output = output + "\n Size : " + fileDetail.getSize();

			javax.ws.rs.core.Response.ResponseBuilder response = Response
					.ok((Object) FileUtils.readFileToByteArray(resFile));
			response.header("Content-Disposition", "attachment; filename=changed.pdf");
			return response.build();
		} catch (Exception e) {

		}
		return null;
	}

	// save uploaded file to new location
	private File writeToFile(InputStream uploadedInputStream, String uploadedFileLocation, String dest)
			throws IOException, DocumentException {

		try {

			System.out.println(">> transformCurrentPdf - writeToFile - uploadedFileLocation:" + uploadedFileLocation);

			// Write pdf document to disk
			System.out.println(">> transformCurrentPdf - writeToFile - Begin");
			OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			System.out.println(">> transformCurrentPdf - writeToFile - End");

			// Replace content
			System.out.println(">> transformCurrentPdf - writeToFile - HERE0");
			PdfReader reader = new PdfReader(uploadedFileLocation);
			System.out.println(">> transformCurrentPdf - writeToFile - HERE1");
			PdfDictionary dict = reader.getPageN(1);
			PdfObject object = dict.getDirectObject(PdfName.CONTENTS);

			if (object instanceof PRStream) {

				PRStream stream = (PRStream) object;
				byte[] data = PdfReader.getStreamBytes(stream);
				stream.setData(new String(data).replace("TITULAIRE", "Changed Value").getBytes());
				System.out.println(">> transformCurrentPdf - writeToFile - Changed Value");
			} /*
				 * if (object instanceof PdfStream) { PdfStream stream =
				 * (PdfStream) object; byte[] data = stream.getBytes();
				 * stream.setData(new String(data).replace("TITULAIRE",
				 * "Changed Value").getBytes()); System.out.
				 * println(">> transformCurrentPdf - writeToFile - Changed Value"
				 * ); }
				 */
			System.out.println(">> transformCurrentPdf - writeToFile - HERE2");
			PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
			System.out.println(">> transformCurrentPdf - writeToFile - HERE3");
			stamper.close();
			reader.close();

			return (new File(dest));

		} catch (IOException e) {

			e.printStackTrace();
		}
		return null;
	}

}
