package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class CommonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private static final String EXTRAS_CALLER = "caller";
	private static final String EXTRAS_ACCOUNTS_FILTER = "accountsFilter";
	
	private TextView pastMonthIncome;
	private TextView pastMonthExpense;
	private TextView currentMonthIncome;
	private TextView currentMonthExpense;
	private TextView currentMonthBalance;
	private TextView pastMonthBalance;
	private TextView currentQuarterBalance;
	private TextView pastQuarterBalance;
	private TextView currentYearBalance;
	private TextView pastYearBalance;
	
	private ImageButton filterButton;
	
	// Filters, passed via extras
	private int mAccountsFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_report);
		filterButton = (ImageButton) findViewById(R.id.filterButton);
		// Hide status bar, but keep title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		ActivityUtils.updateReportTitleBar(this, getHelper(), getAccountsFilter());
		
		mAccountsFilter = getAccountsFilter(); // '-1' corresponds to "All", e.g. - no filtering

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
				FilterActivity.callMe(CommonReportActivity.this, "Common");
				finish();
			}
		});		
		
		pastMonthIncome = (TextView) findViewById(R.id.pastMonthIncomeAmount);
		pastMonthExpense = (TextView) findViewById(R.id.pastMonthExpenseAmount);
		currentMonthIncome = (TextView) findViewById(R.id.currentMonthIncomeAmount);
		currentMonthExpense = (TextView) findViewById(R.id.currentMonthExpenseAmount);
		currentMonthBalance = (TextView) findViewById(R.id.currentMonthBalance);
		pastMonthBalance = (TextView) findViewById(R.id.pastMonthBalance);
		currentQuarterBalance = (TextView) findViewById(R.id.currentQuarterBalance);
		pastQuarterBalance = (TextView) findViewById(R.id.pastQuarterBalance);
		currentYearBalance = (TextView) findViewById(R.id.currentYearBalance);
		pastYearBalance = (TextView) findViewById(R.id.pastYearBalance);
		
		new FillTablesJob(this).execute();
	}
	
	public static void callMe(Context c, String caller, int accountsFilter) {
		Intent intent = new Intent(c, CommonReportActivity.class);
		intent.putExtra(EXTRAS_CALLER, caller);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		c.startActivity(intent);
	}
	
	private String getCallingActivityName() {		
		return getIntent().getStringExtra(EXTRAS_CALLER);
	}
	
	private int getAccountsFilter() {		
		return getIntent().getIntExtra(EXTRAS_ACCOUNTS_FILTER, -1);
	}


	private String[] calculateAmounts() throws SQLException {
		Log.d(TAG, "Populate tables with amounts");
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		String[] amounts = new String[10];
		
		TimeRange currentMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, false);
		TimeRange pastMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, true);
				
		// currentMonthExpenseValue = 
		amounts[0] = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentMonthInterval);
		// currentMonthIncomeValue = 
		amounts[1] = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentMonthInterval);
						
		//String pastMonthExpenseValue = 
		amounts[2] = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastMonthInterval);
		//String pastMonthIncomeValue = 
		amounts[3] = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastMonthInterval);
			
		//String currentMonthBalanceValue = 
		amounts[4] = CalcUtils.calculateBalance(amounts[1], amounts[0]);
		//String pastMonthBalanceValue = 
		amounts[5] = CalcUtils.calculateBalance(amounts[3], amounts[2]);
		
		
		// Balance for quarters
		TimeRange currentQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, false);
		TimeRange pastQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, true);
		String currentQuarterExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentQuarterInterval);
		String currentQuarterIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentQuarterInterval);
		String pastQuarterExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastQuarterInterval);
		String pastQuarterIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastQuarterInterval);
		//String currentQuarterBalanceValue = 
		amounts[6] = CalcUtils.calculateBalance(currentQuarterIncomeValue, currentQuarterExpenseValue);
		//String pastQuarterBalanceValue = 
		amounts[7] = CalcUtils.calculateBalance(pastQuarterIncomeValue, pastQuarterExpenseValue);
		
		
		// Balance for years
		TimeRange currentYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, false);
		TimeRange pastYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, true);
		String currentYearExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentYearInterval);
		String currentYearIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentYearInterval);
		String pastYearExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastYearInterval);
		String pastYearIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastYearInterval);
		//String currentYearBalanceValue = 
		amounts[8] = CalcUtils.calculateBalance(currentYearIncomeValue, currentYearExpenseValue);
		//String pastYearBalanceValue = 
		amounts[9] = CalcUtils.calculateBalance(pastYearIncomeValue, pastYearExpenseValue);
		
		// Round all results - trim rational part
		roundAmounts(amounts);
		
		return amounts;
	}
	
	/**
	 * Convert each string result to double, trim rational part and convert back to String
	 * @param amounts
	 */
	private void roundAmounts(String[] amounts) {
		for (int i=0; i < amounts.length; i++) {
			long number = Math.round(Double.valueOf(amounts[i]));					
			amounts[i] = Long.toString(number); 
		}
	}

	private class FillTablesJob extends AsyncTask<Void,Void,String[]> {
		private ProgressDialog progressDialog;
				
		public FillTablesJob(Context context) {
			super();
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("");
			progressDialog.setMessage(context.getString(R.string.data_calculation_text));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			//Log.d(TAG, "FillTablesJob: called onPreExecute");	
			if (progressDialog != null && !progressDialog.isShowing()) {
				progressDialog.show();
			}
		};
		
		@Override
		protected String[] doInBackground(Void... arg0) {
			//Log.d(TAG, "FillTablesJob: called doInBackground");				
			String[] results;
			
			try {
				results = calculateAmounts();				
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			
			return results;
		}
		
		@Override
        protected void onPostExecute(String[] results) {
			//Log.d(TAG, "FillTablesJob: called onPostExecute");	
            
			// Fill table cells with calculated amounts
			currentMonthExpense.setText(results[0]);				
			currentMonthIncome.setText(results[1]);
			pastMonthExpense.setText(results[2]);		
			pastMonthIncome.setText(results[3]);
			currentMonthBalance.setText(results[4]);
			pastMonthBalance.setText(results[5]);
			currentQuarterBalance.setText(results[6]);
			pastQuarterBalance.setText(results[7]);
			currentYearBalance.setText(results[8]);
			pastYearBalance.setText(results[9]);
			
    		if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        }
	}
}
