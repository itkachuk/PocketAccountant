package com.itkachuk.pa.entities;

import com.itkachuk.pa.sectionedList.SectionedListItem;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category implements SectionedListItem {
	public static final String NAME_FIELD_NAME = "name";
	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	public static final String IS_REMOVABLE_FIELD_NAME = "isRemovable";	
	
	@DatabaseField(id = true, uniqueCombo = true, columnName = NAME_FIELD_NAME)
	private String name;
	
	@DatabaseField(uniqueCombo = true, columnName = IS_EXPENSE_FIELD_NAME)
	private boolean isExpense; // Expense - true, Income - false
	
	@DatabaseField(columnName = IS_REMOVABLE_FIELD_NAME)
	private boolean isRemovable; // Predefined categories can't be removed by user
	
	public Category() {
		// needed by ormlite
	}

	public Category(String name, boolean isExpense, boolean isRemovable) {
		super();
		this.name = name;
		this.isRemovable = isRemovable;
		this.isExpense = isExpense;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRemovable() {
		return isRemovable;
	}

	public void setRemovable(boolean isRemovable) {
		this.isRemovable = isRemovable;
	}

	public boolean isExpense() {
		return isExpense;
	}

	public void setExpense(boolean isExpense) {
		this.isExpense = isExpense;
	}

	@Override
	public String toString() {
		return name == null ? "" : name;
	}

	@Override
	public boolean isSection() {
		return false;
	}	
}
