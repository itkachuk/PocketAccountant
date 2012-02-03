package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.DateUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;


public class HistoryReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private static final String EXTRAS_CALLER = "caller";
	private static final String EXTRAS_RECORDS_TO_SHOW_FILTER = "recordsToShowFilter";
	private static final String EXTRAS_ACCOUNTS_FILTER = "accountsFilter";
	private static final String EXTRAS_CATEGORIES_FILTER = "categoriesFilter";
	private static final String EXTRAS_START_DATE_FILTER = "startDateFilter";
	private static final String EXTRAS_END_DATE_FILTER = "endDateFilter";

	public static final Integer DEFAULT_ROWS_PER_PAGE_NUMBER = 30;
	
	private ListView listView;
	private AlertDialog.Builder builder;
	private ImageButton filterButton;
	
	// Filters, passed via extras
	private String mRecordsToShowFilter;
	private String mAccountsFilter;
	private String mCategoriesFilter;
	private long mStartDateFilter;
	private long mEndDateFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_report);
		builder = new AlertDialog.Builder(this);
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
				FilterActivity.callMe(HistoryReportActivity.this, "History");
				finish();
			}
		});

		listView = (ListView) findViewById(R.id.recordsHistoryList);
	
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				RecordEditorActivity.callMe(HistoryReportActivity.this, record.isExpense(), record.getId());
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				
				//AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
				builder.setMessage(getResources().getString(R.string.history_report_delete_record_dialog))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try{
				        		   Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
				        		   recordDao.deleteById(record.getId());
				        		   fillList();
				        	   } catch (SQLException e) {
				        		   throw new RuntimeException(e);
				        	   }
				           }
				       })
				       .setNegativeButton(getResources().getString(R.string.no_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		});
	}

	private void updateTitleBar() {
		String accountsFilter = getAccountsFilter();
		String currency;
		//If [main] account - get currency from Preferences
		if (accountsFilter.equals(getResources().getString(R.string.main_account_name))) { 
			currency = getSharedPreferences(PreferencesEditorActivity.PREFS_NAME, MODE_PRIVATE)
			.getString(PreferencesEditorActivity.PREFS_MAIN_ACCOUNT_CURRENCY, "");
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
			String categoriesFilter, long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, HistoryReportActivity.class);
		intent.putExtra(EXTRAS_CALLER, caller);
		intent.putExtra(EXTRAS_RECORDS_TO_SHOW_FILTER, recordsToShowFilter);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_CATEGORIES_FILTER, categoriesFilter);
		intent.putExtra(EXTRAS_START_DATE_FILTER, startDateFilter);
		intent.putExtra(EXTRAS_END_DATE_FILTER, endDateFilter);
		c.startActivity(intent);
	}

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
		
		if (mCategoriesFilter != null && !mCategoriesFilter.equals(getResources().getString(R.string.all_text))) {
			if (whereClauseStarted) where.and();
			where.eq(IncomeOrExpenseRecord.CATEGORY_FIELD_NAME, mCategoriesFilter);
			whereClauseStarted = true;
		}
		
		if (mStartDateFilter != DateUtils.DEFAULT_START_DATE || mEndDateFilter != DateUtils.DEFAULT_END_DATE) {
			if (whereClauseStarted) where.and();
			where.between(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, mStartDateFilter, mEndDateFilter);
		}
		
		// getting roesPerPage limit value from ProgramPreferences
		SharedPreferences programSettings = getSharedPreferences(PreferencesEditorActivity.PREFS_NAME, MODE_PRIVATE);
		int rowsPerPage = programSettings.getInt(PreferencesEditorActivity.PREFS_ROWS_PER_PAGE, 
				HistoryReportActivity.DEFAULT_ROWS_PER_PAGE_NUMBER);
		
		builder.orderBy(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, false).limit(rowsPerPage);
		List<IncomeOrExpenseRecord> list = dao.query(builder.prepare());
		ArrayAdapter<IncomeOrExpenseRecord> arrayAdapter = new RecordsAdapter(this, R.layout.record_row, list);
		listView.setAdapter(arrayAdapter);
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
	
	private String getCategoriesFilter() {		
		return getIntent().getStringExtra(EXTRAS_CATEGORIES_FILTER);
	}
	
	private long getStartDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_START_DATE_FILTER, DateUtils.DEFAULT_START_DATE);
	}
	
	private long getEndDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_END_DATE_FILTER, DateUtils.DEFAULT_END_DATE);
	}
	
	private void parseFilters() {
		mRecordsToShowFilter = getRecordsToShowFilter();
		mAccountsFilter = getAccountsFilter();
		mCategoriesFilter = getCategoriesFilter();
		mStartDateFilter = getStartDateFilter();
		mEndDateFilter = getEndDateFilter();
	}

	private class RecordsAdapter extends ArrayAdapter<IncomeOrExpenseRecord> {

		public RecordsAdapter(Context context, int textViewResourceId, List<IncomeOrExpenseRecord> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.record_row, null);
			}
			IncomeOrExpenseRecord record = getItem(position);

			Long millis = record.getTimestamp();
			DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();
			fillText(v, R.id.recordDate, dateFormatter.format(millis));
		
			fillText(v, R.id.recordCategory, record.getCategory());

			fillText(v, R.id.recordDescription, record.getDescription());
						
			String amount = Double.toString(record.getAmount());
			if (record.isExpense()) {
				fillText(v, R.id.recordAmount, "-" + amount);
				setTextColor(v, R.id.recordAmount, R.color.expense_amount_color);
			} else {
				fillText(v, R.id.recordAmount, amount);
				setTextColor(v, R.id.recordAmount, R.color.income_amount_color);
			}	
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
