package com.javacodegeeks.enterprise.rest.jersey;

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
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Path("/gsfilepdf")
public class gsTransform {

	public static final String PROFILE = "resources/img/sRGB.profile";

	@POST
	@Path("/gsconvert")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)

	public Response convertFile(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail)
			throws Exception, InterruptedException, IOException {

		String uploadedFileLocation = "/tmp/" + fileDetail.getFileName();
		String outputPdf = "/tmp/output.pdf";
		String refNewPathPDFA = "/tmp/PDFA_def.ps";
		String refNewPathICC = "/tmp/sRGB2014.icc";

		// WRITE FILE TO HEROKU DISK
		writeToFile(uploadedInputStream, uploadedFileLocation);

		// COPY PDFA_def.ps TO DISK
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("PDFA_def.ps");
		copyFileUsingStream(in, new File(refNewPathPDFA));
		in.close();

		// COPY PDFA_def.ps TO DISK
		InputStream inicc = this.getClass().getClassLoader().getResourceAsStream("sRGB2014.icc");
		copyFileUsingStream(inicc, new File(refNewPathICC));
		inicc.close();
		
		// CHECK ALL IS DONE
		File varTmpSourceFile = new File(uploadedFileLocation);
		boolean existsSource = varTmpSourceFile.exists();
		File fileRef = new File(refNewPathPDFA);
		boolean existsRef = fileRef.exists();

		System.out.println(">> convertFile - existsSource : " + existsSource);
		System.out.println(">> convertFile - existsRef : " + existsRef);

		try {

			if (existsSource) {
				
				System.out.println(">> convertFile - executing script PART I");

				Process awk = new ProcessBuilder("/usr/bin/gs", "-dPDFA", "-dNOOUTERSAVE",
						"-sColorConversionStrategy=RGB", "-sProcessColorModel=DeviceRGB", "-sDEVICE=pdfwrite", "-o",
						"/tmp/output.pdf", "-dPDFACompatibilityPolicy=1", "-dEmbedAllFonts=true",
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

				File varTmpDestFile = new File(outputPdf);
				boolean existsDest = varTmpDestFile.exists();
				System.out.println(">> convertFile - existsDest : " + existsDest);

				// System.out.println(">> convertFile - existsSource:" +
				// existsSource );

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

	private static void copyFileUsingStream(InputStream tmpIn, File dest) throws IOException {

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

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

}
