package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.util.Currency;
import java.util.Locale;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.activities.menus.ServiceMenuActivity;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

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
        
        SharedPreferences programSettings = getSharedPreferences(PreferencesEditorActivity.PREFS_NAME, MODE_PRIVATE);
        if (!programSettings.contains(PreferencesEditorActivity.PREFS_IS_INITIALIZED)) {
        	SharedPreferences.Editor editor = programSettings.edit();
        	// Set default preferences
        	editor.putString(
        			PreferencesEditorActivity.PREFS_DEFAULT_ACCOUNT, 
        			getResources().getString(R.string.main_account_name));
        	
        	editor.putString(
        			PreferencesEditorActivity.PREFS_MAIN_ACCOUNT_CURRENCY, 
        			Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        	Log.d(TAG, "Current locale: " + Locale.getDefault());
        	Log.d(TAG, "Current currency (saved to preferences): " + Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        	
        	editor.putInt(
        			PreferencesEditorActivity.PREFS_ROWS_PER_PAGE, 
        			HistoryReportActivity.DEFAULT_ROWS_PER_PAGE_NUMBER);
        	
        	editor.putBoolean(PreferencesEditorActivity.PREFS_IS_INITIALIZED, true);
        	editor.commit();
        	Log.d(TAG, "Program Preferences initialized successfully.");
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
}