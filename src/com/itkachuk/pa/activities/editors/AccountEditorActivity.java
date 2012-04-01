package com.itkachuk.pa.activities.editors;

import java.sql.SQLException;

import android.content.Context;
import android.content.Intent;
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
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class AccountEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_ACCOUNT_ID = "accountId";
	
	private EditText mAccountNameEditText;
	private Spinner mAccountCurrencySpinner;
	private EditText mAccountDescriptionEditText;
	private Button mSaveButton;
	
	private Account mExistedAccountToEdit;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_editor);
        
        mAccountNameEditText = (EditText) findViewById(R.id.account_name_edit_text);
        mAccountCurrencySpinner = (Spinner) findViewById(R.id.account_currency_spinner);
        mAccountDescriptionEditText = (EditText) findViewById(R.id.account_description_edit_text);
        mSaveButton = (Button) findViewById(R.id.saveButton);      

        refreshCurrencySpinnerEntries();
        try {
	        if (getAccountId() != -1) { // Edit existed account mode
	        	Dao<Account, Integer> accountDao = getHelper().getAccountDao();
	        	mExistedAccountToEdit = accountDao.queryForId(getAccountId());
	        	if (mExistedAccountToEdit != null) {
	        		loadFromObj(mExistedAccountToEdit);
	        	}	        	
	        }               			
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
					Account account = saveToObj();
					Dao<Account, Integer> accountDao = getHelper().getAccountDao();
					if (getAccountId() != -1) { // Edit existed account mode
						accountDao.update(account);
					} else {
						accountDao.createIfNotExists(account); // Create new account
					}					
					finish();					
				} catch (SQLException e){
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});       
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
		Intent intent = new Intent(c, AccountEditorActivity.class);
		c.startActivity(intent);
	}
    
	public static void callMe(Context c, int accountId) {
		Intent intent = new Intent(c, AccountEditorActivity.class);
		intent.putExtra(EXTRAS_ACCOUNT_ID, accountId);		
		c.startActivity(intent);
	}
	
	private int getAccountId() {
		return getIntent().getIntExtra(EXTRAS_ACCOUNT_ID, -1);
	}
	
	private void refreshCurrencySpinnerEntries() {
		String[] currenciesList = getResources().getStringArray(R.array.currencies_list);
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currenciesList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAccountCurrencySpinner.setAdapter(adapter);
	}
	
	private void selectSpinnerCurrency(String currencyCode) {
		SpinnerAdapter adapter = mAccountCurrencySpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			String currency = (String) adapter.getItem(i);
			if (currency != null && currency.equals(currencyCode)) {
				mAccountCurrencySpinner.setSelection(i);
				break;
			}
		}
	}
	
	private Account saveToObj() {
		Account account;
		
		if (getAccountId() != -1) { // Edit existed account mode
			account = mExistedAccountToEdit;
		} else { // Create new account mode
			account = new Account();
			String name = mAccountNameEditText.getText().toString();
			if (name != null && !name.equals("")) { // TODO - add additional input validation for correct chars
				account.setName(mAccountNameEditText.getText().toString()); // Name can't be edited
			} else {
				throw new IllegalArgumentException(getResources().getString(R.string.empty_account_message));	
			}		
		}
		
		// Set Currency
		String currency = (String) mAccountCurrencySpinner.getSelectedItem();
		if (currency != null) {
			account.setCurrency(currency);
		}
		
		// Set description
		try {
			account.setDescription(mAccountDescriptionEditText.getText().toString());
		} catch(Exception e){}
		
		return account;
	}
	
	private void loadFromObj(Account account) {
		mAccountNameEditText.setText(account.getName());
		selectSpinnerCurrency(account.getCurrency());
		mAccountDescriptionEditText.setText(account.getDescription());
	}
}
