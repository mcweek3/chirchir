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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SignUp extends Activity{
	
	Button register = null;
	private EditText username = null;
	private EditText password = null;
	saveCookie pref = new saveCookie(this);
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.signup);
		register = (Button) findViewById(R.id.register); 
		username = (EditText) findViewById(R.id.signupid);
		password = (EditText) findViewById(R.id.signuppw);
		
		
		register.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("가입 중");
				if(username.getText().toString().length()<4 || password.getText().toString().length()<4) Toast.makeText(getApplicationContext(), "ID 또는 암호가 너무 짧습니다", Toast.LENGTH_SHORT).show();
				else new ProcessSignupTask().execute(null,null,null);
			}
		});
	}
	
	private class ProcessSignupTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;
		@Override
		protected Void doInBackground(Void... params) {
				
	
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/signup";
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
			
			
			if (res_str.startsWith("welcome")) {
				pref.put("id", username.getText().toString());
				String text = "Hello, " + pref.getValue("id", null);
				Toast.makeText(getApplicationContext(), text,
						Toast.LENGTH_SHORT).show();
				
				SignUp.this.finish();

			} else {
				Toast.makeText(getApplicationContext(), "중복된 ID입니다.",
						Toast.LENGTH_SHORT).show();
			}
			
			
		}

	}
}
