package com.itkachuk.pa.utils;

import java.util.Calendar;

import android.text.format.Time;
import android.util.Log;

public class DateUtils {
	private static final String TAG = "PocketAccountant";
	
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
				Log.d(TAG, "getActualMaximum for day_of_month=" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
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
	
	public static final TimeRange getTimeRange(int periodType, boolean isPast) {
		Calendar calendar = Calendar.getInstance();
		TimeRange timeRange = new TimeRange(0, 0);
		
		switch (periodType) {
		
		case DateUtils.DAY: {
			if (isPast) calendar.add(Calendar.DAY_OF_MONTH, -1); // roll one day back for past period
			// End Time
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Calendar.DAY_OF_MONTH, 23, 59, 59);
			timeRange.setEndTime(calendar.getTimeInMillis());
			// Start Time		
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Calendar.DAY_OF_MONTH, 0, 0, 0);
			timeRange.setStartTime(calendar.getTimeInMillis());
			return timeRange;
		}
		case DateUtils.WEEK: {
			// TODO!
//			if (isPast) calendar.add(Calendar.WEEK_OF_MONTH, -1); // roll one week back for past period
//			// End Time
//			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Calendar.DAY_OF_MONTH, 23, 59, 59);
//			timeRange.setEndTime(calendar.getTimeInMillis());
//			// Start Time		
//			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Calendar.DAY_OF_MONTH, 0, 0, 0);
//			timeRange.setStartTime(calendar.getTimeInMillis());
			return timeRange;
		}
		
		case DateUtils.MONTH: {
			if (isPast) calendar.add(Calendar.MONTH, -1); // roll one month back for past period
			// End Time
			Log.d(TAG, "getActualMaximum for day_of_month=" + calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
			timeRange.setEndTime(calendar.getTimeInMillis());
			// Start Time		
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);
			timeRange.setStartTime(calendar.getTimeInMillis());
			return timeRange;
		}
		case DateUtils.QUARTER: {
			
			return timeRange;
		}
		case DateUtils.YEAR: {
			if (isPast) calendar.add(Calendar.YEAR, -1); // roll one year back for past period
			// End Time
			calendar.set(calendar.get(Calendar.YEAR), 12, 31, 23, 59, 59);
			timeRange.setEndTime(calendar.getTimeInMillis());
			// Start Time		
			calendar.set(calendar.get(Calendar.YEAR), 1, 1, 0, 0, 0);
			timeRange.setStartTime(calendar.getTimeInMillis());
			return timeRange;
		}
		default: return timeRange;
		}
	}
}
