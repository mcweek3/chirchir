package com.story.tirrtirr;

import android.provider.BaseColumns;

//DataBase Table
public final class DataBases {
	public static final class CreateDB implements BaseColumns {
//		public static final String AUDIO = "audio";
		public static final String ID = "_id";
		public static final String CONTENT = "content";
		public static final String LIKE = "like_count";
		public static final String DATE = "date_db";
		public static final String AUTHOR = "author";
		public static final String CATEGORY = "category";
		public static final String _TABLENAME = "articles";
		public static final String _CREATE = 
				"create table " + _TABLENAME + " ("
						+ ID + " INT not null, "
//						+ AUDIO + " text not null, "
//						+ CONTENT + " text not null, "
						+ CONTENT + " text not null, "
						+ LIKE + " INT not null, "
						+ DATE + " text not null, "
						+ AUTHOR + " INT not null, "
						+ CATEGORY + " INT not null ); ";
	}
}