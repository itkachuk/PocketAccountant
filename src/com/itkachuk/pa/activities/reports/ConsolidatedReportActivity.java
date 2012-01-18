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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class ConsolidatedReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_RECORDS_TO_SHOW_FILTER = "recordsToShowFilter";
	private static final String EXTRAS_ACCOUNTS_FILTER = "accountsFilter";
	private static final String EXTRAS_START_DATE_FILTER = "startDateFilter";
	private static final String EXTRAS_END_DATE_FILTER = "endDateFilter";
	
	private ListView listView;
	
	// Filters, passed via extras
	private boolean mRecordsToShowFilter;
	private String mAccountsFilter;
	private long mStartDateFilter;
	private long mEndDateFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consolidated_report);
		parseFilters();

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});

		listView = (ListView) findViewById(R.id.categoriesAmountsList);
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
	
	public static void callMe(Context c, String recordsToShowFilter, String accountsFilter,
			long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, ConsolidatedReportActivity.class);
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
		
		List<String[]> list = CalcUtils.getAmountsPerCategoryList(dao, mAccountsFilter, 
				mRecordsToShowFilter, new TimeRange(mStartDateFilter, mEndDateFilter));
		
		ArrayAdapter<String[]> arrayAdapter = new AmountsPerCategoryAdapter(this, R.layout.category_amount_row, list);
		listView.setAdapter(arrayAdapter);
	}
	
	private String getRecordsToShowFilter() {		
		return getIntent().getStringExtra(EXTRAS_RECORDS_TO_SHOW_FILTER);
	}
	
	private String getAccountsFilter() {		
		return getIntent().getStringExtra(EXTRAS_ACCOUNTS_FILTER);
	}
	
	private long getStartDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_START_DATE_FILTER, 0L);
	}
	
	private long getEndDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_END_DATE_FILTER, Long.MAX_VALUE);
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

			fillText(v, R.id.categoryAmount, row[1]);						
	
			return v;
		}

		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}
		
		private void setTextColor(View v, int id, int colorId) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setTextColor(getResources().getColor(colorId));
		}
	}
}
