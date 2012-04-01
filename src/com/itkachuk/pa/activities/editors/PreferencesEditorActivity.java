package com.itkachuk.pa.activities.editors;

import java.sql.SQLException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class PreferencesEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	private static final String TAG = "PocketAccountant";
		
	private Spinner mDefaultAccountSpinner;
	private Spinner mLanguageSpinner;
	private EditText mRowsPerPageEditText;
	private Button mSaveButton;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_editor);
        
        mDefaultAccountSpinner = (Spinner) findViewById(R.id.defaultAccountSpinner);
        mLanguageSpinner = (Spinner) findViewById(R.id.languageSpinner);
        mRowsPerPageEditText = (EditText) findViewById(R.id.rowsPerPageEditText);
        mSaveButton = (Button) findViewById(R.id.saveButton);      

        // TODO - populate Language Spinner
        
        try {
        	ActivityUtils.refreshAccountSpinnerEntries(this, getHelper().getAccountDao(), mDefaultAccountSpinner);
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
		ActivityUtils.selectSpinnerAccount(mDefaultAccountSpinner, PreferencesUtils.getDefaultAccountId(this));
	//	selectSpinnerCurrency(PreferencesUtils.getMainAccountCurrency(this)); // TODO
		mRowsPerPageEditText.setText(new Integer(PreferencesUtils.getRowsPerPage(this)).toString());
	}

	private void savePreferences() {
		int rowsPerPage = Integer.valueOf(mRowsPerPageEditText.getText().toString());
		if (rowsPerPage < 5 || rowsPerPage > 50)
			throw new IllegalArgumentException(getResources().getString(R.string.wrong_rows_per_page_number_message));
		
		Account account = (Account)(mDefaultAccountSpinner.getSelectedItem());
		PreferencesUtils.setDefaultAccountId(this, account.getId());				
	//	PreferencesUtils.setMainAccountCurrency(this, mMainAccountCurrencySpinner.getSelectedItem().toString());
		PreferencesUtils.setRowsPerPage(this, Integer.valueOf(mRowsPerPageEditText.getText().toString()));
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
}
