package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
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
	private ImageButton firstPageButton;
	private ImageButton previousPageButton;
	private ImageButton nextPageButton;
	private ImageButton lastPageButton;
	private ImageButton filterButton;
	
	private String accountStringForTitle;
	
	// SQL query data
	private int pageIndex;
	private int totalPagesCount;
	private int rowsPerPage;
	Dao<IncomeOrExpenseRecord, Integer> recordDao;
	QueryBuilder<IncomeOrExpenseRecord, Integer> queryBuilder;
	
	// Filters, passed via extras
	private String mRecordsToShowFilter;
	private int mAccountsFilter;
	private int mCategoriesFilter;
	private long mStartDateFilter;
	private long mEndDateFilter;

	private View.OnClickListener pagingButtonsListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "HistoryReportActivity: onCreate");		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_report);
		builder = new AlertDialog.Builder(this);
		listView = (ListView) findViewById(R.id.recordsHistoryList);
		firstPageButton = (ImageButton) findViewById(R.id.firstPageButton);
		previousPageButton = (ImageButton) findViewById(R.id.previousPageButton);
		nextPageButton = (ImageButton) findViewById(R.id.nextPageButton);
		lastPageButton = (ImageButton) findViewById(R.id.lastPageButton);		
		filterButton = (ImageButton) findViewById(R.id.filterButton);
		// Hide status bar, but keep title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		parseFilters();
		buildAccountStringForTitle();	
				
		// Check calling activity, enable filter button, only if we came from reports menu activity
		if (getCallingActivityName().equals(ReportsMenuActivity.class.getName())) {
			filterButton.setEnabled(true);
		} else {
			filterButton.setEnabled(false);
		}
		
		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		filterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				FilterActivity.callMe(HistoryReportActivity.this, "History");
				finish();
			}
		});		
	
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				RecordEditorActivity.callMe(HistoryReportActivity.this, record.isExpense(), record.getId());
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				
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
		
		// Paging buttons onClick listener
		pagingButtonsListener = new View.OnClickListener() {
			public void onClick(View view) {
				switch(view.getId()){
				case R.id.firstPageButton : {
					pageIndex = 0;
					break;
				}
				case R.id.previousPageButton : {
					if (pageIndex > 0) pageIndex--;
					break;
				}
				case R.id.nextPageButton : {
					if (pageIndex < (totalPagesCount - 1)) pageIndex++;
					break;
				}
				case R.id.lastPageButton : {
					pageIndex = totalPagesCount - 1;
					break;
				}
				}
				
				updatePagingButtonsState();
				updateTitleBar();
				
				try {
					fillList();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		firstPageButton.setOnClickListener(pagingButtonsListener);
		previousPageButton.setOnClickListener(pagingButtonsListener);
		nextPageButton.setOnClickListener(pagingButtonsListener);
		lastPageButton.setOnClickListener(pagingButtonsListener);
		
		new InitialSQLQueryJob(this).execute();
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

	private void initializeSQLQueryParameters() throws SQLException {
		
		// getting rowsPerPage limit value from ProgramPreferences	
		rowsPerPage = PreferencesUtils.getRowsPerPage(this);
		pageIndex = 0;
		totalPagesCount = 0;
		
		boolean whereClauseStarted = false;
		recordDao = getHelper().getRecordDao();
		queryBuilder = recordDao.queryBuilder();
		Where<IncomeOrExpenseRecord, Integer> where = queryBuilder.where();
		
		if (mRecordsToShowFilter != null && mRecordsToShowFilter.equals(getResources().getString(R.string.expenses_text))) {
			where.eq(IncomeOrExpenseRecord.IS_EXPENSE_FIELD_NAME, true);
			whereClauseStarted = true;
		} else if (mRecordsToShowFilter != null && mRecordsToShowFilter.equals(getResources().getString(R.string.incomes_text))) {
			where.eq(IncomeOrExpenseRecord.IS_EXPENSE_FIELD_NAME, false);
			whereClauseStarted = true;
		}
		
		if (mAccountsFilter != -1) {
			if (whereClauseStarted) where.and();
			where.eq(IncomeOrExpenseRecord.ACCOUNT_FIELD_NAME, mAccountsFilter);
			whereClauseStarted = true;
		}
		
		if (mCategoriesFilter != -1) {
			if (whereClauseStarted) where.and();
			where.eq(IncomeOrExpenseRecord.CATEGORY_FIELD_NAME, mCategoriesFilter);
			whereClauseStarted = true;
		}
		
		if (mStartDateFilter != DateUtils.DEFAULT_START_DATE || mEndDateFilter != DateUtils.DEFAULT_END_DATE) {
			if (whereClauseStarted) where.and();
			where.between(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, mStartDateFilter, mEndDateFilter);
		}				
		
		// get total pages count
		int totalRows = recordDao.query(queryBuilder.prepare()).size(); // total record objects count, obtained with current filter
		totalPagesCount = totalRows / rowsPerPage;
		if (totalRows % rowsPerPage > 0) totalPagesCount++ ;
		
	}
	
	private void fillList() throws SQLException {
		Log.d(TAG, "Fill list of records");
		if (queryBuilder != null) { // runs only if query already initialized
			queryBuilder.orderBy(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, false).offset(pageIndex * rowsPerPage).limit(rowsPerPage);
			List<IncomeOrExpenseRecord> list = recordDao.query(queryBuilder.prepare());
			ArrayAdapter<IncomeOrExpenseRecord> arrayAdapter = new RecordsAdapter(this, R.layout.record_row, list);
			listView.setAdapter(arrayAdapter);
		}
	}

	private void buildAccountStringForTitle() {
		ActivityUtils.updateReportTitleBar(this, getHelper(), getAccountsFilter());
		accountStringForTitle = (String) getTitle();
	}
	
	private void updateTitleBar() {
		int pageNumber;
		if (totalPagesCount == 0) pageNumber = 0;
		else pageNumber = (pageIndex + 1);
		String pageText = getResources().getString(R.string.page_text);
		setTitle(accountStringForTitle + "\t\t" + pageText + " " + pageNumber + "/" + totalPagesCount);
	}
	
	private void updatePagingButtonsState() {
		if (totalPagesCount <= 1) {
			disableAllPagingButtons();
			return;
		}
		if (pageIndex == 0) { // if first page
			disableLeftPagingButtons();
			enableRightPagingButtons();
		} else if (pageIndex == (totalPagesCount - 1)) { // if last page
			enableLeftPagingButtons();
			disableRightPagingButtons();
		} else { // if in the middle
			enableAllPagingButtons();
		}
	}	
	
	public static void callMe(Context c, String caller, String recordsToShowFilter, int accountsFilter,
			int categoriesFilter, long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, HistoryReportActivity.class);
		intent.putExtra(EXTRAS_CALLER, caller);
		intent.putExtra(EXTRAS_RECORDS_TO_SHOW_FILTER, recordsToShowFilter);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_CATEGORIES_FILTER, categoriesFilter);
		intent.putExtra(EXTRAS_START_DATE_FILTER, startDateFilter);
		intent.putExtra(EXTRAS_END_DATE_FILTER, endDateFilter);
		c.startActivity(intent);
	}

	
	private String getCallingActivityName() {		
		return getIntent().getStringExtra(EXTRAS_CALLER);
	}
	
	private String getRecordsToShowFilter() {		
		return getIntent().getStringExtra(EXTRAS_RECORDS_TO_SHOW_FILTER);
	}
	
	private int getAccountsFilter() {		
		return getIntent().getIntExtra(EXTRAS_ACCOUNTS_FILTER, -1);
	}
	
	private int getCategoriesFilter() {		
		return getIntent().getIntExtra(EXTRAS_CATEGORIES_FILTER, -1);
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
	
	private class InitialSQLQueryJob extends AsyncTask<Void,Void,String> {
		private String result = null;
		private ProgressDialog progressDialog;
				
		public InitialSQLQueryJob(Context context) {
			super();
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("");
			progressDialog.setMessage(context.getString(R.string.data_loading_text));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			//Log.d(TAG, "InitialSQLQueryJob: called onPreExecute");	
			if (progressDialog != null && !progressDialog.isShowing()) {
				progressDialog.show();
			}
		};
		
		@Override
		protected String doInBackground(Void... arg0) {
			//Log.d(TAG, "InitialSQLQueryJob: called doInBackground");				
			
			try {
				initializeSQLQueryParameters();				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
			return result;
		}
		
		@Override
        protected void onPostExecute(String result) {
			//Log.d(TAG, "InitialSQLQueryJob: called onPostExecute");	
                 
    		updatePagingButtonsState();
    		updateTitleBar();
    		try {
    			fillList();
    		} catch (SQLException e) {
    			throw new RuntimeException(e);
    		}
    		if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        }
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
			
			try {
				Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
				categoryDao.refresh(record.getCategory()); // Refresh foreign object
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			fillText(v, R.id.recordCategory, record.getCategory().getName());

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
	
	// Paging buttons control methods
	private void enableAllPagingButtons() {
		firstPageButton.setEnabled(true);
		previousPageButton.setEnabled(true);
		nextPageButton.setEnabled(true);
		lastPageButton.setEnabled(true);
	}
	private void disableAllPagingButtons() {
		firstPageButton.setEnabled(false);
		previousPageButton.setEnabled(false);
		nextPageButton.setEnabled(false);
		lastPageButton.setEnabled(false);
	}
	
	private void enableRightPagingButtons() {		
		nextPageButton.setEnabled(true);
		lastPageButton.setEnabled(true);
	}
	private void disableRightPagingButtons() {		
		nextPageButton.setEnabled(false);
		lastPageButton.setEnabled(false);
	}
	private void enableLeftPagingButtons() {
		firstPageButton.setEnabled(true);
		previousPageButton.setEnabled(true);		
	}
	private void disableLeftPagingButtons() {
		firstPageButton.setEnabled(false);
		previousPageButton.setEnabled(false);		
	}
}
