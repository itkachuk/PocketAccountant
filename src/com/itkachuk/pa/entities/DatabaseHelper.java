package com.itkachuk.pa.entities;

import java.io.File;
import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
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

    public static final String SDCARD_DIR = "/sdcard";
    public static final String APP_ROOT_DIR = "PocketAccountant";
    public static final String SDCARD_MEMORY_DIR = SDCARD_DIR + File.separator + APP_ROOT_DIR;
	
	private static final String DATABASE_NAME = "pa.db";
	private static final int DATABASE_VERSION = 2;

	private Dao<Account, Integer> accountDao;
	private Dao<IncomeOrExpenseRecord, Integer> recordDao;
	private Dao<Category, Integer> categoryDao;
	private Dao<Description, String> descriptionDao;

	public DatabaseHelper(Context context) {		
		super(context, getDbNameFullPath(), null, DATABASE_VERSION);
	}

	private static String getDbNameFullPath(){
		File cardDir = Environment.getExternalStorageDirectory();		
	    if (cardDir.exists() && cardDir.canRead() && cardDir.canWrite()) {
	    	return SDCARD_MEMORY_DIR + File.separator + DATABASE_NAME;
	    } else {
	    	return DATABASE_NAME; // use phone internal memory, if sdcard isn't available
	    }
	}

	@Override
	public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Account.class);
			TableUtils.createTable(connectionSource, Category.class);
			TableUtils.createTable(connectionSource, Description.class);
			TableUtils.createTable(connectionSource, IncomeOrExpenseRecord.class);
			Log.i(DatabaseHelper.class.getName(), "Successfully created 4 DB tables in onCreate()");
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Unable to create database tables", e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer) {
		// TODO
	}

	public Dao<Account, Integer> getAccountDao() throws SQLException {
		if (accountDao == null) {
			accountDao = getDao(Account.class);
		}
		return accountDao;
	}
	
	public Dao<IncomeOrExpenseRecord, Integer> getRecordDao() throws SQLException {
		if (recordDao == null) {
			recordDao = getDao(IncomeOrExpenseRecord.class);
		}
		return recordDao;
	}

	public Dao<Category, Integer> getCategoryDao() throws SQLException {
		if (categoryDao == null) {
			categoryDao = getDao(Category.class);
		}
		return categoryDao;
	}
	
	// TODO - Integer ??
	public Dao<Description, String> getDescriptionDao() throws SQLException {
		if (descriptionDao == null) {
			descriptionDao = getDao(Description.class);
		}
		return descriptionDao;
	}
	
	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		accountDao = null;
		recordDao = null;
		categoryDao = null;
		descriptionDao = null;
	}
}
