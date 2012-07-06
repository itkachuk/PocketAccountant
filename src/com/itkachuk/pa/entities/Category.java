package com.itkachuk.pa.entities;

import com.itkachuk.pa.sectionedList.SectionedListItem;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category implements SectionedListItem {
	public static final String CATEGORY_ID_FIELD_NAME = "id";
	public static final String NAME_FIELD_NAME = "name";
	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	public static final String IS_REMOVABLE_FIELD_NAME = "isRemovable";	
	
	@DatabaseField(generatedId = true, columnName = CATEGORY_ID_FIELD_NAME)
	private int id;
	
	@DatabaseField(uniqueCombo = true, columnName = NAME_FIELD_NAME)
	private String name;
	
	@DatabaseField(uniqueCombo = true, columnName = IS_EXPENSE_FIELD_NAME)
	private boolean isExpense; // Expense - true, Income - false
	
	public Category() {
		// needed by ormlite
	}

	public Category(int id, String name, boolean isExpense) {
		super();
		this.id = id;
		this.name = name;
		this.isExpense = isExpense;
	}
	
	public Category(String name, boolean isExpense) {
		super();
		this.name = name;
		this.isExpense = isExpense;
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public boolean equals(Category category) {
		return (id == category.getId());
	}
	
	@Override
	public boolean isSection() {
		return false;
	}	
}
