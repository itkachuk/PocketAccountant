package com.itkachuk.pa.utils;

import java.sql.SQLException;
import java.util.List;

import android.util.Log;

import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;

public class CalcUtils {
	private static final String TAG = "PocketAccountant";
	
	public static String calculateBalance(String incomeString, String expenseString) {
		try {
			double income = Double.valueOf(incomeString);
			double expense = Double.valueOf(expenseString);
			double balance = income - expense;			
			balance = (double)Math.round(balance * 100) / 100; // trim for two places after decimal point
			return Double.toString(balance); 
		} catch (NumberFormatException e) {
			return "";
		}
	}

	public static String getSumOfRecords(Dao<IncomeOrExpenseRecord, Integer> recordDao, 
			int accountFilter, boolean isExpense, TimeRange timeRange) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select sum(amount) from IncomeOrExpenseRecord where isExpense = ");
		if (isExpense) queryBuilder.append("1");
		else queryBuilder.append("0");
		if (accountFilter != -1) 
			queryBuilder.append(" and account = '" + accountFilter + "'"); // change account to id ???
		queryBuilder.append(" and isPlanned = 0");
		if (timeRange != null && ((timeRange.getStartTime() > DateUtils.DEFAULT_START_DATE) || (timeRange.getEndTime() < DateUtils.DEFAULT_END_DATE))) 
			queryBuilder.append(" and timestamp >= " + timeRange.getStartTime() + " and timestamp < " + timeRange.getEndTime());
		//Log.d(TAG, "getSumOfRecords query = " + queryBuilder.toString());
		
		GenericRawResults<String[]> rawResults = recordDao.queryRaw(queryBuilder.toString());
		List<String[]> results = rawResults.getResults();
		String[] resultArray = results.get(0);
		if (resultArray[0] == null) resultArray[0] = "0";
		return resultArray[0];
	}
	
	public static List<String[]> getAmountsPerCategoryList(Dao<IncomeOrExpenseRecord, Integer> recordDao, 
			int accountFilter, boolean isExpense, TimeRange timeRange) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append("select category, sum(amount) as amounts, category, category from IncomeOrExpenseRecord where isExpense = ");
		if (isExpense) queryBuilder.append("1");
		else queryBuilder.append("0");
		if (accountFilter != -1) 
			queryBuilder.append(" and account = '" + accountFilter + "'"); // change account to id ???
		queryBuilder.append(" and isPlanned = 0");
		if (timeRange != null && ((timeRange.getStartTime() > DateUtils.DEFAULT_START_DATE) || (timeRange.getEndTime() < DateUtils.DEFAULT_END_DATE))) 
			queryBuilder.append(" and timestamp >= " + timeRange.getStartTime() + " and timestamp < " + timeRange.getEndTime());
		queryBuilder.append(" group by category order by amounts desc");
		//Log.d(TAG, "getAmountsPerCategoryList query = " + queryBuilder.toString());
		
		GenericRawResults<String[]> rawResults = recordDao.queryRaw(queryBuilder.toString());
		return rawResults.getResults();		
	}
}
