package com.itkachuk.pa.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

public class ActivityUtils {
	private static final String TAG = "PocketAccountant";
	
	public static void refreshAccountSpinnerEntries(Context context, Dao<Account, String> accountDao, 
			Spinner accountSpinner) throws SQLException {	
		
		List<Account> accounts = new ArrayList<Account>();
		String mainAccountName = context.getResources().getString(R.string.main_account_name);		
		accounts.add(new Account(mainAccountName, null, null, false)); // first add main account to spinner
		accounts.addAll(accountDao.queryForAll()); // then add all user's accounts from DB
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(context, android.R.layout.simple_spinner_item, accounts);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(adapter);
	}
	
	public static void updateReportTitleBar(Activity activity, DatabaseHelper databaseHelper, String accountName) {
		String currency;
		//If [main] account - get currency from Preferences
		if (accountName.equals(activity.getResources().getString(R.string.main_account_name))) { 
			currency = PreferencesUtils.getMainAccountCurrency(activity);
		} else { // if not [main] - get currency from DB
			try{
	 		   	Dao<Account, String> accountDao = databaseHelper.getAccountDao();
	 		   	Account account = accountDao.queryForEq(Account.NAME_FIELD_NAME, accountName).get(0);
	 		   	currency = account.getCurrency();
	 	   	} catch (SQLException e) {
	 	   		throw new RuntimeException(e);
	 	   	}
		}
		activity.setTitle("Account: " + accountName + ", " + currency);
	}
}
