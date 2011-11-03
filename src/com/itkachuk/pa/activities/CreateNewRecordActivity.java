package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;


public class CreateNewRecordActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	
	private static final String TAG = "PocketAccountant";
	
	private static final String RECORD_ID = "recordId";
	public static final String EXTRAS_IS_EXPENSE = "isExpense";
	public static final String EXTRAS_IS_REGULAR = "isRegular";
	
	private Spinner accountSpinner;
	private EditText amountEditText;
	private Button dateButton;
	private Spinner categorySpinner;
	private AutoCompleteTextView descriptionEditText;
	private Button saveButton;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_record);
        
        accountSpinner = (Spinner) findViewById(R.id.account_spinner);
        amountEditText = (EditText) findViewById(R.id.amount_edit_text);
        dateButton = (Button) findViewById(R.id.edit_date_button);
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        descriptionEditText = (AutoCompleteTextView) findViewById(R.id.subcategory_edit_text);
        saveButton = (Button) findViewById(R.id.create_record_button);
        
        try {
			refreshAccountSpinnerEntries();
			refreshCategorySpinnerEntries();
		} catch (SQLException e) {
			Log.e(TAG, "Error during Spinners refreshing. " + e.getMessage());
		}
        
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
		c.startActivity(new Intent(c, CreateNewRecordActivity.class));
	}

	public static void callMe(Context c, int recordId) {
		Intent intent = new Intent(c, CreateNewRecordActivity.class);
		intent.putExtra(RECORD_ID, recordId);
		c.startActivity(intent);
	}
	
	private int getRecordId() {
		return getIntent().getIntExtra(RECORD_ID, -1);
	}
	
	private boolean isExpense() {
		return getIntent().getBooleanExtra(EXTRAS_IS_EXPENSE, true);
	}
	
	private void refreshAccountSpinnerEntries() throws SQLException {
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		List<Account> accounts = new ArrayList<Account>();
		accounts.addAll(accountDao.queryForAll());
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, accounts);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		accountSpinner.setAdapter(adapter);
	}
	
	private void refreshCategorySpinnerEntries() throws SQLException {
		Dao<Category, String> categoryDao = getHelper().getCategoryDao();
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category()); // first entry should be empty
		if (isExpense()) {
			categories.addAll(categoryDao.queryBuilder().where()
					.eq(Category.IS_EXPENSE_FIELD_NAME, true)
					.query());
		} else {
			categories.addAll(categoryDao.queryBuilder().where()
					.eq(Category.IS_EXPENSE_FIELD_NAME, false)
					.query());
		}
		ArrayAdapter<Category> adapter =
				new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, categories);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(adapter);
	}
	
	private IncomeOrExpenseRecord saveToObj() {
		IncomeOrExpenseRecord record = new IncomeOrExpenseRecord();
		
		// Set Id
		int recordId = getRecordId();
		if (recordId > -1) {
			record.setId(recordId);
		}
		// Set account - TODO work with spinner
		if (accountSpinner.getSelectedItem() != null) {
			Account account = (Account) accountSpinner.getSelectedItem();
			if (account != null) {
				record.setAccount(account);
			}
		}
		// Set amount
		record.setAmount(getDoubleFromString(amountEditText.getText().toString()));
		// Set date and time stamp
		long millis = System.currentTimeMillis();
		record.setTimestamp(millis);
		record.setDate(new Date(millis)); // TODO - now hardcoded to use only current date
		// Set category - TODO work with spinner
		if (categorySpinner.getSelectedItem() != null) {
			Category category = (Category) categorySpinner.getSelectedItem();
			if (category != null) {
				record.setCategory(category);
			}
		}
		// Set description
		record.setDescription(descriptionEditText.getText().toString());
		// Set flags
		if (isExpense()) {
			record.setExpense(true);
		} else {
			record.setExpense(false);
		}
		record.setRegular(false); // from UI wizard - always false
		
		return record;
	}
	
	private double getDoubleFromString(String number) {
		number.replace(',', '.');
		return Double.parseDouble(number);
	}
}
