package sa.com.is.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

import sa.com.is.AddTrusteeActivity;
import sa.com.is.R;
import sa.com.is.db.Trustee;
import sa.com.is.db.TrusteeManager;

/**
 * Created by snouto on 18/11/15.
 */
public class ShowTrusteesActivity extends K9Activity {

    private static final String TAG = "ShowTrusteesActivity";
    private ListView trusteesList;
    private static final int ADD_TRUSTEE_OPTION = 1001;
    private ArrayAdapter<String> adapter;

    private TrusteeManager trusteeManager;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_trustees_activity);
        initViews();
    }


    private void initViews()
    {
        try
        {
            trusteeManager = new TrusteeManager(this);
            //access the list view and the button
            trusteesList = (ListView)findViewById(R.id.trusteeslist);
            //get all trustees
            List<String> availableTrustees = trusteeManager.getAllTrustees();
            //initialize the array adapter
            adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1,availableTrustees);

           trusteesList.setLongClickable(true);
            trusteesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position
                        , long l) {

                   final String item = ShowTrusteesActivity.this.adapter.getItem(position);
                    AlertDialog dialog = new AlertDialog.Builder(ShowTrusteesActivity.this)
                            .setTitle("Choose An Option....")
                            .setItems(new String[]{"Delete Trustee.."}, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {


                                    //delete that now
                                    TrusteeManager trusteeManager = new TrusteeManager(ShowTrusteesActivity.this);
                                    trusteeManager.deleteTrusteeByEmail(item);
                                    dialogInterface.dismiss();
                                    //Notify the adapter
                                    updateViews();

                                }
                            }).create();

                    dialog.show();





                    return true;

                }
            });



            //add the adapter to the list view
            trusteesList.setAdapter(adapter);
            //then notify the adapter that some data have been inserted into it
            // to update its views
            adapter.notifyDataSetChanged();
            //access the button
            Button addTrusteeBtn = (Button)findViewById(R.id.addtrusteebtn);

            addTrusteeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //now open the add Trustee Activity

                    Intent addTrusteeIntent = new Intent(ShowTrusteesActivity.this
                            ,AddTrusteeActivity.class);
                    startActivityForResult(addTrusteeIntent,ADD_TRUSTEE_OPTION);

                }
            });

        }catch (Exception s)
        {
            Log.e(TAG,s.getMessage());
        }
    }


    private void updateViews(){

        List<String> availableTrustees = trusteeManager.getAllTrustees();
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,availableTrustees);
        trusteesList.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ADD_TRUSTEE_OPTION){

            updateViews();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
