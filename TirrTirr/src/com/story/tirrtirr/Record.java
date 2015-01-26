package com.story.tirrtirr;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class Record extends Activity {

	final String uploadFilePath = "/mnt/sdcard/data/";
	String uploadFileName = "temp";
	saveCookie pref = new saveCookie(this);
	
	File file = null;
	public ProgressBar pb = null;
	public Button record;
	public Button stop;
	int category = 0;
	Button upload;
	public MediaRecorder mediaRecorder = new MediaRecorder();
	public EditText edit_title;
	private volatile Thread theProgressBarThread1;
	public int CurrentPosition = 0;
	ArrayAdapter<CharSequence>  adspin;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(ProgressBar.GONE);
		edit_title = (EditText) findViewById(R.id.context);
		edit_title.setHint("내용을 입력하세요.");
		record = (Button) findViewById(R.id.record);
		stop = (Button) findViewById(R.id.stop);
		stop.setVisibility(View.GONE);
		
		
		 Spinner spinner = (Spinner) findViewById(R.id.spinner);
	        spinner.setPrompt("카테고리를 선택하세요.");
	 
	        adspin = ArrayAdapter.createFromResource(this, R.array.selected,    android.R.layout.simple_spinner_item);
	 
	        adspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	        spinner.setAdapter(adspin);
	        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
	            public void onItemSelected(AdapterView<?>  parent, View view, int position, long id) {
	                category = position;
	            }
	            public void onNothingSelected(AdapterView<?>  parent) {
	            }
	        });
		
		
		
		upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new ProcessUploadTask().execute(null,null,null);
			}
		});
		
		
		record.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					pb.setVisibility(ProgressBar.VISIBLE);
					stop.setVisibility(View.VISIBLE);
					record.setVisibility(View.GONE);
					startProgressBarThread();
					Editable title = edit_title.getText();
					mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mediaRecorder
							.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					mediaRecorder
							.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					mediaRecorder.setOutputFile(uploadFilePath
							+ uploadFileName + ".mp4");
					mediaRecorder.prepare();
					mediaRecorder.start();
					Toast.makeText(getApplicationContext(), "녹음 시작",
							Toast.LENGTH_SHORT).show();
				} catch (IOException ioe) {
					Toast.makeText(getApplicationContext(), "IOException",
							Toast.LENGTH_SHORT).show();
					;
				}

			}
		});

		stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(getApplicationContext(), "녹음 끝.",
						Toast.LENGTH_SHORT).show();
				pb.setVisibility(ProgressBar.GONE);
				stop.setVisibility(View.GONE);
				record.setVisibility(View.VISIBLE);
				stopProgressBarThread();
				mediaRecorder.stop();
			}
		});
	}
	

	
	public synchronized void startProgressBarThread() {
		if (theProgressBarThread1 == null) {
			theProgressBarThread1 = new Thread(null, backgroundThread1,
					"startProgressBarThread");
			CurrentPosition = 0;
			theProgressBarThread1.start();
		}
	}

	public synchronized void stopProgressBarThread() {
		if (theProgressBarThread1 != null) {
			Thread tmpThread = theProgressBarThread1;
			theProgressBarThread1 = null;
			tmpThread.interrupt();
		}
		pb.setVisibility(ProgressBar.GONE);
	}
	
	private Runnable backgroundThread1 = new Runnable() {
		@Override
		public void run() {
			if (Thread.currentThread() == theProgressBarThread1) {
				CurrentPosition = 0;
				final int total = 100;

				while (CurrentPosition < total) {
					try {
						progressBarHandle.sendMessage(progressBarHandle
								.obtainMessage());
						Thread.sleep(100);
					} catch (final InterruptedException e) {
						return;
					} catch (final Exception e) {
						return;
					}
				}
			}

		}

		Handler progressBarHandle = new Handler() {
			public void handleMessage(Message msg) {
				CurrentPosition++;
				pb.setProgress(CurrentPosition);
				if (CurrentPosition == 100) {
					Toast.makeText(getApplicationContext(), "�끃�쓬�쓣 �젙吏��빀�땲�떎",
							Toast.LENGTH_SHORT).show();
					pb.setVisibility(ProgressBar.GONE);
					stop.setVisibility(View.GONE);
					record.setVisibility(View.VISIBLE);
					mediaRecorder.stop();
					stopProgressBarThread();
				}
			}
		};
	};
	
	private class ProcessUploadTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;
		@Override
		protected Void doInBackground(Void... params) {
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/upload";
				HttpPost httpPost = new HttpPost(urlString);
				uploadFileName = "temp";
				File file = new File(uploadFilePath + uploadFileName + ".mp4");

				try {
					MultipartEntityBuilder meb = MultipartEntityBuilder.create();
					meb.addPart("image", new FileBody(file));
					ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8);
					meb.addTextBody("id", Integer.toString(pref.getValue("id", 0)),contentType);
					meb.addTextBody("category", ""+category,contentType);
					meb.addTextBody("context", edit_title.getText().toString(),contentType);
					HttpEntity entity = meb.build();
					httpPost.setEntity(entity);
					// Execute HTTP Post Request
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
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			Record.this.finish();
		}
	}
}