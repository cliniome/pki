package sa.com.is.test;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.cms.CMSProcessable;
import org.spongycastle.cms.CMSProcessableByteArray;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.Store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import sa.com.is.Body;
import sa.com.is.BodyPart;
import sa.com.is.internet.MimeMessage;
import sa.com.is.internet.MimeMultipart;
import sa.com.is.internet.TextBody;

/**
 * Created by snouto on 06/09/15.
 */
@RunWith(AndroidJUnit4.class)
public class SignatureVerification {

        private Context instrumentationCtx;


    private static final String MAIL_LOCATION="~/Desktop/mail.mail";
    private static final String EMAIL_CONTENTS = "Delivered-To: mfawzy@is.com.sa\n" +
            "Received: by 10.64.54.201 with SMTP id l9csp550569iep;\n" +
            "        Sun, 6 Sep 2015 01:19:46 -0700 (PDT)\n" +
            "X-Received: by 10.129.57.85 with SMTP id g82mr13906079ywa.143.1441527586426;\n" +
            "        Sun, 06 Sep 2015 01:19:46 -0700 (PDT)\n" +
            "Return-Path: <snouto_snouto@hotmail.com>\n" +
            "Received: from BLU004-OMC3S27.hotmail.com (blu004-omc3s27.hotmail.com. [65.55.116.102])\n" +
            "        by mx.google.com with ESMTPS id m139si4946216yke.157.2015.09.06.01.19.46\n" +
            "        for <mfawzy@is.com.sa>\n" +
            "        (version=TLSv1.2 cipher=ECDHE-RSA-AES128-SHA bits=128/128);\n" +
            "        Sun, 06 Sep 2015 01:19:46 -0700 (PDT)\n" +
            "Received-SPF: pass (google.com: domain of snouto_snouto@hotmail.com designates 65.55.116.102 as permitted sender) client-ip=65.55.116.102;\n" +
            "Authentication-Results: mx.google.com;\n" +
            "       spf=pass (google.com: domain of snouto_snouto@hotmail.com designates 65.55.116.102 as permitted sender) smtp.mailfrom=snouto_snouto@hotmail.com;\n" +
            "       dmarc=pass (p=NONE dis=NONE) header.from=hotmail.com\n" +
            "Received: from BLU437-SMTP7 ([65.55.116.73]) by BLU004-OMC3S27.hotmail.com over TLS secured channel with Microsoft SMTPSVC(7.5.7601.23008);\n" +
            "\t Sun, 6 Sep 2015 01:19:45 -0700\n" +
            "X-TMN: [ag9lm/kQc3DJjp8/ABlFg6Nk0d3mp6Zr]\n" +
            "X-Originating-Email: [snouto_snouto@hotmail.com]\n" +
            "Message-ID: <BLU437-SMTP71FFA71CB6802186B5EB390550@phx.gbl>\n" +
            "Return-Path: snouto_snouto@hotmail.com\n" +
            "Date: Sun, 6 Sep 2015 11:19:31 +0300\n" +
            "From: Mohamed <ahmed_mohamed@hotmail.com>\n" +
            "To: mfawzy@is.com.sa\n" +
            "Subject: Hello Opaque DS\n" +
            "User-Agent: IS-SecureEmail for Android\n" +
            "MIME-Version: 1.0\n" +
            "Content-Type: multipart/signed; protocol=\"application/pkcs7-signature\";\n" +
            "\tmicalg=sha1; boundary=\"----KQJ0ZM17HFPP6A843L0C64KYKM79QV\"\n" +
            "X-OriginalArrivalTime: 06 Sep 2015 08:19:42.0905 (UTC) FILETIME=[C85FBA90:01D0E87C]\n" +
            "\n" +
            "------KQJ0ZM17HFPP6A843L0C64KYKM79QV\n" +
            "Content-Type: text/plain;charset=utf-8;\n" +
            "Content-Transfer-Encoding: quoted-printable\n" +
            "\n" +
            "This is an Opaque DS\n" +
            "------KQJ0ZM17HFPP6A843L0C64KYKM79QV\n" +
            "Content-Type: application/pkcs7-signature; name=\"smime.p7s\"\n" +
            "Content-Transfer-Encoding: base64\n" +
            "Content-Disposition: attachment; filename=\"smime.p7s\"\n" +
            "Content-Description: S/MIME Cryptographic Signature\n" +
            "\n" +
            "MIAGCSqGSIb3DQEHAqCAMIACAQExCzAJBgUrDgMCGgUAMIAGCSqGSIb3DQEHAaCAJIAEFFRoaXMg\n" +
            "aXMgYW4gT3BhcXVlIERTAAAAAAAAoIAwggVJMIIEMaADAgECAhATVmw7CjSkgSbORYbLdnqyMA0G\n" +
            "CSqGSIb3DQEBCwUAMIGbMQswCQYDVQQGEwJHQjEbMBkGA1UECBMSR3JlYXRlciBNYW5jaGVzdGVy\n" +
            "MRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8gQ0EgTGltaXRlZDFBMD8GA1UEAxM4\n" +
            "Q09NT0RPIFNIQS0yNTYgQ2xpZW50IEF1dGhlbnRpY2F0aW9uIGFuZCBTZWN1cmUgRW1haWwgQ0Ew\n" +
            "HhcNMTUwODMxMDAwMDAwWhcNMTYwODMwMjM1OTU5WjAqMSgwJgYJKoZIhvcNAQkBFhlzbm91dG9f\n" +
            "c25vdXRvQGhvdG1haWwuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoK2Uq2ze\n" +
            "pd/Vhci1Gt94t1ZPhEYoQp4p8vHFlXo8ZdPTC70heedTv3L9kbjQnKHXS1jyP3dtpWBPhU+HKamc\n" +
            "2Q4YvTVAD0ohKOnFeEaoWXBYcBqPB9cKXT6LN9l/m8wKBxIWT16XIROLr6NQ1ql9rrg9p+iOxCiR\n" +
            "93O/0wNOHDDMqyzHWyCUFB/O9IKtdesO2wB+JcFXymL0rLMepDDBIUxTOGDVrjqq2gjF6yvl8+GN\n" +
            "0tBbDOHivGcEBWNFvD5pqUtgdwxap2eBmRhMZGUFsgTKxGn6ZDpUCJvhRAiz1IQcVFTVm6WK30vL\n" +
            "GX7gxjlGo2deDEpwLRghMxAcWp2AJQIDAQABo4IB9zCCAfMwHwYDVR0jBBgwFoAUkmFrguGioKpP\n" +
            "7GfxwqP3tIAAwewwHQYDVR0OBBYEFK48fG9plAc5nAtyH6CWdUG9Dbx0MA4GA1UdDwEB/wQEAwIF\n" +
            "oDAMBgNVHRMBAf8EAjAAMCAGA1UdJQQZMBcGCCsGAQUFBwMEBgsrBgEEAbIxAQMFAjARBglghkgB\n" +
            "hvhCAQEEBAMCBSAwRgYDVR0gBD8wPTA7BgwrBgEEAbIxAQIBAQEwKzApBggrBgEFBQcCARYdaHR0\n" +
            "cHM6Ly9zZWN1cmUuY29tb2RvLm5ldC9DUFMwXQYDVR0fBFYwVDBSoFCgToZMaHR0cDovL2NybC5j\n" +
            "b21vZG9jYS5jb20vQ09NT0RPU0hBMjU2Q2xpZW50QXV0aGVudGljYXRpb25hbmRTZWN1cmVFbWFp\n" +
            "bENBLmNybDCBkAYIKwYBBQUHAQEEgYMwgYAwWAYIKwYBBQUHMAKGTGh0dHA6Ly9jcnQuY29tb2Rv\n" +
            "Y2EuY29tL0NPTU9ET1NIQTI1NkNsaWVudEF1dGhlbnRpY2F0aW9uYW5kU2VjdXJlRW1haWxDQS5j\n" +
            "cnQwJAYIKwYBBQUHMAGGGGh0dHA6Ly9vY3NwLmNvbW9kb2NhLmNvbTAkBgNVHREEHTAbgRlzbm91\n" +
            "dG9fc25vdXRvQGhvdG1haWwuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQAr2m6QlJu4bTqd9hCvgPRi\n" +
            "kudLW/BVkPiBTpjc/s9FFXfBRGPYGEmLEN6GO+AAdIngI9dBrMrz3JwQ1cRqe9AP/DOqQTfhapDg\n" +
            "KMO8i0C/SoSlHFkWqBHIUX2a2izh4ywobOOUR3SyJBEi/SS+Fwl5EIr6tUn+DZlEFc1q9N3xihZt\n" +
            "0ak905AZj8O8OxhN1gxDCf+Z5Tnr/kwCYmREtAluVAzscFcSmSglhGyfDJBX/QcVzJ/b2rVdx3A0\n" +
            "8o7lDhWYvPmXauzKocEO9KQAwmoQqwSNvvgG/4dQcXRVa14E59ZjCGdtCslx4OUzElF/Ldnawlbu\n" +
            "h7T6ICk/B06WXIctMIIErzmohamejduyjhjhueejyxUSg1OJrWFuelRnayEwDQYJKoZIhvcNAQEL\n" +
            "BQAwbzELMAkGA1UEBhMCU0UxFDASBgNVBAoTC0FkZFRydXN0IEFCMSYwJAYDVQQLEx1BZGRUcnVz\n" +
            "dCBFeHRlcm5hbCBUVFAgTmV0d29yazEiMCAGA1UEAxMZQWRkVHJ1c3QgRXh0ZXJuYWwgQ0EgUm9v\n" +
            "dDAeFw0xNDEyMjIwMDAwMDBaFw0yMDA1MzAxMDQ4MzhaMIGbMQswCQYDVQQGEwJHQjEbMBkGA1UE\n" +
            "CBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8g\n" +
            "Q0EgTGltaXRlZDFBMD8GA1UEAxM4Q09NT0RPIFNIQS0yNTYgQ2xpZW50IEF1dGhlbnRpY2F0aW9u\n" +
            "IGFuZCBTZWN1cmUgRW1haWwgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCJsQ3a\n" +
            "elMZTnBSHbxWpgYmt7hJ4JbnUavx8FoTSRWjtIwbYLx6UUKneYykIt8XYU6R1XYjChTTSgJ/th0J\n" +
            "gG6lBD3ZursW/qGHqS5DUkMWfK8yUMimT1rpCNjPkyWce4joMGTmpPhWgP0qJBQzF5msROVpi6NG\n" +
            "BkvCM9TpQJ8GsLGsk0C5tQiTOpwqU6MQ2z0gYTxVA47ZTnYlAiEp+qN8cXZP7uFfgen7VIDbw3s1\n" +
            "UreE3iI9LDAtMX9ZvVI3sDNpLUPr+tal8Zd3Z1GM2e4n67ylBzh2jKSpOP/fjPUDrEm+yvdzmToP\n" +
            "MquclToTPQ5GOld0YVC+xkA/y+Tin6IhAgMBAAGjggEXMIIBEzAfBgNVHSMEGDAWgBStvZh6NLQm\n" +
            "9/rEJlTvA73gJMtUGjAdBgNVHQ4EFgQUkmFrguGioKpP7GfxwqP3tIAAwewwDgYDVR0PAQH/BAQD\n" +
            "AgGGMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0lBBYwFAYIKwYBBQUHAwIGCCsGAQUFBwMEMBEG\n" +
            "A1UdIAQKMAgwBgYEVR0gADBEBgNVHR8EPTA7MDmgN6A1hjNodHRwOi8vY3JsLnVzZXJ0cnVzdC5j\n" +
            "b20vQWRkVHJ1c3RFeHRlcm5hbENBUm9vdC5jcmwwNQYIKwYBBQUHAQEEKTAnMCUGCCsGAQUFBzAB\n" +
            "hhlodHRwOi8vb2NzcC51c2VydHJ1c3QuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQAbKm6sVcE6q4jF\n" +
            "2O3NVfOqa2ErwAkQI5kPxWZqb7H1tLV3Xg8CYQDffQX+ErOkgIAA/PsdW2pyAgpBvAW6wVjVJsLq\n" +
            "1U2E+/6CmM9YG+MiY5xS+LsFNqt9WKXeqztj5drVc+/s4Pt74qP/8EIjnMq2jU0+5EsYA7KoLdTY\n" +
            "u0JLkGmFENumNzToe+ABEKWcyjrHn0+ING6KZdAairup3MrKNtH0/MJkKTWv1rGncRHSA0Oxjz6a\n" +
            "7J4yU/R2ksqGNAe5LMrmHErYmQ3BhuKQkvtaQmojIRDpZcf11bt+6oyFIAJi6tE6ByxZxZkz8jiJ\n" +
            "5bbpFnofeRT2ShAaJvp8ivubMIIENjCCAx6gAwIBAgIBATANBgkqhkiG9w0BAQUFADBvMQswCQYD\n" +
            "VQQGEwJTRTEUMBIGA1UEChMLQWRkVHJ1c3QgQUIxJjAkBgNVBAsTHUFkZFRydXN0IEV4dGVybmFs\n" +
            "IFRUUCBOZXR3b3JrMSIwIAYDVQQDExlBZGRUcnVzdCBFeHRlcm5hbCBDQSBSb290MB4XDTAwMDUz\n" +
            "MDEwNDgzOFoXDTIwMDUzMDEwNDgzOFowbzELMAkGA1UEBhMCU0UxFDASBgNVBAoTC0FkZFRydXN0\n" +
            "IEFCMSYwJAYDVQQLEx1BZGRUcnVzdCBFeHRlcm5hbCBUVFAgTmV0d29yazEiMCAGA1UEAxMZQWRk\n" +
            "VHJ1c3QgRXh0ZXJuYWwgQ0EgUm9vdDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALf3\n" +
            "GjPm8gAELTngTlvtH7xsD821+iO2zt6bETOXpClMfZOfvUq8k+0DGuOPz+VtUFrWlymUWoCwSXrb\n" +
            "LpX9uMq/NzgtHj6RQa1wVsfwTz/oMp50ysiQVOnGXw94nZpAPA6sYapeFI+eh6FqUNzXmk6vBbOm\n" +
            "cZSccbNQYArHE504B4YCqOmoaSYYkKtMsE8jqzpPhNjfzp/haW+710LXa0Tkx63ubUFfclpxCDez\n" +
            "eWWkWaCUN/cALw3CknLa0Dhy2xSoRcRdKn23tNbE7qzNE0S3ySvdQwAl+mG5aWpYIxG3pzOPVnVZ\n" +
            "9c0p10a3CitlttNCbxWyuHv77+ldU9U0WicCAwEAAaOB3DCB2TAdBgNVHQ4EFgQUrb2YejS0Jvf6\n" +
            "xCZU7wO94CTLVBowCwYDVR0PBAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wgZkGA1UdIwSBkTCBjoAU\n" +
            "rb2YejS0Jvf6xCZU7wO94CTLVBqhc6RxMG8xCzAJBgNVBAYTAlNFMRQwEgYDVQQKEwtBZGRUcnVz\n" +
            "dCBBQjEmMCQGA1UECxMdQWRkVHJ1c3QgRXh0ZXJuYWwgVFRQIE5ldHdvcmsxIjAgBgNVBAMTGUFk\n" +
            "ZFRydXN0IEV4dGVybmFsIENBIFJvb3SCAQEwDQYJKoZIhvcNAQEFBQADggEBALCb4IUlwtYj4g+W\n" +
            "BpKdQZic2YR5gdkeWxQHIzZlj7DYd7usQWxHYINRsPkyPef89iYTx4AWpb9a/IfPeHmJIZriTAcK\n" +
            "hjW88t5RxNKWt9x+Tu5w/Rw56wwCURQtjr0W4MHfRnXnJK3s9EK0hZNwEGe6nQY1ShjTK3rMUUKh\n" +
            "emPR5ruhxSvCNr4TDea9Y355e6cJDUCrat2PisP29owaQgVR1EX1n6diIWgVIEM8med8vSTYqZEX\n" +
            "c4g/VhsxOBi0cQ+azcgOno4uG+GMmIPLHzHxREzGBHNJdmAPx/i9F4BrLunMTA5amnkPIAou1Z5j\n" +
            "Jh5VkpTYghdae9C8x49OhgQAADGCA1wwggNYAgEBMIGwMIGbMQswCQYDVQQGEwJHQjEbMBkGA1UE\n" +
            "CBMSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHEwdTYWxmb3JkMRowGAYDVQQKExFDT01PRE8g\n" +
            "Q0EgTGltaXRlZDFBMD8GA1UEAxM4Q09NT0RPIFNIQS0yNTYgQ2xpZW50IEF1dGhlbnRpY2F0aW9u\n" +
            "IGFuZCBTZWN1cmUgRW1haWwgQ0ECEBNWbDsKNKSBJs5Fhst2erIwCQYFKw4DAhoFAKCCAYAwGAYJ\n" +
            "KoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMTUwOTA2MDgxOTM0WjAjBgkq\n" +
            "hkiG9w0BCQQxFgQUinoaBMP1iA/UbVIx5fQmvRpLmyswWwYJKoZIhvcNAQkPMU4wTDALBglghkgB\n" +
            "ZQMEASowCwYJYIZIAWUDBAECMAsGCWCGSAFlAwQBFjAKBggqhkiG9w0DBzAOBggqhkiG9w0DAgIC\n" +
            "AIAwBwYFKw4DAgcwgcMGCyqGSIb3DQEJEAILMYGzoIGwMIGbMQswCQYDVQQGEwJHQjEbMBkGA1UE\n" +
            "CAwSR3JlYXRlciBNYW5jaGVzdGVyMRAwDgYDVQQHDAdTYWxmb3JkMRowGAYDVQQKDBFDT01PRE8g\n" +
            "Q0EgTGltaXRlZDFBMD8GA1UEAww4Q09NT0RPIFNIQS0yNTYgQ2xpZW50IEF1dGhlbnRpY2F0aW9u\n" +
            "IGFuZCBTZWN1cmUgRW1haWwgQ0ECEBNWbDsKNKSBJs5Fhst2erIwDQYJKoZIhvcNAQEBBQAEggEA\n" +
            "YI2JzFzDmA6aQGLVZTEoj7/3hMhjgK1tcjYla1Dr+ZMYytyU1dhDf9hVhnAgJaiBtdnHO9bqPeCx\n" +
            "qT3xxsoWNCPuLOhi1IEA36HuDcVf6WbznSCA72JP7dm6Yzjqip1gumjFjzg3IRaB7OwJUeAUYZ2R\n" +
            "Mf1CdrP1NhXPr7IdTFIACIeOVKKkHfTzkdrRbD+Rs7JjX6gxr+3F7lG0A01A+emVPy46tLmkwIQN\n" +
            "6y7BvvlX8kri8s1b2ddXsK5NXpduL2AJHyEJx7N781BByRW1H8tkXGzzz56jp45rBhrWj9bTpAT/\n" +
            "7aTgvbUmwVAmsk5cL5xFncjTmuoy7bqwtPN4IgAAAAAAAA==\n" +
            "\n" +
            "------KQJ0ZM17HFPP6A843L0C64KYKM79QV--\n";


        @Before
        public void onInit()
        {
               this.instrumentationCtx = InstrumentationRegistry.getContext();
        }

    @Test
    public void loadMail()
    {
        try {

            ByteArrayInputStream inputStream = new ByteArrayInputStream(EMAIL_CONTENTS.getBytes("UTF-8"));

            MimeMessage originalMessage = new MimeMessage(inputStream,true);

           // Log.i("SignatureVerification", "Message Subject:" + originalMessage.getSubject());


            if(originalMessage.getBody() instanceof MimeMultipart)
            {
                    MimeMultipart parts = (MimeMultipart)originalMessage.getBody();


                    //get the message body
                    BodyPart msgBody = parts.getBodyPart(0);

                    Log.i("SignatureVerification","Text Body ContentType"+msgBody.getContentType());

                    ByteArrayOutputStream msgText = new ByteArrayOutputStream();

                    msgBody.getBody().writeTo(msgText);




                   // Log.i("SignatureVerification","Message Body :" + new String(msgText.toByteArray(),"UTF-8"));

                    int sign_index = parts.getCount() - 1;

                    BodyPart signBody = parts.getBodyPart(sign_index);

                    Log.i("SignatureVerification","Sign Body Disposition:"+signBody.getHeader("Content-Transfer-Encoding")[0]);

                    ByteArrayOutputStream bodyout = new ByteArrayOutputStream();

                    signBody.getBody().writeTo(bodyout);

                    //now verify the message
                    verify(msgText.toByteArray(),bodyout.toByteArray());

                  //  Log.i("SignatureVerification",new String(bodyout.toByteArray()));
            }



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch (Exception s)
        {
            s.printStackTrace();
        }
    }



        private void verify(byte[] messageContents, byte[] ds) {

                try
                {
                       // CMSProcessable msgContents = new CMSProcessableByteArray(messageContents);
                        CMSSignedData envelopedData = new CMSSignedData(Base64.decode(ds,Base64.DEFAULT));

                        //Get the certificates
                        Store certs = envelopedData.getCertificates();

                        //get the signers infos
                        SignerInformationStore signerInfos = envelopedData.getSignerInfos();

                        Collection c = signerInfos.getSigners();

                        Iterator iterator = c.iterator();

                        while(iterator.hasNext())
                        {
                                SignerInformation signer = (SignerInformation) iterator.next();
                                Collection certCollection = certs.getMatches(signer.getSID());
                                Iterator certIt = certCollection.iterator();
                                X509CertificateHolder certHolder = (X509CertificateHolder) certIt.next();
                                X509Certificate cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate(certHolder);
                                if (signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert))) {
                                     Log.i("SignatureVerification","Verification Status : Verified");
                                }else
                                        Log.i("SignatureVerification","Verification Status : Not Verified");
                        }




                }catch (Exception s)
                {
                        Log.i("SignatureVerification",s.getMessage());
                }
        }
}
