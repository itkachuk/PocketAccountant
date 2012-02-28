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
	public static void refreshAccountSpinnerEntries(Context context, Dao<Account, String> accountDao, 
			Spinner accountSpinner) throws SQLException {	
		
		List<Account> accounts = new ArrayList<Account>();
		String mainAccountName = context.getResources().getString(R.string.main_account_name);
		Account mainAccount = new Account(mainAccountName, PreferencesUtils.getMainAccountCurrency(context), null, false);
		accounts.add(mainAccount); // first add main account to spinner
		accounts.addAll(accountDao.queryForAll()); // then add all user's accounts from DB
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(context, android.R.layout.simple_spinner_item, accounts);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(adapter);
		
		// Select default account	        	
		selectSpinnerAccount(accountSpinner, PreferencesUtils.getDefaultAccountName(context));
	}
	
	/**
	 * 
	 * @param accountSpinner
	 * @param accountName
	 */
	public static void selectSpinnerAccount(Spinner accountSpinner, String accountName) {
		SpinnerAdapter adapter = accountSpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Account account = (Account) adapter.getItem(i);
			if (account != null && account.getName() != null && account.getName().equals(accountName)) {
				accountSpinner.setSelection(i);
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param activity
	 * @param databaseHelper
	 * @param accountName
	 */
	public static void updateReportTitleBar(Activity activity, DatabaseHelper databaseHelper, String accountName) {
		String currency;
		//If [main] account - get currency from Preferences
		if (accountName.equals(activity.getResources().getString(R.string.main_account_name))) { 
			currency = PreferencesUtils.getMainAccountCurrency(activity);
		} else { // if not [main] - get currency from DB
			try{
				Dao<Account, String> accountDao = databaseHelper.getAccountDao();
	 		   	List<Account> accountsList = accountDao.queryForEq(Account.NAME_FIELD_NAME, accountName);
	 		   	if (accountsList != null && accountsList.size() > 0) {
	 		   		Account account = accountsList.get(0);
	 		   		currency = account.getCurrency();	 		   		
	 		   	} else {
	 		   		throw new RuntimeException("Account \"" + accountName + "\" was not found in DB!");
	 		   	}
	 	   	} catch (SQLException e) {
	 	   		throw new RuntimeException(e);
	 	   	}
		}
		activity.setTitle(activity.getResources().getString(R.string.account_text) + ": " + accountName + ", " + currency);
	}
}
