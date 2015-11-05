package sa.com.is.ui.message;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import sa.com.is.K9;
import sa.com.is.Message;
import sa.com.is.mailstore.LocalMessageExtractor;
import sa.com.is.mailstore.MessageViewInfo;
import sa.com.is.ui.crypto.MessageCryptoAnnotations;


public class DecodeMessageLoader extends AsyncTaskLoader<MessageViewInfo> {
    private final Message message;
    private MessageViewInfo messageViewInfo;
    private MessageCryptoAnnotations annotations;

    public DecodeMessageLoader(Context context, Message message, MessageCryptoAnnotations annotations) {
        super(context);
        this.message = message;
        this.annotations = annotations;
    }

    @Override
    protected void onStartLoading() {
        if (messageViewInfo != null) {
            super.deliverResult(messageViewInfo);
        }

        if (takeContentChanged() || messageViewInfo == null) {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(MessageViewInfo messageViewInfo) {
        this.messageViewInfo = messageViewInfo;
        super.deliverResult(messageViewInfo);
    }

    @Override
    public MessageViewInfo loadInBackground() {
        try {
            return LocalMessageExtractor.decodeMessageForView(getContext(), message, annotations);
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error while decoding message", e);
            return null;
        }
    }
}
