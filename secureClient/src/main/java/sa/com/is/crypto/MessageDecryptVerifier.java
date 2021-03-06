package sa.com.is.crypto;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import sa.com.is.Body;
import sa.com.is.BodyPart;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.Part;
import sa.com.is.internet.MessageExtractor;
import org.openintents.openpgp.util.OpenPgpUtils;


public class MessageDecryptVerifier {
    private static final String MULTIPART_ENCRYPTED = "multipart/encrypted";
    private static final String MULTIPART_SIGNED = "multipart/signed";
    private static final String PROTOCOL_PARAMETER = "protocol";
    private static final String APPLICATION_PGP_ENCRYPTED = "application/pgp-encrypted";
    private static final String APPLICATION_PGP_SIGNATURE = "application/pgp-signature";
    private static final String APPLICATION_ENCRYPTED_ENVELOPED = "application/pkcs7-mime";
    private static final String TEXT_PLAIN = "text/plain";


    public static List<Part> findEncryptedParts(Part startPart) {
        List<Part> encryptedParts = new ArrayList<Part>();
        Stack<Part> partsToCheck = new Stack<Part>();
        partsToCheck.push(startPart);

        while (!partsToCheck.isEmpty()) {
            Part part = partsToCheck.pop();
            String mimeType = part.getMimeType();
            Body body = part.getBody();

            if (APPLICATION_ENCRYPTED_ENVELOPED.equals(mimeType)) {
                encryptedParts.add(part);
            } else if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (int i = multipart.getCount() - 1; i >= 0; i--) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    partsToCheck.push(bodyPart);
                }
            }
        }


        return encryptedParts;
    }


    public static boolean isSignedEmail(Part startPart){

        List<Part> signedParts = findSignedParts(startPart);

        if(signedParts != null && signedParts.size() > 0) return true;
        else return false;
    }

    public static boolean isEncryptedEmail(Part startPart){

        List<Part> encryptedParts = findEncryptedParts(startPart);

        if(encryptedParts != null &&encryptedParts.size() > 0) return true;
        else return false;
    }



    public static List<Part> findSignedParts(Part startPart) {
        List<Part> signedParts = new ArrayList<Part>();
        Stack<Part> partsToCheck = new Stack<Part>();
        partsToCheck.push(startPart);

        while (!partsToCheck.isEmpty()) {
            Part part = partsToCheck.pop();
            String mimeType = part.getMimeType();
            Body body = part.getBody();

            if (MULTIPART_SIGNED.equals(mimeType)) {
                signedParts.add(part);
            } else if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (int i = multipart.getCount() - 1; i >= 0; i--) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    partsToCheck.push(bodyPart);
                }
            }
        }

        return signedParts;
    }

    public static List<Part> findPgpInlineParts(Part startPart) {
        List<Part> inlineParts = new ArrayList<Part>();
        Stack<Part> partsToCheck = new Stack<Part>();
        partsToCheck.push(startPart);

        while (!partsToCheck.isEmpty()) {
            Part part = partsToCheck.pop();
            String mimeType = part.getMimeType();
            Body body = part.getBody();

            if (TEXT_PLAIN.equalsIgnoreCase(mimeType)) {
                String text = MessageExtractor.getTextFromPart(part);
                switch (OpenPgpUtils.parseMessage(text)) {
                    case OpenPgpUtils.PARSE_RESULT_MESSAGE:
                    case OpenPgpUtils.PARSE_RESULT_SIGNED_MESSAGE:
                        inlineParts.add(part);
                }
            } else if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (int i = multipart.getCount() - 1; i >= 0; i--) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    partsToCheck.push(bodyPart);
                }
            }
        }

        return inlineParts;
    }

    public static byte[] getSignatureData(Part part) throws IOException, MessagingException {

        if (MULTIPART_SIGNED.equals(part.getMimeType())) {
            Body body = part.getBody();
            if (body instanceof Multipart) {
                Multipart multi = (Multipart) body;
                BodyPart signatureBody = multi.getBodyPart(1);
                if (APPLICATION_PGP_SIGNATURE.equals(signatureBody.getMimeType())) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    signatureBody.getBody().writeTo(bos);
                    return bos.toByteArray();
                }
            }
        }

        return null;
    }

    public static boolean isPgpMimeSignedPart(Part part) {
        return MULTIPART_SIGNED.equals(part.getMimeType());
    }

    public static boolean isPgpMimeEncryptedPart(Part part) {
        //FIXME: Doesn't work right now because LocalMessage.getContentType() doesn't load headers from database
//        String contentType = part.getContentType();
//        String protocol = MimeUtility.getHeaderParameter(contentType, PROTOCOL_PARAMETER);
//        return APPLICATION_PGP_ENCRYPTED.equals(protocol);
        return MULTIPART_ENCRYPTED.equals(part.getMimeType());
    }
}
