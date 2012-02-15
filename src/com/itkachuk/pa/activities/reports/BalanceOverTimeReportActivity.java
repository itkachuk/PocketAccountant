package com.itkachuk.pa.activities.reports;

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
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.utils.ChartUtils;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class BalanceOverTimeReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";

	private static final String EXTRAS_ACCOUNTS_FILTER = null;
	private static final String EXTRAS_YEAR_TIME_FILTER = null;
	
	private Spinner mAccountsFilterSpinner;
	private TextView mYearTimeFilter;
	private ImageButton mRollYearForwardButton;
	private ImageButton mRollYearBackwardButton;
	private Button mShowReportButton;
	
	private Intent mBarChartIntent;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.balance_report);
                   
        mAccountsFilterSpinner = (Spinner) findViewById(R.id.accountsFilteringSpinner);
        mYearTimeFilter = (TextView) findViewById(R.id.yearTimeFilter);
        mRollYearForwardButton = (ImageButton) findViewById(R.id.rollYearForwardButton);
        mRollYearBackwardButton = (ImageButton) findViewById(R.id.rollYearBackwardButton);
        mShowReportButton = (Button) findViewById(R.id.showReportButton);
        // Hide status bar, but keep title bar
		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
		mBarChartIntent = getBarChartIntent(this);
        
        // temp
        mYearTimeFilter.setText(getYearTimeFilter());
        
        findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
        
        mShowReportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				startActivity(mBarChartIntent);
			}
        });
	}
	
	
	public static void callMe(Context c, String accountsFilter, String yearTimeFilter) {
		Intent intent = new Intent(c, BalanceOverTimeReportActivity.class);
		intent.putExtra(EXTRAS_ACCOUNTS_FILTER, accountsFilter);
		intent.putExtra(EXTRAS_YEAR_TIME_FILTER, yearTimeFilter);
		c.startActivity(intent);
	}
	
	private String getAccountsFilter() {		
		return getIntent().getStringExtra(EXTRAS_ACCOUNTS_FILTER);
	}
	
	private String getYearTimeFilter() {		
		return getIntent().getStringExtra(EXTRAS_YEAR_TIME_FILTER);
	}
	
	private Intent getBarChartIntent(Context context) {
		String[] titles = new String[] { "Incomes", "Expenses" };
		List<double[]> values = new ArrayList<double[]>();
		values.add(new double[] { 14230, 12300, 14240, 15244, 15900, 19200, 22030, 21200, 19500, 15500,
				12600, 14000 });
		values.add(new double[] { 5230, 7300, 9240, 10540, 7900, 9200, 12030, 11200, 9500, 10500,
				11600, 13500 });
		int[] colors = new int[] { Color.BLUE, Color.CYAN };
		XYMultipleSeriesRenderer renderer = ChartUtils.buildBarRenderer(colors);
		ChartUtils.setChartSettings(renderer, "Monthly balance during " + getYearTimeFilter() + " year", "Month", "Amount", 
				0.5, 12.5, 0, 24000, Color.GRAY, Color.LTGRAY);
		renderer.getSeriesRendererAt(0).setDisplayChartValues(true);
		renderer.getSeriesRendererAt(1).setDisplayChartValues(true);
		renderer.setXLabels(12);
		renderer.setYLabels(10);
		renderer.setXLabelsAlign(Align.LEFT);
		renderer.setYLabelsAlign(Align.LEFT);
		renderer.setPanEnabled(true, false);
		renderer.setBackgroundColor(context.getResources().getColor(R.color.background));
		renderer.setApplyBackgroundColor(true);
		// renderer.setZoomEnabled(false);
		renderer.setZoomRate(1.1f);
		renderer.setBarSpacing(0.5f);
		return ChartFactory.getBarChartIntent(context, ChartUtils.buildBarDataset(titles, values), renderer,
				Type.STACKED);
	}
}
