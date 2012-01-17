package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
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
	private String mRecordsToShowFilter;
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
/*	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			fillList();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
*/	
	public static void callMe(Context c, String recordsToShowFilter, String accountsFilter,
			long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, ConsolidatedReportActivity.class);
		intent.putExtra(EXTRAS_RECORDS_TO_SHOW_FILTER, recordsToShowFilter);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_START_DATE_FILTER, startDateFilter);
		intent.putExtra(EXTRAS_END_DATE_FILTER, endDateFilter);
		c.startActivity(intent);
	}
	
/*
	private void fillList() throws SQLException {
		// TODO - implement paging!
		Log.d(TAG, "Show list of records");
		boolean whereClauseStarted = false;
		Dao<IncomeOrExpenseRecord, Integer> dao = getHelper().getRecordDao();
		QueryBuilder<IncomeOrExpenseRecord, Integer> builder = dao.queryBuilder();
		Where<IncomeOrExpenseRecord, Integer> where = builder.where();
		
		if (mRecordsToShowFilter != null && mRecordsToShowFilter.equals(getResources().getString(R.string.expenses_text))) {
			where.eq(IncomeOrExpenseRecord.IS_EXPENSE_FIELD_NAME, true);
			whereClauseStarted = true;
		} else if (mRecordsToShowFilter != null && mRecordsToShowFilter.equals(getResources().getString(R.string.incomes_text))) {
			where.eq(IncomeOrExpenseRecord.IS_EXPENSE_FIELD_NAME, false);
			whereClauseStarted = true;
		}
		
		if (mAccountsFilter != null && !mAccountsFilter.equals(getResources().getString(R.string.all_text))) {
			if (whereClauseStarted) where.and();
			where.eq(IncomeOrExpenseRecord.ACCOUNT_FIELD_NAME, mAccountsFilter);
			whereClauseStarted = true;
		}		
		
		if (mStartDateFilter != FilterActivity.DEFAULT_START_DATE || mEndDateFilter != FilterActivity.DEFAULT_END_DATE) {
			if (whereClauseStarted) where.and();
			where.between(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, mStartDateFilter, mEndDateFilter);
		}
		
		builder.orderBy(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, false);
		List<IncomeOrExpenseRecord> list = dao.query(builder.prepare());
		ArrayAdapter<IncomeOrExpenseRecord> arrayAdapter = new RecordsAdapter(this, R.layout.record_row, list);
		listView.setAdapter(arrayAdapter);
	}*/
	
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
		mRecordsToShowFilter = getRecordsToShowFilter();
		mAccountsFilter = getAccountsFilter();
		mStartDateFilter = getStartDateFilter();
		mEndDateFilter = getEndDateFilter();
	}
}
