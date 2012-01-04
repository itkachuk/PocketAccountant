package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.util.List;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.stmt.QueryBuilder;

public class CommonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.common_report);

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
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
	
	private void fillTables() throws SQLException {
		Log.d(TAG, "Populate tables with amounts");
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		
		TimeRange currentMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, false);
		//Log.d(TAG, "currentMonth=" + currentMonth.toString());		
		
		String currentMonthExpenseValue = getSumOfRecords(recordDao, true, currentMonthInterval);
		//Log.d(TAG, "currentMonthExpense=" + currentMonthExpenseValue);
		currentMonthExpense.setText(currentMonthExpenseValue);
		String currentMonthIncomeValue = getSumOfRecords(recordDao, false, currentMonthInterval);
		//Log.d(TAG, "currentMonthIncome=" + currentMonthIncomeValue);
		currentMonthIncome.setText(currentMonthIncomeValue);
		
		TimeRange pastMonthInterval = DateUtils.getTimeRange(DateUtils.MONTH, true);
		String pastMonthExpenseValue = getSumOfRecords(recordDao, true, pastMonthInterval);
		pastMonthExpense.setText(pastMonthExpenseValue);
		String pastMonthIncomeValue = getSumOfRecords(recordDao, false, pastMonthInterval);
		pastMonthIncome.setText(pastMonthIncomeValue);
		
		String currentMonthBalanceValue = calculateBalance(currentMonthIncomeValue, currentMonthExpenseValue);
		String pastMonthBalanceValue = calculateBalance(pastMonthIncomeValue, pastMonthExpenseValue);
		currentMonthBalance.setText(currentMonthBalanceValue);
		pastMonthBalance.setText(pastMonthBalanceValue);
		
		// Balance for quarters
		TimeRange currentQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, false);
		TimeRange pastQuarterInterval = DateUtils.getTimeRange(DateUtils.QUARTER, true);
		String currentQuarterExpenseValue = getSumOfRecords(recordDao, true, currentQuarterInterval);
		String currentQuarterIncomeValue = getSumOfRecords(recordDao, false, currentQuarterInterval);
		String pastQuarterExpenseValue = getSumOfRecords(recordDao, true, pastQuarterInterval);
		String pastQuarterIncomeValue = getSumOfRecords(recordDao, false, pastQuarterInterval);
		String currentQuarterBalanceValue = calculateBalance(currentQuarterIncomeValue, currentQuarterExpenseValue);
		String pastQuarterBalanceValue = calculateBalance(pastQuarterIncomeValue, pastQuarterExpenseValue);
		currentQuarterBalance.setText(currentQuarterBalanceValue);
		pastQuarterBalance.setText(pastQuarterBalanceValue);
		
		// Balance for years
		TimeRange currentYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, false);
		TimeRange pastYearInterval = DateUtils.getTimeRange(DateUtils.YEAR, true);
		String currentYearExpenseValue = getSumOfRecords(recordDao, true, currentYearInterval);
		String currentYearIncomeValue = getSumOfRecords(recordDao, false, currentYearInterval);
		String pastYearExpenseValue = getSumOfRecords(recordDao, true, pastYearInterval);
		String pastYearIncomeValue = getSumOfRecords(recordDao, false, pastYearInterval);
		String currentYearBalanceValue = calculateBalance(currentYearIncomeValue, currentYearExpenseValue);
		String pastYearBalanceValue = calculateBalance(pastYearIncomeValue, pastYearExpenseValue);
		currentYearBalance.setText(currentYearBalanceValue);
		pastYearBalance.setText(pastYearBalanceValue);
	}

	private String calculateBalance(String incomeString, String expenseString) {
		try {
			double income = Double.valueOf(incomeString);
			double expense = Double.valueOf(expenseString);
			double balance = income - expense;			
			balance = (double)Math.round(balance * 100) / 100; // trim for two places after decimal point
			return Double.toString(balance); // TODO - trim rational part
		} catch (NumberFormatException e) {
			return "";
		}
	}

	private String getSumOfRecords(Dao<IncomeOrExpenseRecord, Integer> recordDao, boolean isExpense, TimeRange timeRange) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select sum(amount) from IncomeOrExpenseRecord where isExpense = ");
		if (isExpense) queryBuilder.append("1");
		else queryBuilder.append("0");
		queryBuilder.append(" and isPlanned = 0");
		queryBuilder.append(" and timestamp >= " + timeRange.getStartTime() + " and timestamp < " + timeRange.getEndTime());
		//Log.d(TAG, "getSumOfRecords query = " + queryBuilder.toString());
		
		GenericRawResults<String[]> rawResults = recordDao.queryRaw(queryBuilder.toString());
		List<String[]> results = rawResults.getResults();
		String[] resultArray = results.get(0);
		if (resultArray[0] == null) resultArray[0] = "0";
		return resultArray[0];
	}
}
