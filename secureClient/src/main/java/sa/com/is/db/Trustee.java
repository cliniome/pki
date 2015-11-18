package sa.com.is.db;

/**
 * Created by snouto on 18/11/15.
 */
public class Trustee {

    private String ID;
    private String emailAddress;
    private String emailCertificate;

    public Trustee(){}
    public Trustee(String emailAddress , String emailCertificate)
    {
        this.setEmailAddress(emailAddress);
        this.setEmailCertificate(emailCertificate);
    }




    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailCertificate() {
        return emailCertificate;
    }

    public void setEmailCertificate(String emailCertificate) {
        this.emailCertificate = emailCertificate;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
}
