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
import com.itkachuk.pa.activities.editors.AccountEditorActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.sectionedList.ListItemAdapter;
import com.itkachuk.pa.sectionedList.SectionItem;
import com.itkachuk.pa.sectionedList.SectionedListItem;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class AccountsManagementActivity extends OrmLiteBaseActivity<DatabaseHelper> {
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
		addButton.setText(getResources().getString(R.string.add_account_button_label));
		addButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				AccountEditorActivity.callMe(AccountsManagementActivity.this);
			}
		});

		listView = (ListView) findViewById(R.id.itemsList);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Account account = (Account) listView.getAdapter().getItem(i);
				AccountEditorActivity.callMe(AccountsManagementActivity.this, account.getId());
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Account account = (Account) listView.getAdapter().getItem(i);

				builder.setMessage(getResources().getString(R.string.account_delete_dialog))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try{
				        		   Dao<Account, Integer> accountDao = getHelper().getAccountDao();
				        		   accountDao.deleteById(account.getId());
				        		   // TODO - implement all relevant records removing !!!
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
		Log.d(TAG, "Show list of accounts");
		ArrayList<SectionedListItem> list = new ArrayList<SectionedListItem>();
		// Add section
		list.add(new SectionItem(getResources().getString(R.string.accounts_label)));
		// Load all accounts from DB
		Dao<Account, Integer> accountDao = getHelper().getAccountDao();
		list.addAll(accountDao.queryForAll());
		ListItemAdapter listItemAdapter = new ListItemAdapter(this, list);
		listView.setAdapter(listItemAdapter);
	}
	
	// Old variant of accounts list
	/*
	private void fillList() throws SQLException {
		Log.d(TAG, "Show list of accounts");
		ArrayList<SectionedListItem> list = new ArrayList<SectionedListItem>();
		// Add section
		list.add(new SectionItem(getResources().getString(R.string.predefined_text) + " " +
				getResources().getString(R.string.accounts_label)));
		// Load predefined Main account first
		list.add(new Account(
				getResources().getString(R.string.main_account_name),
				PreferencesUtils.getMainAccountCurrency(this), // get currency from Program Preferences
				getResources().getString(R.string.main_account_description),
				false));
		// Add section
		list.add(new SectionItem(getResources().getString(R.string.custom_text) + " " +
				getResources().getString(R.string.accounts_label)));
		// Load custom accounts from DB
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		list.addAll(accountDao.queryForAll());
		ListItemAdapter listItemAdapter = new ListItemAdapter(this, list);
		listView.setAdapter(listItemAdapter);
	}*/

	// Not used any more
	/* 
	private class AcountsAdapter extends ArrayAdapter<Account> {

		public AcountsAdapter(Context context, int textViewResourceId, List<Account> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.account_row, null);
			}
			Account account = getItem(position);
			fillText(v, R.id.accountName, account.getName());
			fillText(v, R.id.accountCurrency, account.getCurrency());
			fillText(v, R.id.accountDescription, account.getDescription());						
			return v;
		}
		
		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}		
	}	*/
}
