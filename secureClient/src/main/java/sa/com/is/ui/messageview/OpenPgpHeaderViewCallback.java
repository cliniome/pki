package sa.com.is.ui.messageview;


import android.app.PendingIntent;


interface OpenPgpHeaderViewCallback {
    void onPgpSignatureButtonClick(PendingIntent pendingIntent);
}
