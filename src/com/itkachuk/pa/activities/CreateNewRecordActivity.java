package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
	
	private Spinner mAccountSpinner;
	private EditText mAmountEditText;
	private Button mDateButton;
	private Spinner mCategorySpinner;
	private AutoCompleteTextView mDescriptionEditText;
	private Button mSaveButton;
	
	private IncomeOrExpenseRecord mExistedRecordToEdit;
	// Date picker fields
	private DatePickerDialog.OnDateSetListener mOnDateSetListener;
	private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_new_record);
        
        mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
        mAmountEditText = (EditText) findViewById(R.id.amount_edit_text);
        mDateButton = (Button) findViewById(R.id.edit_date_button);
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mDescriptionEditText = (AutoCompleteTextView) findViewById(R.id.subcategory_edit_text);
        mSaveButton = (Button) findViewById(R.id.create_record_button);
              
        // get the current date
        getCurrentDate();
       
        try {
        	refreshAccountSpinnerEntries();
			refreshCategorySpinnerEntries();
			
	        if (getRecordId() > -1) { // Edit existed record mode
	        	Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
	        	mExistedRecordToEdit = recordDao.queryForId(getRecordId());
	        	if (mExistedRecordToEdit != null) {
	        		loadFromObj(mExistedRecordToEdit);
	        	}
	        	
	        } else {
	        	updateDateButtonLabel();
	        }
               			
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}
		
		// add a click listener to the Save button
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					IncomeOrExpenseRecord record = saveToObj();
					Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
					// TODO logic for update goes here
					recordDao.create(record);
					Intent intent = new Intent(CreateNewRecordActivity.this, StartupActivity.class);        
			        startActivity(intent);
					
				} catch (SQLException e){
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					// TODO - show error dialog
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		// add a click listener to the Date button
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });
        
        // add a listener to the Date Picker dialog
        mOnDateSetListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                updateDateButtonLabel();
            }
        };
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
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    mOnDateSetListener,
	                    mYear, mMonth, mDay);
	    }
	    return null;
	}
	
	private int getRecordId() {
		return getIntent().getIntExtra(RECORD_ID, -1);
	}
	
	private boolean isExpense() {
		return getIntent().getBooleanExtra(EXTRAS_IS_EXPENSE, true);
	}
	
	private void getCurrentDate() {
		final Calendar calendar = Calendar.getInstance();
	    mYear = calendar.get(Calendar.YEAR);
	    mMonth = calendar.get(Calendar.MONTH);
	    mDay = calendar.get(Calendar.DAY_OF_MONTH);
	}
	
	private void refreshAccountSpinnerEntries() throws SQLException {
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		List<Account> accounts = new ArrayList<Account>();
		accounts.addAll(accountDao.queryForAll());
		ArrayAdapter<Account> adapter =
				new ArrayAdapter<Account>(this, android.R.layout.simple_spinner_item, accounts);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAccountSpinner.setAdapter(adapter);
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
		mCategorySpinner.setAdapter(adapter);
	}
	
	private void updateDateButtonLabel() {
		//Date date = new Date();
		//SimpleDateFormat dateFormatter = new SimpleDateFormat();
		DateFormat dateFormatter = SimpleDateFormat.getDateInstance();
		//dateFormatter.applyPattern("yyyy-MM-dd");
		mDateButton.setText(dateFormatter.format(new Date(mYear - 1900, mMonth, mDay)));
		
	}
	
	private void updateDateButtonLabel(int year, int month, int day) {
		//Date date = new Date();
		//SimpleDateFormat dateFormatter = new SimpleDateFormat();
		DateFormat dateFormatter = SimpleDateFormat.getDateInstance();
		//dateFormatter.applyPattern("yyyy-MM-dd");
		mDateButton.setText(dateFormatter.format(new Date(year - 1900, month, day)));
		
	}
	
	private IncomeOrExpenseRecord saveToObj() {
		IncomeOrExpenseRecord record;
		
		// Set Id
		int recordId = getRecordId();
		if (recordId > -1) { // Edit existed record mode
			record = mExistedRecordToEdit;
		} else { // Create new record mode
			record = new IncomeOrExpenseRecord();		
		}
		
		// Set account - TODO work with spinner
		if (mAccountSpinner.getSelectedItem() != null) {
			Account account = (Account) mAccountSpinner.getSelectedItem();
			if (account != null) {
				record.setAccount(account);
			}
		}
		
		// Set amount TODO - handle exceptions
		try {
			record.setAmount(getDoubleFromString(mAmountEditText.getText().toString()));
		} catch(Exception e) {
			throw new IllegalArgumentException(getResources().getString(R.string.wrong_amount_number_message));
		}
		
		// Set date and time stamp
		if (recordId == -1) { // Create new record mode
			long millis = System.currentTimeMillis();
			record.setTimestamp(millis);			
		}
		record.setDate(new Date(mYear - 1900, mMonth, mDay)); 
		
		// Set category - TODO work with spinner
		if (mCategorySpinner.getSelectedItem() != null) {
			Category category = (Category) mCategorySpinner.getSelectedItem();
			if (category != null && !category.toString().equals("")) {
				record.setCategory(category);
			} else {
				throw new IllegalArgumentException(getResources().getString(R.string.empty_category_message));
			}
		}
		
		// Set description
		try {
			record.setDescription(mDescriptionEditText.getText().toString());
		} catch(Exception e){}
		
		// Set flags
		if (isExpense()) {
			record.setExpense(true);
		} else {
			record.setExpense(false);
		}
		record.setRegular(false); // from UI wizard - always false
		
		return record;
	}
	
	private void loadFromObj(IncomeOrExpenseRecord record) throws SQLException {
		//TODO
	}
	
	private double getDoubleFromString(String number) {
		number.replace(',', '.');
		return Double.parseDouble(number);
	}
}
