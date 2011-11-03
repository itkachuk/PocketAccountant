package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IncomeCategory {
	
	@DatabaseField(id = true, unique = true)
	private String name;
	
	@DatabaseField
	private boolean isRemovable; // Predefined categories can't be removed by user
	
	IncomeCategory() {
		// needed by ormlite
	}
	
	public IncomeCategory(String name, boolean isRemovable) {
		super();
		this.name = name;
		this.isRemovable = isRemovable;
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

	@Override
	public String toString() {
		return "IncomeCategory [name=" + name + ", isRemovable=" + isRemovable
				+ "]";
	}
}
