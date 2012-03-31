package com.itkachuk.pa.utils;

import java.util.Calendar;

public class DateUtils {
	private static final String TAG = "PocketAccountant";
	
	public static final long DEFAULT_START_DATE = 0L;
//	public static final long DEFAULT_END_DATE = 4102444800000L; // 1 Jan 2100
	public static final long DEFAULT_END_DATE = Long.MAX_VALUE; // 20 Nov 2286
	
	public static final int DAY = 0;
	public static final int WEEK = 1;
	public static final int MONTH = 2;
	public static final int QUARTER = 3;
	public static final int YEAR = 4;


	/**
	 * 
	 * @param periodType Month(0), Quarter(1) or Year(2)
	 * @param isPast If true, returns timestamp for previous month/quarter/year
	 * @param isForEnd If true - returns timestamp for the end of period, false - for start of the period
	 * @return
	 */
	public static final long getTimestamp(int periodType, boolean isPast, boolean isForEnd) {
		Calendar calendar = Calendar.getInstance();
		switch (periodType) {
		case DateUtils.MONTH: {
			if (isPast) calendar.add(Calendar.MONTH, -1); // roll one month back for past period
			if (isForEnd) {
				//Log.d(TAG, "getActualMaximum for day_of_month=" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
				calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
				return calendar.getTimeInMillis();
				
			} else {
				calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
				return calendar.getTimeInMillis();
			}
		}
		case DateUtils.QUARTER: {
			return 0;

		}
		case DateUtils.YEAR: {
			return 0;
		}
		default: return 0;
		}
	}
	
	
	/**
	 * Function calculates start/end timestamps for various past or current time intervals
	 * @param periodType Day(0), Week(1), Month(2), Quarter(3) or Year(4)
	 * @param isPast If true, returns timestamp for previous day/week/month/quarter/year
	 * @return TimeRange object (includes start/end timestamps)
	 */
	public static final TimeRange getTimeRange(int periodType, boolean isPast) {
		Calendar calendar = Calendar.getInstance();
		// Reset hour/min/sec values, since they are not in use
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		TimeRange timeRange = new TimeRange(DEFAULT_START_DATE, DEFAULT_END_DATE);
		
		switch (periodType) {
		
		case DateUtils.DAY: {			
			if (isPast) calendar.add(Calendar.DAY_OF_MONTH, -1); // roll one day back for past day
			// Start Time
			timeRange.setStartTime(calendar.getTimeInMillis());
			// End Time
			calendar.add(Calendar.DAY_OF_MONTH, 1); // roll one day forward
			timeRange.setEndTime(calendar.getTimeInMillis());								
			return timeRange;
		}
		
		case DateUtils.WEEK: {			
			calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // set cursor to first day of week						
			
			if (isPast) calendar.add(Calendar.WEEK_OF_MONTH, -1); // roll one week back for past period			
			// Start Time
			timeRange.setStartTime(calendar.getTimeInMillis());							
			// End Time
			calendar.add(Calendar.WEEK_OF_MONTH, 1); // roll one week forward
			timeRange.setEndTime(calendar.getTimeInMillis());									
			return timeRange;
		}
		
		case DateUtils.MONTH: {
			if (isPast) calendar.add(Calendar.MONTH, -1); // roll one month back for past period		
			// Start Time		
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			timeRange.setStartTime(calendar.getTimeInMillis());
			// End Time			
			calendar.add(Calendar.MONTH, 1);
			timeRange.setEndTime(calendar.getTimeInMillis());
			return timeRange;
		}
		
		case DateUtils.QUARTER: {
			int quarter = getQuarter();
			int[] months = { 0, 3, 6, 9 };
			int monthIndex = quarter - 1;
			
			switch(quarter) {
			case 1: {
				if (isPast) {
					calendar.add(Calendar.YEAR, -1); // roll 1 year back for past quarter
					monthIndex = 3;
				}
				// Start Time
				calendar.set(calendar.get(Calendar.YEAR), months[monthIndex], 1);
				timeRange.setStartTime(calendar.getTimeInMillis());				
				// End Time
				calendar.add(Calendar.MONTH, 3); // roll 3 months forward
				timeRange.setEndTime(calendar.getTimeInMillis());					
				break;
			}
			case 2:
			case 3:
			case 4: {
				if (isPast) monthIndex--;
				// Start Time
				calendar.set(calendar.get(Calendar.YEAR), months[monthIndex], 1);
				timeRange.setStartTime(calendar.getTimeInMillis());				
				// End Time
				calendar.add(Calendar.MONTH, 3); // roll 3 months forward
				timeRange.setEndTime(calendar.getTimeInMillis());					
				break;
			}
			}
			return timeRange;
		}
		
		case DateUtils.YEAR: {
			if (isPast) calendar.add(Calendar.YEAR, -1); // roll one year back for past period		
			// Start Time		
			calendar.set(calendar.get(Calendar.YEAR), 0, 1, 0, 0, 0);
			timeRange.setStartTime(calendar.getTimeInMillis());			
			// End Time
			calendar.add(Calendar.YEAR, 1);
			timeRange.setEndTime(calendar.getTimeInMillis());
			return timeRange;
		}
		default: return timeRange;
		}
	}
	
	/**
	 * Returns number of quarter of current year: 1, 2, 3 or 4
	 * @return
	 */
	public static int getQuarter() {
		Calendar calendar = Calendar.getInstance();
		long currentTime = calendar.getTimeInMillis();
		int currentYear = calendar.get(Calendar.YEAR);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		
		int[] months = { 0, 3, 6, 9 };
		int count = 0;
		do {
			calendar.set(currentYear, months[count++], 1);
			calendar.add(Calendar.MONTH, 3);
			long tempTime = calendar.getTimeInMillis();
			if(currentTime < tempTime)
				return count;
		} while(count < 4);
		return 0;
	}
	
	
	/**
	 * Function returns the TimeRange instance for specified month and year.
	 * @param year
	 * @param month
	 * @return timeRange
	 */
	public static final TimeRange getTimeRangeForMonth(int year, int month) {
		TimeRange timeRange = new TimeRange(DEFAULT_START_DATE, DEFAULT_END_DATE);
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		timeRange.setStartTime(calendar.getTimeInMillis());
		calendar.add(Calendar.MONTH, 1);
		timeRange.setEndTime(calendar.getTimeInMillis());
		return timeRange;
	}
	
}
