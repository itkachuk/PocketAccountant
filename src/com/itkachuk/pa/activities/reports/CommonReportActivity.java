package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;

public class CommonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
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
	
	// Filters, passed via extras
	private String mAccountsFilter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_report);
		// Hide status bar, but keep title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		updateTitleBar();
		
		mAccountsFilter = getAccountsFilter();
		if (mAccountsFilter.equals(getResources().getString(R.string.all_text))) {
			mAccountsFilter = null; 
		}

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		findViewById(R.id.filterButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				FilterActivity.callMe(CommonReportActivity.this, "Common");
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
		
		try {
			fillTables();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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
	
	public static void callMe(Context c, String accountsFilter) {
		Intent intent = new Intent(c, CommonReportActivity.class);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		c.startActivity(intent);
	}
	
	private String getAccountsFilter() {		
		return getIntent().getStringExtra(EXTRAS_ACCOUNTS_FILTER);
	}
	
	private void fillTables() throws SQLException {
		Log.d(TAG, "Populate tables with amounts");
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		
		TimeRange currentMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, false);
		TimeRange pastMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, true);
		//Log.d(TAG, "currentMonth=" + currentMonth.toString());				
		String currentMonthExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentMonthInterval);
		String currentMonthIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentMonthInterval);
		//Log.d(TAG, "currentMonthExpense=" + currentMonthExpenseValue);
		//Log.d(TAG, "currentMonthIncome=" + currentMonthIncomeValue);
		currentMonthExpense.setText(currentMonthExpenseValue);				
		currentMonthIncome.setText(currentMonthIncomeValue);		
		
		String pastMonthExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastMonthInterval);
		String pastMonthIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastMonthInterval);
		pastMonthExpense.setText(pastMonthExpenseValue);		
		pastMonthIncome.setText(pastMonthIncomeValue);
		
		String currentMonthBalanceValue = CalcUtils.calculateBalance(currentMonthIncomeValue, currentMonthExpenseValue);
		String pastMonthBalanceValue = CalcUtils.calculateBalance(pastMonthIncomeValue, pastMonthExpenseValue);
		currentMonthBalance.setText(currentMonthBalanceValue);
		pastMonthBalance.setText(pastMonthBalanceValue);
		
		// Balance for quarters
		TimeRange currentQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, false);
		TimeRange pastQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, true);
		String currentQuarterExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentQuarterInterval);
		String currentQuarterIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentQuarterInterval);
		String pastQuarterExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastQuarterInterval);
		String pastQuarterIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastQuarterInterval);
		String currentQuarterBalanceValue = CalcUtils.calculateBalance(currentQuarterIncomeValue, currentQuarterExpenseValue);
		String pastQuarterBalanceValue = CalcUtils.calculateBalance(pastQuarterIncomeValue, pastQuarterExpenseValue);
		currentQuarterBalance.setText(currentQuarterBalanceValue);
		pastQuarterBalance.setText(pastQuarterBalanceValue);
		
		// Balance for years
		TimeRange currentYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, false);
		TimeRange pastYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, true);
		String currentYearExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, currentYearInterval);
		String currentYearIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, currentYearInterval);
		String pastYearExpenseValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, true, pastYearInterval);
		String pastYearIncomeValue = CalcUtils.getSumOfRecords(recordDao, mAccountsFilter, false, pastYearInterval);
		String currentYearBalanceValue = CalcUtils.calculateBalance(currentYearIncomeValue, currentYearExpenseValue);
		String pastYearBalanceValue = CalcUtils.calculateBalance(pastYearIncomeValue, pastYearExpenseValue);
		currentYearBalance.setText(currentYearBalanceValue);
		pastYearBalance.setText(pastYearBalanceValue);
	}

	
}
