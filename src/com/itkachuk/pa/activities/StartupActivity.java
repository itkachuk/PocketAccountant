package com.itkachuk.pa.activities;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import android.app.Activity;
import android.content.Intent;
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
        View serviceButton = findViewById(R.id.service_button);
        serviceButton.setOnClickListener(this);
        View exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(this);
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
	          intent = new Intent(StartupActivity.this, CreateNewRecordActivity.class);
	          intent.putExtra(CreateNewRecordActivity.EXTRAS_IS_EXPENSE, true);
	          startActivity(intent);
	         break;
	         // ...
	         
	      case R.id.new_income_button:
	    	  Log.d(TAG, "clicked on \"New Income\"");
	          intent = new Intent(StartupActivity.this, CreateNewRecordActivity.class);
	          intent.putExtra(CreateNewRecordActivity.EXTRAS_IS_EXPENSE, false);
	          startActivity(intent);
	         break;

	      case R.id.service_button:
	         
	    	  // Temporary code for DB init!!!
	    	  
	    	  
	          break;
	      case R.id.exit_button:
	         finish();
	         break;
	         
	      }
		
	}
}