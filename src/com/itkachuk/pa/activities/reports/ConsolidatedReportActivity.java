package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class ConsolidatedReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_CALLER = "caller";
	private static final String EXTRAS_RECORDS_TO_SHOW_FILTER = "recordsToShowFilter";
	private static final String EXTRAS_ACCOUNTS_FILTER = "accountsFilter";
	private static final String EXTRAS_START_DATE_FILTER = "startDateFilter";
	private static final String EXTRAS_END_DATE_FILTER = "endDateFilter";
	
	private ListView listView;
	private ImageButton filterButton;
	private ImageButton mChangeViewButton;
	
	private int reportViewsCounter = 0; // 0 - amounts, 1 - percentages. TBD: 2 - pie chart, 3 - bars.
	private static final int REPORT_VIEWS_QTY = 2;
	
	// Filters, passed via extras
	private boolean mRecordsToShowFilter;
	private String mAccountsFilter;
	private long mStartDateFilter;
	private long mEndDateFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consolidated_report);
		filterButton = (ImageButton) findViewById(R.id.filterButton);
		// Hide status bar, but keep title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		parseFilters();
		updateTitleBar();

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		// Check calling activity, enable filter button, only if we came from reports menu activity
		if (getCallingActivityName().equals(ReportsMenuActivity.class.getName())) {
			filterButton.setEnabled(true);
		} else {
			filterButton.setEnabled(false);
		}
		
		filterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				FilterActivity.callMe(ConsolidatedReportActivity.this, "Consolidated");
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.categoriesAmountsList);		
		mChangeViewButton = (ImageButton) findViewById(R.id.changeViewButton);
		
		mChangeViewButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				reportViewsCounter++;
				if (reportViewsCounter >= REPORT_VIEWS_QTY)
					reportViewsCounter = 0;
				try {
					fillList();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		});  
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String[] categoryAmountRow = (String[]) listView.getAdapter().getItem(i);
				HistoryReportActivity.callMe(ConsolidatedReportActivity.this, "", getRecordsToShowFilter(), 
						mAccountsFilter, categoryAmountRow[0], mStartDateFilter, mEndDateFilter);
			}
		});
	}
	
	private void updateTitleBar() {
		String accountsFilter = getAccountsFilter();
		String currency;
		//If [main] account - get currency from Preferences
		if (accountsFilter.equals(getResources().getString(R.string.main_account_name))) { 
			currency = PreferencesUtils.getMainAccountCurrency(this);
		} else { // if not [main] - get currency from DB
			try{
	 		   	Dao<Account, String> accountDao = getHelper().getAccountDao();
	 		   	Account account = accountDao.queryForEq(Account.NAME_FIELD_NAME, accountsFilter).get(0);
	 		   	currency = account.getCurrency();
	 	   	} catch (SQLException e) {
	 	   		throw new RuntimeException(e);
	 	   	}
		}
		setTitle("Account: " + accountsFilter + ", " + currency);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			fillList();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void callMe(Context c, String caller, String recordsToShowFilter, String accountsFilter,
			long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, ConsolidatedReportActivity.class);
		intent.putExtra(EXTRAS_CALLER, caller);
		intent.putExtra(EXTRAS_RECORDS_TO_SHOW_FILTER, recordsToShowFilter);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_START_DATE_FILTER, startDateFilter);
		intent.putExtra(EXTRAS_END_DATE_FILTER, endDateFilter);
		c.startActivity(intent);
	}
	

	private void fillList() throws SQLException {
		// TODO - implement paging!
		Log.d(TAG, "Show list of aggregated amounts per category");
		Dao<IncomeOrExpenseRecord, Integer> dao = getHelper().getRecordDao();
		TimeRange timeRange = new TimeRange(mStartDateFilter, mEndDateFilter);
		
		List<String[]> list = CalcUtils.getAmountsPerCategoryList(dao, mAccountsFilter, 
				mRecordsToShowFilter, timeRange);
		roundAmountsOfCategoriesList(list);
		
		if (reportViewsCounter == 1) { // Percentages displaying mode
			String sumOfRecords = CalcUtils.getSumOfRecords(dao, mAccountsFilter, mRecordsToShowFilter, timeRange);
			addPercentValuesToCategoriesList(list, sumOfRecords);
		}
		
		ArrayAdapter<String[]> arrayAdapter = new AmountsPerCategoryAdapter(this, R.layout.category_amount_row, list);
		listView.setAdapter(arrayAdapter);
	}
	
	private void addPercentValuesToCategoriesList(List<String[]> list, String totalAmount) {
		float categoryPercent;
		float totalSum = Float.valueOf(totalAmount);
		for (String[] row : list) {
			categoryPercent = (Float.valueOf(row[1])/totalSum) * 100;
			row[2] = Float.toString(Math.round(categoryPercent * 10)/10f); // third column of the row is dedicated for percents
		}
	}
	
	private void roundAmountsOfCategoriesList(List<String[]> list) {
		double categoryAmount;
		for (String[] row : list) {
			categoryAmount = Double.valueOf(row[1]);
			//row[1] = Double.toString(Math.round(categoryAmount * 100)/100f); // trim amount for two places after decimal point
			row[1] = Long.toString(Math.round(categoryAmount)); // trim rational part
		}
	}

	private String getCallingActivityName() {		
		return getIntent().getStringExtra(EXTRAS_CALLER);
	}
	
	private String getRecordsToShowFilter() {		
		return getIntent().getStringExtra(EXTRAS_RECORDS_TO_SHOW_FILTER);
	}
	
	private String getAccountsFilter() {		
		return getIntent().getStringExtra(EXTRAS_ACCOUNTS_FILTER);
	}
	
	private long getStartDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_START_DATE_FILTER, DateUtils.DEFAULT_START_DATE);
	}
	
	private long getEndDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_END_DATE_FILTER, DateUtils.DEFAULT_END_DATE);
	}
	
	private void parseFilters() {
		if (getRecordsToShowFilter().equals(getResources().getString(R.string.expenses_text)))
			mRecordsToShowFilter = true;
		else mRecordsToShowFilter = false;
		mAccountsFilter = getAccountsFilter();
		if (mAccountsFilter.equals(getResources().getString(R.string.all_text))) 
			mAccountsFilter = null;
		mStartDateFilter = getStartDateFilter();
		mEndDateFilter = getEndDateFilter();
	}
	
	private class AmountsPerCategoryAdapter extends ArrayAdapter<String[]> {

		public AmountsPerCategoryAdapter(Context context, int textViewResourceId, List<String[]> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.category_amount_row, null);
			}
			String[] row = getItem(position);
		
			fillText(v, R.id.categoryName, row[0]);

			if (reportViewsCounter == 0) { // Amounts displaying mode
				fillText(v, R.id.categoryAmount, row[1]);
			}			
			if (reportViewsCounter == 1) { // Percentages displaying mode
				fillText(v, R.id.categoryAmount, row[2] + " %");
			}
	
			return v;
		}

		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}
		
/*		private void setTextColor(View v, int id, int colorId) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setTextColor(getResources().getColor(colorId));
		}*/
	}
}
