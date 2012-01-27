package com.itkachuk.pa.activities.editors;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.itkachuk.pa.widgets.DateTimePicker;


public class RecordEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	
	private static final String TAG = "PocketAccountant";
	
	private static final String EXTRAS_RECORD_ID = "recordId";
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
	private RelativeLayout mDateTimeDialogView;
	private Dialog mDateTimeDialog;
	private DateTimePicker mDateTimePicker;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_editor);
        
        mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
        mAmountEditText = (EditText) findViewById(R.id.amount_edit_text);
        mDateButton = (Button) findViewById(R.id.edit_date_button);
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mDescriptionEditText = (AutoCompleteTextView) findViewById(R.id.description_edit_text);
        mSaveButton = (Button) findViewById(R.id.saveButton);
        mAmountEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        mDescriptionEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        
        setupDateTimeDialog();
       
        try {
        	refreshAccountSpinnerEntries();
			refreshCategorySpinnerEntries();
			
	        if (getRecordId() > -1) { // Edit existed record mode
	        	Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
	        	mExistedRecordToEdit = recordDao.queryForId(getRecordId());
	        	if (mExistedRecordToEdit != null) {
	        		loadFromObj(mExistedRecordToEdit);
	        	}	        	
	        }               			
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}
		updateDateButtonLabel();
		
		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		// add a click listener to the Save button
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					IncomeOrExpenseRecord record = saveToObj();
					Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
					if (getRecordId() > -1) { // Edit existed record mode
						recordDao.update(record);
					} else {
						recordDao.create(record); // Create new record
					}					
					finish();
					
				} catch (SQLException e){
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
		
		// add a click listener to the Date button
        mDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //showDialog(DATE_DIALOG_ID);

        		// Display the dialog
        		mDateTimeDialog.show();
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
    
	public static void callMe(Context c, boolean isExpense) {
		Intent intent = new Intent(c, RecordEditorActivity.class);
		intent.putExtra(EXTRAS_IS_EXPENSE, isExpense);
		c.startActivity(intent);
	}
    
	public static void callMe(Context c, boolean isExpense, int recordId) {
		Intent intent = new Intent(c, RecordEditorActivity.class);
		intent.putExtra(EXTRAS_IS_EXPENSE, isExpense);
		intent.putExtra(EXTRAS_RECORD_ID, recordId);		
		c.startActivity(intent);
	}
	
	private int getRecordId() {
		return getIntent().getIntExtra(EXTRAS_RECORD_ID, -1);
	}
	
	private boolean isExpense() {
		return getIntent().getBooleanExtra(EXTRAS_IS_EXPENSE, true);
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
		mAccountSpinner.setAdapter(adapter);
	}
	
	private void refreshCategorySpinnerEntries() throws SQLException {
		Dao<Category, String> categoryDao = getHelper().getCategoryDao();
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category()); // first entry should be empty
		if (isExpense()) {
			String[] array = getResources().getStringArray(R.array.expense_categories);
			for(String categoryName : array) {
				categories.add(new Category(categoryName, true, false)); // Add predefined categories
			}
			categories.addAll(categoryDao.queryBuilder().where() // Add custom categories from DB
					.eq(Category.IS_EXPENSE_FIELD_NAME, true)
					.query());
		} else {
			String[] array = getResources().getStringArray(R.array.income_categories);
			for(String categoryName : array) {
				categories.add(new Category(categoryName, false, false));
			}
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
		DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();
		mDateButton.setText(dateFormatter.format(mDateTimePicker.getDateTimeMillis()));
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
		
		if (mAccountSpinner.getSelectedItem() != null) {
			Account account = (Account) mAccountSpinner.getSelectedItem();
			if (account != null) {
				record.setAccount(account.getName());
			}
		}
		
		try {
			double amount = Double.valueOf(mAmountEditText.getText().toString());
			amount = (double)Math.round(amount * 100) / 100; // trim for two places after decimal point
			record.setAmount(amount);
		} catch(Exception e) {
			throw new IllegalArgumentException(getResources().getString(R.string.wrong_amount_number_message));
		}
		
		// Set date and time stamp
		long millis = mDateTimePicker.getDateTimeMillis();
		record.setTimestamp(mDateTimePicker.getDateTimeMillis());
		if (recordId == -1) { // Create new record mode			
			record.setDate(new Date(millis)); 
		} else {
			record.getDate().setTime(millis);
		}
		
		
		if (mCategorySpinner.getSelectedItem() != null) {
			Category category = (Category) mCategorySpinner.getSelectedItem();
			if (category != null && !category.toString().equals("")) {
				record.setCategory(category.getName());
			} else {
				throw new IllegalArgumentException(getResources().getString(R.string.not_selected_category_message));
			}
		}
		
		// Set description
		try {
			record.setDescription(mDescriptionEditText.getText().toString());
		} catch(Exception e){}
		
		// Set flags, only when create new record. We can't edit them later
		if (recordId == -1) { // Create new record mode
			if (isExpense()) {
				record.setExpense(true);
			} else {
				record.setExpense(false);
			}
			record.setRegular(false); // from UI wizard - always false
			record.setPlanned(false); // functionality for Expenses Planning isn't implemented yet. Set to false.
		}
		
		return record;
	}
	
	private void loadFromObj(IncomeOrExpenseRecord record) throws SQLException {
		selectSpinnerAccount(record.getAccount()); 
		mAmountEditText.setText(Double.toString(record.getAmount()));
		// Update Date components
		mDateTimePicker.setDateTimeMillis(record.getTimestamp());
		mDateTimePicker.updateDate(mDateTimePicker.get(Calendar.YEAR), mDateTimePicker.get(Calendar.MONTH), mDateTimePicker.get(Calendar.DAY_OF_MONTH));
		mDateTimePicker.updateTime(mDateTimePicker.get(Calendar.HOUR_OF_DAY), mDateTimePicker.get(Calendar.MINUTE));
		//Log.d(TAG, "loadFromObj: date " + mDateTimePicker.get(Calendar.YEAR) + "-" + mDateTimePicker.get(Calendar.MONTH) + "-" + mDateTimePicker.get(Calendar.DAY_OF_MONTH));
		//Log.d(TAG, "loadFromObj: time " + mDateTimePicker.get(Calendar.HOUR_OF_DAY) + ":" + mDateTimePicker.get(Calendar.MINUTE));
		
		selectSpinnerCategory(record.getCategory()); 
		mDescriptionEditText.setText(record.getDescription());
	}
	
	private void selectSpinnerAccount(String accountName) {
		SpinnerAdapter adapter = mAccountSpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Account account = (Account) adapter.getItem(i);
			if (account != null && account.getName() != null && account.getName().equals(accountName)) {
				mAccountSpinner.setSelection(i);
				break;
			}
		}
	}
	
	private void selectSpinnerCategory(String categoryName) {
		SpinnerAdapter adapter = mCategorySpinner.getAdapter();
		int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			Category category = (Category) adapter.getItem(i);
			if (category != null && category.getName() != null && category.getName().equals(categoryName)) {
				mCategorySpinner.setSelection(i);
				break;
			}
		}
	}	
	
	private void setupDateTimeDialog() {
		// Create the dialog
		mDateTimeDialog = new Dialog(this);
		// Inflate the root layout
		mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
		// Grab widget instance
		mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
		// Check is system is set to use 24h time (this doesn't seem to work as expected though)
		final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
		final boolean is24h = !(timeS == null || timeS.equals("12"));
		
		// Update demo TextViews when the "OK" button is clicked 
		((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.clearFocus();
				updateDateButtonLabel();
				mDateTimeDialog.dismiss();
			}
		});

		// Cancel the dialog when the "Cancel" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimeDialog.cancel();
			}
		});

		// Reset Date and Time pickers when the "Reset" button is clicked
		((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mDateTimePicker.reset();
			}
		});
		
		// Setup TimePicker
		mDateTimePicker.setIs24HourView(is24h);
		// No title on the dialog window
		mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Set the dialog content view
		mDateTimeDialog.setContentView(mDateTimeDialogView);
	}
}
