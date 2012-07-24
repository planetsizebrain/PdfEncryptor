package be.planetsizebrain.pdfencryptor;

import com.itextpdf.text.pdf.PdfEncryptor;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Simple servlet that encrypts a PDF, based on the following example:
 *
 *      http://api.itextpdf.com/itext/com/itextpdf/text/pdf/PdfEncryptor.html#encrypt(com.itextpdf.text.pdf.PdfReader, java.io.OutputStream, int, java.lang.String, java.lang.String, int)
 *
 * Author Jan Eerdekens (jan.eerdekens@gmail.com or Twitter @planetsizebrain)
 */
public class PdfEncryptorServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String password = "";
        ByteArrayOutputStream bos = null;
        OutputStream os = resp.getOutputStream();

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (item.isFormField()) {
                    password = IOUtils.toString(stream);
                } else {
                    bos = new ByteArrayOutputStream();
                    PdfReader reader = new PdfReader(stream);
                    PdfEncryptor.encrypt(reader, bos, PdfWriter.ENCRYPTION_AES_128, password, password, PdfWriter.ALLOW_PRINTING);
                }
            }

        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        byte[] encrypted = bos.toByteArray();
        resp.setContentType("application/octet-stream");
        resp.setContentLength(encrypted.length);
        resp.setHeader("Content-Disposition", "attachment; filename=\"encrypted.pdf\"");

        ByteArrayInputStream bis = new ByteArrayInputStream(encrypted);
        IOUtils.copy(bis, os);
        IOUtils.closeQuietly(bis);
        IOUtils.closeQuietly(os);
    }
}
