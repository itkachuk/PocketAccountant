package com.itkachuk.pa.activities.menus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.itkachuk.pa.R;
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
    	finish();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	finish();
    }
    
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
	      	case R.id.history_report_button:
		    	Log.d(TAG, "clicked on \"History Report\"");
		        intent = new Intent(ReportsMenuActivity.this, HistoryReportActivity.class);
		        //intent.putExtra(HistoryReportActivity.EXTRAS_IS_EXPENSE, true);
		        startActivity(intent);
		        // TODO - show no data was found
		    break;
	         
	      	case R.id.common_report_button:
	      		Log.d(TAG, "clicked on \"Common Report\"");

	        break;

	      	case R.id.consolidated_report_button:
	      		Log.d(TAG, "clicked on \"Consolidated Report\"");
	    	  
	      	break;
		}	
	}
}
