package com.itkachuk.pa.activities.menus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.reports.CommonReportActivity;
import com.itkachuk.pa.activities.reports.ConsolidatedReportActivity;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.utils.DateUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class ReportsMenuActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnClickListener {
	private static final String TAG = "PocketAccountant";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reports_menu);
        
        // Set up click listeners for all the buttons
        View historyReportButton = findViewById(R.id.history_report_button);
        historyReportButton.setOnClickListener(this);
        View commonReportButton = findViewById(R.id.common_report_button);
        commonReportButton.setOnClickListener(this);
        View consolidatedReportButton = findViewById(R.id.consolidated_report_button);
        consolidatedReportButton.setOnClickListener(this);
        
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
    }

    @Override
    protected void onResume() {
    	super.onResume();
    }

    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	//finish();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	finish();
    }
    
	@Override
	public void onClick(View v) {
		String recordsToShowFilter, accountsFilter, categoriesFilter;
		// Get default account name from preferences
		accountsFilter = getSharedPreferences(PreferencesEditorActivity.PREFS_NAME, MODE_PRIVATE)
		.getString(PreferencesEditorActivity.PREFS_DEFAULT_ACCOUNT, 
				getResources().getString(R.string.main_account_name));
		// Use default "All" for other filters		
		recordsToShowFilter = getResources().getString(R.string.all_text);
		categoriesFilter = getResources().getString(R.string.all_text);
		
		switch (v.getId()) {
	      	case R.id.history_report_button:
		    	Log.d(TAG, "clicked on \"History Report\"");
		    	//FilterActivity.callMe(ReportsMenuActivity.this, "History");
		    	HistoryReportActivity.callMe(ReportsMenuActivity.this, recordsToShowFilter, accountsFilter, 
						categoriesFilter, DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
		        // TODO - show no data was found
		    break;
	         
	      	case R.id.common_report_button:
	      		Log.d(TAG, "clicked on \"Common Report\"");
	      		//FilterActivity.callMe(ReportsMenuActivity.this, "Common");
	      		CommonReportActivity.callMe(ReportsMenuActivity.this, accountsFilter);
	        break;

	      	case R.id.consolidated_report_button:
	      		Log.d(TAG, "clicked on \"Consolidated Report\"");
	      		//FilterActivity.callMe(ReportsMenuActivity.this, "Consolidated");
	      		recordsToShowFilter = getResources().getString(R.string.expenses_text);
	      		ConsolidatedReportActivity.callMe(ReportsMenuActivity.this, recordsToShowFilter, accountsFilter, 
						DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
	      	break;
		}	
	}
}
