package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.filters.FilterActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.ChartUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class ConsolidatedReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_CALLER = "caller";
	private static final String EXTRAS_RECORDS_TO_SHOW_FILTER = "recordsToShowFilter";
	private static final String EXTRAS_ACCOUNTS_FILTER = "accountsFilter";
	private static final String EXTRAS_START_DATE_FILTER = "startDateFilter";
	private static final String EXTRAS_END_DATE_FILTER = "endDateFilter";
	
	private ListView mListView;
	private ImageButton mFilterButton;
	private ImageButton mChangeViewButton;
	
	private int reportViewsCounter = 0; // 0 - amounts, 1 - percentages, 2 - bars.
	private static final int REPORT_VIEWS_QTY = 2;
	
	// Filters, passed via extras
	private boolean mRecordsToShowFilter;
	private int mAccountsFilter;
	private long mStartDateFilter;
	private long mEndDateFilter;
	
	// Chart components
//	private LinearLayout mBarChartLayout;
//	private GraphicalView mChartView;
	private XYMultipleSeriesDataset mDataset;// = new XYMultipleSeriesDataset();
	private XYMultipleSeriesRenderer mRenderer;// = new XYMultipleSeriesRenderer();
	
	private XYSeries mCurrentSeries;	
	private XYSeriesRenderer mCurrentRenderer;
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.consolidated_report);
	//	mBarChartLayout = (LinearLayout) findViewById(R.id.consolidatedBarChart);
		mFilterButton = (ImageButton) findViewById(R.id.filterButton);
		// Hide status bar, but keep title bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		parseFilters();
		ActivityUtils.updateReportTitleBar(this, getHelper(), getAccountsFilter());
		
		//initChartRenderer();

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		// Check calling activity, enable filter button, only if we came from reports menu activity
		if (getCallingActivityName().equals(ReportsMenuActivity.class.getName())) {
			mFilterButton.setEnabled(true);
		} else {
			mFilterButton.setEnabled(false);
		}
		
		mFilterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				FilterActivity.callMe(ConsolidatedReportActivity.this, "Consolidated");
				finish();
			}
		});

		mListView = (ListView) findViewById(R.id.categoriesAmountsList);		
		mChangeViewButton = (ImageButton) findViewById(R.id.changeViewButton);
		
		mChangeViewButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				reportViewsCounter++;
				if (reportViewsCounter >= REPORT_VIEWS_QTY)
					reportViewsCounter = 0;
				try {
					fillList();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		});  
		
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				String[] categoryAmountRow = (String[]) mListView.getAdapter().getItem(i);
				HistoryReportActivity.callMe(ConsolidatedReportActivity.this, "", getRecordsToShowFilter(), 
						mAccountsFilter, Integer.parseInt(categoryAmountRow[0]), mStartDateFilter, mEndDateFilter);
			}
		});
	}
	
	/*
	private void initChartRenderer() {
		mRenderer = ChartUtils.buildBarRenderer(new int[] { Color.CYAN });
		mRenderer.setOrientation(Orientation.VERTICAL);
				
	}*/

	
	@Override
	protected void onResume() {
		super.onResume();
		try {
			fillList();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void callMe(Context c, String caller, String recordsToShowFilter, int accountsFilter,
			long startDateFilter, long endDateFilter) {
		Intent intent = new Intent(c, ConsolidatedReportActivity.class);
		intent.putExtra(EXTRAS_CALLER, caller);
		intent.putExtra(EXTRAS_RECORDS_TO_SHOW_FILTER, recordsToShowFilter);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_START_DATE_FILTER, startDateFilter);
		intent.putExtra(EXTRAS_END_DATE_FILTER, endDateFilter);
		c.startActivity(intent);
	}
	

	private void fillList() throws SQLException {
		// TODO - put to async task
		Log.d(TAG, "Show list of aggregated amounts per category");
		Dao<IncomeOrExpenseRecord, Integer> dao = getHelper().getRecordDao();
		TimeRange timeRange = new TimeRange(mStartDateFilter, mEndDateFilter);
		
		List<String[]> list = CalcUtils.getAmountsPerCategoryList(dao, mAccountsFilter, 
				mRecordsToShowFilter, timeRange);
		retreiveCategoryNamesByIds(list);
		roundAmountsOfCategoriesList(list);
		
		if (reportViewsCounter == 2) { // TODO Bars displaying mode - not working now! To be completed later
//			String sumOfRecords = CalcUtils.getSumOfRecords(dao, mAccountsFilter, mRecordsToShowFilter, timeRange);
//			prepareRendererAndDataSet(list, sumOfRecords);
//			if (mChartView == null) {
//				mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
//				mBarChartLayout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));    				     
//			} else {
//				mChartView.repaint();
//			}
//			mListView.setEnabled(false);
//			mBarChartLayout.setEnabled(true);
		} else {
			if (reportViewsCounter == 1) { // Percentages displaying mode
		
				String sumOfRecords = CalcUtils.getSumOfRecords(dao, mAccountsFilter, mRecordsToShowFilter, timeRange);
				addPercentValuesToCategoriesList(list, sumOfRecords);
			}
		
			ArrayAdapter<String[]> arrayAdapter = new AmountsPerCategoryAdapter(this, R.layout.category_amount_row, list);
			mListView.setAdapter(arrayAdapter);
//			mListView.setEnabled(true);
//			mBarChartLayout.setEnabled(false);
		}
	}
	
	/* Not working, to be completed later */
/*	private void prepareRendererAndDataSet(List<String[]> list, String totalAmount) {
		float totalSum = Float.valueOf(totalAmount);
		
		ChartUtils.setChartSettings(mRenderer, "Incomes/Expenses per category", "Amount", "Categories", 
				0, 25, 0, totalSum, Color.GRAY, Color.LTGRAY);
		
		mRenderer.setXLabels(1);
		mRenderer.setYLabels(list.size());
		
		String[] titles = new String[] { "title?" };
		List<double[]> arrayListOfValues = new ArrayList<double[]>();
		double[] values = new double[list.size()];
		int i = 0;
		// translate array of strings to array of numbers, and in parallel add Y-axis labels (Categories)
		for (String[] row : list) {
			double amountNumber = Double.valueOf(row[1]);
			values[i] = amountNumber;
			mRenderer.addXTextLabel(i+1, row[0]);
			i++;
		}
		arrayListOfValues.add(values);

		mRenderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		
		mDataset = ChartUtils.buildBarDataset(titles, arrayListOfValues);
	}*/

	private void addPercentValuesToCategoriesList(List<String[]> list, String totalAmount) {
		float categoryPercent;
		float totalSum = Float.valueOf(totalAmount);
		for (String[] row : list) {
			categoryPercent = (Float.valueOf(row[1])/totalSum) * 100;
			row[2] = Float.toString(Math.round(categoryPercent * 10)/10f); // third column of the row is dedicated for percents
		}
	}
	
	private void roundAmountsOfCategoriesList(List<String[]> list) {
		double categoryAmount;
		for (String[] row : list) {
			categoryAmount = Double.valueOf(row[1]);
			//row[1] = Double.toString(Math.round(categoryAmount * 100)/100f); // trim amount for two places after decimal point
			row[1] = Long.toString(Math.round(categoryAmount)); // trim rational part
		}
	}
	
	private void retreiveCategoryNamesByIds(List<String[]> list) {
		Category category;
		try {
			Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
			for (String[] row : list) {
				category = categoryDao.queryForId(Integer.parseInt(row[0]));
				row[3] = category.getName();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}					
	}

	private String getCallingActivityName() {		
		return getIntent().getStringExtra(EXTRAS_CALLER);
	}
	
	private String getRecordsToShowFilter() {		
		return getIntent().getStringExtra(EXTRAS_RECORDS_TO_SHOW_FILTER);
	}
	
	private int getAccountsFilter() {		
		return getIntent().getIntExtra(EXTRAS_ACCOUNTS_FILTER, -1);
	}
	
	private long getStartDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_START_DATE_FILTER, DateUtils.DEFAULT_START_DATE);
	}
	
	private long getEndDateFilter() {		
		return getIntent().getLongExtra(EXTRAS_END_DATE_FILTER, DateUtils.DEFAULT_END_DATE);
	}
	
	private void parseFilters() {
		if (getRecordsToShowFilter().equals(getResources().getString(R.string.expenses_text)))
			mRecordsToShowFilter = true;
		else mRecordsToShowFilter = false;
		mAccountsFilter = getAccountsFilter();		
		mStartDateFilter = getStartDateFilter();
		mEndDateFilter = getEndDateFilter();
	}
	
	private class AmountsPerCategoryAdapter extends ArrayAdapter<String[]> {

		public AmountsPerCategoryAdapter(Context context, int textViewResourceId, List<String[]> items) {
			super(context, textViewResourceId, items);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.category_amount_row, null);
			}
			String[] row = getItem(position);
		
			fillText(v, R.id.categoryName, row[3]);

			if (reportViewsCounter == 0) { // Amounts displaying mode
				fillText(v, R.id.categoryAmount, row[1]);
			}			
			if (reportViewsCounter == 1) { // Percentages displaying mode
				fillText(v, R.id.categoryAmount, row[2] + " %");
			}
	
			return v;
		}

		private void fillText(View v, int id, String text) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setText(text == null ? "" : text);
		}
		
/*		private void setTextColor(View v, int id, int colorId) {
			TextView textView = (TextView) v.findViewById(id);
			textView.setTextColor(getResources().getColor(colorId));
		}*/
	}
}
