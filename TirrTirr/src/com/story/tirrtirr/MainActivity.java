package com.story.tirrtirr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private EditText username = null;
	private EditText password = null;
	private Button signup = null;
	private Button login = null;
	saveCookie pref = new saveCookie(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		username = (EditText) findViewById(R.id.editText1);
		password = (EditText) findViewById(R.id.editText2);
		signup = (Button) findViewById(R.id.signbtn);
		login = (Button) findViewById(R.id.loginbtn);
		if(pref.getValue("email", null)!=null) username.setText(pref.getValue("email",""));
		signup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent myIntent = new Intent(getApplicationContext(),
						SignUp.class);
				startActivity(myIntent);
			}
		});

		login.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				feed();

			}
		});
	}

	private void feed() {
		new ProcessFacebookTask().execute(null, null, null);
	}

	// AsyncTask<Params,Progress,Result>
	private class ProcessFacebookTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;
		@Override
		protected Void doInBackground(Void... params) {
				
	
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/login";
				HttpPost httpPost = new HttpPost(urlString);
	
				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
							2);
					nameValuePairs.add(new BasicNameValuePair("id", username.getText().toString()));
					nameValuePairs.add(new BasicNameValuePair("pwd",password.getText().toString()));
					httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	
					// Execute HTTP Post Request
					response = httpClient.execute(httpPost);
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			return null;
		}
		
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			HttpEntity entity = response.getEntity();
			StringBuilder sb = new StringBuilder();
			try {
			    BufferedReader reader = 
			           new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
			    String line = null;

			    while ((line = reader.readLine()) != null) {
			        sb.append(line);
			    }
			}
			catch (IOException e) { e.printStackTrace(); }
			catch (Exception e) { e.printStackTrace(); }


			
			String res_str = sb.toString();
			System.out.println(res_str);
			if (!res_str.equals("nothing")) {
				pref.put("email", username.getText().toString());
				pref.put("id", res_str.split(" ")[0]);
				String text = "Hello, " + res_str.split(" ")[1];
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
				
				Intent myIntent = new Intent(getApplicationContext(),
						Main.class);
				startActivity(myIntent);
				

			} else {
				Toast.makeText(getApplicationContext(), "Wrong Credentials",
						Toast.LENGTH_SHORT).show();
			}
			
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
