package com.itkachuk.pa.entities;

import java.util.Date;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IncomeOrExpenseRecord {
	
	public static final String DATE_FIELD_NAME = "date";
	public static final String TIMESTAMP_FIELD_NAME = "timestamp";
	public static final String ACCOUNT_FIELD_NAME = "account";
	public static final String CATEGORY_FIELD_NAME = "category";
	public static final String DESCRIPTION_FIELD_NAME = "description";
	public static final String AMOUNT_FIELD_NAME = "amount";
	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	public static final String IS_REGULAR_FIELD_NAME = "isRegular";
	public static final String IS_PLANNED_FIELD_NAME = "isPlanned";

	// id is generated by the database and set on the object automatically
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(columnName = DATE_FIELD_NAME)
	private Date date;
	
	@DatabaseField(columnName = TIMESTAMP_FIELD_NAME)
	private long timestamp; // UTC timestamp in milliseconds
	
	@DatabaseField(columnName = ACCOUNT_FIELD_NAME) // 'null' value will correspond to "main" account (non-removable)
	private String account;
	
	@DatabaseField(canBeNull = false, columnName = CATEGORY_FIELD_NAME)
	private String category;
	
	@DatabaseField(columnName = DESCRIPTION_FIELD_NAME)
	private String description; // can play a role of subcategory
	
	@DatabaseField(columnName = AMOUNT_FIELD_NAME)
	private double amount;
	
	@DatabaseField(columnName = IS_EXPENSE_FIELD_NAME)
	private boolean isExpense; // Expense - true, Income - false
	
	@DatabaseField(columnName = IS_REGULAR_FIELD_NAME)
	private boolean isRegular; // Record added by service - true, added manually - false
	
	@DatabaseField(columnName = IS_PLANNED_FIELD_NAME)
	private boolean isPlanned; // Planned Income/Expense record - true, actual - false
	
	public IncomeOrExpenseRecord() {
		// needed by ormlite
	}

	public IncomeOrExpenseRecord(Date date, String account, String category, String description, 
			double amount, boolean isExpense, boolean isRegular, boolean isPlanned) {
		super();
		this.timestamp = System.currentTimeMillis();
		this.date = date;
		this.account = account;
		this.amount = amount;
		this.category = category;
		this.description = description;
		this.isExpense = isExpense;
		this.isRegular = isRegular;
		this.isPlanned = isPlanned;
	}

	public void setId(int recordId) {
		this.id = recordId;
	}
	
	public int getId() {
		return id;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long millis) {
		this.timestamp = millis;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public boolean isRegular() {
		return isRegular;
	}

	public void setRegular(boolean isRegular) {
		this.isRegular = isRegular;
	}

	public boolean isExpense() {
		return isExpense;
	}

	public void setExpense(boolean isExpense) {
		this.isExpense = isExpense;
	}
	
	public boolean isPlanned() {
		return isPlanned;
	}

	public void setPlanned(boolean isPlanned) {
		this.isPlanned = isPlanned;
	}

	@Override
	public String toString() {
		return "IncomeOrExpenseRecord [id=" + id + ", date=" + date
				+ ", timestamp=" + timestamp + ", account=" + account
				+ ", category=" + category + ", description=" + description
				+ ", amount=" + amount + ", isExpense=" + isExpense
				+ ", isRegular=" + isRegular + ", isPlanned=" + isPlanned + "]";
	}
}
