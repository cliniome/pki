package sa.com.is.mailstore;


import java.util.Stack;

import sa.com.is.Body;
import sa.com.is.BodyPart;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.Part;
import sa.com.is.internet.MimeBodyPart;


public class MessageHelper {

    public static boolean isCompletePartAvailable(Part part) {
        Stack<Part> partsToCheck = new Stack<Part>();
        partsToCheck.push(part);

        while (!partsToCheck.isEmpty()) {
            Part currentPart = partsToCheck.pop();
            Body body = currentPart.getBody();

            boolean isBodyMissing = body == null;
            if (isBodyMissing) {
                return false;
            }

            if (body instanceof Multipart) {
                Multipart multipart = (Multipart) body;
                for (BodyPart bodyPart : multipart.getBodyParts()) {
                    partsToCheck.push(bodyPart);
                }
            }
        }

        return true;
    }

    public static MimeBodyPart createEmptyPart() {
        try {
            return new MimeBodyPart(null);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
}
