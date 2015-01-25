package com.story.tirrtirr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends ListActivity{
	
	public static Context mMainContext;
	
	Button download = null;
	EditText target = null;
	saveCookie pref = new saveCookie(this);
	File file = null;
	public ProgressBar pb = null;
	public Button record;
	public MediaRecorder mediaRecorder = new MediaRecorder();
	public EditText edit_title;
	
	final String uploadFilePath = "/mnt/sdcard/data/";
	final String uploadFileName = "temp.m4a";
	
	private List<String> songs = new ArrayList<String>();
	private MediaPlayer mp = new MediaPlayer();
	private Handler mHandler = new Handler();

	public int Position = 0;
	
	public SeekBar songProgressBar;
	private volatile Thread theProgressBarThread1;
	public int CurrentPosition = 0;
	     
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		download = (Button)findViewById(R.id.download);

		record = (Button) findViewById(R.id.gorecord);
		target = (EditText) findViewById(R.id.target);
		
		updateSongList();
		
		download.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new ProcessDownloadTask().execute(null,null,null);
			}
		});
		
		record.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent myIntent = new Intent(getApplicationContext(),
						Record.class);
				startActivity(myIntent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateSongList();
	}
	
	
	
	public void updateSongList() {
		File home = new File(uploadFilePath); // home으로 sd카드의 root를 지정합니다.
		if (home.listFiles(new Mp3Filter()).length > 0) {
			for (File file : home.listFiles(new Mp3Filter())) {
				if (!songs.contains(file.getName()))
					songs.add(file.getName()); // 재생목록 리스트에 파일 이름을 추가합니다.
			}
			ArrayAdapter<String> songList = new ArrayAdapter<String>(this,
					R.layout.song_item, R.id.song_item_text, songs); // ListView의
																		// 레이아웃
																		// 및 참조할
																		// 리스트를
																		// 설정합니다.
			setListAdapter(songList); // ListView와 ArrayList를 연결합니다.

		}
	}
	
	/////////////////////////////////////////////////////
	
	
	
	
	
	
	public void myPlayHandler(View v) {

		ListView lvItems = getListView();

		// get the row the clicked button is in
		RelativeLayout vwParentRow = (RelativeLayout) v.getParent();

		TextView child = (TextView) vwParentRow.getChildAt(0);
		ProgressBar pro = (ProgressBar) vwParentRow.getChildAt(1);
		int index = songs.indexOf(child.getText());
		// Position = index;
		playSong(v, uploadFilePath + songs.get(index));

	}
	
	public void myLikeHandler(View v) {
		ListView lvItems = getListView();

		// get the row the clicked button is in
		RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
		TextView child = (TextView) vwParentRow.getChildAt(0);
		ImageButton likeButton = (ImageButton) vwParentRow.getChildAt(3);
		
		int index = songs.indexOf(child.getText());
		// Position = index;
		
		like();
		//Toast.makeText(this, index + "",Toast.LENGTH_SHORT).show();
	}
	
	private void playSong(View v, String songPath) {
		try {

			RelativeLayout vwParentRow = (RelativeLayout) v.getParent();
			songProgressBar = (SeekBar) vwParentRow.getChildAt(1);
			mp.reset();
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);
			updateProgressBar();
			Toast.makeText(this, "재생 : " + songPath, Toast.LENGTH_SHORT).show();
			TextView status = (TextView) findViewById(R.id.playStatus);
			status.setText("재생중 : " + songPath);
			
			songProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					// TODO Auto-generated method stub
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					mHandler.removeCallbacks(mUpdateTimeTask);
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					mHandler.removeCallbacks(mUpdateTimeTask);
					int totalDuration = mp.getDuration();
					int currentPosition = (int) (((float) seekBar.getProgress() / 100) * totalDuration);

					// forward or backward to certain seconds
					mp.seekTo(currentPosition);

					// update timer progress again
					updateProgressBar();

				}
						
			});
			// 한 곡의 재생이 끝나면 다음 곡을 재생하도록 합니다.
			mp.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					// nextSong();
					finishSong();
				}
			});
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	/////////////////////////////
	private void finishSong() {
		TextView status = (TextView) findViewById(R.id.playStatus);
		songProgressBar.setProgress(0);
		status.setText("준비됨");
		Toast.makeText(getApplicationContext(), "끝!.", Toast.LENGTH_SHORT)
				.show();
	}
	
	
	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}	
	
	/**
	 * Background Runnable thread
	 * */
	
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Updating progress bar
			int progress = (int) ((float) currentDuration / totalDuration * 100);
			float progress_float = (float) ((float) currentDuration / totalDuration * 100);
			songProgressBar.setProgress((int) progress);
			

			// Running this thread after 100 milliseconds
			if (currentDuration != totalDuration - 20)
				mHandler.postDelayed(this, 100);
		}
	};
	
	////////////////////////////////
	
	private class ProcessDownloadTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;
		@Override
		protected Void doInBackground(Void... params) {
				// 이미 있으면 안하도록
			
				file = new File("/mnt/sdcard/data/" + target.getText().toString() +".mp4");
				if(file.exists()){
					System.out.println("이미 있습니다");
					return null;
				}
				System.out.println("받아오겠습니다.");
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/upload/" + target.getText().toString() +".mp4";
				HttpGet httpGet = new HttpGet(urlString);
			
	
				try {
					// Execute HTTP Post Request
					response = httpClient.execute(httpGet);
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
				
				
				HttpEntity entity = response.getEntity();
				try{
					OutputStream outStream = new FileOutputStream(file);
				    entity.writeTo(outStream);
				    outStream.close();
					} catch(IOException e){
					}
				
				
			return null;
		}
		
		
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			updateSongList();
			
			
//			MediaPlayer mp = new MediaPlayer();
//			try {
//				mp.setDataSource("/mnt/sdcard/data/file.m4a");
//				//mp.setDataSource("http://54.65.81.18:9000/upload/temp.m4a");
//				mp.prepare();
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (SecurityException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalStateException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			
//			mp.start();
			

			
			
		}
	}
	
	 private void like() {
	      new ProcessLikeTask().execute(null, null, null);
	   }

	   // AsyncTask<Params,Progress,Result>
	   private class ProcessLikeTask extends AsyncTask<Void, Void, Void> {
	      HttpResponse response = null;
	      @Override
	      protected Void doInBackground(Void... params) {
	            HttpClient httpClient = new DefaultHttpClient();
	            String urlString = "http://54.65.81.18:9000/like";
	            HttpPost httpPost = new HttpPost(urlString);
	   
	            try {
	               List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
	               nameValuePairs.add(new BasicNameValuePair("id", "7"));
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
	             String text = "Like, " + res_str;
	             Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	          } else {
	             Toast.makeText(getApplicationContext(), "Wrong Credentials",
	                   Toast.LENGTH_SHORT).show();
	          }
	          
	       }
	      	      
	   }

}