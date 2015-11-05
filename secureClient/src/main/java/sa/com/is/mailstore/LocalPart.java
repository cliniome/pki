package sa.com.is.mailstore;


public interface LocalPart {
    String getAccountUuid();
    long getId();
    String getDisplayName();
    long getSize();
    boolean isFirstClassAttachment();
    LocalMessage getMessage();
}
