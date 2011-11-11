package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.j256.ormlite.dao.Dao;

public class AccountsManagementActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private ListView listView;
	private AlertDialog.Builder builder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.accounts_list);
		builder = new AlertDialog.Builder(this);

//		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
//				finish(); // Close activity on Back button pressing
//			}
//		});

		listView = (ListView) findViewById(R.id.accountsList);
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				// TODO - implement account editing wizard
				//IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				//CreateNewRecordActivity.callMe(HistoryReportActivity.this, record.isExpense(), record.getId());
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final Account account = (Account) listView.getAdapter().getItem(i);
				if (!account.isRemovable()) return true;

				builder.setMessage(getResources().getString(R.string.account_delete_dialog))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try{
				        		   Dao<Account, String> accountDao = getHelper().getAccountDao();
				        		   accountDao.deleteById(account.getName());
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
		List<Account> list = new ArrayList<Account>();
		// Load predefined Main account first
		list.add(new Account(
				getResources().getString(R.string.main_account_name),
				getResources().getString(R.string.main_account_description),
				false));
		// Load custom accounts from DB
		Dao<Account, String> accountDao = getHelper().getAccountDao();
		list.addAll(accountDao.queryForAll());
		ArrayAdapter<Account> arrayAdapter = new AcountsAdapter(this, R.layout.account_row, list);
		listView.setAdapter(arrayAdapter);
	}
	
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
			fillText(v, R.id.accountDescription, account.getDescription());						
			return v;
		}
		
		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}		
	}	
}
