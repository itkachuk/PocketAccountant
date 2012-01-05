package com.itkachuk.pa.activities.filters;

import com.itkachuk.pa.R;
import com.itkachuk.pa.utils.TimeRange;


public class FilterSingleton {
	private static FilterSingleton _instance = new FilterSingleton();
	
	private String recordsFilter;
	private boolean isRecordsFilterEnabled;
	private String accountsFilter;
	private boolean isAccountsFilterEnabled;
	private String categoriesFilter;
	private boolean isCategoriesFilterEnabled;
	private String timeFilter;
	private boolean isTimeFilterEnabled;
	private TimeRange timeRange;
	
	private FilterSingleton()
    {
		isRecordsFilterEnabled = false;
		isAccountsFilterEnabled = false;
		isCategoriesFilterEnabled = false;
		isTimeFilterEnabled = false;
		//String[] array = getResources().getStringArray(R.array.expense_categories);
		recordsFilter = "All";
		timeRange = new TimeRange(0, Long.MAX_VALUE);
    }

    public static FilterSingleton getInstance()
    {
    	return _instance;
    }

	public String getRecordsFilter() {
		return recordsFilter;
	}

	public void setRecordsFilter(String recordsFilter) {
		this.recordsFilter = recordsFilter;
	}

	public boolean isRecordsFilterEnabled() {
		return isRecordsFilterEnabled;
	}

	public void setRecordsFilterEnabled(boolean isRecordsFilterEnabled) {
		this.isRecordsFilterEnabled = isRecordsFilterEnabled;
	}

	public String getAccountsFilter() {
		return accountsFilter;
	}

	public void setAccountsFilter(String accountsFilter) {
		this.accountsFilter = accountsFilter;
	}

	public boolean isAccountsFilterEnabled() {
		return isAccountsFilterEnabled;
	}

	public void setAccountsFilterEnabled(boolean isAccountsFilterEnabled) {
		this.isAccountsFilterEnabled = isAccountsFilterEnabled;
	}

	public String getCategoriesFilter() {
		return categoriesFilter;
	}

	public void setCategoriesFilter(String categoriesFilter) {
		this.categoriesFilter = categoriesFilter;
	}

	public boolean isCategoriesFilterEnabled() {
		return isCategoriesFilterEnabled;
	}

	public void setCategoriesFilterEnabled(boolean isCategoriesFilterEnabled) {
		this.isCategoriesFilterEnabled = isCategoriesFilterEnabled;
	}

	public String getTimeFilter() {
		return timeFilter;
	}

	public void setTimeFilter(String timeFilter) {
		this.timeFilter = timeFilter;
	}

	public boolean isTimeFilterEnabled() {
		return isTimeFilterEnabled;
	}

	public void setTimeFilterEnabled(boolean isTimeFilterEnabled) {
		this.isTimeFilterEnabled = isTimeFilterEnabled;
	}

	public TimeRange getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(TimeRange timeRange) {
		if (timeRange != null)
			this.timeRange = timeRange;
	}
	
    
}
