package sa.com.is.internet;


import sa.com.is.Body;
import sa.com.is.Message;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.Part;
import org.apache.james.mime4j.util.MimeUtil;


public class MimeMessageHelper {
    private MimeMessageHelper() {
    }

    public static void setBody(Part part, Body body) throws MessagingException {
        part.setBody(body);

        if (part instanceof Message) {
            part.setHeader("MIME-Version", "1.0");
        }

        if (body instanceof Multipart) {
            Multipart multipart = ((Multipart) body);

            multipart.setParent(part);
            String mimeType = multipart.getMimeType();



            String contentType = String.format("%s; boundary=\"%s\"", mimeType, multipart.getBoundary());
            part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);
            if ("multipart/signed".equalsIgnoreCase(mimeType)) {
                setEncoding(part, MimeUtil.ENC_7BIT);
                setContentHeaders(part, multipart);
               // part.removeHeader("Content-Transfer-Encoding");
            } else {
                setEncoding(part, MimeUtil.ENC_8BIT);
            }
        } else if (body instanceof TextBody) {
            String contentType = String.format("%s;charset=utf-8;", part.getMimeType());
            String name = MimeUtility.getHeaderParameter(part.getContentType(), "name");
            if (name != null) {
                contentType += String.format(";\r\n name=\"%s\"", name);
            }
            part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);

            setEncoding(part, MimeUtil.ENC_8BIT);
        }
    }

    private static void setContentHeaders(Part part, Multipart multipart) throws MessagingException {


        String contentType = String.format("%s;protocol=\"application/pkcs7-signature\";micalg=sha-256;boundary=\"%s\"", multipart.getMimeType(),
                multipart.getBoundary());
        part.setHeader(MimeHeader.HEADER_CONTENT_TYPE,contentType);

    }


    public static void setEncoding(Part part, String encoding) throws MessagingException {
        Body body = part.getBody();
        if (body != null) {
            body.setEncoding(encoding);
        }
        part.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, encoding);
    }
}
