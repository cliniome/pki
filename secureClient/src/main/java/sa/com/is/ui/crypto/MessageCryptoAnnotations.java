package sa.com.is.ui.crypto;


import java.util.HashMap;

import sa.com.is.Part;
import sa.com.is.mailstore.OpenPgpResultAnnotation;


public class MessageCryptoAnnotations {
    private HashMap<Part, OpenPgpResultAnnotation> annotations = new HashMap<Part, OpenPgpResultAnnotation>();

    MessageCryptoAnnotations() {
        // Package-private constructor
    }

    void put(Part part, OpenPgpResultAnnotation annotation) {
        annotations.put(part, annotation);
    }

    public OpenPgpResultAnnotation get(Part part) {
        return annotations.get(part);
    }

    public boolean has(Part part) {
        return annotations.containsKey(part);
    }
}
