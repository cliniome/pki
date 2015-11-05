package org.openintents.openpgp.util;

import android.util.Log;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by snouto on 12/08/15.
 */
public class CertificateHelper {


    private static final String TAG = "Certificate";


    public static X509Certificate getCertificateByName(String certificateName)
    {
        X509Certificate certificate = null;

        try
        {

                KeyStore ks = KeyStore.getInstance("AndroidCAStore");
                ks.load(null, null);
                Enumeration aliases = ks.aliases();
                while (aliases.hasMoreElements()) {
                    String alias = (String)aliases.nextElement();
                    X509Certificate cert = (X509Certificate)
                            ks.getCertificate(alias);
                    Log.d(TAG, "Subject DN: " +
                            cert.getSubjectDN().getName());
                    Log.d(TAG, "Subject SN: " +
                            cert.getSerialNumber().toString());
                    Log.d(TAG, "Issuer DN: " +
                            cert.getIssuerDN().getName());

                    String name = parseName(cert.getIssuerDN().getName());

                    if(name.equals(certificateName))
                    {
                        certificate = cert;
                        break;
                    }
                }




        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());
            return null;
        }

        finally {

            return certificate;
        }
    }

    private static String parseName(String certName)
    {
        String certificateName = "";
        try
        {


            String[] splitted = certName.split(",");

            if(splitted != null && splitted.length > 0)
            {
                for(String current : splitted)
                {
                    if(current.startsWith("CN") || current.startsWith("OU") ||
                            current.startsWith("O"))
                    {
                        certificateName = (current.split("=")[1]);
                    }
                }
            }

        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());

        }

        finally {

            return certificateName;
        }

    }


    public static List<CertificateLocator> getInstalledUserCertificates()
    {
        List<CertificateLocator> locators = new ArrayList<CertificateLocator>();

        try
        {
            KeyStore ks = KeyStore.getInstance("AndroidCAStore");
            ks.load(null, null);
            Enumeration aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = (String)aliases.nextElement();
                X509Certificate cert = (X509Certificate)
                        ks.getCertificate(alias);
                Log.d(TAG, "Subject DN: " +
                        cert.getSubjectDN().getName());
                Log.d(TAG, "Subject SN: " +
                        cert.getSerialNumber().toString());
                Log.d(TAG, "Issuer DN: " +
                        cert.getIssuerDN().getName());

                locators.add(new CertificateLocator(cert.getIssuerDN().getName(),
                        cert.toString()));
            }

        }catch (Exception s)
        {
            Log.e("Error",s.getMessage());

        }

        return locators;
    }



   static class CertificateLocator
    {
        private String certName;
        private String certLocation;

        public CertificateLocator(String cert_name , String cert_loc)
        {

            this.parseName(cert_name);
            this.setCertLocation(cert_loc);
        }

        private void parseName(String certName)
        {
            try
            {
                String[] splitted = certName.split(",");

                if(splitted != null && splitted.length > 0)
                {
                    for(String current : splitted)
                    {
                        if(current.startsWith("CN") || current.startsWith("OU") ||
                                current.startsWith("O"))
                        {
                            this.certName = (current.split("=")[1]);
                        }
                    }
                }

            }catch (Exception s)
            {
                Log.e("Error",s.getMessage());
            }
            finally {

                if(this.certName == null || this.certName.length() <=0 || this.certName.isEmpty())
                    this.certName = certName;
            }
        }


        @Override
        public String toString() {

            return this.getCertName();
        }

        public String getCertName() {
            return certName;
        }

        public void setCertName(String certName) {
            this.certName =certName;
            this.parseName(this.certName);
        }

        public String getCertLocation() {
            return certLocation;
        }

        public void setCertLocation(String certLocation) {
            this.certLocation = certLocation;
        }
    }
}
