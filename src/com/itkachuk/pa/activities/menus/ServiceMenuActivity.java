package com.itkachuk.pa.activities.menus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.management.AccountsManagementActivity;
import com.itkachuk.pa.activities.management.CategoriesManagementActivity;
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
        
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
    }

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
	      	case R.id.accounts_button:
		    	Log.d(TAG, "clicked on \"Accounts\"");
		        intent = new Intent(ServiceMenuActivity.this, AccountsManagementActivity.class);
		        startActivity(intent);	        
		    break;
	         
	      	case R.id.categories_button:
	      		Log.d(TAG, "clicked on \"Categories\"");
	      		intent = new Intent(ServiceMenuActivity.this, CategoriesManagementActivity.class);
	      		startActivity(intent);
	        break;

	      	case R.id.program_settings_button:
	      		Log.d(TAG, "clicked on \"Program Settings\"");
	    	    PreferencesEditorActivity.callMe(ServiceMenuActivity.this);
	      	break;
		}	
	}
}
