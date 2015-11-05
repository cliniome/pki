package sa.com.is.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by snouto on 26/08/15.
 */
public class CryptoUtilities {


    public static void main(String... args)
    {
        try {

            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

            byte[] randomKey = new byte[56];
            random.nextBytes(randomKey);

            SecretKeySpec secretKeySpec = new SecretKeySpec(randomKey,"DES");

            //Create a secret key factory
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DES");

            //get the secret key
            try {
                SecretKey desKey = factory.generateSecret(secretKeySpec);
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
