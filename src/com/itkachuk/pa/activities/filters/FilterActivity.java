package com.itkachuk.pa.activities.filters;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.itkachuk.pa.R;
import com.itkachuk.pa.activities.editors.PreferencesEditorActivity;
import com.itkachuk.pa.activities.editors.RecordEditorActivity;
import com.itkachuk.pa.activities.menus.ReportsMenuActivity;
import com.itkachuk.pa.activities.reports.CommonReportActivity;
import com.itkachuk.pa.activities.reports.ConsolidatedReportActivity;
import com.itkachuk.pa.activities.reports.HistoryReportActivity;
import com.itkachuk.pa.entities.Account;
import com.itkachuk.pa.entities.Category;
import com.itkachuk.pa.entities.DatabaseHelper;
import com.itkachuk.pa.utils.ActivityUtils;
import com.itkachuk.pa.utils.DateUtils;
import com.itkachuk.pa.utils.PreferencesUtils;
import com.itkachuk.pa.utils.TimeRange;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.Dao;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class FilterActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	private static final String TAG = "PocketAccountant";
	
	private static final String EXTRAS_REPORT_NAME = "reportName";	
	
	private Spinner mRecordsFilterSpinner;
	private Spinner mAccountsFilterSpinner;
	private Spinner mCategoriesFilterSpinner;
	private Spinner mTimeFilterSpinner;
	private Button mStartDateButton;
	private Button mEndDateButton;
	private ImageButton mRollForwardButton;
	private ImageButton mRollBackwardButton;
	private Button mShowReportButton;
	
    private Calendar mStartDate;
    private Calendar mEndDate;
    
    private View.OnClickListener rollButtonsOnClickListener;

    static final int START_DATE_DIALOG_ID = 0;
    static final int END_DATE_DIALOG_ID = 1;
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mStartDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                	mStartDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
                	mStartDate.clear(Calendar.MILLISECOND);
                    updateDateButtonLabel(mStartDateButton, mStartDate.getTimeInMillis());
                }
            };
    private DatePickerDialog.OnDateSetListener mEndDateSetListener =
        new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
            	mEndDate.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
            	mEndDate.clear(Calendar.MILLISECOND);
                updateDateButtonLabel(mEndDateButton, mEndDate.getTimeInMillis());
            }
        };        
            
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_editor);
        
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();
        
        mRecordsFilterSpinner = (Spinner) findViewById(R.id.recordsFilterSpinner);
        mAccountsFilterSpinner = (Spinner) findViewById(R.id.accountsFilteringSpinner);
        mCategoriesFilterSpinner = (Spinner) findViewById(R.id.categoriesFilteringSpinner);
        mTimeFilterSpinner = (Spinner) findViewById(R.id.timeFilteringSpinner);
        mStartDateButton = (Button) findViewById(R.id.startDateButton);
        mEndDateButton = (Button) findViewById(R.id.endDateButton);
        mRollForwardButton = (ImageButton) findViewById(R.id.rollForwardButton);
        mRollBackwardButton = (ImageButton) findViewById(R.id.rollBackwardButton);
        mShowReportButton = (Button) findViewById(R.id.showReportButton);      

        setUIObjectsState();
        
        try {
	        refreshRecordsSpinnerEntries();
	        ActivityUtils.refreshAccountSpinnerEntries(this, getHelper().getAccountDao(), mAccountsFilterSpinner);
	        refreshCategoriesSpinnerEntries();
	        refreshTimeSpinnerEntries();
            setDefaultTimeRanges();
		} catch (SQLException e) {
			Log.e(TAG, "SQL Error in onCreate method. " + e.getMessage());
		}
        
		// add a click listener to the Date picker buttons
		mStartDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(START_DATE_DIALOG_ID);
            }
        });
		mEndDateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(END_DATE_DIALOG_ID);
            }
        });
		
		// Time Spinner Listener
		mTimeFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				//Log.d(TAG, "OnItemSelectedListener: i=" + i + ", l=" + l);
				TimeRange timeRange = null;
				
				switch(i){
				case 0: { // "All Time" time filter
					clearDateButtons();
					disableDateButtons();
					disableRollButtons();
					break;
				}
				case 1: { // "Custom" time filter
					updateDateButtons();
					enableDateButtons();
					disableRollButtons();
					break;
				}
				case 2: { // "Day" time filter
					timeRange = DateUtils.getTimeRange(DateUtils.DAY, false);
					updateCalendarsAndButtons(timeRange);
					enableRollButtons();
					break;
				}
				case 3: { // "Week" time filter
					timeRange = DateUtils.getTimeRange(DateUtils.WEEK, false);
					updateCalendarsAndButtons(timeRange);
					enableRollButtons();
					break;
				}
				case 4: { // "Month" time filter
					timeRange = DateUtils.getTimeRange(DateUtils.MONTH, false);
					updateCalendarsAndButtons(timeRange);
					enableRollButtons();
					break;
				}
				case 5: { // "Quarter" time filter
					timeRange = DateUtils.getTimeRange(DateUtils.QUARTER, false);
					updateCalendarsAndButtons(timeRange);
					enableRollButtons();
					break;
				}
				case 6: { // "Year" time filter
					timeRange = DateUtils.getTimeRange(DateUtils.YEAR, false);
					updateCalendarsAndButtons(timeRange);
					enableRollButtons();
					break;
				}
				case 7: { // "Past Day" time filter
					timeRange = DateUtils.getPastTimeRange(DateUtils.DAY);
					updateCalendarsAndButtons(timeRange);
					disableRollButtons();
					break;
				}
				case 8: { // "Past Week" time filter
					timeRange = DateUtils.getPastTimeRange(DateUtils.WEEK);
					updateCalendarsAndButtons(timeRange);
					disableRollButtons();
					break;
				}
				case 9: { // "Past Month" time filter
					timeRange = DateUtils.getPastTimeRange(DateUtils.MONTH);
					updateCalendarsAndButtons(timeRange);
					disableRollButtons();
					break;
				}
				case 10: { // "Past Quarter" time filter
					timeRange = DateUtils.getPastTimeRange(DateUtils.QUARTER);
					updateCalendarsAndButtons(timeRange);
					disableRollButtons();
					break;
				}
				case 11: { // "Past Year" time filter
					timeRange = DateUtils.getPastTimeRange(DateUtils.YEAR);
					updateCalendarsAndButtons(timeRange);
					disableRollButtons();
					break;
				}
				}
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				return;
			}
		});
		
		rollButtonsOnClickListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				switch((int)mTimeFilterSpinner.getSelectedItemId()){
				case 2: { // Day
					if (v.getId() == R.id.rollBackwardButton) {
						mStartDate.add(Calendar.DAY_OF_MONTH, -1);
						mEndDate.add(Calendar.DAY_OF_MONTH, -1);
					} else {
						mStartDate.add(Calendar.DAY_OF_MONTH, 1);
						mEndDate.add(Calendar.DAY_OF_MONTH, 1);
					}
					updateDateButtons();
					break;
				}
				case 3: { // Week
					if (v.getId() == R.id.rollBackwardButton) {
						mStartDate.add(Calendar.WEEK_OF_YEAR, -1);
						mEndDate.add(Calendar.WEEK_OF_YEAR, -1);
					} else {
						mStartDate.add(Calendar.WEEK_OF_YEAR, 1);
						mEndDate.add(Calendar.WEEK_OF_YEAR, 1);
					}
					updateDateButtons();
					break;
				}
				case 4: { // Month
					if (v.getId() == R.id.rollBackwardButton) {
						mStartDate.add(Calendar.MONTH, -1);
						mEndDate.add(Calendar.MONTH, -1);
					} else {
						mStartDate.add(Calendar.MONTH, 1);
						mEndDate.add(Calendar.MONTH, 1);
					}
					updateDateButtons();
					break;
				}
				case 5: { // Quarter
					if (v.getId() == R.id.rollBackwardButton) {
						mStartDate.add(Calendar.MONTH, -3);
						mEndDate.add(Calendar.MONTH, -3);
					} else {
						mStartDate.add(Calendar.MONTH, 3);
						mEndDate.add(Calendar.MONTH, 3);
					}
					updateDateButtons();
					break;
				}
				case 6: { // Year
					if (v.getId() == R.id.rollBackwardButton) {
						mStartDate.add(Calendar.YEAR, -1);
						mEndDate.add(Calendar.YEAR, -1);
					} else {
						mStartDate.add(Calendar.YEAR, 1);
						mEndDate.add(Calendar.YEAR, 1);
					}
					updateDateButtons();
					break;
				}
				}				
			}
		};
		
		// Roll Time Buttons Listeners
		mRollForwardButton.setOnClickListener(rollButtonsOnClickListener);		
		mRollBackwardButton.setOnClickListener(rollButtonsOnClickListener);	

		findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish(); // Close activity on Back button pressing
			}
		});
		
		mShowReportButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String reportName = getReportName();
				
				if (reportName != null) {
					int accountsFilter, categoriesFilter;
					String recordsToShowFilter = mRecordsFilterSpinner.getSelectedItem().toString();
					accountsFilter = ((Account) mAccountsFilterSpinner.getSelectedItem()).getId();
					categoriesFilter = ((Category) mCategoriesFilterSpinner.getSelectedItem()).getId();
										
					if (reportName.equals("History")) {										
						if (mTimeFilterSpinner.getSelectedItemId() > 0) { // If time filter was set - not "All Time" item selected
							if (mStartDate.compareTo(mEndDate) >= 0) { // Error, if dates are equal or start > end
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_time_filter_message), Toast.LENGTH_LONG).show();
								return;
							}
							HistoryReportActivity.callMe(FilterActivity.this, "", recordsToShowFilter, accountsFilter, 
									categoriesFilter, mStartDate.getTimeInMillis(), mEndDate.getTimeInMillis());
						} else {
							HistoryReportActivity.callMe(FilterActivity.this, "", recordsToShowFilter, accountsFilter, 
									categoriesFilter, DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
						}
						
					}
					
					if (reportName.equals("Common")) {
						CommonReportActivity.callMe(FilterActivity.this, "", accountsFilter);
					}
					
					if (reportName.equals("Consolidated")) {
						if (mTimeFilterSpinner.getSelectedItemId() > 0) { // If time filter was set - not "All Time" item selected
							if (mStartDate.compareTo(mEndDate) >= 0) { // Error, if dates are equal or start > end
								Toast.makeText(getApplicationContext(), getResources().getString(R.string.wrong_time_filter_message), Toast.LENGTH_LONG).show();
								return;
							}
							ConsolidatedReportActivity.callMe(FilterActivity.this, "", recordsToShowFilter, accountsFilter, 
									mStartDate.getTimeInMillis(), mEndDate.getTimeInMillis());
						} else {
							ConsolidatedReportActivity.callMe(FilterActivity.this, "", recordsToShowFilter, accountsFilter, 
									DateUtils.DEFAULT_START_DATE, DateUtils.DEFAULT_END_DATE);
						}
					}
				} else {
					// TODO - throw exception
				}
			}
		});       
    }

	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case START_DATE_DIALOG_ID: {
	        return new DatePickerDialog(this,
	                    mStartDateSetListener,
	                    mStartDate.get(Calendar.YEAR), 
	                    mStartDate.get(Calendar.MONTH), 
	                    mStartDate.get(Calendar.DAY_OF_MONTH));
	    }
		case END_DATE_DIALOG_ID: {
	        return new DatePickerDialog(this,
	                    mEndDateSetListener,
	                    mEndDate.get(Calendar.YEAR), 
	                    mEndDate.get(Calendar.MONTH), 
	                    mEndDate.get(Calendar.DAY_OF_MONTH));
	    }
	    }
	    return null;
	}
	
	private void updateDateButtonLabel(Button dateButton, long millis) {
		DateFormat dateFormatter = SimpleDateFormat.getDateInstance();
		dateButton.setText(dateFormatter.format(millis));
	}
	
	private void updateDateButtons() {		
		updateDateButtonLabel(mStartDateButton, mStartDate.getTimeInMillis());
		updateDateButtonLabel(mEndDateButton, mEndDate.getTimeInMillis());
	}
	
	private void clearDateButtons() {		
		mStartDateButton.setText("");
		mEndDateButton.setText("");
	}
	
	private void enableDateButtons() {		
		mStartDateButton.setEnabled(true);
		mEndDateButton.setEnabled(true);
	}
	
	private void disableRollButtons() {		
		mRollForwardButton.setEnabled(false);
		mRollBackwardButton.setEnabled(false);
	}
	
	private void enableRollButtons() {		
		mRollForwardButton.setEnabled(true);
		mRollBackwardButton.setEnabled(true);
	}
	
	private void disableDateButtons() {		
		mStartDateButton.setEnabled(false);
		mEndDateButton.setEnabled(false);
	}
	
	private void updateCalendarsAndButtons(TimeRange timeRange) {
		mStartDate.setTimeInMillis(timeRange.getStartTime());
		mEndDate.setTimeInMillis(timeRange.getEndTime());
		updateDateButtons();
		disableDateButtons();
	}

	private void setUIObjectsState() {
		String reportName = getReportName();
		if (reportName.equals("History")) {
			mRecordsFilterSpinner.setEnabled(true);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(true);
			mTimeFilterSpinner.setEnabled(true);			
		}
		if (reportName.equals("Common")) {
			mRecordsFilterSpinner.setEnabled(false);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(false);
		}
		if (reportName.equals("Consolidated")) {
			mRecordsFilterSpinner.setEnabled(true);
			mAccountsFilterSpinner.setEnabled(true);
			mCategoriesFilterSpinner.setEnabled(false);
			mTimeFilterSpinner.setEnabled(true);			
		}
		mStartDateButton.setEnabled(false);
		mEndDateButton.setEnabled(false);
	}

    /**
     * Update the time filter selection according to the report name
     * History Report - will show only records for past 30/31 days
     * Other reports - for now, will show all the data
     */
    private void setDefaultTimeRanges() {
        String reportName = getReportName();
        TimeRange timeRange;
        if (reportName.equals("History")) {
            timeRange = DateUtils.getPastTimeRange(DateUtils.MONTH);
            mTimeFilterSpinner.setSelection(9); // programmatically select "Past Month" spinner item
            updateCalendarsAndButtons(timeRange);
            disableRollButtons();
        }
        //if (reportName.equals("Consolidated")) {
            // ?
        //}
    }

	private void refreshRecordsSpinnerEntries() {
		String[] filterItemsList;		
		if (getReportName().equals("Consolidated")) {
			filterItemsList = new String[2];
			filterItemsList[0] = getResources().getString(R.string.expenses_text);
			filterItemsList[1] = getResources().getString(R.string.incomes_text);			
		} else {
			filterItemsList = new String[3];
			filterItemsList[0] = getResources().getString(R.string.all_text);
			filterItemsList[1] = getResources().getString(R.string.expenses_text);
			filterItemsList[2] = getResources().getString(R.string.incomes_text);
		}		
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, filterItemsList);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mRecordsFilterSpinner.setAdapter(adapter);
	}
	
	private void refreshCategoriesSpinnerEntries() throws SQLException {
		// TODO - separation for Income/Expense
		
		Dao<Category, Integer> categoryDao = getHelper().getCategoryDao();
		List<Category> categories = new ArrayList<Category>();
		categories.add(new Category(-1, getResources().getString(R.string.all_text), true)); // first entry should be "All" item (has index -1 for filters)
		
			categories.addAll(categoryDao.queryBuilder().where() // Add categories from DB
					.eq(Category.IS_EXPENSE_FIELD_NAME, true)
					.query());
	
			categories.addAll(categoryDao.queryBuilder().where()
					.eq(Category.IS_EXPENSE_FIELD_NAME, false)
					.query());
		
		ArrayAdapter<Category> adapter =
				new ArrayAdapter<Category>(this, android.R.layout.simple_spinner_item, categories);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCategoriesFilterSpinner.setAdapter(adapter);
	}
	
	private void refreshTimeSpinnerEntries() {
		String[] timeIntervals = getResources().getStringArray(R.array.time_ranges_list);		
		ArrayAdapter<String> adapter =
				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeIntervals);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTimeFilterSpinner.setAdapter(adapter);
	}
	
	public static void callMe(Context c, String reportName) {
		Intent intent = new Intent(c, FilterActivity.class);
		intent.putExtra(EXTRAS_REPORT_NAME, reportName);
		c.startActivity(intent);
	}
	
	private String getReportName() {
		return getIntent().getStringExtra(EXTRAS_REPORT_NAME);
	}
	
}
