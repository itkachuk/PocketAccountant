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
		
		try {
			fillTables();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void fillTables() throws SQLException {
		Log.d(TAG, "Populate tables with amounts");
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		
		Long currentMonthStartTime = DateUtils.getTimestamp(DateUtils.MONTH, false, false);
		//Log.d(TAG, "currentMonthStartTime=" + currentMonthStartTime);
		Long currentMonthEndTime = DateUtils.getTimestamp(DateUtils.MONTH, false, true);
		//Log.d(TAG, "currentMonthEndTime=" + currentMonthEndTime);		
		String currentMonthExpenseValue = getSumOfRecords(recordDao, true, currentMonthStartTime, currentMonthEndTime);
		//Log.d(TAG, "currentMonthExpense=" + currentMonthExpenseValue);
		currentMonthExpense.setText(currentMonthExpenseValue);
		String currentMonthIncomeValue = getSumOfRecords(recordDao, false, currentMonthStartTime, currentMonthEndTime);
		//Log.d(TAG, "currentMonthIncome=" + currentMonthIncomeValue);
		currentMonthIncome.setText(currentMonthIncomeValue);
		
		Long pastMonthStartTime = DateUtils.getTimestamp(DateUtils.MONTH, true, false);
		Long pastMonthEndTime = DateUtils.getTimestamp(DateUtils.MONTH, true, true);
		String pastMonthExpenseValue = getSumOfRecords(recordDao, true, pastMonthStartTime, pastMonthEndTime);
		pastMonthExpense.setText(pastMonthExpenseValue);
		String pastMonthIncomeValue = getSumOfRecords(recordDao, false, pastMonthStartTime, pastMonthEndTime);
		pastMonthIncome.setText(pastMonthIncomeValue);
		
		String currentMonthBalanceValue = calculateBalance(currentMonthIncomeValue, currentMonthExpenseValue);
		String pastMonthBalanceValue = calculateBalance(pastMonthIncomeValue, pastMonthExpenseValue);
		currentMonthBalance.setText(currentMonthBalanceValue);
		pastMonthBalance.setText(pastMonthBalanceValue);
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

	private String getSumOfRecords(Dao<IncomeOrExpenseRecord, Integer> recordDao, boolean isExpense, long startTime, long endTime) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select sum(amount) from IncomeOrExpenseRecord where isExpense = ");
		if (isExpense) queryBuilder.append("1");
		else queryBuilder.append("0");
		queryBuilder.append(" and isPlanned = 0");
		queryBuilder.append(" and timestamp >= " + startTime + " and timestamp <= " + endTime);
		//Log.d(TAG, "getSumOfRecords query = " + queryBuilder.toString());
		
		GenericRawResults<String[]> rawResults = recordDao.queryRaw(queryBuilder.toString());
		List<String[]> results = rawResults.getResults();
		String[] resultArray = results.get(0);
		if (resultArray[0] == null) resultArray[0] = "0";
		return resultArray[0];
	}
}
