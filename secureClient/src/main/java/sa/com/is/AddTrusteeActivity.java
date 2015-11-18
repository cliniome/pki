package sa.com.is;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import sa.com.is.activity.K9Activity;
import sa.com.is.db.Trustee;
import sa.com.is.db.TrusteeManager;

/**
 * Created by snouto on 18/11/15.
 */
public class AddTrusteeActivity extends K9Activity {


    private static final String TAG="AddTrusteeActivity";
    private static final int PICK_CERTIFICATE_OPTION = 101;

    private EditText emailAddress;
    private byte[] certificateBytes;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trustee_layout);
        initView();
    }

    private void initView() {

        try
        {
            //get the Email Address
            emailAddress = (EditText)findViewById(R.id.emailAddress);
            //get the "Add Trustee" button
            Button trusteeCertBtn = (Button)findViewById(R.id.pickCertBtn);

            //set on click listener
            trusteeCertBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Pick the certificate
                    if(Build.VERSION.SDK_INT < 19)
                    {
                        Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        filePickerIntent.setType("*/*");


                        //now start the current intent
                        startActivityForResult(Intent.createChooser(filePickerIntent, "Pick a Certificate")
                                , PICK_CERTIFICATE_OPTION);
                    }else {

                        Intent filePickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        filePickerIntent.setType("*/*");
                        filePickerIntent.addCategory(Intent.CATEGORY_OPENABLE);


                        //now start the current intent
                        startActivityForResult(Intent.createChooser(filePickerIntent, "Pick a Certificate")
                                , PICK_CERTIFICATE_OPTION);
                    }
                }
            });

        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == PICK_CERTIFICATE_OPTION && resultCode == RESULT_OK){

            //get the uri of the certificate being picked
            Uri certLoc = data.getData();
            //now try to open this uri
            try {


                if(certLoc == null) return;

                InputStream certStream = this.getContentResolver().openInputStream(certLoc);
                certificateBytes = new byte[certStream.available()];
                //read all the data from the input Stream
                certStream.read(certificateBytes, 0, certificateBytes.length);
                //close the input stream
                certStream.close();
                //convert the binary data into Base64 Default to be stored into the database
                String encodedCertificate = Base64.encodeToString(certificateBytes,Base64.DEFAULT);
                //now create a trustee to add directly
                Trustee newTrustee = new Trustee(emailAddress.getText().toString(),encodedCertificate);

                //now try to save him/her into the database
                TrusteeManager trusteeManager = new TrusteeManager(this);

                boolean result = trusteeManager.addTrustee(newTrustee);

                String message = "There was a problem , Please try again !";

                if(result){
                    message = "Trustee/Recipient has been added successfully";
                }


                //create an alert Dialog now
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //now you have to finish the current activity
                                AddTrusteeActivity.this.finish();

                            }
                        }).setTitle("Trustee Operation")
                        .setMessage(message)
                        .setCancelable(false)
                        .create();

                dialog.show();



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage());
            }catch (IOException s){
                s.printStackTrace();
                Log.e(TAG,s.getMessage());
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }
}
