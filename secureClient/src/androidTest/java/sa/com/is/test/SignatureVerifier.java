package sa.com.is.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by snouto on 03/09/15.
 */
@RunWith(AndroidJUnit4.class)
public class SignatureVerifier {




    private Context instrumentationCtx;

    public static final String DATA_TO_BE_VERIFIED="Sign";


    public static final String SIGNATURE_ENCODED="MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgEFADCABgkqhkiG9w0BBwGggCSABART\n" +
            "aWduAAAAAAAAoIAwggSvMIIDl6ADAgECAhEA4CPLFRKDU4mtYW56VGdrITANBgkqhkiG9w0BAQsF\n" +
            "ADBvMQswCQYDVQQGEwJTRTEUMBIGA1UEChMLQWRkVHJ1c3QgQUIxJjAkBgNVBAsTHUFkZFRydXN0\n" +
            "IEV4dGVybmFsIFRUUCBOZXR3b3JrMSIwIAYDVQQDExlBZGRUcnVzdCBFeHRlcm5hbCBDQSBSb290\n" +
            "MB4XDTE0MTIyMjAwMDAwMFoXDTIwMDUzMDEwNDgzOFowgZsxCzAJBgNVBAYTAkdCMRswGQYDVQQI\n" +
            "ExJHcmVhdGVyIE1hbmNoZXN0ZXIxEDAOBgNVBAcTB1NhbGZvcmQxGjAYBgNVBAoTEUNPTU9ETyBD\n" +
            "QSBMaW1pdGVkMUEwPwYDVQQDEzhDT01PRE8gU0hBLTI1NiBDbGllbnQgQXV0aGVudGljYXRpb24g\n" +
            "YW5kIFNlY3VyZSBFbWFpbCBDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAImxDdp6\n" +
            "UxlOcFIdvFamBia3uEngludRq/HwWhNJFaO0jBtgvHpRQqd5jKQi3xdhTpHVdiMKFNNKAn+2HQmA\n" +
            "bqUEPdm6uxb+oYepLkNSQxZ8rzJQyKZPWukI2M+TJZx7iOgwZOak+FaA/SokFDMXmaxE5WmLo0YG\n" +
            "S8Iz1OlAnwawsayTQLm1CJM6nCpToxDbPSBhPFUDjtlOdiUCISn6o3xxdk/u4V+B6ftUgNvDezVS\n" +
            "t4TeIj0sMC0xf1m9UjewM2ktQ+v61qXxl3dnUYzZ7ifrvKUHOHaMpKk4/9+M9QOsSb7K93OZOg8y\n" +
            "q5yVOhM9DkY6V3RhUL7GQD/L5OKfoiECAwEAAaOCARcwggETMB8GA1UdIwQYMBaAFK29mHo0tCb3\n" +
            "+sQmVO8DveAky1QaMB0GA1UdDgQWBBSSYWuC4aKgqk/sZ/HCo/e0gADB7DAOBgNVHQ8BAf8EBAMC\n" +
            "AYYwEgYDVR0TAQH/BAgwBgEB/wIBADAdBgNVHSUEFjAUBggrBgEFBQcDAgYIKwYBBQUHAwQwEQYD\n" +
            "VR0gBAowCDAGBgRVHSAAMEQGA1UdHwQ9MDswOaA3oDWGM2h0dHA6Ly9jcmwudXNlcnRydXN0LmNv\n" +
            "bS9BZGRUcnVzdEV4dGVybmFsQ0FSb290LmNybDA1BggrBgEFBQcBAQQpMCcwJQYIKwYBBQUHMAGG\n" +
            "GWh0dHA6Ly9vY3NwLnVzZXJ0cnVzdC5jb20wDQYJKoZIhvcNAQELBQADggEBABsqbqxVwTqriMXY\n" +
            "7c1V86prYSvACRAjmQ/FZmpvsfW0tXdeDwJhAN99Bf4Ss6SAgAD8+x1banICCkG8BbrBWNUmwurV\n" +
            "TYT7/oKYz1gb4yJjnFL4uwU2q31Ypd6rO2Pl2tVz7+zg+3vio//wQiOcyraNTT7kSxgDsqgt1Ni7\n" +
            "QkuQaYUQ26Y3NOh74AEQpZzKOsefT4g0bopl0BqKu6ncyso20fT8wmQpNa/WsadxEdIDQ7GPPprs\n" +
            "njJT9HaSyoY0B7ksyuYcStiZDcGG4pCS+1pCaiMhEOllx/XVu37qjIUgAmLq0ToHLFnFmTPyOInl\n" +
            "tukWeh95FPZKEBom+nyK+5swggQ2MIIDHqADAgECAgEBMA0GCSqGSIb3DQEBBQUAMG8xCzAJBgNV\n" +
            "BAYTAlNFMRQwEgYDVQQKEwtBZGRUcnVzdCBBQjEmMCQGA1UECxMdQWRkVHJ1c3QgRXh0ZXJuYWwg\n" +
            "VFRQIE5ldHdvcmsxIjAgBgNVBAMTGUFkZFRydXN0IEV4dGVybmFsIENBIFJvb3QwHhcNMDAwNTMw\n" +
            "MTA0ODM4WhcNMjAwNTMwMTA0ODM4WjBvMQswCQYDVQQGEwJTRTEUMBIGA1UEChMLQWRkVHJ1c3Qg\n" +
            "QUIxJjAkBgNVBAsTHUFkZFRydXN0IEV4dGVybmFsIFRUUCBOZXR3b3JrMSIwIAYDVQQDExlBZGRU\n" +
            "cnVzdCBFeHRlcm5hbCBDQSBSb290MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAt/ca\n" +
            "M+byAAQtOeBOW+0fvGwPzbX6I7bO3psRM5ekKUx9k5+9SryT7QMa44/P5W1QWtaXKZRagLBJetsu\n" +
            "lf24yr83OC0ePpFBrXBWx/BPP+gynnTKyJBU6cZfD3idmkA8Dqxhql4Uj56HoWpQ3NeaTq8Fs6Zx\n" +
            "lJxxs1BgCscTnTgHhgKo6ahpJhiQq0ywTyOrOk+E2N/On+Fpb7vXQtdrROTHre5tQV9yWnEIN7N5\n" +
            "ZaRZoJQ39wAvDcKSctrQOHLbFKhFxF0qfbe01sTurM0TRLfJK91DACX6YblpalgjEbenM49WdVn1\n" +
            "zSnXRrcKK2W200JvFbK4e/vv6V1T1TRaJwIDAQABo4HcMIHZMB0GA1UdDgQWBBStvZh6NLQm9/rE\n" +
            "JlTvA73gJMtUGjALBgNVHQ8EBAMCAQYwDwYDVR0TAQH/BAUwAwEB/zCBmQYDVR0jBIGRMIGOgBSt\n" +
            "vZh6NLQm9/rEJlTvA73gJMtUGqFzpHEwbzELMAkGA1UEBhMCU0UxFDASBgNVBAoTC0FkZFRydXN0\n" +
            "IEFCMSYwJAYDVQQLEx1BZGRUcnVzdCBFeHRlcm5hbCBUVFAgTmV0d29yazEiMCAGA1UEAxMZQWRk\n" +
            "VHJ1c3QgRXh0ZXJuYWwgQ0EgUm9vdIIBATANBgkqhkiG9w0BAQUFAAOCAQEAsJvghSXC1iPiD5YG\n" +
            "kp1BmJzZhHmB2R5bFAcjNmWPsNh3u6xBbEdgg1Gw+TI95/z2JhPHgBalv1r8h894eYkhmuJMBwqG\n" +
            "Nbzy3lHE0pa33H5O7nD9HDnrDAJRFC2OvRbgwd9Gdeckrez0QrSFk3AQZ7qdBjVKGNMresxRQqF6\n" +
            "Y9Hmu6HFK8I2vhMN5r1jfnl7pwkNQKtq3Y+Kw/b2jBpCBVHURfWfp2IhaBUgQzyZ53y9JNipkRdz\n" +
            "iD9WGzE4GLRxD5rNyA6eji4b4YyYg8sfMfFETMYEc0l2YA/H+L0XgGsu6cxMDlqaeQ8gCi7VnmMm\n" +
            "HlWSlNiCF1p70LzHj06GBAAAMYIDbDCCA2gCAQEwgbAwgZsxCzAJBgNVBAYTAkdCMRswGQYDVQQI\n" +
            "ExJHcmVhdGVyIE1hbmNoZXN0ZXIxEDAOBgNVBAcTB1NhbGZvcmQxGjAYBgNVBAoTEUNPTU9ETyBD\n" +
            "QSBMaW1pdGVkMUEwPwYDVQQDEzhDT01PRE8gU0hBLTI1NiBDbGllbnQgQXV0aGVudGljYXRpb24g\n" +
            "YW5kIFNlY3VyZSBFbWFpbCBDQQIQE1ZsOwo0pIEmzkWGy3Z6sjANBglghkgBZQMEAgEFAKCCAYww\n" +
            "GAYJKoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMTUwOTAyMjE1NTU1WjAv\n" +
            "BgkqhkiG9w0BCQQxIgQgDJYY6YSaYsaP9bDmI2HAX0twsYHbHvR+7UWKF/X087YwWwYJKoZIhvcN\n" +
            "AQkPMU4wTDALBglghkgBZQMEASowCwYJYIZIAWUDBAECMAsGCWCGSAFlAwQBFjAKBggqhkiG9w0D\n" +
            "BzAOBggqhkiG9w0DAgICAIAwBwYFKw4DAgcwgcMGCyqGSIb3DQEJEAILMYGzoIGwMIGbMQswCQYD\n" +
            "VQQGEwJHQjEbMBkGA1UECAwSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHDAdTYWxmb3JkMRow\n" +
            "GAYDVQQKDBFDT01PRE8gQ0EgTGltaXRlZDFBMD8GA1UEAww4Q09NT0RPIFNIQS0yNTYgQ2xpZW50\n" +
            "IEF1dGhlbnRpY2F0aW9uIGFuZCBTZWN1cmUgRW1haWwgQ0ECEBNWbDsKNKSBJs5Fhst2erIwDQYJ\n" +
            "KoZIhvcNAQEBBQAEggEAZnxj5B8uH3oplBG8s1N5qu+uLGkFpWCGniJXzkhfRgGMnRUV7JpjhxRs\n" +
            "72VytKpcgVI5gFFd0mPyMbJaTdFoPlGYfR7nrp9cA0GsJJ843EO2MOBzDi8360b0hcZfFV4kDrsv\n" +
            "iFGf1GBnZ9frGayYTyDXOZFJSaJduD+y4ZknXVuyRjdEcv9wIbW7GSznm+FLMgiOAxzPDxbuIFC6\n" +
            "EifmeeH+c985HNH1dKlI/j7a4/Qc8NMbi1JDzDPuliYEFQabWX6NGwRnvAKsDK4219JxkmGXLI+m\n" +
            "8Mvbq6WESCJnVXfkly5+oQ1Pwq8hWuJZwSgB308bxiwd2Xg3BgwigIBWAwAAAAAAAA==";



    @Before
    public void setup()
    {
        this.instrumentationCtx = InstrumentationRegistry.getContext();
    }


/*

    public void doVerification()
    {
        try
        {
            Security.addProvider(new BouncyCastleProvider());
            KeyStore store = KeyStore.getInstance("PKCS12", "BC");

            String certificatePath = "~/Desktop/Certificates/hotmail.pfx";
            InputStream fis = new FileInputStream(certificatePath);

            //load the certificate in the keystore
            store.load(fis,"snouto".toCharArray());
            Enumeration<String> aliases = store.aliases();
            String privateAlias = "";
            //Get the alias
            Certificate[] certs = null;
            List<X509Certificate> certifs = new ArrayList<>();

            while(aliases.hasMoreElements())
            {
                privateAlias = aliases.nextElement();

                certs = store.getCertificateChain(privateAlias);

                if(certs != null && certs.length >0)
                    break;
            }

            //get the certificate containing the public key
            X509Certificate certificate = (X509Certificate)certs[0];


            assert certificate != null;




            if(certificate != null)
            {
                //Access the public key
                PublicKey publicKey = certificate.getPublicKey();

                //define the bytes of the digital signature
                byte[] digitalSignature = SIGNATURE_ENCODED.getBytes("UTF-8");

                //define the signature to be used
                Signature verifier = Signature.getInstance(certificate.getSigAlgName(),"SC");

                if(verifier == null){
                    System.out.println("Signature object can't be null");
                    return;
                }

                //Initialize the verifier with the public key
                verifier.initVerify(publicKey);

                byte[] data = DATA_TO_BE_VERIFIED.getBytes("UTF-8");

                verifier.update(data);

                boolean verified = verifier.verify(digitalSignature);

                assert verified != true;

                System.out.println("Result of verification :" + String.valueOf(verified));




            }

        }catch (Exception s)
        {
            s.printStackTrace();
        }
    }
*/


    public X509Certificate getCertificate()
    {
        try
        {
            Security.addProvider(new BouncyCastleProvider());
            KeyStore store = KeyStore.getInstance("PKCS12", "BC");
            String certificatePath = "content://com.android.providers.downloads.documents/document/11";
            InputStream fis = this.instrumentationCtx.getContentResolver().openInputStream(Uri.parse(certificatePath));

            //load the certificate in the keystore
            store.load(fis,"snouto".toCharArray());
            Enumeration<String> aliases = store.aliases();
            String privateAlias = "";
            //Get the alias
            Certificate[] certs = null;
            List<X509Certificate> certifs = new ArrayList<>();

            while(aliases.hasMoreElements())
            {
                privateAlias = aliases.nextElement();

                certs = store.getCertificateChain(privateAlias);

                if(certs != null && certs.length >0)
                    break;
            }

            //get the certificate containing the public key
            X509Certificate certificate = (X509Certificate)certs[0];


            return certificate;

        }catch (Exception s)
        {
            Log.e("SignatureVerifier",s.getMessage());
            return null;
        }
    }


    @Test
    public void signTest()
    {

        try
        {
            String message = signMessage("Signed");

            Log.e("SignatureVerifier","Signed Message :" + message);

        }catch (Exception s)
        {
            Log.e("SignatureVerifier",s.getMessage());
        }


    }

    public String signMessage(String message) throws Exception
    {
        X509Certificate certificate = getCertificate();

        Signature signature = Signature.getInstance(certificate.getSigAlgName(),"SC");

        //get the private key

        PrivateKey privateKey = getPrivateKey();

        signature.initSign(privateKey);

        byte[] data = message.getBytes("UTF-8");

        signature.update(data);

        byte[] signedData = signature.sign();

        String signedHash = bytes2String(signedData);

        return signedHash;



    }

    private  String bytes2String(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(0x00FF & b);
            string.append(hexString.length() == 1 ? "0" + hexString : hexString);
        }
        return string.toString();
    }


    private PrivateKey getPrivateKey()
    {
        try
        {
            Security.addProvider(new BouncyCastleProvider());
            KeyStore store = KeyStore.getInstance("PKCS12", "BC");
            String certificatePath = "content://com.android.providers.downloads.documents/document/11";
            InputStream fis = this.instrumentationCtx.getContentResolver().openInputStream(Uri.parse(certificatePath));

            //load the certificate in the keystore
            store.load(fis,"snouto".toCharArray());
            Enumeration<String> aliases = store.aliases();
            String privateAlias = "";
            //Get the alias
            Certificate[] certs = null;
            List<X509Certificate> certifs = new ArrayList<>();

            while(aliases.hasMoreElements())
            {
                privateAlias = aliases.nextElement();

                certs = store.getCertificateChain(privateAlias);

                if(certs != null && certs.length >0)
                    break;
            }

            //get the certificate containing the public key
            PrivateKey privatekey = (PrivateKey)store.getKey(privateAlias,"snouto".toCharArray());

            return privatekey;

        }catch (Exception s)
        {
            Log.e("SignatureVerifier",s.getMessage());
            return null;
        }
    }


    @Test
    public void verify()
    {
        try
        {


            //get the certificate containing the public key
            X509Certificate certificate = getCertificate();


            assert certificate != null;




            if(certificate != null)
            {
                //Access the public key
                PublicKey publicKey = certificate.getPublicKey();

                //define the bytes of the digital signature
                byte[] digitalSignature = SIGNATURE_ENCODED.getBytes("UTF-8");

                //define the signature to be used
                Signature verifier = Signature.getInstance(certificate.getSigAlgName(),"SC");

                if(verifier == null){
                    Log.i("SignatureVerifier","signature object can't be null");
                    return;
                }

                //Initialize the verifier with the public key
                verifier.initVerify(publicKey);

                byte[] data = DATA_TO_BE_VERIFIED.getBytes("UTF-8");

                verifier.update(data);



                boolean verified = verifier.verify(digitalSignature);

                assert verified != true;

                Log.i("SignatureVerifier",String.format("Result of verification :%s",verified));




            }

        }catch (Exception s)
        {
            Log.e("JunitTesting",s.getMessage());
            s.printStackTrace();
        }
    }

}
