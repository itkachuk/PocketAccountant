package com.itkachuk.pa.activities.filters;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	
	private long mStartDate = 0L;
	private long mEndDate = Long.MAX_VALUE;
	
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
        
        try {
	        refreshRecordsSpinnerEntries();
	        refreshAccountsSpinnerEntries();
	        refreshCategoriesSpinnerEntries();
	        refreshTimeSpinnerEntries();
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}
        

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
				//moveTaskToBack(true);
			}
		});
		
		mShowReportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String reportName = getReportName();
				Intent intent;
				if (reportName != null) {
					if (reportName.equals("History")) {
						
						String recordsToShowFilter = mRecordsFilterSpinner.getSelectedItem().toString();
						String accountsFilter = mAccountsFilterSpinner.getSelectedItem().toString();
						String categoriesFilter = mCategoriesFilterSpinner.getSelectedItem().toString();
						
						//mStartDate = 1320617246585L;
						//mEndDate = 1320617785016L;
						HistoryReportActivity.callMe(FilterActivity.this, recordsToShowFilter, accountsFilter, 
								categoriesFilter, mStartDate, mEndDate);
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
		String reportName = getReportName();
		if (reportName.equals("History")) {
			mRecordsFilterSpinner.setEnabled(true);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(true);
		}
		if (reportName.equals("Common")) {
			mRecordsFilterSpinner.setEnabled(false);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(false);
		}
	}


	private void refreshRecordsSpinnerEntries() {
		String[] filterItemsList;		
		if (getReportName().equals("Consolidated")) {
			filterItemsList = new String[2];
			filterItemsList[0] = getResources().getString(R.string.expenses_text);
			filterItemsList[1] = getResources().getString(R.string.incomes_text);			
		} else {
			filterItemsList = new String[3];
			filterItemsList[0] = getResources().getString(R.string.all_text);
			filterItemsList[1] = getResources().getString(R.string.expenses_text);
			filterItemsList[2] = getResources().getString(R.string.incomes_text);
		}		
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterItemsList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mRecordsFilterSpinner.setAdapter(adapter);
	}
	
	private void refreshAccountsSpinnerEntries() throws SQLException {
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		List<Account> accounts = new ArrayList<Account>();
		String mainAccountName = getResources().getString(R.string.main_account_name);		
		accounts.add(new Account(mainAccountName, null, null, false)); // first add main account to spinner
		accounts.addAll(accountDao.queryForAll()); // then add all user's accounts from DB
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, accounts);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAccountsFilterSpinner.setAdapter(adapter);				
	}
	
	private void refreshCategoriesSpinnerEntries() {
		// TODO 
		String[] filterItemsList;
		filterItemsList = new String[1];
		filterItemsList[0] = getResources().getString(R.string.all_text);
		ArrayAdapter<String> adapter =
			new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterItemsList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategoriesFilterSpinner.setAdapter(adapter);
	}
	
	private void refreshTimeSpinnerEntries() {
		// TODO 
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
