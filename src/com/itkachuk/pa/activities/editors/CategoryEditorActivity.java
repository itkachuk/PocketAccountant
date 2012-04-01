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
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class CategoryEditorActivity extends OrmLiteBaseActivity<DatabaseHelper>{
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_CATEGORY_ID = "categoryId";
	
	private EditText mCategoryNameEditText;
	private Spinner mCategoryGroupSpinner;
	private Button mSaveButton;
	
	private Category mExistedCategoryToEdit;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.category_editor);
        
        mCategoryNameEditText = (EditText) findViewById(R.id.categoryNameEditText);
        mCategoryGroupSpinner = (Spinner) findViewById(R.id.categoryGroupSpinner);
        mSaveButton = (Button) findViewById(R.id.saveButton);      

        try {
	        if (getCategoryId() != -1) { // Edit existed category mode
	        	Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
	        	mExistedCategoryToEdit = categoryDao.queryForId(getCategoryId());
	        	if (mExistedCategoryToEdit != null) {
	        		loadFromObj(mExistedCategoryToEdit);
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
					Category category = saveToObj();
					Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
					if (getCategoryId() != -1) { // Edit existed category mode
						categoryDao.update(category); 
					} else {
						categoryDao.createIfNotExists(category); // Create new category
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
		Intent intent = new Intent(c, CategoryEditorActivity.class);
		c.startActivity(intent);
	}
    
	public static void callMe(Context c, int categoryId) {
		Intent intent = new Intent(c, CategoryEditorActivity.class);
		intent.putExtra(EXTRAS_CATEGORY_ID, categoryId);		
		c.startActivity(intent);
	}
	
	private int getCategoryId() {
		return getIntent().getIntExtra(EXTRAS_CATEGORY_ID, -1);
	}
	
	private void selectSpinnerCategoryGroup(boolean isExpense) {
		if (isExpense) {
			mCategoryGroupSpinner.setSelection(0); // "Expenses" group is on first position
		} else {
			mCategoryGroupSpinner.setSelection(1); // "Incomes" group is on second position
		}
	}
	
	private Category saveToObj() {
		Category category;
		
		if (getCategoryId() != -1) { // Edit existed category mode
			category = mExistedCategoryToEdit;
		} else { // Create new category mode
			category = new Category();
		}
		// For Category changing name is allowed
		String name = mCategoryNameEditText.getText().toString();
		if (name != null && !name.equals("")) { // TODO - add additional input validation for correct chars
			category.setName(mCategoryNameEditText.getText().toString()); 
		} else {
			throw new IllegalArgumentException(getResources().getString(R.string.empty_category_message));	
		}			
		
		// Set Group
		int position = (int) mCategoryGroupSpinner.getSelectedItemPosition();
		if (position == 0) {
			category.setExpense(true);
		} else {
			category.setExpense(false);
		}				
		
		return category;
	}
	
	private void loadFromObj(Category category) {
		mCategoryNameEditText.setText(category.getName());
		selectSpinnerCategoryGroup(category.isExpense());		
		// Now we are trying to use the data model with foreign keys
		
		// Old comment: foreign keys are not in use now. This means, 
		// that if category is updated, the records which include it's old name, won't be updated.
		// We might want to return back the foreign key mechanism in DB model.
	}
}
