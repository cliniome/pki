package sa.com.is.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.sun.mail.util.BASE64DecoderStream;

import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1EncodableVector;

import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.spongycastle.asn1.smime.SMIMECapability;
import org.spongycastle.asn1.smime.SMIMECapabilityVector;
import org.spongycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.spongycastle.asn1.x500.X500Name;

import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.x509.X509Extensions;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cms.CMSAlgorithm;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataParser;
import org.spongycastle.cms.CMSTypedData;
import org.spongycastle.cms.DefaultSignedAttributeTableGenerator;
import org.spongycastle.cms.RecipientId;
import org.spongycastle.cms.RecipientInformation;
import org.spongycastle.cms.RecipientInformationStore;
import org.spongycastle.cms.SignerId;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.SignerInformationVerifier;
import org.spongycastle.cms.SignerInformationVerifierProvider;
import org.spongycastle.cms.bc.BcRSASignerInfoVerifierBuilder;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.spongycastle.cms.jcajce.JceKeyAgreeRecipientInfoGenerator;
import org.spongycastle.cms.jcajce.JceKeyTransEnvelopedRecipient;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientId;
import org.spongycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.spongycastle.jcajce.provider.asymmetric.X509;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.mail.smime.SMIMEEnveloped;
import org.spongycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.spongycastle.mail.smime.SMIMESigned;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.mail.smime.SMIMESignedParser;
import org.spongycastle.mail.smime.SMIMEToolkit;
import org.spongycastle.mail.smime.SMIMEUtil;
import org.spongycastle.mail.smime.validator.SignedMailValidator;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.ContentVerifierProvider;
import org.spongycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.spongycastle.operator.DigestCalculatorProvider;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.bc.BcDigestCalculatorProvider;
import org.spongycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaContentVerifierProviderBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.CollectionStore;
import org.spongycastle.util.Store;
import org.spongycastle.x509.extension.X509ExtensionUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import sa.com.is.BodyPart;
import sa.com.is.Message;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.activity.setup.AccountSettings;
import sa.com.is.crypto.DecryptedTempFileBody;
import sa.com.is.db.Trustee;
import sa.com.is.db.TrusteeManager;
import sa.com.is.internet.TextBody;
import sa.com.is.mailstore.BinaryMemoryBody;
import sa.com.is.mailstore.DecryptStreamParser;
import sa.com.is.mailstore.LocalMessage;


/**
 * Created by snouto on 07/09/15.
 */
public class SigningManager {

    private static final String RESOURCE_NAME = "org.spongycastle.mail.smime.validator.SignedMailValidatorMessages";

    private static final String PROVIDER_NAME = BouncyCastleProvider.PROVIDER_NAME;


    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";




    private Context context;

    private PrivateKey privateKey;

    private String accountEmail;

    private String passPhrase;

    public SigningManager(Context conn,String accountEmail)
    {
        this.context = conn;
        this.setAccountEmail(accountEmail);
        addMailCapabilities();
    }

    private void addMailCapabilities() {

        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);


    }


    public Message decryptMessage(Message msg){
        try
        {
            try
            {
                String passphrase = getPrivateKeyPhrase(msg);

                this.setPassPhrase(passphrase);

            }catch (Exception s)
            {
                Log.e(TAG,s.getMessage());
            }
            //Get the signing Certificate which includes the current recipient private key
            X509Certificate cert = getSigningCertificate();
            PrivateKey privKey = getPrivateKey();

            //Convert the current message into MimeMessage
            MimeMessage mimeMessage = convertMessageFromBinary(msg);

            //Create a Recipient Id
            RecipientId recipientId = new JceKeyTransRecipientId(cert);

            //create a secure SMIME Envelope
            SMIMEEnveloped m = new SMIMEEnveloped(mimeMessage);

            RecipientInformationStore recipients = m.getRecipientInfos();
            RecipientInformation recipient = recipients.get(recipientId);

            MimeBodyPart  resultantPart = SMIMEUtil.
                    toMimeBodyPart(recipient.
                            getContent(new JceKeyTransEnvelopedRecipient(privKey).setProvider(PROVIDER_NAME)));

           //Message message =  convertMimeBodyPart(msg,resultantPart);

            convertMimeBodyPart(msg,resultantPart);




        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());

        }

        return msg;
    }

    private void convertMimeBodyPart(Message msg , MimeBodyPart resultantPart)
            throws IOException, javax.mail.MessagingException, MessagingException {


        boolean signed = false;
        final String CONTENT_TYPE = "application/pkcs7-signature";
        final String MIME_TYPE = "multipart/signed";


        if(resultantPart.getContent() instanceof String){

            TextBody body = new TextBody(String.valueOf(resultantPart.getContent()));

            sa.com.is.internet.MimeMultipart mp = new sa.com.is.internet.MimeMultipart();
            mp.addBodyPart(new sa.com.is.internet.MimeBodyPart(body,"text/plain"));
            msg.setBody(mp);
        }else if (resultantPart.getContent() instanceof MimeMultipart){

            MimeMultipart mps = ((MimeMultipart)resultantPart.getContent());
            sa.com.is.internet.MimeMultipart mbps  = new sa.com.is.internet.MimeMultipart();



            for(int i = 0 ; i < mps.getCount();i++){

                javax.mail.BodyPart bp = mps.getBodyPart(i);

                if(bp.getContentType() != null && bp.getContentType().contains("multipart/mixed"))
                {
                    MimeMultipart mimeMultipart = ((MimeMultipart)bp.getContent());

                    for(int j=0;j<mimeMultipart.getCount();j++){


                        javax.mail.BodyPart bodyPart = mimeMultipart.getBodyPart(j);

                        if(bodyPart.getContentType() != null && bodyPart.getContentType().equals("text/plain"))
                        {
                            TextBody txtBody = new TextBody(String.valueOf(bodyPart.getContent()));
                            mbps.addBodyPart(new sa.com.is.internet.MimeBodyPart(txtBody));
                        }else if (bodyPart.getDisposition() != null && bodyPart.getDisposition().equals("attachment"))
                        {
                            byte[] data = new byte[0];

                            if(bodyPart.getContent() instanceof String){
                                data = ((String)bodyPart.getContent()).getBytes("UTF-8");
                            }else if (bodyPart.getContent() instanceof BASE64DecoderStream)
                            {
                                BASE64DecoderStream bds = ((BASE64DecoderStream)bodyPart.getContent());

                                data = new byte[bds.available()];

                                bds.read(data);
                                bds.close();
                            }

                            BinaryMemoryBody bmb = new BinaryMemoryBody(data,"base64");

                            sa.com.is.internet.MimeBodyPart part =  new sa.com.is.internet.MimeBodyPart(bmb);

                            Enumeration enumeration = bodyPart.getAllHeaders();

                            while(enumeration.hasMoreElements()){

                                Header header = (Header) enumeration.nextElement();

                                if(header.getValue() != null && header.getValue().contains(CONTENT_TYPE))
                                {
                                    signed = true;
                                }

                                part.setHeader(header.getName(),header.getValue());

                            }

                            mbps.addBodyPart(part);
                        }

                    }

                } else if(bp.getContentType() != null && bp.getContentType().contains("text/plain") && bp.getDisposition() == null){
                    String content = (bp.getContent() == null) ? "" :String.valueOf(bp.getContent());
                    TextBody txtbody = new TextBody(content);
                    mbps.addBodyPart(new sa.com.is.internet.MimeBodyPart(txtbody,"text/plain"));

                    continue;
                }else if (bp.getDisposition() != null && bp.getDisposition().equals("attachment")){

                    //sa.com.is.internet.MimeBodyPart mbp = DecryptStreamParser.parse(context,bp.getInputStream());

                    byte[] data = new byte[0];

                    if(bp.getContent() instanceof String){

                        data = ((String) bp.getContent()).getBytes("UTF-8");

                    }else if (bp.getContent() instanceof BASE64DecoderStream){

                        BASE64DecoderStream bds = ((BASE64DecoderStream)bp.getContent());

                        data = new byte[bds.available()];

                        bds.read(data);
                        bds.close();
                    }


                        BinaryMemoryBody bmb = new BinaryMemoryBody(data,"base64");

                        sa.com.is.internet.MimeBodyPart part =  new sa.com.is.internet.MimeBodyPart(bmb);



                        Enumeration enumeration = bp.getAllHeaders();

                        while(enumeration.hasMoreElements()){

                            Header header = (Header) enumeration.nextElement();

                            if(header.getValue() != null && header.getValue().contains(CONTENT_TYPE))
                            {
                                signed = true;
                            }

                            part.setHeader(header.getName(),header.getValue());

                        }
                        mbps.addBodyPart(part);




                }

            }

            if(signed){
                mbps.setMimeType(MIME_TYPE);
            }else
            {
                mbps.setMimeType("multipart/mixed");
            }

            msg.setBody(mbps);

            if(msg instanceof LocalMessage){

                if(signed){

                    ((LocalMessage)msg).setMimeType(MIME_TYPE);
                    ((LocalMessage)msg).setVerified(signed);
                }else
                {
                    ((LocalMessage)msg).setMimeType("multipart/alternative");
                }
            }


        }

        /*MimeMessage convertedMessage = convertMessageFromBinary(msg);
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(resultantPart);
        convertedMessage.setContent(mp);
        //now convert it back
       return convertFromMessageToISMessage(convertedMessage);*/
    }

    private List<X509Certificate> getCertificateChain() throws Exception{

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        KeyStore store = getKeyStore();

        Enumeration<String> aliases = store.aliases();
        String privateAlias = "";

        while(aliases.hasMoreElements())
        {
            privateAlias = aliases.nextElement();

            if(store.isKeyEntry(privateAlias))
            {
                KeyStore.Entry entry = store.getEntry(privateAlias,null);

                if(!(entry instanceof KeyStore.PrivateKeyEntry))
                    continue;
                privateKey = (PrivateKey) store.getKey(privateAlias,getPassPhrase().toCharArray());
                break;
            }
        }

        if(privateAlias == null)
            throw new Exception("Can't find the private key");


        Certificate[] chained = store.getCertificateChain(privateAlias);


        List<X509Certificate> certs = new ArrayList<X509Certificate>();

        for(Certificate current : chained){

            certs.add((X509Certificate)current);
        }

        return certs;

    }


    public MimeMessage encryptMessage(Message msg)
    {
        try
        {

            MimeMessage convertedMessage = convertMessageFromBinary(msg);

            //set the private key
            try
            {
                String passphrase = getPrivateKeyPhrase(msg);

                this.setPassPhrase(passphrase);

            }catch (Exception s)
            {
                Log.e(TAG,s.getMessage());
            }

            //Create the generator
            SMIMEEnvelopedGenerator  gen = new SMIMEEnvelopedGenerator();

            List<X509Certificate> certs = getRecipientsCertificates(msg);



            if(certs == null || certs.size() <= 0)
            {
                //add the signing certificate instead
                //as if he was sending for himself
                certs.add(getSigningCertificate());
            }


            for(X509Certificate cert : certs){

                gen.addRecipientInfoGenerator(new JceKeyTransRecipientInfoGenerator(cert)
                        .setProvider(PROVIDER_NAME));
            }

            MimeBodyPart mp = gen.generate(convertedMessage,
                    new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC)
                            .setProvider(PROVIDER_NAME).build());

            if(mp == null) throw new Exception("failed to encrypt the message using" +
                    " key agreement strategy- Recipient info generator");


            //Convert an Empty message without the body content
            MimeMessage emptyMessage = convertEmptyMessage(msg);

            if(emptyMessage == null) throw new Exception("Failed to send a null Message");

            emptyMessage.setContent(mp.getContent(), mp.getContentType());

            emptyMessage.saveChanges();

            return emptyMessage;


        }catch (Exception s)
        {
            s.printStackTrace();

            return null;
        }
    }

    private void addCertificate(List<X509Certificate> certs , X509Certificate cert){

        if(!certs.contains(cert)){
            certs.add(cert);
        }
    }


    private List<X509Certificate> getRecipientsCertificates(Message msg)
            throws Exception , MessagingException{

        List<X509Certificate> recipientsCertificates = new ArrayList<X509Certificate>();

        sa.com.is.Address[] fromAddresses = msg.getFrom();

        String emailFrom = null;

        if(fromAddresses != null && fromAddresses.length > 0){


            emailFrom = fromAddresses[0].getAddress();
        }




        //Get to Users
        sa.com.is.Address[] To = msg.getRecipients(Message.RecipientType.TO);

        if(To != null && To.length > 0)
        {
            TrusteeManager trusteeManager = new TrusteeManager(context);

            for(sa.com.is.Address addr : To){

                Trustee current = trusteeManager.getTrustee(addr.getAddress());

                if(current != null){

                    X509Certificate cert = readCertificateFromString(current.getEmailCertificate());

                    if(cert != null)
                        addCertificate(recipientsCertificates,cert);
                }

                if(addr != null && addr.getAddress().equals(emailFrom))
                {
                    addCertificate(recipientsCertificates,getSigningCertificate());
                }
            }
        }

        sa.com.is.Address[] CC = msg.getRecipients(Message.RecipientType.CC);

        if(CC != null && CC.length > 0){

            TrusteeManager trusteeManager = new TrusteeManager(context);

            for(sa.com.is.Address addr : CC){

                Trustee current = trusteeManager.getTrustee(addr.getAddress());

                if(current != null){

                    X509Certificate cert = readCertificateFromString(current.getEmailCertificate());

                    if(cert != null)
                        addCertificate(recipientsCertificates, cert);
                }

                if(addr != null && addr.getAddress().equals(emailFrom))
                {
                    addCertificate(recipientsCertificates,getSigningCertificate());
                }
            }
        }


        sa.com.is.Address[] BCC = msg.getRecipients(Message.RecipientType.BCC);

        if(BCC != null && BCC.length > 0){

            TrusteeManager trusteeManager = new TrusteeManager(context);

            for(sa.com.is.Address addr : BCC){

                Trustee current = trusteeManager.getTrustee(addr.getAddress());

                if(current != null){

                    X509Certificate cert = readCertificateFromString(current.getEmailCertificate());

                    if(cert != null)
                        addCertificate(recipientsCertificates,cert);
                }

                if(addr != null && addr.getAddress().equals(emailFrom))
                {
                    addCertificate(recipientsCertificates,getSigningCertificate());
                }
            }
        }

        return recipientsCertificates;

    }

    private X509Certificate readCertificateFromString(String emailCertificate) throws CertificateException {

        if(emailCertificate == null || emailCertificate.length() <= 0) return  null;

        byte[] CertificateBytes = Base64.decode(emailCertificate.getBytes(),Base64.DEFAULT);

        ByteArrayInputStream bais = new ByteArrayInputStream(CertificateBytes);

        CertificateFactory factory = CertificateFactory.getInstance("X.509");

        return ((X509Certificate)factory.generateCertificate(bais));


    }

    private Certificate getInnovatCert() {

        //Create the certificate factory
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            InputStream fis = context.getAssets().open("innovatcert.cer");

            //now generate the certificate from the input stream
            Certificate tempCert = factory.generateCertificate(fis);

            return tempCert;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    public boolean testValidation(Message msg)
    {

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        boolean validationResult = true;
        try
        {

            //convert the message
            MimeMessage mimeMessage = convertMessageFromBinary(msg);
            //Extract the Certificate
            SMIMESigned signed = new SMIMESigned((MimeMultipart)mimeMessage.getContent());

            Store certificate = signed.getCertificates();

            SignerInformationStore signers = signed.getSignerInfos();

            Collection c = signers.getSigners();

            Iterator it = c.iterator();

            SignerInformation signer = (SignerInformation)it.next();

            Collection certCollection = certificate.getMatches(signer.getSID());

            final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                    .getCertificate((X509CertificateHolder) certCollection.iterator().next());

             SignedDataVerifier verifier = new SignedDataVerifier(cert);


            CMSTypedData data = signed.getSignedContent();

            MimePart part = (MimePart) data.getContent();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            part.writeTo(baos);

            validationResult &= verifier.signatureVerified(new CMSSignedData(baos.toByteArray()));

           /* TrustAnchor trustAnchor = getTrustAnchorFromCertificate(cert);

            Set anchors = new HashSet();
            anchors.add(trustAnchor);
            //Create PKIXParameters
            PKIXParameters parameters = new PKIXParameters(anchors);
            parameters.setRevocationEnabled(false);


            SignedMailValidator validator = new SignedMailValidator(mimeMessage,parameters);

            SignerInformation signerInformation = validator
                    .getSignerInformationStore().getSigners().iterator().next();

            SignedMailValidator.ValidationResult results = validator
                    .getValidationResult(signerInformation);*/





        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());

            validationResult = false;
        }

        finally {

            return validationResult;
        }
    }

    private TrustAnchor getTrustAnchorFromCertificate(X509Certificate cert) throws Exception{

        if (cert != null)
        {
            byte[] ncBytes = cert
                    .getExtensionValue(X509Extensions.NameConstraints.getId());

            if (ncBytes != null)
            {
                ASN1Encodable extValue = X509ExtensionUtil
                        .fromExtensionValue(ncBytes);

                return new TrustAnchor(cert,extValue.toASN1Primitive().getEncoded());
            }
            return new TrustAnchor(cert, null);
        }
        return null;
    }


    public boolean doVerificationWithParameters(Message msg,String accountName)
    {
        try
        {
            this.setAccountEmail(accountName);

            Security.insertProviderAt(new BouncyCastleProvider(), 1);

            MimeMessage message = convertMessageFromBinary(msg);

            PKIXParameters param = null;


            this.setPassPhrase(getPrivateKeyPhrase(accountName));

            KeyStore store = getKeyStore();

            param = new PKIXParameters(store);

            return verifyWithParameters(message,param);

        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());
            return false;
        }
    }

    private boolean verifyWithParameters(MimeMessage msg, PKIXParameters param) throws Exception{


        boolean verificationResult = false;

        // validate signatures
        SignedMailValidator validator = new SignedMailValidator(msg, param);

        // iterate over all signatures and print results
        Iterator it = validator.getSignerInformationStore().getSigners()
                .iterator();
        while (it.hasNext()) {
            SignerInformation signer = (SignerInformation) it.next();
            SignedMailValidator.ValidationResult result = validator
                    .getValidationResult(signer);


            verificationResult = result.isValidSignature();
        }

        return verificationResult;

    }

    private  boolean verify(SMIMESigned signedMessage) throws Exception {

        Store certs = signedMessage.getCertificates();

        SignerInformationStore signers = signedMessage.getSignerInfos();

        Collection c = signers.getSigners();

        Iterator it = c.iterator();

        SignerInformation signer = (SignerInformation)it.next();

        Collection certCollection = certs.getMatches(signer.getSID());

        Iterator certIt = certCollection.iterator();

        final X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME)
                .getCertificate((X509CertificateHolder) certIt.next());



        return signedMessage.verifySignatures(new SignerInformationVerifierProvider() {
            @Override
            public SignerInformationVerifier get(SignerId signerId) throws OperatorCreationException {
                return new JcaSimpleSignerInfoVerifierBuilder().build(cert);
            }
        });



    }

    public boolean checkVerification(Message msg)
    {
       try
       {
           Security.insertProviderAt(new BouncyCastleProvider(),1);

           MimeMessage mimeMessage = convertMessage(msg);

           SMIMESigned signed = new SMIMESigned((MimeMultipart)mimeMessage.getContent());

           return this.verify(signed);

       }catch (Exception s)
       {
           Log.e(TAG,s.getMessage());
           return false;
       }
    }

    public Message convertFromMessageToISMessage(MimeMessage msg) throws IOException , MessagingException,
            javax.mail.MessagingException{

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        Message currentMessage = new sa.com.is.internet.MimeMessage(bais,false);

        return currentMessage;
    }


    private MimeMessage convertMessageFromBinary(Message msg) throws IOException, MessagingException,
            javax.mail.MessagingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        msg.writeTo(baos);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        MimeMessage mimeMessage = new MimeMessage((Session)null,bais);

        return mimeMessage;
    }


    public boolean verifyMessage(Message msg)
    {

        boolean verificationResult = true;

        try
        {
                    //Convert the Local Message into MimeMessage
            MimeMessage mimeMessage = convertMessageFromBinary(msg);

                    //Now verify the message
            if(mimeMessage.getContent() instanceof BASE64DecoderStream)
            {
                MimeMultipart mp = new MimeMultipart("signed");

                System.setProperty("mail.mime.base64.ignoreerrors", "true");

                sa.com.is.internet.MimeMultipart mps = ((sa.com.is.internet.MimeMultipart)msg.getBody());

                for(int i=0;i<mps.getCount();i++){

                    BodyPart bp = mps.getBodyPart(i);

                    if(bp.getBody() instanceof TextBody){

                        TextBody txtbody = ((TextBody)bp.getBody());
                        byte[] data = txtbody.getText().getBytes("UTF-8");

                        MimeBodyPart mbp = new MimeBodyPart(new ByteArrayInputStream(data));

                        mp.addBodyPart(mbp);

                    }else if (bp.getBody() instanceof BinaryMemoryBody){

                        BinaryMemoryBody bmb = ((BinaryMemoryBody)bp.getBody());

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmb.writeTo(baos);

                        MimeBodyPart anotherBody = new MimeBodyPart(new ByteArrayInputStream(baos.toByteArray()));

                        mp.addBodyPart(anotherBody);

                    }

                }

                /*BASE64DecoderStream decoderStream = ((BASE64DecoderStream)mimeMessage.getContent());

                int length = decoderStream.available();
                byte[] data = new byte[length];

                decoderStream.read(data);
                decoderStream.close();

                javax.mail.BodyPart bodyPart = new MimeBodyPart(new ByteArrayInputStream(data));

               *//* Enumeration enumeration = mimeMessage.getAllHeaders();

                while(enumeration.hasMoreElements()){

                    Header header = (Header) enumeration.nextElement();

                    bodyPart.addHeader(header.getName(),header.getValue());
                }
*//*
                mp.addBodyPart(bodyPart);*/


                mimeMessage.setContent(mp);

            }

            SMIMESignedParser signedContents = new SMIMESignedParser(new JcaDigestCalculatorProviderBuilder().setProvider("SC").build(),
                    (MimeMultipart)mimeMessage.getContent());



            CollectionStore certs = (CollectionStore)signedContents.getCertificates();



            SignerInformationStore signers = signedContents.getSignerInfos();


            for(Object o : signers.getSigners()){

                SignerInformation signerInformation = (SignerInformation) o;

                Collection<X509CertificateHolder> collection = certs.getMatches(signerInformation.getSID());

                if (!collection.isEmpty()) {


                    for (X509CertificateHolder certificate : collection) {


                        /*JcaContentVerifierProviderBuilder jcaContentVerifierProviderBuilder = new JcaContentVerifierProviderBuilder();
                        jcaContentVerifierProviderBuilder.setProvider(PROVIDER_NAME);

                        ContentVerifierProvider contentVerifierProvider = jcaContentVerifierProviderBuilder.build(certificate);*/

                        X509Certificate certFromSignedData = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME)
                                .getCertificate(certificate);

                        verificationResult &= validateCertificate(mimeMessage,certFromSignedData);


                        if(signerInformation.verify(new JcaSimpleSignerInfoVerifierBuilder()
                                .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(certFromSignedData.getPublicKey())))
                            verificationResult &= true;

                        else verificationResult &= false;
                    }
                }
            }

            /*Collection c = signers.getSigners();
            Iterator it = c.iterator();

            while (it.hasNext())
            {
                SignerInformation signer = (SignerInformation)it.next();


                Collection          certCollection = certs.getMatches(signer.getSID());

                Iterator        certIt = certCollection.iterator();


                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();




*//*
                X509Certificate cert = new JcaX509CertificateConverter().
                        setProvider(BouncyCastleProvider.PROVIDER_NAME)
                        .getCertificate(certHolder);*//*
                //
                // verify that the sig is correct and that it was generated
                // when the certificate was current
                //
               try
               {


                   X509Certificate certFromSignedData = new JcaX509CertificateConverter().setProvider(PROVIDER_NAME)
                           .getCertificate(certHolder);


                   boolean verificationOp = signer.verify(new JcaSimpleSignerInfoVerifierBuilder()
                           .setProvider(BouncyCastleProvider.PROVIDER_NAME).build(certFromSignedData.getPublicKey()));

                   if (verificationOp)
                   {
                       verificationResult &= true;
                   }
                   else
                   {
                       verificationResult &= false;
                   }



               }catch (Exception s)
               {
                   verificationResult &= false;
               }
            }*/

        }catch (Exception s)
        {
            Log.e("SigningManager",s.getMessage());
            verificationResult = false;
        }

        finally {

            return verificationResult;
        }
    }

    private boolean validateCertificate(MimeMessage message , X509Certificate certFromSignedData) {

        boolean finalResult = true;

       try
       {

           //Check the validity of date
           finalResult &= (certFromSignedData.getNotAfter().after(new Date()));

           //is the signer email equals the from message email
           if(certFromSignedData.getSubjectDN() != null)
           {
               String principalName = certFromSignedData.getSubjectDN().getName();

               if(principalName == null || principalName.isEmpty()) throw new Exception();



               Pattern p = Pattern.compile(EMAIL_PATTERN);

               principalName = principalName.replace("E=","");

               Matcher matcher = p.matcher(principalName);



               if(matcher.matches())
               {
                   String foundEmail = matcher.group(0);

                   if(foundEmail == null || foundEmail.isEmpty()) throw new Exception();

                   finalResult &= foundEmail.equals(getEmailAddress(message.getFrom()[0].toString()));

               }else throw new Exception();


           }else finalResult &= false;

       }catch (Exception s)
       {
           finalResult = false;
       }

        finally {

           return finalResult;
       }
    }


    private String getEmailAddress(String input) throws Exception
    {
        input = sanitizeInput(input);

        String email = "";

        Pattern p = Pattern.compile(EMAIL_PATTERN);

        Matcher matcher = p.matcher(input);

        if(matcher.matches())
        {
            email = matcher.group(0);
        }

        return email;
    }

    private String sanitizeInput(String input) {

        if(input != null){

            if(input.contains("<") || input.contains(">"))
            {
                int start = input.indexOf("<");
                int end = input.indexOf(">");
                input = input.substring(start+1,end);
            }
        }


        return input;
    }

    private MimeMessage convertEmptyMessage(Message mm){


        try
        {
            MimeMessage message = new MimeMessage((Session)null);
            message.setSubject(mm.getSubject());
            Address fromAddress = convertAddress(mm.getFrom()[0]);
            message.setFrom(fromAddress);
            //copy To
            Address[] TO = convertAddresses(mm.getRecipients(Message.RecipientType.TO));

            if(TO != null && TO.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.TO,TO);
            }

            //copy CC
            Address[] CC = convertAddresses(mm.getRecipients(Message.RecipientType.CC));

            if(CC != null && CC.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.CC,CC);
            }

            //copy BCC
            Address[] BCC = convertAddresses(mm.getRecipients(Message.RecipientType.BCC));

            if(BCC != null && BCC.length >0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.BCC, BCC);
            }

            //copy Reply To
            Address[] replyTo = convertAddresses(mm.getReplyTo());

            if(replyTo != null && replyTo.length > 0)
            {
                message.setReplyTo(replyTo);
            }
            return message;

        }catch (Exception s)
        {
            Log.e("SigningManager",s.getMessage());
            return null;
        }




    }

    private MimeMessage convertMessage(Message mm)
    {
        try
        {
            MimeMessage message = new MimeMessage((Session)null);
            message.setSubject(mm.getSubject());
            Address fromAddress = convertAddress(mm.getFrom()[0]);
            message.setFrom(fromAddress);
            //copy To
            Address[] TO = convertAddresses(mm.getRecipients(Message.RecipientType.TO));

            if(TO != null && TO.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.TO,TO);
            }

            //copy CC
            Address[] CC = convertAddresses(mm.getRecipients(Message.RecipientType.CC));

            if(CC != null && CC.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.CC,CC);
            }

            //copy BCC
            Address[] BCC = convertAddresses(mm.getRecipients(Message.RecipientType.BCC));

            if(BCC != null && BCC.length >0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.BCC,BCC);
            }

            //copy Reply To
            Address[] replyTo = convertAddresses(mm.getReplyTo());

            if(replyTo != null && replyTo.length > 0)
            {
                message.setReplyTo(replyTo);
            }
            //set the message content
            if(!(mm.getBody() instanceof sa.com.is.internet.MimeMultipart))
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mm.getBody().writeTo(baos);

                message.setContent(baos.toString("UTF-8"), "text/plain");
            }else
            {
                sa.com.is.internet.MimeMultipart multipart = (sa.com.is.internet.MimeMultipart)mm.getBody();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                multipart.writeTo(baos);

                message.setContent(baos.toString("UTF-8"), "text/plain");


                 multipart = (sa.com.is.internet.MimeMultipart)mm.getBody();

                //define the new mimemultipart
                MimeMultipart mimemm = new MimeMultipart();
                String subType = "";
                if(multipart.getMimeType() != null && !multipart.getMimeType().isEmpty())
                {
                    if(multipart.getMimeType().contains("/"))
                    {
                        subType = multipart.getMimeType().split("/")[1];

                        mimemm.setSubType(subType);
                    }
                }


                for(BodyPart part :multipart.getBodyParts())
                {
                    javax.mail.BodyPart mimePart = new MimeBodyPart();

                    fillMimeBodyHeaders(part, mimePart);

                    if(part.getBody().getEncoding().equals("base64"))
                    {
                        //that means it is an attachment
                        baos = new ByteArrayOutputStream();
                        part.getBody().writeTo(baos);
                        byte[] decodedAttachment = Base64.decode(baos.toByteArray(),Base64.DEFAULT);

                        DataSource attachmentSource = new ByteArrayDataSource(decodedAttachment,part.getMimeType());
                        mimePart.setDataHandler(new DataHandler(attachmentSource));

                    }else
                    {
                        baos = new ByteArrayOutputStream();
                        part.getBody().writeTo(baos);
                        mimePart.setContent(baos.toString("UTF-8"),part.getContentType());
                    }

                    mimemm.addBodyPart(mimePart);
                }

                baos = new ByteArrayOutputStream();
                mimemm.writeTo(baos);
               message.setContent(mimemm);
            }

            //message.saveChanges();


            return message;

        }catch (Exception s)
        {
            Log.e("SigningManager",s.getMessage());
            return null;
        }
    }




    private static final String TAG = "SigningManager";

    public MimeMessage signMessage(Message mm)
    {
        try
        {

            try
            {
                String passphrase = getPrivateKeyPhrase(mm);

                this.setPassPhrase(passphrase);

            }catch (Exception s)
            {
                Log.e(TAG,s.getMessage());
            }
            //create a java mail mime message
            MimeMessage message = new MimeMessage((Session)null);
            message.setSubject(mm.getSubject());
            Address fromAddress = convertAddress(mm.getFrom()[0]);
            message.setFrom(fromAddress);
            //copy To
            Address[] TO = convertAddresses(mm.getRecipients(Message.RecipientType.TO));

            if(TO != null && TO.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.TO,TO);
            }

            //copy CC
            Address[] CC = convertAddresses(mm.getRecipients(Message.RecipientType.CC));

            if(CC != null && CC.length > 0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.CC,CC);
            }

            //copy BCC
            Address[] BCC = convertAddresses(mm.getRecipients(Message.RecipientType.BCC));

            if(BCC != null && BCC.length >0)
            {
                message.setRecipients(javax.mail.Message.RecipientType.BCC,BCC);
            }

            //copy Reply To
            Address[] replyTo = convertAddresses(mm.getReplyTo());

            if(replyTo != null && replyTo.length > 0)
            {
                message.setReplyTo(replyTo);
            }
            //set the message content
            if(!(mm.getBody() instanceof sa.com.is.internet.MimeMultipart))
            {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mm.getBody().writeTo(baos);

                message.setContent(baos.toString("UTF-8"), "text/plain");
            }else
            {
                sa.com.is.internet.MimeMultipart multipart = (sa.com.is.internet.MimeMultipart)mm.getBody();

                //define the new mimemultipart
                MimeMultipart mimemm = new MimeMultipart();
                String subType = "";
                if(multipart.getMimeType() != null && !multipart.getMimeType().isEmpty())
                {
                    if(multipart.getMimeType().contains("/"))
                    {
                        subType = multipart.getMimeType().split("/")[1];

                        mimemm.setSubType(subType);
                    }
                }


                for(BodyPart part :multipart.getBodyParts())
                {
                    javax.mail.BodyPart mimePart = new MimeBodyPart();

                    fillMimeBodyHeaders(part, mimePart);

                    if(part.getBody().getEncoding().equals("base64"))
                    {
                        //that means it is an attachment
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        part.getBody().writeTo(baos);
                        byte[] decodedAttachment = Base64.decode(baos.toByteArray(),Base64.DEFAULT);

                        DataSource attachmentSource = new ByteArrayDataSource(decodedAttachment,part.getMimeType());
                        mimePart.setDataHandler(new DataHandler(attachmentSource));

                    }else
                    {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        part.getBody().writeTo(baos);
                        mimePart.setContent(baos.toString("UTF-8"),part.getContentType());
                    }

                    mimemm.addBodyPart(mimePart);
                }

                message.setContent(mimemm);
            }

            message.saveChanges();

            //now begin the signing process

            return doSigningProcess(message);

        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());
            return null;
        }
    }

    private void fillMimeBodyHeaders(BodyPart part, javax.mail.BodyPart mimePart) throws Exception {

        String[] headers = {"Content-Type","Content-Transfer-Encoding","Content-Disposition"};

        for(String headerName :headers)
        {
            String[] headerValues = part.getHeader(headerName);

            if(headerValues != null && headerValues.length > 0)
            {
                mimePart.setHeader(headerName,headerValues[0]);
            }
        }
    }

    private MimeMessage doSigningProcess(MimeMessage message) throws Exception{

        //Get the signing certificate
        X509Certificate signingCertificate = getSigningCertificate();

        //Define the signing Attributes
        ASN1EncodableVector signedAttrs = new ASN1EncodableVector();
        SMIMECapabilityVector caps = new SMIMECapabilityVector();
        caps.addCapability(SMIMECapability.aES256_CBC);
        caps.addCapability(SMIMECapability.dES_EDE3_CBC);
        caps.addCapability(SMIMECapability.dES_CBC);
        signedAttrs.add(new SMIMECapabilitiesAttribute(caps));

        IssuerAndSerialNumber issAndSer = new IssuerAndSerialNumber(
                new X500Name(signingCertificate.getSubjectDN().getName()),
                signingCertificate.getSerialNumber());
        signedAttrs.add(new SMIMEEncryptionKeyPreferenceAttribute(issAndSer));


        //Create the s/mime signing generator
        SMIMESignedGenerator gen = new SMIMESignedGenerator();

        ContentSigner sha1Signer = new JcaContentSignerBuilder(signingCertificate.getSigAlgName()).setProvider("SC").build(getPrivateKey());

        gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("SC").build())
                .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(signedAttrs)))
                .build(sha1Signer, signingCertificate));

        gen.addCertificates(new JcaCertStore(Arrays.asList(signingCertificate)));


        //Now do the actual Signing
        MimeMultipart mm = gen.generate(message);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        mm.writeTo(baos);

        MimeMessage signedMessage = new MimeMessage((Session)null);

        Enumeration<?> headers = message.getAllHeaderLines();

        while (headers.hasMoreElements()) {
            signedMessage.addHeaderLine((String) headers.nextElement());
        }
        signedMessage.setContent(mm);
        signedMessage.saveChanges();


        return signedMessage;


    }

    private X509Certificate getSigningCertificate() throws Exception{

        //Load the Security provider with the highest priority to make it used

        Security.insertProviderAt(new BouncyCastleProvider(), 1);

        KeyStore store = getKeyStore();

        Enumeration<String> aliases = store.aliases();
        String privateAlias = "";

        while(aliases.hasMoreElements())
        {
            privateAlias = aliases.nextElement();

            if(store.isKeyEntry(privateAlias))
            {
                KeyStore.Entry entry = store.getEntry(privateAlias,null);

                if(!(entry instanceof KeyStore.PrivateKeyEntry))
                    continue;
                privateKey = (PrivateKey) store.getKey(privateAlias,getPassPhrase().toCharArray());
                break;
            }
        }


        //get the certificate
        X509Certificate signingCertificate = (X509Certificate) store.getCertificate(privateAlias);

        return signingCertificate;
    }

    private KeyStore getKeyStore() throws Exception {

        KeyStore store = KeyStore.getInstance("PKCS12",BouncyCastleProvider.PROVIDER_NAME);
        // KeyStore store = KeyStore.getInstance("AndroidKeyStore");

        //get the certificate location
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String certificateLocation = prefs.getString(getAccountEmail(), null);
        InputStream is = context.getContentResolver().openInputStream(Uri.parse(certificateLocation));
        store.load(is, getPassPhrase().toCharArray());
        is.close();

        return store;


    }

    private String getPrivateKeyPhrase(String AccountName){

        String key = AccountSettings.KEY_PASSPHRASE+AccountName;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String passPhrase = preferences.getString(key,null);
        return passPhrase;
    }

    private String getPrivateKeyPhrase(Message message) throws Exception
    {
        String key = AccountSettings.KEY_PASSPHRASE+message.getFrom()[0].getAddress();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.context);

        String passPhrase = preferences.getString(key,null);

        return passPhrase;
    }

    private PrivateKey getPrivateKey() {

        return this.privateKey;
    }

    private Address[] convertAddresses(sa.com.is.Address[] recipients) throws Exception {

        if(recipients != null && recipients.length >0)
        {
            List<Address> addresses = new ArrayList<Address>();

            for(sa.com.is.Address addr :recipients)
            {
                addresses.add(new InternetAddress(addr.getAddress()));
            }

            return addresses.toArray(new Address[]{});

        }else return null;
    }

    private Address convertAddress(sa.com.is.Address address) throws Exception {

        return new InternetAddress(address.getAddress());
    }


    public String getAccountEmail() {
        return accountEmail;
    }

    public void setAccountEmail(String accountEmail) {
        this.accountEmail = accountEmail;
    }

    public String getPassPhrase() {

        if(passPhrase == null) return "";
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }


    static {

        Security.insertProviderAt(new BouncyCastleProvider(),1);
    }


}
