package com.itkachuk.pa.utils;

public class TimeRange {
	
	private long mStartTime;
	private long mEndTime;
	
	public TimeRange(long startTime, long endTime) throws IllegalArgumentException {
		super();
		validateInput(startTime, endTime);
		this.mStartTime = startTime;
		this.mEndTime = endTime;
	}
	
	public long getStartTime() {
		return mStartTime;
	}
	public void setStartTime(long startTime) {
		validateInput(startTime, this.mEndTime);
		this.mStartTime = startTime;
	}
	public long getEndTime() {
		return mEndTime;
	}
	public void setEndTime(long endTime) {
		validateInput(this.mStartTime, endTime);
		this.mEndTime = endTime;
	}
	
	private void validateInput(long startTime, long endTime) {
		if (startTime > endTime) {
			throw new IllegalArgumentException("Invalid time range, end time can't be grater then start time.\n" + 
					"Start: " + startTime + ", End: " + endTime);
		}
	}
}
