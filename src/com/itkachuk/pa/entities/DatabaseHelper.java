package com.itkachuk.pa.entities;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Database helper which creates and upgrades the database and provides the DAOs for the app.
 * 
 * @author itkachuk
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "pa.db";
	private static final int DATABASE_VERSION = 1;

	private Dao<Account, String> accountDao;
	private Dao<ExpenseRecord, Integer> expenseRecordDao;
	private Dao<IncomeRecord, Integer> incomeRecordDao;	
	private Dao<ExpenseCategory, String> expenseCategoryDao;
	private Dao<IncomeCategory, String> incomeCategoryDao;
	private Dao<ExpenseDescription, String> expenseDescriptionDao;
	private Dao<IncomeDescription, String> incomeDescriptionDao;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}


	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Account.class);
			TableUtils.createTable(connectionSource, ExpenseCategory.class);
			TableUtils.createTable(connectionSource, ExpenseDescription.class);
			TableUtils.createTable(connectionSource, ExpenseRecord.class);
			TableUtils.createTable(connectionSource, IncomeCategory.class);
			TableUtils.createTable(connectionSource, IncomeDescription.class);
			TableUtils.createTable(connectionSource, IncomeRecord.class);
			Log.i(DatabaseHelper.class.getName(), "Successfully created 7 DB tables in onCreate()");
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create database tables", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		// TODO
	}

	public Dao<Account, String> getAccountDao() throws SQLException {
		if (accountDao == null) {
			accountDao = getDao(Account.class);
		}
		return accountDao;
	}
	
	public Dao<ExpenseRecord, Integer> getExpenseRecordDao() throws SQLException {
		if (expenseRecordDao == null) {
			expenseRecordDao = getDao(ExpenseRecord.class);
		}
		return expenseRecordDao;
	}
	
	public Dao<IncomeRecord, Integer> getIncomeRecordDao() throws SQLException {
		if (incomeRecordDao == null) {
			incomeRecordDao = getDao(IncomeRecord.class);
		}
		return incomeRecordDao;
	}

	public Dao<ExpenseCategory, String> getExpenseCategoryDao() throws SQLException {
		if (expenseCategoryDao == null) {
			expenseCategoryDao = getDao(ExpenseCategory.class);
		}
		return expenseCategoryDao;
	}
	
	public Dao<IncomeCategory, String> getIncomeCategoryDao() throws SQLException {
		if (incomeCategoryDao == null) {
			incomeCategoryDao = getDao(IncomeCategory.class);
		}
		return incomeCategoryDao;
	}
	
	public Dao<ExpenseDescription, String> getExpenseDescriptionDao() throws SQLException {
		if (expenseDescriptionDao == null) {
			expenseDescriptionDao = getDao(ExpenseDescription.class);
		}
		return expenseDescriptionDao;
	}
	
	public Dao<IncomeDescription, String> getIncomeDescriptionDao() throws SQLException {
		if (incomeDescriptionDao == null) {
			incomeDescriptionDao = getDao(IncomeDescription.class);
		}
		return incomeDescriptionDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		accountDao = null;
		expenseRecordDao = null;
		incomeRecordDao = null;
		expenseCategoryDao = null;
		incomeCategoryDao = null;
		expenseDescriptionDao = null;
		incomeDescriptionDao = null;
	}
}
