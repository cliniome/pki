package sa.com.is.store;

import android.content.Context;
import android.net.ConnectivityManager;

import sa.com.is.MessagingException;
import sa.com.is.ServerSettings;
import sa.com.is.ServerSettings.Type;
import sa.com.is.Store;
import sa.com.is.ssl.DefaultTrustedSocketFactory;
import sa.com.is.ssl.TrustedSocketFactory;
import sa.com.is.store.imap.ImapStore;
import sa.com.is.store.pop3.Pop3Store;
import sa.com.is.store.webdav.WebDavStore;

import java.util.HashMap;
import java.util.Map;

public abstract class RemoteStore extends Store {
    public static final int SOCKET_CONNECT_TIMEOUT = 30000;
    public static final int SOCKET_READ_TIMEOUT = 60000;

    protected StoreConfig mStoreConfig;
    protected TrustedSocketFactory mTrustedSocketFactory;

    /**
     * Remote stores indexed by Uri.
     */
    private static Map<String, Store> sStores = new HashMap<String, Store>();


    public RemoteStore(StoreConfig storeConfig, TrustedSocketFactory trustedSocketFactory) {
        mStoreConfig = storeConfig;
        mTrustedSocketFactory = trustedSocketFactory;
    }

    /**
     * Get an instance of a remote mail store.
     */
    public synchronized static Store getInstance(Context context,
                                                 StoreConfig storeConfig) throws MessagingException {
        String uri = storeConfig.getStoreUri();

        if (uri.startsWith("local")) {
            throw new RuntimeException("Asked to get non-local Store object but given LocalStore URI");
        }

        Store store = sStores.get(uri);
        if (store == null) {
            if (uri.startsWith("imap")) {
                store = new ImapStore(storeConfig,
                        new DefaultTrustedSocketFactory(context),
                        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
            } else if (uri.startsWith("pop3")) {
                store = new Pop3Store(storeConfig,
                        new DefaultTrustedSocketFactory(context));
            } else if (uri.startsWith("webdav")) {
                store = new WebDavStore(storeConfig);
            }

            if (store != null) {
                sStores.put(uri, store);
            }
        }

        if (store == null) {
            throw new MessagingException("Unable to locate an applicable Store for " + uri);
        }

        return store;
    }

    /**
     * Release reference to a remote mail store instance.
     *
     * @param storeConfig {@link sa.com.is.store.StoreConfig} instance that is used to get the remote mail store instance.
     */
    public static void removeInstance(StoreConfig storeConfig) {
        String uri = storeConfig.getStoreUri();
        if (uri.startsWith("local")) {
            throw new RuntimeException("Asked to get non-local Store object but given " +
                    "LocalStore URI");
        }
        sStores.remove(uri);
    }

    /**
     * Decodes the contents of store-specific URIs and puts them into a {@link ServerSettings}
     * object.
     *
     * @param uri
     *         the store-specific URI to decode
     *
     * @return A {@link ServerSettings} object holding the settings contained in the URI.
     *
     * @see sa.com.is.store.imap.ImapStore#decodeUri(String)
     * @see sa.com.is.store.pop3.Pop3Store#decodeUri(String)
     * @see sa.com.is.store.webdav.WebDavStore#decodeUri(String)
     */
    public static ServerSettings decodeStoreUri(String uri) {
        if (uri.startsWith("imap")) {
            return ImapStore.decodeUri(uri);
        } else if (uri.startsWith("pop3")) {
            return Pop3Store.decodeUri(uri);
        } else if (uri.startsWith("webdav")) {
            return WebDavStore.decodeUri(uri);
        } else {
            throw new IllegalArgumentException("Not a valid store URI");
        }
    }

    /**
     * Creates a store URI from the information supplied in the {@link ServerSettings} object.
     *
     * @param server
     *         The {@link ServerSettings} object that holds the server settings.
     *
     * @return A store URI that holds the same information as the {@code server} parameter.
     *
     * @see sa.com.is.store.imap.ImapStore#createUri(ServerSettings)
     * @see sa.com.is.store.pop3.Pop3Store#createUri(ServerSettings)
     * @see sa.com.is.store.webdav.WebDavStore#createUri(ServerSettings)
     */
    public static String createStoreUri(ServerSettings server) {
        if (Type.IMAP == server.type) {
            return ImapStore.createUri(server);
        } else if (Type.POP3 == server.type) {
            return Pop3Store.createUri(server);
        } else if (Type.WebDAV == server.type) {
            return WebDavStore.createUri(server);
        } else {
            throw new IllegalArgumentException("Not a valid store URI");
        }
    }
}
