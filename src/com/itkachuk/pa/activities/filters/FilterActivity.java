package com.itkachuk.pa.activities.filters;

import java.sql.SQLException;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.activities.reports.CommonReportActivity;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class FilterActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private static final String EXTRAS_REPORT_NAME = "reportName";
	
	private Spinner mRecordsFilterSpinner;
	private Spinner mAccountsFilterSpinner;
	private Spinner mCategoriesFilterSpinner;
	private Spinner mTimeFilterSpinner;
	private Button mStartDateButton;
	private Button mEndDateButton;
	private Button mShowReportButton;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_editor);
        
        mRecordsFilterSpinner = (Spinner) findViewById(R.id.recordsFilterSpinner);
        mAccountsFilterSpinner = (Spinner) findViewById(R.id.accountsFilteringSpinner);
        mCategoriesFilterSpinner = (Spinner) findViewById(R.id.categoriesFilteringSpinner);
        mTimeFilterSpinner = (Spinner) findViewById(R.id.timeFilteringSpinner);
        mStartDateButton = (Button) findViewById(R.id.startDateButton);
        mEndDateButton = (Button) findViewById(R.id.endDateButton); 
        mShowReportButton = (Button) findViewById(R.id.showReportButton);      

        setUIObjectsState();
        
        refreshRecordsSpinnerEntries();
        refreshAccountsSpinnerEntries();
        //refreshCategoriesSpinnerEntries();
        refreshTimeSpinnerEntries();

        

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		mShowReportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String reportName = getReportName();
				Intent intent;
				if (reportName != null) {
					if (reportName.equals("History")) {
						intent = new Intent(FilterActivity.this, HistoryReportActivity.class);
				        startActivity(intent);
					}
					if (reportName.equals("Common")) {
						intent = new Intent(FilterActivity.this, CommonReportActivity.class);
				        startActivity(intent);
					}
				}
			}
		});       
    }

	private void setUIObjectsState() {
		// TODO Auto-generated method stub
		FilterSingleton filterSingleton = FilterSingleton.getInstance();
		String reportName = getReportName();
		if (reportName.equals("History")) {
			mRecordsFilterSpinner.setEnabled(true);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(true);
			filterSingleton.setRecordsFilterEnabled(true);
			filterSingleton.setAccountsFilterEnabled(true);
			filterSingleton.setCategoriesFilterEnabled(false);
			filterSingleton.setTimeFilterEnabled(true);
		}
		if (reportName.equals("Common")) {
			mRecordsFilterSpinner.setEnabled(false);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(false);
			filterSingleton.setRecordsFilterEnabled(false);
			filterSingleton.setAccountsFilterEnabled(true);
			filterSingleton.setCategoriesFilterEnabled(false);
			filterSingleton.setTimeFilterEnabled(false);
		}
	}

	private void updateUIObjectsState() {
		// TODO Auto-generated method stub
		FilterSingleton filterSingleton = FilterSingleton.getInstance();
		if (filterSingleton.isRecordsFilterEnabled()) {
			mRecordsFilterSpinner.setEnabled(true);
		} else {
			mRecordsFilterSpinner.setEnabled(false);
		}
		if (filterSingleton.isAccountsFilterEnabled()) {
			mAccountsFilterSpinner.setEnabled(true);
		} else {
			mAccountsFilterSpinner.setEnabled(false);
		}
		if (filterSingleton.isCategoriesFilterEnabled()) {
			mCategoriesFilterSpinner.setEnabled(true);
		} else {
			mCategoriesFilterSpinner.setEnabled(false);
		}
		if (filterSingleton.isTimeFilterEnabled()) {
			mTimeFilterSpinner.setEnabled(true);
			
		} else {
			mTimeFilterSpinner.setEnabled(false);
			mStartDateButton.setEnabled(false);
			mEndDateButton.setEnabled(false);
		}
	}

	private void refreshRecordsSpinnerEntries() {
		// TODO Auto-generated method stub
		
	}
	private void refreshAccountsSpinnerEntries() {
		// TODO Auto-generated method stub
		
	}
	private void refreshCategoriesSpinnerEntries() {
		// TODO Auto-generated method stub
		
	}
	private void refreshTimeSpinnerEntries() {
		// TODO Auto-generated method stub
		String[] timeIntervals = getResources().getStringArray(R.array.time_ranges_list);		
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeIntervals);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTimeFilterSpinner.setAdapter(adapter);
	}
	
	public static void callMe(Context c, String reportName) {
		Intent intent = new Intent(c, FilterActivity.class);
		intent.putExtra(EXTRAS_REPORT_NAME, reportName);
		c.startActivity(intent);
	}
	
	private String getReportName() {
		return getIntent().getStringExtra(EXTRAS_REPORT_NAME);
	}
}
