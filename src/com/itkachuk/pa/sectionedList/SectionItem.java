package com.itkachuk.pa.sectionedList;

public class SectionItem implements SectionedListItem {
	
	private final String title;
	
	public SectionItem(String title) {
		this.title = title;
	}
	
	public String getTitle(){
		return title;
	}
	
	@Override
	public boolean isSection() {
		return true;
	}
}
