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
     * This method was created specifically for generating past time periods. It differs from the getTimeRange() method above with isPast argument set to true
     * because it is generating time ranges based on current moment of time minus specified period to the past.
     * Example: for past month, current date is 13/10/2013; method will return startTime as 13/09/2013, endTime as 13/10/2013
     * @param periodType
     * @return TimeRange
     */
    public static final TimeRange getPastTimeRange(int periodType) {
        Calendar calendar = Calendar.getInstance();

        TimeRange timeRange = new TimeRange(DEFAULT_START_DATE, DEFAULT_END_DATE);

        // End Time
        timeRange.setEndTime(calendar.getTimeInMillis()); // end time is the current time and it is common for any time period

        // Calculating Start Time
        switch (periodType) {

            case DateUtils.DAY: {
                calendar.add(Calendar.DAY_OF_MONTH, -1); // roll one day back to get start timestamp
                break;
            }
            case DateUtils.WEEK: {
                calendar.add(Calendar.WEEK_OF_MONTH, -1); // roll one week back to get start timestamp
                break;
            }
            case DateUtils.MONTH: {
                calendar.add(Calendar.MONTH, -1); // roll one month back to get start timestamp
                break;
            }
            case DateUtils.QUARTER: {
                calendar.add(Calendar.MONTH, -3); // roll one quarter back to get start timestamp
                break;
            }
            case DateUtils.YEAR: {
                calendar.add(Calendar.YEAR, -1); // roll one year back to get start timestamp
                break;
            }
            default: {
                // by default, return past month
                calendar.add(Calendar.MONTH, -1); // roll one month back to get start timestamp
            }
        }
        // Set Start Time
        timeRange.setStartTime(calendar.getTimeInMillis());
        return timeRange;
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
