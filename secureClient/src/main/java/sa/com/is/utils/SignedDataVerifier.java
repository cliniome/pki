package sa.com.is.utils;

import org.spongycastle.cert.jcajce.JcaCertStoreBuilder;
import org.spongycastle.cms.CMSException;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataParser;
import org.spongycastle.cms.SignerId;
import org.spongycastle.cms.SignerInformation;
import org.spongycastle.cms.SignerInformationStore;
import org.spongycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.spongycastle.util.Store;

import java.io.IOException;
        import java.security.GeneralSecurityException;
        import java.security.cert.CertPathBuilder;
        import java.security.cert.CertStore;
        import java.security.cert.PKIXBuilderParameters;
        import java.security.cert.PKIXCertPathBuilderResult;
        import java.security.cert.TrustAnchor;
        import java.security.cert.X509CertSelector;
        import java.security.cert.X509Certificate;
        import java.util.Collection;
        import java.util.Collections;
        import java.util.Iterator;

/**
 * Validator for CMS SignedData objects which verifies the signature and that the certificate path is valid as well.
 */
public class SignedDataVerifier
{
    private final X509Certificate trustAnchor;

    /**
     * Base constructor.
     *
     * @param trustAnchor the root certificate that certificate paths must extend from.
     */
    public SignedDataVerifier(X509Certificate trustAnchor)
    {
        this.trustAnchor = trustAnchor;
    }

    /**
     * Verify the passed in CMS signed data, return false on failure.
     *
     * @param cmsData a CMSSignedData object.
     * @return true if signature checks out, false if there is a problem with the signature or the path to its verifying certificate.
     */
    public boolean signatureVerified(CMSSignedData cmsData)
    {
        Store certs = cmsData.getCertificates();
        SignerInformationStore signers = cmsData.getSignerInfos();

        Collection c = signers.getSigners();
        Iterator it = c.iterator();

        SignerInformation signer = (SignerInformation)it.next();

        try
        {
            PKIXCertPathBuilderResult result = checkCertPath(signer.getSID(), certs);

            X509Certificate cert = (X509Certificate)result.getCertPath().getCertificates().get(0);

            return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Verify the passed in CMS signed data, return false on failure.
     * <p>
     * Note: this method assumes the parser has been freshly created and its content not read or drained.
     * </p>
     *
     * @param cmsParser a CMSSignedData object.
     * @return true if signature checks out, false if there is a problem with the signature or the path to its verifying certificate.
     */
    public boolean signatureVerified(CMSSignedDataParser cmsParser)
            throws IOException, CMSException
    {
        cmsParser.getSignedContent().drain();

        Store certs = cmsParser.getCertificates();
        SignerInformationStore signers = cmsParser.getSignerInfos();

        Collection c = signers.getSigners();
        Iterator it = c.iterator();

        SignerInformation signer = (SignerInformation)it.next();

        try
        {
            PKIXCertPathBuilderResult result = checkCertPath(signer.getSID(), certs);

            X509Certificate cert = (X509Certificate)result.getCertPath().getCertificates().get(0);

            return signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(cert));
        }
        catch (Exception e)
        {
            // TODO: logging?
            return false;
        }
    }

    private PKIXCertPathBuilderResult checkCertPath(SignerId signerId, Store certs)
            throws IOException, GeneralSecurityException
    {
        CertStore store = new JcaCertStoreBuilder().setProvider("BC").addCertificates(certs).build();

        CertPathBuilder pathBuilder = CertPathBuilder.getInstance("PKIX","BC");
        X509CertSelector targetConstraints = new X509CertSelector();

        targetConstraints.setIssuer(signerId.getIssuer().getEncoded());
        targetConstraints.setSerialNumber(signerId.getSerialNumber());

        PKIXBuilderParameters params = new PKIXBuilderParameters(Collections.singleton(new TrustAnchor(trustAnchor, null)), targetConstraints);

        params.addCertStore(store);
        params.setRevocationEnabled(false);            // TODO: CRLs?

        return (PKIXCertPathBuilderResult)pathBuilder.build(params);
    }
}