package sa.com.is.utils.transport;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;


import net.suberic.crypto.EncryptionKeyManager;
import net.suberic.crypto.EncryptionManager;
import net.suberic.crypto.EncryptionUtils;

import org.spongycastle.asn1.ASN1EncodableVector;
import org.spongycastle.asn1.cms.AttributeTable;
import org.spongycastle.asn1.cms.IssuerAndSerialNumber;
import org.spongycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.spongycastle.asn1.smime.SMIMECapability;
import org.spongycastle.asn1.smime.SMIMECapabilityVector;
import org.spongycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
//import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.mail.smime.SMIMESignedGenerator;
import org.spongycastle.util.Store;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import sa.com.is.Account;
import sa.com.is.Address;
import sa.com.is.Message;
import sa.com.is.MessagingException;
import sa.com.is.R;
import sa.com.is.ServerSettings;
import sa.com.is.Transport;
import sa.com.is.filter.EOLConvertingOutputStream;
import sa.com.is.filter.LineWrapOutputStream;
import sa.com.is.filter.SmtpDataStuffing;
import sa.com.is.transport.SmtpTransport;
import sa.com.is.utils.EmailConstants;

/**
 * Created by snouto on 16/08/15.
 */
public class SecureTransport extends Transport {


    private Context context;

    private String certificateLocation;
    private Account account;

    private ServerSettings settings;

    public SecureTransport(Context con, Account account)
    {
        this.context = con;
        this.account = account;
    }


    public SecureTransport(){}

    @Override
    public void open() throws MessagingException {

        try
        {
            if(this.context != null)
            {
                //Get the preferences
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.context);

                this.certificateLocation = prefs.getString(context
                        .getString(R.string.IS_CERT_CONTENT_LOCATION),null);

                this.settings = SmtpTransport.decodeUri(account.getTransportUri());


                MailcapCommandMap mailcap = (MailcapCommandMap) CommandMap
                        .getDefaultCommandMap();

                mailcap
                        .addMailcap("application/pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_signature");

                mailcap
                        .addMailcap("application/pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.pkcs7_mime");
                mailcap
                        .addMailcap("application/x-pkcs7-signature;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_signature");
                mailcap
                        .addMailcap("application/x-pkcs7-mime;; x-java-content-handler=org.spongycastle.mail.smime.handlers.x_pkcs7_mime");
                mailcap
                        .addMailcap("multipart/signed;; x-java-content-handler=org.spongycastle.mail.smime.handlers.multipart_signed");

                CommandMap.setDefaultCommandMap(mailcap);

            }

        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());
        }
    }

    /*

    InputStream is = context.getContentResolver().openInputStream(Uri.parse(this.certificateLocation));
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            String base64EncodedString = Base64.encodeToString(data,Base64.DEFAULT);
     */

    @Override
    public void sendMessage(Message message) throws MessagingException {

        try
        {
            this.open();

            Security.addProvider(new BouncyCastleProvider());

            KeyStore store = KeyStore.getInstance("PKCS12","BC");
            //Load the certificate
            InputStream is = context.getContentResolver().openInputStream(Uri.parse(this.certificateLocation));

            store.load(is,"snouto".toCharArray());

            is.close();

            Enumeration<String> aliases = store.aliases();
            String privateAlias = "";
            //Get the alias
            if(aliases.hasMoreElements())
                privateAlias = aliases.nextElement();

            if(privateAlias != null)
            {
                //begin sending the email
              //this.beginSendingEmail(store,privateAlias,message);
                //this.beginSmimeCryptoSend(privateAlias ,message);
            }



        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());
        }

        finally
        {
            close();
        }

    }

    private void beginSmimeCryptoSend(String alias , Message message)
    {
        try
        {
            Properties props = System.getProperties();
            props.put(EmailConstants.MAIL_TRANSPORT_PROTOCOL,EmailConstants.SMTPS);
            props.put(EmailConstants.MAIL_SMTP_AUTH,true);
            props.put(EmailConstants.MAIL_HOST,settings.host);
            props.put(EmailConstants.MAIL_SMTP_USER,settings.username);
            props.put(EmailConstants.MAIL_SMTP_PASSWORD,settings.password);
            props.put(EmailConstants.MAIL_PORT,settings.port);
            props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_ENABLE,true);
            props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_REQUIRED,true);
            props.put(EmailConstants.MAIL_TRANSPORT_TLS,true);
            props.put("mail.smtp.socketFactory.port", "465");
            props.put(EmailConstants.MAIL_SMTP_SOCKET_FACTORY_PORT,settings.port);
            props.put(EmailConstants.MAIL_SMTP_SSL_SOCKET_FACTORY_PORT,settings.port);
            props.put(EmailConstants.MAIL_SMTP_SSL_ENABLE,true);
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");


            //Create the session
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(settings.username,settings.password);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            //Add from
            this.addFrom(mimeMessage, message);
            this.addTo(mimeMessage, message);
            this.addCC(mimeMessage, message);
            this.addBCC(mimeMessage, message);

            //set the subject
            mimeMessage.setSubject(message.getSubject());

            ByteArrayOutputStream mOut = new ByteArrayOutputStream(2048);

            message.getBody().writeTo(mOut);
            mOut.flush();
            mimeMessage.setContent(new String(mOut.toByteArray()), "text/plain");
            mimeMessage.saveChanges();




            //Begin the encryption in here
            EncryptionUtils smimeUtils = EncryptionManager.getEncryptionUtils(EncryptionManager.SMIME);

            EncryptionKeyManager smimeKeyMgr = smimeUtils.createKeyManager();

            InputStream is = context.getContentResolver().openInputStream(Uri.parse(this.certificateLocation));
            smimeKeyMgr.loadPrivateKeystore(is, "snouto".toCharArray());

            java.security.Key smimeKey = smimeKeyMgr.getPrivateKey(alias,"snouto".toCharArray());

            MimeMessage smimeSignedMsg = smimeUtils.signMessage(session, mimeMessage, smimeKey);

            //Now try to send the message
            javax.mail.Transport transport = session.getTransport("smtps");
            transport.connect(settings.host, settings.username, settings.password);
            transport.sendMessage(smimeSignedMsg,smimeSignedMsg.getAllRecipients());
            transport.close();




        }catch (Exception s)
        {
            Log.e("SecureTransport",s.getMessage());
        }
    }




    private void beginSendingEmail(KeyStore store,String alias,Message message) {

        try
        {
            Certificate[] certs = store.getCertificateChain(alias);

              /* Get the private key to sign the message with */
            PrivateKey privateKey = (PrivateKey)store.getKey(alias,
                    "snouto".toCharArray());
            if (privateKey == null)
            {
                throw new Exception("cannot find private key for alias: ");
            }

            Properties props = System.getProperties();
            props.put(EmailConstants.MAIL_TRANSPORT_PROTOCOL,EmailConstants.SMTPS);
            props.put(EmailConstants.MAIL_SMTP_AUTH,true);
            props.put(EmailConstants.MAIL_HOST,settings.host);
            props.put(EmailConstants.MAIL_SMTP_USER,settings.username);
            props.put(EmailConstants.MAIL_SMTP_PASSWORD,settings.password);
            props.put(EmailConstants.MAIL_PORT,settings.port);
            props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_ENABLE,true);
            props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_REQUIRED,true);
            props.put(EmailConstants.MAIL_TRANSPORT_TLS,true);
            props.put("mail.smtp.socketFactory.port", "465");
            props.put(EmailConstants.MAIL_SMTP_SOCKET_FACTORY_PORT,settings.port);
            props.put(EmailConstants.MAIL_SMTP_SSL_SOCKET_FACTORY_PORT,settings.port);
            props.put(EmailConstants.MAIL_SMTP_SSL_ENABLE,true);
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

           /* props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_ENABLE,true);
            props.put(EmailConstants.MAIL_TRANSPORT_STARTTLS_REQUIRED,true);
            props.put(EmailConstants.MAIL_TRANSPORT_TLS,true);
            props.put(EmailConstants.MAIL_TRANSPORT_PROTOCOL,EmailConstants.SMTP);
            props.put("mail.smtp.socketFactory.port", settings.port);
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put(EmailConstants.MAIL_SMTP_SSL_ENABLE,true);
            props.put(EmailConstants.MAIL_SMTP_SSL_SOCKET_FACTORY_PORT,settings.port);*/


            //Create the session
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(settings.username,settings.password);
                }
            });

            MimeMessage mimeMessage = new MimeMessage(session);
            //Add from
            this.addFrom(mimeMessage, message);
            this.addTo(mimeMessage, message);
            this.addCC(mimeMessage, message);
            this.addBCC(mimeMessage, message);

            //set the subject
            mimeMessage.setSubject(message.getSubject());

            ByteArrayOutputStream mOut = new ByteArrayOutputStream(2048);

            message.getBody().writeTo(mOut);
            mOut.flush();
            mimeMessage.setContent(new String(mOut.toByteArray()), "text/plain");
            mimeMessage.saveChanges();


            //  Create the SMIMESignedGenerator
            SMIMECapabilityVector capabilities = new SMIMECapabilityVector();
            capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
            capabilities.addCapability(SMIMECapability.rC2_CBC, 128);
            capabilities.addCapability(SMIMECapability.dES_CBC);

            ASN1EncodableVector attributes = new ASN1EncodableVector();
            attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(
                    new IssuerAndSerialNumber(
                            new X500Name(((X509Certificate) certs[0])
                                    .getIssuerDN().getName()),
                            ((X509Certificate) certs[0]).getSerialNumber())));
            attributes.add(new SMIMECapabilitiesAttribute(capabilities));

            SMIMESignedGenerator signer = new SMIMESignedGenerator();
            signer.addSignerInfoGenerator(new JcaSimpleSignerInfoGeneratorBuilder()
                    .setProvider(BouncyCastleProvider.PROVIDER_NAME).
                            setSignedAttributeGenerator(new AttributeTable(attributes)).
                            build("DSA".equals(privateKey.getAlgorithm())
                                    ? "SHA1withDSA" : "MD5withRSA", privateKey, (X509Certificate) certs[0]));
              // Add the list of certs to the generator
            List certList = new ArrayList();
            certList.add(certs[0]);
            Store certificates = new JcaCertStore(certList);
            signer.addCertificates(certificates);





            MimeMultipart mm = signer.generate(mimeMessage);
            MimeMessage signedMessage = new MimeMessage(session);



             //Set all original MIME headers in the signed message
            Enumeration headers = mimeMessage.getAllHeaderLines();
            while (headers.hasMoreElements())
            {
                signedMessage.addHeaderLine((String)headers.nextElement());
            }

           //  Set the content of the signed message
            signedMessage.setContent(mm);
            signedMessage.saveChanges();



            //Now send the signed message
            javax.mail.Transport transport = session.getTransport("smtps");
            transport.connect(settings.host,settings.username,settings.password);
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
            transport.close();

        }catch (Exception s)
        {
            Log.e("SecureTransport",s.getMessage());

        }

        finally {
            close();
        }
    }

    private void addBCC(MimeMessage mimeMessage, Message message)
            throws AddressException , javax.mail.MessagingException , MessagingException{

        //Add To
        Address[] addresses = message.getRecipients(Message.RecipientType.BCC);

        List<javax.mail.Address> addrs = new ArrayList<>();

        for(Address addr : addresses)
        {
            addrs.add(new InternetAddress(addr.getAddress()));
        }

        mimeMessage.addRecipients(javax.mail.Message.RecipientType.BCC,
                addrs.toArray(new javax.mail.Address[]{}));

    }

    private void addCC(MimeMessage mimeMessage, Message message)
            throws AddressException , javax.mail.MessagingException , MessagingException {

        //Add To
        Address[] addresses = message.getRecipients(Message.RecipientType.CC);

        List<javax.mail.Address> addrs = new ArrayList<>();

        for(Address addr : addresses)
        {
            addrs.add(new InternetAddress(addr.getAddress()));
        }

        mimeMessage.addRecipients(javax.mail.Message.RecipientType.CC,
                addrs.toArray(new javax.mail.Address[]{}));

    }

    private void addTo(MimeMessage mimeMessage, Message message)
    throws AddressException , javax.mail.MessagingException , MessagingException{

            //Add To
            Address[] addresses = message.getRecipients(Message.RecipientType.TO);

            List<javax.mail.Address> addrs = new ArrayList<>();

            for(Address addr : addresses)
            {
                addrs.add(new InternetAddress(addr.getAddress()));
            }

        mimeMessage.addRecipients(javax.mail.Message.RecipientType.TO,
                addrs.toArray(new javax.mail.Address[]{}));




    }

    private void addFrom(MimeMessage mimeMessage, Message message) throws AddressException,
            javax.mail.MessagingException {

        if(message.getFrom()  == null || message.getFrom().length <=0) return;

        if(message.getFrom() != null)
        {
            Address[] addrs = message.getFrom();

            List<javax.mail.Address> addresses = new ArrayList<>();

            for(Address addr : addrs)
            {
                addresses.add(new InternetAddress(addr.getAddress()));
            }

            mimeMessage.addFrom(addresses.toArray(new javax.mail.Address[]{}));
        }
    }


    @Override
    public void close() {

    }


}
