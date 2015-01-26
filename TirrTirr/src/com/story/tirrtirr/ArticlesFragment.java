package com.story.tirrtirr;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ArticlesFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private onFragmentInteractionListener mListener;
	private ArticleCursorAdapter adapter;

	public interface onFragmentInteractionListener {
		public int getCategory();

		public int getAuthor();
	}

	@Override
	public void onAttach(Activity activity) {
		// Assign FragmentInteractionListener job to parent activity.
		super.onAttach(activity);

		mListener = (onFragmentInteractionListener) activity;
	}

	@Override
	public void onDetach() {
		// Detach activity from the listener job
		super.onDetach();

		mListener = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Link DB to ListView
		super.onCreate(savedInstanceState);

		// Adapter & DB Linking
		adapter = new ArticleCursorAdapter(getActivity(), null, 0);
		/*
		 * // How Adapter should inflate items adapter.setViewBinder(new
		 * SimpleCursorAdapter.ViewBinder() {
		 * 
		 * @Override public boolean setViewValue(View view, Cursor cursor, int
		 * columnIndex) { switch(view.getId()) { case R.id.txt_content:
		 * ((TextView) view).setText(cursor.getString(columnIndex)); return
		 * true; case R.id.txt_like_count: ((TextView)
		 * view).setText(Integer.valueOf
		 * (cursor.getInt(columnIndex)).toString()); return true; case
		 * R.id.author: ((TextView)
		 * view).setText(cursor.getString(columnIndex)); return true; } return
		 * false; } });
		 */
		// setting up
		setListAdapter(adapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// No dividers between list items
		getListView().setDivider(null);

		// Bundle to tell loader who the user is
		Bundle args = new Bundle();
		args.putInt("category", mListener.getCategory());
		args.putInt("author", mListener.getAuthor());

		// initialize loader
		// getLoaderManager().initLoader(0, null, this);
		getLoaderManager().initLoader(0, args, this);

		Log.i("ASDF", "on ActivityCreated 2");
	}

	// ////////////////// Loader Part /////////////////////////

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// Loader set up
		int category = args.getInt("category");
		int author = args.getInt("author");
		boolean isCategory = false;
		if (author == 0)
			isCategory = true;

		// from URI, which ones, arguments, in what order

		if (isCategory) {
			if (category != 6) {
				CursorLoader loader = new CursorLoader(
						getActivity(),
						Uri.parse("content://com.story.tirrtirr.provider/articles"),
						null,
						// "category" + " = ?",
						"category = ?", new String[] { Integer
								.valueOf(category).toString() }, "date_db DESC");
				// "id" + " ASC");

				return loader;
			} else {
				CursorLoader loader = new CursorLoader(
						getActivity(),
						Uri.parse("content://com.story.tirrtirr.provider/articles"),
						null,
						// "category" + " = ?",
						null, null, "like_count DESC LIMIT 5");
				// "id" + " ASC");

				return loader;

			}
		} else if (author == -1) { // Following List
			String temp_templete = "author = ?";
			for(int i = 0; i<Category.givingList.length -1 ; i++){
				temp_templete += "or author = ?";
			}
			CursorLoader loader = new CursorLoader(
					getActivity(),
					Uri.parse("content://com.story.tirrtirr.provider/articles"),
					null,
					// "category" + " = ?",
					temp_templete ,Category.givingList, "date_db DESC");
			return loader;
		} else {
			CursorLoader loader = new CursorLoader(
					getActivity(),
					Uri.parse("content://com.story.tirrtirr.provider/articles"),
					null,
					// "category" + " = ?",
					"author = ?", new String[] { Integer.valueOf(author)
							.toString() }, "date_db DESC");
			return loader;
		}

	}
	
	
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}
}
