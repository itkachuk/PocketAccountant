package com.itkachuk.pa.activities;

import java.sql.SQLException;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class AccountEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_ACCOUNT_NAME = "accountName";
	
	private EditText mAccountNameEditText;
	private EditText mAccountDescriptionEditText;
	private Button mBackButton;
	private Button mSaveButton;
	
	private Account mExistedAccountToEdit;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_editor);
        
        mAccountNameEditText = (EditText) findViewById(R.id.account_name_edit_text);
        mAccountDescriptionEditText = (EditText) findViewById(R.id.account_description_edit_text);
        mSaveButton = (Button) findViewById(R.id.save_button);
        mBackButton = (Button) findViewById(R.id.back_button);      

       
        try {
	        if (getAccountName() != null) { // Edit existed account mode
	        	Dao<Account, String> recordDao = getHelper().getAccountDao();
	        	mExistedAccountToEdit = recordDao.queryForId(getAccountName());
	        	if (mExistedAccountToEdit != null) {
	        		loadFromObj(mExistedAccountToEdit);
	        	}	        	
	        }               			
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}

		mBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				try {
					Account account = saveToObj();
					Dao<Account, String> accountDao = getHelper().getAccountDao();
					if (getAccountName() != null) { // Edit existed account mode
						accountDao.update(account);
					} else {
						accountDao.create(account); // Create new account
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
    
	public static void callMe(Context c, String accountName) {
		Intent intent = new Intent(c, AccountEditorActivity.class);
		intent.putExtra(EXTRAS_ACCOUNT_NAME, accountName);		
		c.startActivity(intent);
	}
	
	private String getAccountName() {
		return getIntent().getStringExtra(EXTRAS_ACCOUNT_NAME);
	}
	
	private Account saveToObj() {
		Account account;
		
		if (getAccountName() != null) { // Edit existed record mode
			account = mExistedAccountToEdit;
		} else { // Create new record mode
			account = new Account();
			String name = mAccountNameEditText.getText().toString();
			if (name != null && name != "") {
				account.setName(mAccountNameEditText.getText().toString()); // Name can't be edited
			} else {
				throw new IllegalArgumentException(getResources().getString(R.string.empty_account_message));	
			}		
		}
		
		// Set description
		try {
			account.setDescription(mAccountDescriptionEditText.getText().toString());
		} catch(Exception e){}
		
		// Set removable flag
		account.setRemovable(true);
		
		return account;
	}
	
	private void loadFromObj(Account account) {
		mAccountNameEditText.setText(account.getName());
		mAccountDescriptionEditText.setText(account.getDescription());
		// Disable Account Name Edit Text, when we are editing existing account
		mAccountNameEditText.setEnabled(false);
	}
}
