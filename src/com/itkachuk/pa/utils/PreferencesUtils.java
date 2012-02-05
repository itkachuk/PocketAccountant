package com.itkachuk.pa.utils;

import java.util.Currency;
import java.util.Locale;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class PreferencesUtils {
	private static final String TAG = "PocketAccountant";
	
	// Shared Preferences keys
	public static final String PREFS_NAME = "paPreferences";
	public static final String PREFS_IS_INITIALIZED = "isInitialized";
	public static final String PREFS_DEFAULT_ACCOUNT = "defaultAccount";
	public static final String PREFS_MAIN_ACCOUNT_CURRENCY = "mainAccountCurrency";
	public static final String PREFS_ROWS_PER_PAGE = "rowsPerPage";
	
	public static SharedPreferences getSharedPreferences(Context context) {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	public static String getDefaultAccountName(Context context) {
		SharedPreferences programSettings = getSharedPreferences(context);
		return programSettings.getString(PREFS_DEFAULT_ACCOUNT, 
				context.getResources().getString(R.string.main_account_name));
	}
	
	public static String getMainAccountCurrency(Context context) {
		SharedPreferences programSettings = getSharedPreferences(context);
		return programSettings.getString(PREFS_MAIN_ACCOUNT_CURRENCY, 
				Currency.getInstance(Locale.getDefault()).getCurrencyCode());
	}
	
	public static int getRowsPerPage(Context context) {
		SharedPreferences programSettings = getSharedPreferences(context);
		return programSettings.getInt(PREFS_ROWS_PER_PAGE, 
				HistoryReportActivity.DEFAULT_ROWS_PER_PAGE_NUMBER);
	}
	
	public static void setDefaultAccountName(Context context, String accountName) {
		SharedPreferences programSettings = getSharedPreferences(context);
		SharedPreferences.Editor editor = programSettings.edit();
			
		editor.putString(PREFS_DEFAULT_ACCOUNT, accountName);						
		editor.commit();
	}
	
	public static void setMainAccountCurrency(Context context, String currencyCode) {
		SharedPreferences programSettings = getSharedPreferences(context);
		SharedPreferences.Editor editor = programSettings.edit();
							
		editor.putString(PREFS_MAIN_ACCOUNT_CURRENCY, currencyCode);
		editor.commit();
	}
	
	public static void setRowsPerPage(Context context, int rowsPerPageNumber) {
		SharedPreferences programSettings = getSharedPreferences(context);
		SharedPreferences.Editor editor = programSettings.edit();
							
		editor.putInt(PREFS_ROWS_PER_PAGE, rowsPerPageNumber);
		editor.commit();
	}

	public static void initializePreferences(Context context) {
		SharedPreferences programSettings = getSharedPreferences(context);
        if (!programSettings.contains(PREFS_IS_INITIALIZED)) {
        	SharedPreferences.Editor editor = programSettings.edit();
        	// Set default preferences
        	editor.putString(
        			PREFS_DEFAULT_ACCOUNT, 
        			context.getResources().getString(R.string.main_account_name));
        	
        	editor.putString(
        			PREFS_MAIN_ACCOUNT_CURRENCY, 
        			Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        	Log.d(TAG, "Current locale: " + Locale.getDefault());
        	Log.d(TAG, "Current currency (saved to preferences): " + Currency.getInstance(Locale.getDefault()).getCurrencyCode());
        	
        	editor.putInt(
        			PREFS_ROWS_PER_PAGE, 
        			HistoryReportActivity.DEFAULT_ROWS_PER_PAGE_NUMBER);
        	
        	editor.putBoolean(PREFS_IS_INITIALIZED, true);
        	editor.commit();
        	Log.d(TAG, "Program Preferences initialized successfully.");
        }		
	}
}
