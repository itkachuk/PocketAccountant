package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.util.Currency;
import java.util.Locale;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.activities.menus.ServiceMenuActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class StartupActivity extends OrmLiteBaseActivity<DatabaseHelper> implements OnClickListener {
	
	private static final String TAG = "PocketAccountant";
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Set up click listeners for all the buttons
        View newExpenseButton = findViewById(R.id.new_expense_button);
        newExpenseButton.setOnClickListener(this);
        View newIncomeButton = findViewById(R.id.new_income_button);
        newIncomeButton.setOnClickListener(this);
        View reportsButton = findViewById(R.id.reports_button);
        reportsButton.setOnClickListener(this);
        View serviceButton = findViewById(R.id.service_button);
        serviceButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
        
        if (!PreferencesUtils.isInitialized(this)) {
        	PreferencesUtils.initializePreferences(this);
        	populateDatabaseByDefault();
        	Log.d(TAG, "Database was populated with default values.");
        	Toast.makeText(getApplicationContext(), "Database was populated with default values.", Toast.LENGTH_LONG).show();
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
    
	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
	      case R.id.new_expense_button:
	    	  Log.d(TAG, "clicked on \"New Expense\"");
	          RecordEditorActivity.callMe(StartupActivity.this, true);
	         break;
	         
	      case R.id.new_income_button:
	    	  Log.d(TAG, "clicked on \"New Income\"");
	    	  RecordEditorActivity.callMe(StartupActivity.this, false);
	         break;

	      case R.id.reports_button:
	    	  Log.d(TAG, "clicked on \"Reports\"");
	          intent = new Intent(StartupActivity.this, ReportsMenuActivity.class);
	          startActivity(intent);
	    	  break;
	         
	      case R.id.service_button:
	    	  Log.d(TAG, "clicked on \"Service\"");
	    	  intent = new Intent(StartupActivity.this, ServiceMenuActivity.class);
	          startActivity(intent);
	    	  
	    	  // Temporary code for DB init!!!
//	    	  String mainAccountName = getResources().getString(R.string.main_account_name);
//	    	  String[] expenseCategories = getResources().getStringArray(R.array.expense_categories);
//	    	  String[] incomeCategories = getResources().getStringArray(R.array.income_categories);
//	    	  
//	    	  try {
//		    	  Dao<Account, String> accountDao = getHelper().getAccountDao();
//		    	  Dao<Category, String> categoryDao = getHelper().getCategoryDao();
//
//		    	  accountDao.createIfNotExists(new Account(mainAccountName, "Main account. Can't be removed by user"));
//		    	  
//		    	  for (String categoryName : expenseCategories) {
//		    		  categoryDao.createIfNotExists(new Category(categoryName, true, false));
//		    	  }
//		    	  for (String categoryName : incomeCategories) {
//		    		  categoryDao.createIfNotExists(new Category(categoryName, false, false));
//		    	  }
//	    	  } catch(SQLException e) {
//	    		  Log.e(TAG, "Error during DB init. " + e.getMessage());
//	    	  }  
	    	  // Temporary code for DB init!!!
	    	  
	          break;
	      case R.id.exit_button:
	         finish();
	         break;
	         
	      }
		
	}
	
	private void populateDatabaseByDefault() {

		String mainAccountName = getResources().getString(R.string.main_account_name);
		String mainAccountDescription = getResources().getString(R.string.main_account_description);
  	  	String[] expenseCategories = getResources().getStringArray(R.array.expense_categories);
  	  	String[] incomeCategories = getResources().getStringArray(R.array.income_categories);
  	  	Log.d(TAG, "Current locale: " + Locale.getDefault());
  	  	Log.d(TAG, "Current currency (saved to preferences): " + Currency.getInstance(Locale.getDefault()).getCurrencyCode());
  	  	String mainAccountCurrency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
		
		
		try {
			Dao<Account, Integer> accountDao = getHelper().getAccountDao();
			Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
			
			// Create main account
			accountDao.createIfNotExists(new Account(mainAccountName, mainAccountCurrency, mainAccountDescription));
			Account mainAccount = (Account) accountDao.queryForEq(Account.NAME_FIELD_NAME, mainAccountName).get(0);
			// Store main account as default
			PreferencesUtils.setDefaultAccountId(this, mainAccount.getId());

			for (String categoryName : expenseCategories) {
				categoryDao.createIfNotExists(new Category(categoryName, true));
			}
			for (String categoryName : incomeCategories) {
				categoryDao.createIfNotExists(new Category(categoryName, false));
			}
		} catch(SQLException e) {
			Log.e(TAG, "Error during DB init. " + e.getMessage());
		}
	}
}