package com.itkachuk.pa.activities.menus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.reports.CommonReportActivity;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.DatabaseHelper;
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
		switch (v.getId()) {
	      	case R.id.history_report_button:
		    	Log.d(TAG, "clicked on \"History Report\"");
		    	FilterActivity.callMe(ReportsMenuActivity.this, "History");
		        // TODO - show no data was found
		    break;
	         
	      	case R.id.common_report_button:
	      		Log.d(TAG, "clicked on \"Common Report\"");
	      		FilterActivity.callMe(ReportsMenuActivity.this, "Common");
	        break;

	      	case R.id.consolidated_report_button:
	      		Log.d(TAG, "clicked on \"Consolidated Report\"");
	      		FilterActivity.callMe(ReportsMenuActivity.this, "Consolidated");
	      	break;
		}	
	}
}
