package sa.com.is.ui.message;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import sa.com.is.K9;
import sa.com.is.Message;
import sa.com.is.crypto.MessageDecryptVerifier;
import sa.com.is.mailstore.LocalMessageExtractor;
import sa.com.is.mailstore.MessageViewInfo;
import sa.com.is.ui.crypto.MessageCryptoAnnotations;
import sa.com.is.ui.messageview.MessageViewFragment;


public class DecodeMessageLoader extends AsyncTaskLoader<MessageViewInfo> {
    private final Message message;
    private MessageViewInfo messageViewInfo;
    private MessageCryptoAnnotations annotations;
    private MessageViewFragment messageViewFragment;

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
            //download full message if signed or encrypted
            //TODO: Snouto Uncomment thisnc
           /* if(messageViewFragment != null){
                if((MessageDecryptVerifier.isSignedEmail(message) || MessageDecryptVerifier.isEncryptedEmail(message))){

                    messageViewFragment.downloadCompleteMessage();
                }
            }*/
            return LocalMessageExtractor.decodeMessageForView(getContext(), message, annotations);
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, "Error while decoding message", e);
            return null;
        }
    }

    public MessageViewFragment getMessageViewFragment() {
        return messageViewFragment;
    }

    public void setMessageViewFragment(MessageViewFragment messageViewFragment) {
        this.messageViewFragment = messageViewFragment;
    }
}
