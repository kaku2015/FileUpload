package com.skr.fileupload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

public class UploadLogService {
	private DBOpenHelper dbOpenHelper;
	
	public UploadLogService(Context context){
		dbOpenHelper = new DBOpenHelper(context);
	}
	
	public String getBindId(File file){
		SQLiteDatabase db = dbOpenHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select sourceid from uploadlog where path=?", new String[]{file.getAbsolutePath()});
		if(cursor.moveToFirst()){
			return cursor.getString(0);
		}
		return null;
	}
	
	public void save(String sourceid, File file){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("insert into uploadlog(path,sourceid) values(?,?)", 
				new Object[]{file.getAbsolutePath(), sourceid});
	}
	
	public void delete(File file){
		SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
		db.execSQL("delete from uploadlog where path=?", new Object[]{file.getAbsolutePath()});
	}
 }
