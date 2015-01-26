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

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private EditText et_username = null;
	private EditText et_password = null;
	saveCookie pref = new saveCookie(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_login);
		
		et_username = (EditText) findViewById(R.id.et_username);
		et_password = (EditText) findViewById(R.id.et_password);
		if (pref.getValue("email", null) != null)
			et_username.setText(pref.getValue("email", ""));
	}

	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.signbtn:
			Intent myIntent = new Intent(getApplicationContext(),
					SignUp.class);
			startActivity(myIntent);
			break;
		case R.id.loginbtn:
			new ProcessFacebookTask().execute(null, null, null);
		}
	}
	
	private class ProcessFacebookTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;

		@Override
		protected Void doInBackground(Void... params) {
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/login";
				HttpPost httpPost = new HttpPost(urlString);

				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair> (2);

					nameValuePairs.add(new BasicNameValuePair("id", et_username.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("pwd", et_password.getText().toString()));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					response = httpClient.execute(httpPost);
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

			if (!res_str.equals("nothing")) {
				pref.put("email", et_username.getText().toString());
				pref.put("id", Integer.parseInt(res_str.split(" ")[0]));
				System.out.println(pref.getValue("id", 15));
				Toast.makeText(getApplicationContext(), "Hello, " + res_str.split(" ")[1],
						Toast.LENGTH_SHORT).show();
				Intent myIntent = new Intent(getApplicationContext(),
						Category.class);
				startActivity(myIntent);
			} else
				Toast.makeText(getApplicationContext(), "Wrong Credentials",
						Toast.LENGTH_SHORT).show();
		}
	}
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
