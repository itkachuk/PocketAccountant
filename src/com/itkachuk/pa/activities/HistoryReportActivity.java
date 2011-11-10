package com.itkachuk.pa.activities;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;


public class HistoryReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final Integer MAX_ITEMS_PER_PAGE = 20;
	
	private ListView listView;
	private AlertDialog.Builder builder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.history_report);
		builder = new AlertDialog.Builder(this);

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});

		listView = (ListView) findViewById(R.id.recordsHistoryList);
	
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				CreateNewRecordActivity.callMe(HistoryReportActivity.this, record.isExpense(), record.getId());
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
				final IncomeOrExpenseRecord record = (IncomeOrExpenseRecord) listView.getAdapter().getItem(i);
				
				//AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
				builder.setMessage(getResources().getString(R.string.history_report_delete_record_dialog))
				       .setCancelable(false)
				       .setPositiveButton(getResources().getString(R.string.yes_button_label), new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   try{
				        		   Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
				        		   recordDao.deleteById(record.getId());
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
		// TODO - implement paging!
		Log.d(TAG, "Show list of records");
		Dao<IncomeOrExpenseRecord, Integer> dao = getHelper().getRecordDao();
		QueryBuilder<IncomeOrExpenseRecord, Integer> builder = dao.queryBuilder();
		builder.orderBy(IncomeOrExpenseRecord.TIMESTAMP_FIELD_NAME, false).limit(HistoryReportActivity.MAX_ITEMS_PER_PAGE);
		List<IncomeOrExpenseRecord> list = dao.query(builder.prepare());
		ArrayAdapter<IncomeOrExpenseRecord> arrayAdapter = new RecordsAdapter(this, R.layout.record_row, list);
		listView.setAdapter(arrayAdapter);
	}
	
	private class RecordsAdapter extends ArrayAdapter<IncomeOrExpenseRecord> {

		public RecordsAdapter(Context context, int textViewResourceId, List<IncomeOrExpenseRecord> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.record_row, null);
			}
			IncomeOrExpenseRecord record = getItem(position);

			Long millis = record.getTimestamp();
			DateFormat dateFormatter = SimpleDateFormat.getDateTimeInstance();
			fillText(v, R.id.recordDate, dateFormatter.format(millis));
		
			fillText(v, R.id.recordCategory, record.getCategory());

			fillText(v, R.id.recordDescription, record.getDescription());
						
			String amount = Double.toString(record.getAmount());
			if (record.isExpense()) {
				fillText(v, R.id.recordAmount, "-" + amount);
				setTextColor(v, R.id.recordAmount, R.color.expense_amount_color);
			} else {
				fillText(v, R.id.recordAmount, amount);
				setTextColor(v, R.id.recordAmount, R.color.income_amount_color);
			}	
			return v;
		}

		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}
		
		private void setTextColor(View v, int id, int colorId) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setTextColor(getResources().getColor(colorId));
		}
	}
}
