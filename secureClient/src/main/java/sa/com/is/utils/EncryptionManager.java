package sa.com.is.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.security.KeyChain;
import android.util.Base64;
import android.util.Log;

import org.openintents.openpgp.util.CertificateHelper;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.ASN1Object;
import org.spongycastle.asn1.ASN1ObjectIdentifier;
import org.spongycastle.asn1.ASN1Set;
import org.spongycastle.asn1.DEROctetString;
import org.spongycastle.asn1.DEROutputStream;
import org.spongycastle.asn1.DERSet;
import org.spongycastle.asn1.DERUTCTime;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.CMSAttributes;
import org.spongycastle.asn1.pkcs.Attribute;
import org.spongycastle.asn1.pkcs.IssuerAndSerialNumber;
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.spongycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.spongycastle.asn1.smime.SMIMECapability;
import org.spongycastle.asn1.smime.SMIMECapabilityVector;
import org.spongycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;
import org.spongycastle.asn1.x509.AttributeCertificate;
import org.spongycastle.cert.X509AttributeCertificateHolder;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.DefaultSignedAttributeTableGenerator;
import org.spongycastle.cms.SimpleAttributeTableGenerator;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMESigned;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;
import org.spongycastle.util.encoders.Base64Encoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

import javax.activation.ActivationDataFlavor;
import javax.crypto.Cipher;

import sa.com.is.Body;
import sa.com.is.Message;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.R;
import sa.com.is.crypto.PgpData;
import sa.com.is.internet.MimeBodyPart;
import sa.com.is.internet.MimeMessage;
import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.PKCS9Attributes;
import sun.security.pkcs.SignerInfo;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.x509.AlgorithmId;
import sun.security.x509.X500Name;

/**
 * Created by snouto on 12/08/15.
 */
public class EncryptionManager {


    public static final String CONTENT_TYPE_HEADER = "Content-Type:";
    public static final String MIME_SIGNED_TYPE="\"multipart/signed\"";
    public static final String ALGORITHMNAME = "SHA1withRSA";


    public static void main(String... args) throws UnsupportedEncodingException
    {
        String certLocation = "/home/snouto/Desktop/email/mycertificate.pfx";

       /* Provider[] providers = Security.getProviders();

        if(providers != null )
        {
            for(Provider current : providers )
            {
                System.out.println(current.getName() + " : " + current.getInfo());
            }
        }*/



    }






    public static byte[] signMessage(Context context,byte[] body)
    {
        try
        {



          Security.insertProviderAt(new BouncyCastleProvider(),1);


            KeyStore store = KeyStore.getInstance("PKCS12","SC");
           // KeyStore store = KeyStore.getInstance("AndroidKeyStore");

           //get the certificate location
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String certificateLocation = prefs.getString("IS_CERT_CONTENT_LOCATION", null);

            if(certificateLocation != null)
            {
                InputStream is = context.getContentResolver().openInputStream(Uri.parse(certificateLocation));

                store.load(is,"snouto".toCharArray());


                is.close();

                Enumeration<String> aliases = store.aliases();
                String privateAlias = "";
                //Get the alias
                Certificate[] certs = null;
                List<X509Certificate> certifs = new ArrayList<>();

                PrivateKey privateKey = null;

                while(aliases.hasMoreElements())
                {
                    privateAlias = aliases.nextElement();

                    if(store.isKeyEntry(privateAlias))
                    {
                        KeyStore.Entry entry = store.getEntry(privateAlias,null);

                        if(!(entry instanceof KeyStore.PrivateKeyEntry))
                            continue;

                        privateKey = (PrivateKey) store.getKey(privateAlias,"snouto".toCharArray());

                        Log.e("PrivateKey",bytes2String(privateKey.getEncoded()));
                        break;
                    }
                }

                if(privateKey == null) return null;



                certs = store.getCertificateChain(privateAlias);

                for(Certificate current :certs)
                {
                    certifs.add((X509Certificate)current);
                }

                //Get the default certificate out of the available Certificates
                X509Certificate certificate = (X509Certificate) store.getCertificate(privateAlias);

                Log.e("PublicKey",bytes2String(certificate.getPublicKey().getEncoded()));

                if(certificate != null)
                {




                    //byte[] bodyContent = extractBodyContentAsByteArray(body);


                   //byte[] encryptedBytes = getSignedInfo_(certificate,privateKey,bodyContent);

                  byte[] encryptedBytes = doDigitalSignature(body,privateKey,certificate,Arrays.asList(certificate));



                    return Base64.encode(encryptedBytes,Base64.DEFAULT);

                   // return new String(Base64.encode(encryptedBytes,Base64.NO_WRAP));
                }else throw new Exception("Certificate can't be null");




            }throw new Exception("Certificate Can't be null");


        }catch (Exception s)
        {
            Log.e("EncryptionManager",s.getMessage());
            return null;
        }
    }



    private static byte[] doDigitalSignature(byte[] bodyContent, PrivateKey privateKey,X509Certificate certificate,List Certificates) {

        try
        {
            byte[] signedData = sign(bodyContent, certificate, privateKey,Certificates);


            return signedData;


        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());
            return null;
        }

    }


   /* private static byte[] getSignedInfo_(X509Certificate certificate,PrivateKey privateKey , byte[] dataTobeSigned) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException, IOException {
        X509Certificate x509 = certificate;
        PrivateKey priv = privateKey;
        byte[] data = dataTobeSigned;

        String digestAlgorithm = "MD5";
        String signingAlgorithm = "SHA1withRSA";

        AlgorithmId[] digestAlgorithmIds = new AlgorithmId[]{AlgorithmId.get(digestAlgorithm)};

        Signature sigSigner = Signature.getInstance(signingAlgorithm);
        sigSigner.initSign(priv);
        sigSigner.update(data);
        byte[] signedAttributes = sigSigner.sign();

        ContentInfo contentInfo = new ContentInfo(
                sun.security.pkcs.ContentInfo.DATA_OID,
                new DerValue(DerValue.tag_OctetString, data));

        X509Certificate[] certificates = {x509};

        BigInteger serial = x509.getSerialNumber();
        String issuerName = x509.getIssuerDN().getName();
        AlgorithmId dAlgId =  AlgorithmId.getAlgorithmId(digestAlgorithm);

        SignerInfo si = new SignerInfo(
                new X500Name(issuerName), serial, dAlgId, null,
                new AlgorithmId(AlgorithmId.RSAEncryption_oid),
                signedAttributes,null);



        SignerInfo[] signerInfos = {si};
        PKCS7 p7 = new PKCS7(digestAlgorithmIds, contentInfo, certificates, signerInfos);
        DerOutputStream bytes = new DerOutputStream();
        p7.encodeSignedData(bytes);


        return bytes.toByteArray();
    }*/

    /*private static byte[] getSignedInfo(CMSSignedData s) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        DEROutputStream dOut = new DEROutputStream(bOut);
        dOut.writeObject(s.toASN1Structure().toASN1Primitive());
        dOut.close();

        return bOut.toByteArray();
    }*/




    public static byte[] sign(byte[] data,  X509Certificate cert , PrivateKey privateKey,List certificates) throws GeneralSecurityException, CMSException, IOException, OperatorCreationException {


        CMSTypedData msg = new CMSProcessableByteArray(data); //Data to sign
        Store certs = new JcaCertStore(certificates);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        //Initializing the the BC's Signer
        ContentSigner sha1Signer = new JcaContentSignerBuilder(cert.getSigAlgName()).setProvider("SC").build(privateKey);
        //  Create the SMIMESignedGenerator
        SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
        capabilities.addCapability(SMIMECapability.aES256_CBC);
        capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
        capabilities.addCapability(SMIMECapability.dES_CBC);
        ASN1EncodableVector attributes = new ASN1EncodableVector();
        attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(
                new org.spongycastle.asn1.cms.IssuerAndSerialNumber(
                        new org.spongycastle.asn1.x500.X500Name((cert.getSubjectDN().getName())),
                        (cert).getSerialNumber())));
        attributes.add(new SMIMECapabilitiesAttribute(capabilities));
        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("SC").build())
                .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(attributes)))
                .build(sha1Signer, cert));



       /* gen.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder().setProvider("SC").setSignedAttributeGenerator(new AttributeTable(attributes))
        .build(cert.getSigAlgName(),privateKey,cert));*/

        gen.addCertificates(certs);


        //Getting the signed data

        CMSSignedData sigData = gen.generate(msg,true);



        return sigData.getEncoded();
    }

    private static String bytes2String(byte[] bytes) {
        StringBuilder string = new StringBuilder();
        for (byte b : bytes) {
            String hexString = Integer.toHexString(0x00FF & b);
            string.append(hexString.length() == 1 ? "0" + hexString : hexString);
        }
        return string.toString();
    }

    private static byte[] extractBodyContentAsByteArray(Body body) throws MessagingException , IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        body.writeTo(os);

        os.flush();

        return os.toByteArray();
    }

    public static boolean encryptMessage(PgpData data , String textToEncrypt , String certificateName)
    {
        boolean result = false;
        try
        {
            if(data == null)
                data = new PgpData();

            //get the certificate used
            X509Certificate certificate = CertificateHelper.getCertificateByName(certificateName);

            if(certificate != null)
            {
                //do the encryption in here
                //get the public key
                PublicKey publicKey = certificate.getPublicKey();

                Cipher encryptor = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                encryptor.init(Cipher.ENCRYPT_MODE, publicKey);

                byte[] encryptedBytes = encryptor.doFinal(textToEncrypt.getBytes("UTF-8"));

                //set the cipher text into PgpData
                data.setEncryptedData(Base64.encodeToString(encryptedBytes,Base64.DEFAULT));





            }


        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());
        }

        finally {

            return result;
        }
    }
}
