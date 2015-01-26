package com.story.tirrtirr;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class Category extends Activity {

	ImageButton food = null;
	ImageButton music = null;
	ImageButton movie = null;
	ImageButton people = null;
	ImageButton love = null;
	ImageButton trash = null;
	ImageButton best = null;
	ImageButton star = null;
	ImageButton profile = null;
	saveCookie pref = new saveCookie(this);
	ArrayList<String> followList = new ArrayList<String>();
	public JSONArray following = new JSONArray();
	public static String[] givingList = {};
	public static int selected_category = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);
		
		food = (ImageButton) findViewById(R.id.Food);
		music = (ImageButton) findViewById(R.id.Music);
		movie = (ImageButton) findViewById(R.id.Movie);
		people = (ImageButton) findViewById(R.id.People);
		love = (ImageButton) findViewById(R.id.Love);
		trash = (ImageButton) findViewById(R.id.Trash);
		best = (ImageButton) findViewById(R.id.Best);
		star = (ImageButton) findViewById(R.id.Star);
		profile = (ImageButton)findViewById(R.id.Profile);
		
		
		food.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 0;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		music.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 1;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);				
			}
		});
		
		movie.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 2;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		people.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 3;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		love.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 4;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		trash.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 5;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		best.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				selected_category = 6;
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
			}
		});
		
		star.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new ProcessFacebookTask().execute(null,null,null);
				
			}
		});
		
		profile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pref.put("view_id", pref.getValue("id", 0));
				Intent myIntent = new Intent(getApplicationContext(),
						MyPage.class);
				startActivity(myIntent);
			}
		});

	}
	
	private class ProcessFacebookTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;

		@Override
		protected Void doInBackground(Void... params) {
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/followlist/" + pref.getValue("id", 0);
				HttpGet httpGet = new HttpGet(urlString);

				try {
					response = httpClient.execute(httpGet);
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			return null;
		}
		
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			HttpEntity entity = response.getEntity();
			StringBuilder sb = new StringBuilder();

			try {
			    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
			    String line = null;

			    while ((line = reader.readLine()) != null)
			        sb.append(line);
			}
			catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			String res_str = sb.toString();
			try {
            	following = new JSONArray(res_str);
            } catch (JSONException e) {
            	e.printStackTrace();
            }
            
            JSONObject temp = null;
            ContentValues values = null;
            
            for (int i = 0; i < following.length(); i++) {
            	try {
					temp = following.getJSONObject(i);
            	}catch(Exception e){}
            	
            	try {
					followList.add(Integer.toString(temp.getInt("dest")));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
			
			givingList = followList.toArray(new String[followList.size()]);
			selected_category = 7;
			Intent myIntent = new Intent(getApplicationContext(),
					Main.class);
			startActivity(myIntent);

		}
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
