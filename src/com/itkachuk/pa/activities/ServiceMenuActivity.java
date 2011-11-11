package com.itkachuk.pa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class ServiceMenuActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnClickListener {
	private static final String TAG = "PocketAccountant";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_menu);
        
        // Set up click listeners for all the buttons
        View accountsButton = findViewById(R.id.accounts_button);
        accountsButton.setOnClickListener(this);
        View categoriesButton = findViewById(R.id.categories_button);
        categoriesButton.setOnClickListener(this);
        View programSettingsButton = findViewById(R.id.program_settings_button);
        programSettingsButton.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
	      	case R.id.accounts_button:
		    	Log.d(TAG, "clicked on \"Accounts\"");
		        intent = new Intent(ServiceMenuActivity.this, AccountsManagementActivity.class);
		        //intent.putExtra(HistoryReportActivity.EXTRAS_IS_EXPENSE, true);
		        startActivity(intent);
		        
		    break;
	         
	      	case R.id.categories_button:
	      		Log.d(TAG, "clicked on \"Categories\"");

	        break;

	      	case R.id.program_settings_button:
	      		Log.d(TAG, "clicked on \"Program Settings\"");
	    	  
	      	break;
		}	
	}
}
