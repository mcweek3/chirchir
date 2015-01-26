package com.story.tirrtirr;

import android.content.UriMatcher;

/*
 * Constants. Each nested classes' names represent relevant usages.
 */
public class Constants {
	public static class DB {
		public static final String DATABASE_NAME = "articles.db";
		public static final int DATABASE_VER = 1; 
		
//		public static final Uri URI_PROFILE = Uri.parse("content://com.example.chitchat.db.provider/profile");
//		public static final Uri URI_MESSAGES = Uri.parse("content://com.example.chitchat.db.provider/messages");

/*		public static final int PROFILE_ALLROWS = 1;
		public static final int PROFILE_SINGLE_ROW = 2;
		public static final int MESSAGES_ALLROWS = 3;
		public static final int MESSAGES_SINGLE_ROW = 4;		
*/		public static final UriMatcher uriMatcher;
		static {
			uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			uriMatcher.addURI("com.story.tirrtirr.provider", "articles", 1);
			uriMatcher.addURI("com.story.tirrtirr.provider", "articles/#", 2);
		}
		
//		public static final String COL_ID = "_id";
		public static class PROFILE {
			public static final String TABLE = "profile";
			public static final String COL_COUNT = "count";
			public static final String COL_EMAIL = "email";
			public static final String COL_NAME = "name";
		}
		public static class MESSAGES {
			public static final String TABLE = "messages";
			public static final String COL_AT = "at";
			public static final String COL_FROM = "email";
			public static final String COL_TO = "email2";
			public static final String COL_MSG = "msg";

		}
	}
}
