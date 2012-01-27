package com.itkachuk.pa.activities.editors;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class PreferencesEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	private static final String TAG = "PocketAccountant";
	
	// Shared Preferences keys
	public static final String PREFS_NAME = "paPreferences";
	public static final String PREFS_IS_INITIALIZED = "isInitialized";
	public static final String PREFS_DEFAULT_ACCOUNT = "defaultAccount";
	public static final String PREFS_MAIN_ACCOUNT_CURRENCY = "mainAccountCurrency";
	public static final String PREFS_ROWS_PER_PAGE = "rowsPerPage";
	
	private Spinner mDefaultAccountSpinner;
	private Spinner mMainAccountCurrencySpinner;
	private EditText mRowsPerPageEditText;
	private Button mSaveButton;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_editor);
        
        mDefaultAccountSpinner = (Spinner) findViewById(R.id.defaultAccountSpinner);
        mMainAccountCurrencySpinner = (Spinner) findViewById(R.id.mainAccountCurrencySpinner);
        mRowsPerPageEditText = (EditText) findViewById(R.id.rowsPerPageEditText);
        mSaveButton = (Button) findViewById(R.id.saveButton);      

        
        refreshCurrencySpinnerEntries();
        
        try {
        	refreshAccountSpinnerEntries();
        	restorePreferences();
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					savePreferences();
					finish();					
//				} catch (SQLException e){
//					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});       
    }

	private void restorePreferences() {
		SharedPreferences programSettings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		selectSpinnerAccount(programSettings.getString(PREFS_DEFAULT_ACCOUNT, 
				getResources().getString(R.string.main_account_name)));
		selectSpinnerCurrency(programSettings.getString(PREFS_MAIN_ACCOUNT_CURRENCY, 
				Currency.getInstance(Locale.getDefault()).getCurrencyCode()));
		mRowsPerPageEditText.setText(Integer.toString(programSettings.getInt(PREFS_ROWS_PER_PAGE, 
				HistoryReportActivity.DEFAULT_ROWS_PER_PAGE_NUMBER)));
	}

	private void savePreferences() {
		int rowsPerPage = Integer.valueOf(mRowsPerPageEditText.getText().toString());
		if (rowsPerPage < 5 || rowsPerPage > 50)
			throw new IllegalArgumentException(getResources().getString(R.string.wrong_rows_per_page_number_message));
		
		SharedPreferences programSettings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		SharedPreferences.Editor editor = programSettings.edit();
		
		editor.putString(PREFS_DEFAULT_ACCOUNT, mDefaultAccountSpinner.getSelectedItem().toString());				
		editor.putString(PREFS_MAIN_ACCOUNT_CURRENCY, mMainAccountCurrencySpinner.getSelectedItem().toString());
		editor.putInt(PREFS_ROWS_PER_PAGE, Integer.valueOf(mRowsPerPageEditText.getText().toString()));
		editor.commit();
	}
	
	@Override
    protected void onResume() {
       super.onResume();
    }

    @Override
    protected void onPause() {
       super.onPause();
    }
    
    @Override
    protected void onDestroy() {
       super.onDestroy();
       finish();
    }
    
	public static void callMe(Context c) {
		Intent intent = new Intent(c, PreferencesEditorActivity.class);
		c.startActivity(intent);
	}
	
	private void refreshAccountSpinnerEntries() throws SQLException {
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		List<Account> accounts = new ArrayList<Account>();
		String mainAccountName = getResources().getString(R.string.main_account_name);		
		accounts.add(new Account(mainAccountName, null, null, false)); // first add main account to spinner
		accounts.addAll(accountDao.queryForAll()); // then add all user's accounts from DB
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, accounts);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mDefaultAccountSpinner.setAdapter(adapter);
	}
	
	private void selectSpinnerAccount(String accountName) {
		SpinnerAdapter adapter = mDefaultAccountSpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Account account = (Account) adapter.getItem(i);
			if (account != null && account.getName() != null && account.getName().equals(accountName)) {
				mDefaultAccountSpinner.setSelection(i);
				break;
			}
		}
	}
	
	private void refreshCurrencySpinnerEntries() {
		String[] currenciesList = getResources().getStringArray(R.array.currencies_list);
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currenciesList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mMainAccountCurrencySpinner.setAdapter(adapter);
	}
	
	private void selectSpinnerCurrency(String currencyCode) {
		SpinnerAdapter adapter = mMainAccountCurrencySpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			String currency = (String) adapter.getItem(i);
			if (currency != null && currency.equals(currencyCode)) {
				mMainAccountCurrencySpinner.setSelection(i);
				break;
			}
		}
	}		
}
