package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Category {
	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	
	@DatabaseField(id = true, unique = true)
	private String name;

	@DatabaseField
	private boolean isRemovable; // Predefined categories can't be removed by user
	
	@DatabaseField(columnName = IS_EXPENSE_FIELD_NAME)
	private boolean isExpense; // Expense - true, Income - false
	
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
}
