package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.ChartUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class MonthlyBalanceReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private Calendar calendar;
	
	private Spinner mAccountsFilterSpinner;
	private TextView mYearTimeFilter;
	private ImageButton mRollYearForwardButton;
	private ImageButton mRollYearBackwardButton;
	private Button mShowReportButton;
	
	private Context context;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_report);
        context = this;
        calendar = Calendar.getInstance();
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
                   
        mAccountsFilterSpinner = (Spinner) findViewById(R.id.accountsFilteringSpinner);
        mYearTimeFilter = (TextView) findViewById(R.id.yearTimeFilter);
        mRollYearForwardButton = (ImageButton) findViewById(R.id.rollYearForwardButton);
        mRollYearBackwardButton = (ImageButton) findViewById(R.id.rollYearBackwardButton);
        mShowReportButton = (Button) findViewById(R.id.showReportButton);
        // Hide status bar, but keep title bar
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
               
        try {
        	Dao<Account, Integer> accountDao = getHelper().getAccountDao();
			ActivityUtils.refreshAccountSpinnerEntries(this, accountDao, mAccountsFilterSpinner);
		} catch (SQLException e) {
			e.printStackTrace();
		}
        updateYearText();      
        
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
        
        mRollYearForwardButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		calendar.add(Calendar.YEAR, 1);
        		updateYearText();
			}
        });
        
        mRollYearBackwardButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		calendar.add(Calendar.YEAR, -1);
        		updateYearText();
			}
        });
        
        mShowReportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				new RunBarChartReportJob(context).execute();
			}
        });
	}
	
	public static void callMe(Context c) {
		Intent intent = new Intent(c, MonthlyBalanceReportActivity.class);
		c.startActivity(intent);
	}
	
	private void updateYearText() {
		mYearTimeFilter.setText(Integer.toString(calendar.get(Calendar.YEAR)));
	}
	
	private Intent getBarChartIntent(Context context) throws SQLException {
		// Set bars labels: "Incomes", "Expenses", "Balance"		
		String[] titles = new String[] { 
				getResources().getString(R.string.incomes_text), 
				getResources().getString(R.string.expenses_text), 
				getResources().getString(R.string.balance_text) };
		
		int accountFilter = -1;
		String  accountCurrency = null;
		if (mAccountsFilterSpinner.getSelectedItem() != null) {
			Account account = (Account) mAccountsFilterSpinner.getSelectedItem();
			if (account != null) {
				accountFilter = account.getId();
				accountCurrency = account.getCurrency();
			}
		}
		
		List<double[]> values = calculateChartValues(accountFilter, calendar.get(Calendar.YEAR));
		double minValue = values.get(3)[0]; // "values.get(3)[0]" - getting the minValue from values arrays
		double maxValue = values.get(3)[1] + values.get(3)[1] * 0.1f; // "values.get(3)[1]" - getting the maxValue from values arrays
		
		int[] colors = new int[] { 
				context.getResources().getColor(R.color.income_amount_color),
				context.getResources().getColor(R.color.expense_amount_color),
				context.getResources().getColor(R.color.light_yellow_color)};	
		XYMultipleSeriesRenderer renderer = ChartUtils.buildBarRenderer(colors);
		
		String balanceForYearText = getResources().getString(R.string.balance_for_year_text);
		String amountText = getResources().getString(R.string.amount_text);
		if (accountCurrency != null) {
			amountText = amountText + ", " + accountCurrency;
		}		
		ChartUtils.setChartSettings(renderer, balanceForYearText + " " + calendar.get(Calendar.YEAR), 
				getResources().getString(R.string.month_text), amountText, 
				0.5, 12.5, minValue, maxValue, Color.GRAY, Color.LTGRAY); 
		renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
		renderer.getSeriesRendererAt(2).setDisplayChartValues(true);
		
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(true, true);
		renderer.setMargins(new int[]{30, 25, 20, 10});
		renderer.setZoomButtonsVisible(false);
		renderer.setZoomEnabled(true);
		renderer.setZoomRate(1.0f);
		renderer.setBarSpacing(0.3f);
		return ChartFactory.getBarChartIntent(context, ChartUtils.buildBarDataset(titles, values), renderer,
				Type.DEFAULT);
	}
	
	private List<double[]> calculateChartValues(int accountFilter, int year) throws SQLException {
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		List<double[]> values = new ArrayList<double[]>();
		double[] incomes = new double[12]; 
		double[] expenses = new double[12];
		double[] balances = new double[12];
		double[] minMaxValues = new double[2];
		minMaxValues[0] = 0; // min
		minMaxValues[1] = 0; // max
		double income, expense, balance;
		
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		TimeRange timeRange = new TimeRange(DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
		
		
		for (int month = 0; month < 12; month++) {
			timeRange.setStartTime(calendar.getTimeInMillis());
			//Log.d(TAG, "StartDate: " + DateFormat.format("dd.MM.yy hh:mm:ss", calendar) + "(" + month + ")");
			calendar.add(Calendar.MONTH, 1);
			timeRange.setEndTime(calendar.getTimeInMillis());
			//Log.d(TAG, "EndDate: " + DateFormat.format("dd.MM.yy hh:mm:ss", calendar) + "(" + month + ")");
			
			income = toDoubleAndRound(CalcUtils.getSumOfRecords(recordDao, accountFilter, false, timeRange));
			expense = toDoubleAndRound(CalcUtils.getSumOfRecords(recordDao, accountFilter, true, timeRange));
			balance = income - expense;
			
			incomes[month] = income;
			expenses[month] = expense;
			balances[month] = balance;
			
			// identify the min and max values for BarChart graph (Y-axis)
			if (balance < 0) minMaxValues[0] = balance; // min			
			if (income > minMaxValues[1]) minMaxValues[1] = income;
			if (expense > minMaxValues[1]) minMaxValues[1] = expense;
		}
		
		values.add(incomes);
		values.add(expenses);
		values.add(balances);
		values.add(minMaxValues);
		
		return values;
	}
	
	private double toDoubleAndRound(String value) {
		return Math.round(Double.valueOf(value));
	}
	
	private class RunBarChartReportJob extends AsyncTask<Void,Void,Intent> {
		private ProgressDialog progressDialog;
				
		public RunBarChartReportJob(Context context) {
			super();
			progressDialog = new ProgressDialog(context);
			progressDialog.setTitle("");
			progressDialog.setMessage(context.getString(R.string.data_calculation_text));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
		}

		@Override
		protected void onPreExecute() {
			//Log.d(TAG, "RunBarChartReportJob: called onPreExecute");	
			if (progressDialog != null && !progressDialog.isShowing()) {
				progressDialog.show();
			}
		};
		
		@Override
		protected Intent doInBackground(Void... arg0) {
			//Log.d(TAG, "RunBarChartReportJob: called doInBackground");				
			Intent barChartIntent = null;
			
			try {
				barChartIntent = getBarChartIntent(context);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			return barChartIntent;
		}
		
		@Override
        protected void onPostExecute(Intent barChartIntent) {
			//Log.d(TAG, "RunBarChartReportJob: called onPostExecute");	                					
    		if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    		startActivity(barChartIntent);
        }
	}
}
