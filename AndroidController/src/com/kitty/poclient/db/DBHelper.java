package com.kitty.poclient.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.util.Log;

public class DBHelper  extends SQLiteOpenHelper {//initD,new DBHelper()
    private static String databaseName="phoneclient.db";
    private static int databaseseversion=0;
  
    private static SQLiteDatabase sqLitedatabase;
//    private Handler handler;
    
	public DBHelper(Context context, String name, CursorFactory factory,
			int version,String databaseName,Handler handler) {
		
	  super(context, databaseName, null, version);
	  this.databaseseversion=version;
//	  this.handler=handler;
	
	  sqLitedatabase=getWritableDatabase();
	  
	}
	 
	@Override
	public void onCreate(SQLiteDatabase db) {
		
		Log.i("dbxx", "sdfs");
		db.execSQL(DBTable.create_table_album);  
		db.execSQL(DBTable.create_table_disk);  
		db.execSQL(DBTable.create_table_music);
		db.execSQL(DBTable.create_table_artist);
		db.execSQL(DBTable.create_table_pack);
		db.execSQL(DBTable.create_table_technology);
		db.execSQL(DBTable.create_table_product_artist);
		db.execSQL(DBTable.create_table_technology);
		db.execSQL(DBTable.create_table_product_pack);
		db.execSQL(DBTable.create_table_zx_tag);
		System.out.println("create_table_zx_tag success!");
		db.execSQL(DBTable.create_table_search_history);
		System.out.println("create_table_search_history success!");
        
	}
       
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DBTable.create_table_album);  
		db.execSQL(DBTable.create_table_disk);  
		db.execSQL(DBTable.create_table_music);
		db.execSQL(DBTable.create_table_artist);
		db.execSQL(DBTable.create_table_pack);
		db.execSQL(DBTable.create_table_technology);
		db.execSQL(DBTable.create_table_product_artist);
		db.execSQL(DBTable.create_table_technology);
		db.execSQL(DBTable.create_table_product_pack);
		db.execSQL(DBTable.create_table_zx_tag);
       
	}

	public static void setDatabaseName(String databaseName) {
		DBHelper.databaseName = databaseName;
	}

	public static int getDatabaseseversion() {
		return databaseseversion;
	}

	public static void setDatabaseseversion(int databaseseversion) {
		DBHelper.databaseseversion = databaseseversion;
	}

	public static SQLiteDatabase getSqLitedatabase() {
		return sqLitedatabase;
	}

	public static void setSqLitedatabase(SQLiteDatabase sqLitedatabase) {
		DBHelper.sqLitedatabase = sqLitedatabase;
	}
}
