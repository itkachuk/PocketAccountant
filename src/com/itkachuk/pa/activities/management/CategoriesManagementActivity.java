package com.itkachuk.pa.activities.management;

import java.sql.SQLException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.sectionedList.ListItemAdapter;
import com.itkachuk.pa.sectionedList.SectionItem;
import com.itkachuk.pa.sectionedList.SectionedListItem;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class CategoriesManagementActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private ListView listView;
	private AlertDialog.Builder builder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.items_list);
		builder = new AlertDialog.Builder(this);

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		Button addButton = (Button) findViewById(R.id.addButton);		
		addButton.setText(getResources().getString(R.string.add_category_button_label));
		
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				//AccountEditorActivity.callMe(CategoriesManagementActivity.this);
			}
		});

		listView = (ListView) findViewById(R.id.itemsList);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Category category = (Category) listView.getAdapter().getItem(i);
				if (category.isRemovable()) {
					//AccountEditorActivity.callMe(CategoriesManagementActivity.this, account.getName());
				}
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Category category = (Category) listView.getAdapter().getItem(i);
				if (!category.isRemovable()) return true;

				builder.setMessage(getResources().getString(R.string.category_delete_dialog))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try{
				        		   Dao<Category, String> categoryDao = getHelper().getCategoryDao();
				        		   categoryDao.deleteById(category.getName());
				        		   fillList();
				        	   } catch (SQLException e) {
				        		   throw new RuntimeException(e);
				        	   }
				           }
				       })
				       .setNegativeButton(getResources().getString(R.string.no_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
				return true;
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			fillList();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void fillList() throws SQLException {
		Log.d(TAG, "Show list of categories");
		Dao<Category, String> categoryDao = getHelper().getCategoryDao();		
		ArrayList<SectionedListItem> list = new ArrayList<SectionedListItem>();
		
		// Add section
		list.add(new SectionItem(getResources().getString(R.string.predefined_text) + " " +
				getResources().getString(R.string.categories_label)));
		
		// Load predefined categories first
		String[] arrayExpense = getResources().getStringArray(R.array.expense_categories);
		String[] arrayIncome = getResources().getStringArray(R.array.income_categories);
		for(String categoryName : arrayExpense) {
			list.add(new Category(categoryName, true, false)); // Add predefined expense categories
		}
		for(String categoryName : arrayIncome) {
			list.add(new Category(categoryName, false, false)); // Add predefined income categories
		}
		
		// Add section
		list.add(new SectionItem(getResources().getString(R.string.custom_text) + " " +
				getResources().getString(R.string.categories_label)));
		
		// Load custom categories from DB		
		list.addAll(categoryDao.queryBuilder().where() // Add custom expense categories from DB
				.eq(Category.IS_EXPENSE_FIELD_NAME, true)
				.query());
		list.addAll(categoryDao.queryBuilder().where() // Add custom income categories from DB
				.eq(Category.IS_EXPENSE_FIELD_NAME, false)
				.query());
		
		ListItemAdapter listItemAdapter = new ListItemAdapter(this, list);
		listView.setAdapter(listItemAdapter);
	}
}
