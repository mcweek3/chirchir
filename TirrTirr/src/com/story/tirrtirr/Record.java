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
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Record extends Activity {

	final String uploadFilePath = "/mnt/sdcard/data/";
	String uploadFileName = "temp";
	saveCookie pref = new saveCookie(this);
	
	File file = null;
	public ProgressBar pb = null;
	public Button record;
	public Button stop;
	Button upload;
	public MediaRecorder mediaRecorder = new MediaRecorder();
	public EditText edit_title;
	private volatile Thread theProgressBarThread1;
	public int CurrentPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);
		
		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(ProgressBar.GONE);
		edit_title = (EditText) findViewById(R.id.context);
		edit_title.setHint("제목을 입력하세요");
		record = (Button) findViewById(R.id.record);
		stop = (Button) findViewById(R.id.stop);
		stop.setVisibility(View.GONE);
		
		upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
					Toast.makeText(getApplicationContext(), "녹음을 시작합니다",
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
				Toast.makeText(getApplicationContext(), "녹음을 정지합니다",
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
	//////////////////////////////////////////////////////////////////////////////////////////////
	
	// 프로그레스바 쓰레드
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
					Toast.makeText(getApplicationContext(), "녹음을 정지합니다",
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
					meb.addTextBody("context", edit_title.getText().toString());
					meb.addTextBody("id", pref.getValue("id", ""));
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


			
//			String res_str = sb.toString();
//			System.out.println(res_str);
			
			
		}
	}
}