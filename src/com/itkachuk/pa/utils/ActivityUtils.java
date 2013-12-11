package com.itkachuk.pa.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

public class ActivityUtils {
	private static final String TAG = "PocketAccountant";
	
	/**
	 * 
	 * @param context
	 * @param accountDao
	 * @param accountSpinner
	 * @throws SQLException
	 */
	public static void refreshAccountSpinnerEntries(Context context, Dao<Account, Integer> accountDao, 
			Spinner accountSpinner) throws SQLException {	
		
		List<Account> accounts = new ArrayList<Account>();
		accounts.addAll(accountDao.queryForAll()); // add all user's accounts from DB
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(context, android.R.layout.simple_spinner_item, accounts);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(adapter);
		
		// Select default account	        	
		selectSpinnerAccount(accountSpinner, PreferencesUtils.getDefaultAccountId(context));
	}
	
	/**
	 * select account by object
	 * @param accountSpinner
	 * @param selectAccount
	 */
	public static void selectSpinnerAccount(Spinner accountSpinner, Account selectAccount) {
		SpinnerAdapter adapter = accountSpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Account account = (Account) adapter.getItem(i);
			if (account != null && account.equals(selectAccount)) {
				accountSpinner.setSelection(i);
				break;
			}
		}
	}
	
	/**
	 * select account by id
	 * @param accountSpinner
	 * @param selectAccountId
	 */
	public static void selectSpinnerAccount(Spinner accountSpinner, int selectAccountId) {
		SpinnerAdapter adapter = accountSpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Account account = (Account) adapter.getItem(i);
			if (account != null && account.getId() == selectAccountId) {
				accountSpinner.setSelection(i);
				break;
			}
		}
	}
	
	/**
	 * Method for populating spinner by filtered categories: only expense categories, or only income ones
	 * @param context
	 * @param categoryDao
	 * @param categorySpinner
	 * @param isExpense
	 * @throws SQLException
	 */
	public static void refreshCategorySpinnerEntries(Context context, Dao<Category, Integer> categoryDao, 
			Spinner categorySpinner, boolean isExpense) throws SQLException {	
		
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category()); // first entry should be empty
		if (isExpense) {
			categories.addAll(categoryDao.queryBuilder().where() // Add expense categories from DB
					.eq(Category.IS_EXPENSE_FIELD_NAME, true)
					.query());
		} else {
			categories.addAll(categoryDao.queryBuilder().where() // Add income categories from DB
					.eq(Category.IS_EXPENSE_FIELD_NAME, false)
					.query());
		}
		ArrayAdapter<Category> adapter =
				new ArrayAdapter<Category>(context, android.R.layout.simple_spinner_item, categories);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
	}
	
	/**
	 * Method for populating the spinner by all available categories (expense + income)
	 * @param context
	 * @param categoryDao
	 * @param categorySpinner
	 * @throws SQLException
	 */
	public static void refreshCategorySpinnerEntries(Context context, Dao<Category, Integer> categoryDao, 
			Spinner categorySpinner) throws SQLException {	
		
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category()); // first entry should be empty

		categories.addAll(categoryDao.queryForAll()); // Add all categories from DB
		
		ArrayAdapter<Category> adapter =
				new ArrayAdapter<Category>(context, android.R.layout.simple_spinner_item, categories);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
	}
	
	/**
	 * 
	 * @param categorySpinner
	 * @param selectCategory
	 */
	public static void selectSpinnerCategory(Spinner categorySpinner, Category selectCategory) {
		SpinnerAdapter adapter = categorySpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Category category = (Category) adapter.getItem(i);
			if (category != null && category.equals(selectCategory)) {
				categorySpinner.setSelection(i);
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param activity
	 * @param databaseHelper
	 * @param accountId
	 */
	public static void updateReportTitleBar(Activity activity, DatabaseHelper databaseHelper, int accountId) {
		String accountName = null, currency;
		try{
			Dao<Account, Integer> accountDao = databaseHelper.getAccountDao();
 		   	Account account = accountDao.queryForId(accountId);
 		   	if (account != null) {
 		   		accountName = account.getName();
 		   		currency = account.getCurrency();	 		   		
 		   	} else {
 		   		throw new RuntimeException("Account \"" + accountName + "(id=" + accountId + ")\" was not found in DB!");
 		   	}
 	   	} catch (SQLException e) {
 	   		throw new RuntimeException(e);
 	   	}

		activity.setTitle(activity.getResources().getString(R.string.account_text) + ": " + accountName + ", " + currency);
	}
}
