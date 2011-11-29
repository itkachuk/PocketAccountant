package com.itkachuk.pa.activities.reports;

import android.widget.TextView;

import com.itkachuk.pa.entities.DatabaseHelper;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

public class CommonReportActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private TextView currentMonthIncome;
	private TextView currentMonthExpense;
}
