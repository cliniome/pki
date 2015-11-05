package sa.com.is.store.imap;

import sa.com.is.AuthType;
import sa.com.is.ConnectionSecurity;
import sa.com.is.NetworkType;

/**
 * Settings source for IMAP. Implemented in order to remove coupling between {@link ImapStore} and {@link ImapConnection}.
 */
interface ImapSettings {
    String getHost();

    int getPort();

    ConnectionSecurity getConnectionSecurity();

    AuthType getAuthType();

    String getUsername();

    String getPassword();

    String getClientCertificateAlias();

    boolean useCompression(NetworkType type);

    String getPathPrefix();

    void setPathPrefix(String prefix);

    String getPathDelimiter();

    void setPathDelimiter(String delimiter);

    String getCombinedPrefix();

    void setCombinedPrefix(String prefix);
}
