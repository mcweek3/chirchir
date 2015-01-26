package com.story.tirrtirr;

import com.story.tirrtirr.Constants.DB;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class DataProvider extends ContentProvider {
	private DbHelper dbHelper;

	@Override
	public boolean onCreate() {
		dbHelper = new DbHelper(getContext());
		Log.i("ASDF", "provider made");
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (DB.uriMatcher.match(uri)) {
		case 1:
			qb.setTables("articles");
			break;
		case 2:
			qb.setTables("articles");
			qb.appendWhere("_id = " + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long id;
		
		switch (DB.uriMatcher.match(uri)) {
		case 1:
			id = db.insertOrThrow("articles", null, values);
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		Uri insertUri = ContentUris.withAppendedId(uri, id);
		getContext().getContentResolver().notifyChange(insertUri, null);
		return insertUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		
		switch (DB.uriMatcher.match(uri)) {
		case 1:
			count = db.delete("articles", selection, selectionArgs);
			break;
		case 2:
			count = db.delete("articles", "_id = ?", new String[] {uri.getLastPathSegment()});
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		
		switch (DB.uriMatcher.match(uri)) {
		case 1:
			count = db.update("articles", values, selection, selectionArgs);
			break;
		case 2:
			count = db.update("articles", values, "_id = ?", new String[] {uri.getLastPathSegment()});
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	private static class DbHelper extends SQLiteOpenHelper {
		public DbHelper(Context context) {
			super(context, "articles.db", null, 8);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
            db.execSQL(DataBases.CreateDB._CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DataBases.CreateDB._TABLENAME);
            onCreate(db);
		}
	}
}