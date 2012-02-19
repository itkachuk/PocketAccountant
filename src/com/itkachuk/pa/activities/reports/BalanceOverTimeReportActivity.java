package com.itkachuk.pa.activities.reports;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.entities.IncomeOrExpenseRecord;
import com.itkachuk.pa.utils.CalcUtils;
import com.itkachuk.pa.utils.ChartUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.itkachuk.pa.utils.SpinnerUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

public class BalanceOverTimeReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_ACCOUNTS_FILTER = null;

	private Calendar calendar;
	
	private Spinner mAccountsFilterSpinner;
	private TextView mYearTimeFilter;
	private ImageButton mRollYearForwardButton;
	private ImageButton mRollYearBackwardButton;
	private Button mShowReportButton;
	
	private Context context;
	private Intent mBarChartIntent;
	
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
        	Dao<Account, String> accountDao = getHelper().getAccountDao();
			SpinnerUtils.refreshAccountSpinnerEntries(this, accountDao, mAccountsFilterSpinner);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
				// TODO - put to async task
				try {
					mBarChartIntent = getBarChartIntent(context);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(mBarChartIntent);
			}
        });
	}
	
	
	public static void callMe(Context c, String accountsFilter) {
		Intent intent = new Intent(c, BalanceOverTimeReportActivity.class);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		c.startActivity(intent);
	}
	
	private String getAccountsFilter() {		
		return getIntent().getStringExtra(EXTRAS_ACCOUNTS_FILTER);
	}
	
	private void updateYearText() {
		mYearTimeFilter.setText(Integer.toString(calendar.get(Calendar.YEAR)));
	}
	
	private Intent getBarChartIntent(Context context) throws SQLException {
				
		String[] titles = new String[] { "Incomes", "Expenses", "Balance" };
		
		String accountFilter = null;
		if (mAccountsFilterSpinner.getSelectedItem() != null) {
			Account account = (Account) mAccountsFilterSpinner.getSelectedItem();
			if (account != null) {
				accountFilter = account.getName();
			} else {
				accountFilter = PreferencesUtils.getDefaultAccountName(context);
			}
		}
		
		List<double[]> values = calculateChartValues(accountFilter, calendar.get(Calendar.YEAR));
		double maxValue = values.get(3)[0] + values.get(3)[0] * 0.1f; // "values.get(3)[0]" - getting the maxValue from values arrays
		int[] colors = new int[] { 
				context.getResources().getColor(R.color.income_amount_color),
				context.getResources().getColor(R.color.expense_amount_color),
				context.getResources().getColor(R.color.light_yellow_color)};
		
		XYMultipleSeriesRenderer renderer = ChartUtils.buildBarRenderer(colors);
		ChartUtils.setChartSettings(renderer, "Monthly balance during " + calendar.get(Calendar.YEAR) + " year", "Month", "Amount", 
				0.5, 12.5, 0, maxValue, Color.GRAY, Color.LTGRAY); 
		renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
		renderer.getSeriesRendererAt(2).setDisplayChartValues(true);
		
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(true, false);
		//renderer.setBackgroundColor(context.getResources().getColor(R.color.background));
		//renderer.setApplyBackgroundColor(true);
		renderer.setZoomButtonsVisible(true);
		renderer.setZoomEnabled(true);
		renderer.setZoomRate(1.0f);
		renderer.setBarSpacing(0.5f);
		return ChartFactory.getBarChartIntent(context, ChartUtils.buildBarDataset(titles, values), renderer,
				Type.STACKED);
	}
	
	private List<double[]> calculateChartValues(String accountFilter, int year) throws SQLException {
		Dao<IncomeOrExpenseRecord, Integer> recordDao = getHelper().getRecordDao();
		List<double[]> values = new ArrayList<double[]>();
		double[] incomes = new double[12]; 
		double[] expenses = new double[12];
		double[] balances = new double[12];
		double[] maxValue = new double[1];
		maxValue[0] = 0;
		double income, expense, balance;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, 0);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.clear(Calendar.HOUR_OF_DAY);
		calendar.clear(Calendar.MINUTE);
		calendar.clear(Calendar.SECOND);
		TimeRange timeRange = new TimeRange(DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
		
		for (int month = 0; month < 12; month++) {
			timeRange.setStartTime(calendar.getTimeInMillis());
			calendar.add(Calendar.MONTH, 1);
			timeRange.setEndTime(calendar.getTimeInMillis());
			
			income = toDoubleAndRound(CalcUtils.getSumOfRecords(recordDao, accountFilter, false, timeRange));
			expense = toDoubleAndRound(CalcUtils.getSumOfRecords(recordDao, accountFilter, true, timeRange));
			balance = income - expense;
			
			incomes[month] = income;
			expenses[month] = expense;
			balances[month] = balance;
			
			// identify the max value for BarChart graph (Y-axis)
			if (income > maxValue[0]) maxValue[0] = income;
			if (expense > maxValue[0]) maxValue[0] = expense;
		}
		
		values.add(incomes);
		values.add(expenses);
		values.add(balances);
		values.add(maxValue);
		
		return values;
	}
	
	private double toDoubleAndRound(String value) {
		return Math.round(Double.valueOf(value));
	}
}
