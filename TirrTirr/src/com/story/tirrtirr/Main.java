package com.story.tirrtirr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

public class Main extends Activity implements ArticlesFragment.onFragmentInteractionListener {
	public static Context mMainContext;
	Button btn_download = null;
	EditText et_target = null;
	saveCookie pref = new saveCookie(this);
	File file = null;
	public ProgressBar pb = null;
	public Button btn_record;
	public Button btn_update;
	public MediaRecorder mediaRecorder = new MediaRecorder();
	public EditText et_title;
	final String uploadFilePath = "/mnt/sdcard/data/";
	final String uploadFileName = "temp.m4a";
	private List<String> songs = new ArrayList<String> ();
	private MediaPlayer mp = new MediaPlayer();
	private Handler mHandler = new Handler();
	public int Position = 0;
	public SeekBar songProgressBar;
//	private volatile Thread theProgressBarThread1;
	public int CurrentPosition = 0;
	public JSONArray articles = new JSONArray();
	ArrayList<Integer> local_list = new ArrayList<Integer>();
	int category_num;
	int return_for_getauthor = 0;
	public String temp_global = null;
	boolean mutex = false;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ac_main);
		
		category_num = Category.selected_category;
		if(category_num == 7){
			return_for_getauthor = -1;
		}
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_record:
			Intent myIntent = new Intent(getApplicationContext(),
					Record.class);
			startActivity(myIntent);
			break;
		case R.id.btn_update:
			new ProcessUpdateTask().execute(null, null, null);
			break;
		}
	}
	
	public String nickMid(int id){
		try {
			return new ProcessNicknameTask().execute(id,null,null).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp_global;
	}
	
	public class ProcessNicknameTask extends AsyncTask<Integer, Void, String> {
		HttpResponse response = null;

		@Override
		protected String doInBackground(Integer... params) {
				HttpClient httpClient = new DefaultHttpClient();
				String urlString = "http://54.65.81.18:9000/nickname/" + params[0];
				HttpGet httpGet = new HttpGet(urlString);

				try {
					response = httpClient.execute(httpGet);
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
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
				temp_global = res_str;
				
				
				
			return temp_global;
		}
		
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			
	
		}
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
			Toast.makeText(this, "�옱�깮 : " + songPath, Toast.LENGTH_SHORT).show();
			songProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					mHandler.removeCallbacks(mUpdateTimeTask);
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					mHandler.removeCallbacks(mUpdateTimeTask);

					int totalDuration = mp.getDuration();
					int currentPosition = (int) (((float) seekBar.getProgress() / 100) * totalDuration);

					mp.seekTo(currentPosition);
					updateProgressBar();
				}
			});
			mp.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer arg0) {
					finishSong();
				}
			});
		} catch (IOException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private void finishSong() {
		songProgressBar.setProgress(0);
		Toast.makeText(getApplicationContext(), "�걹!.", Toast.LENGTH_SHORT)
				.show();
	}
	
	/*
	 * Update timer on seekbar
	 */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}	
	
	/*
	 * Background Runnable thread
	 */
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
	
	/*
	 * Download mp4 file from server
	 */
	private class ProcessDownloadTask extends AsyncTask<Void, Void, Void> {
		HttpResponse response = null;

		@Override
		protected Void doInBackground(Void... params) {
			file = new File("/mnt/sdcard/data/" + et_target.getText().toString() +".mp4");
			if (file.exists())
				return null;

			HttpClient httpClient = new DefaultHttpClient();
			String urlString = "http://54.65.81.18:9000/upload/" + et_target.getText().toString() +".mp4";
			HttpPost httpPost = new HttpPost(urlString);
	
			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair> (2);

				nameValuePairs.add(new BasicNameValuePair("category","6"));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = httpClient.execute(httpPost);
			} catch (ClientProtocolException e) {
			} catch (IOException e) {
			}

			HttpEntity entity = response.getEntity();
				
			try {
				OutputStream outStream = new FileOutputStream(file);
			    
				entity.writeTo(outStream);
			    outStream.close();
			} catch(IOException e){
			}
			return null;
		}
	}
	
	/*
	 * Updates db.
	 */
    private class ProcessUpdateTask extends AsyncTask<Void, Void, Void> {
        HttpResponse response = null;
        StringBuilder sb = null;
        
        
        
        @Override
        protected Void doInBackground(Void... params) {
        	HttpClient httpClient = new DefaultHttpClient();
            String urlString = "http://54.65.81.18:9000/update";
            HttpPost httpPost = new HttpPost(urlString);
            saveCookie sc = new saveCookie(Main.this);
     
            try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair> (2);

            nameValuePairs.add(new BasicNameValuePair("category", "3"));
            nameValuePairs.add(new BasicNameValuePair("last_update", sc.getValue("last_update", "0000-00-00 00:00:00")));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            sc.put("last_update", (new java.sql.Timestamp(Calendar.getInstance().getTime().getTime() - (3600 * 9 * 1000 + 5000))).toString());
                response = httpClient.execute(httpPost);
            } catch (ClientProtocolException e) {
            } catch (IOException e) {
            }

            HttpEntity entity = response.getEntity(); 
            sb = new StringBuilder();
            
            try {
                BufferedReader reader = 
                       new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
                String line = null;

                while ((line = reader.readLine()) != null)
                    sb.append(line);
            }
            catch (Exception e) {
            	e.printStackTrace();
            }
              
            return null;
        }
        
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            Log.i("ASDF", "starting");
            String res_str = sb.toString();
            try {
            	articles = new JSONArray(res_str);
            } catch (JSONException e) {
            	e.printStackTrace();
            }
            
            JSONObject temp = null;
            ContentValues values = null;
            
            for (int i = 0; i < articles.length(); i++) {
            	try {
					temp = articles.getJSONObject(i);
					int temp_id = temp.getInt("id");
					getContentResolver().delete(Uri.parse("content://com.story.tirrtirr.provider/articles"), "_id = ?",new String[] {Integer.toString(temp_id)});
				
					
					values = new ContentValues(6);
					
					values.put("_id", temp_id);
	            	values.put("content", temp.getString("content"));
	            	values.put("like_count", temp.getInt("like_count"));
	            	values.put("date_db", temp.getString("date_db"));
	            	values.put("author", temp.getInt("author"));
	            	values.put("category", temp.getInt("category"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
            	getContentResolver().insert(Uri.parse("content://com.story.tirrtirr.provider/articles"), values);
            }
        }
    }
	
	private void like() {
		 new ProcessLikeTask().execute(null, null, null);
	}

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
	            BufferedReader reader =
	            		new BufferedReader(new InputStreamReader(entity.getContent()), 65728);
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
                Toast.makeText(getApplicationContext(), "Like, " + res_str, Toast.LENGTH_SHORT).show();
	        } else
	        	Toast.makeText(getApplicationContext(), "Wrong Credentials",
	        			Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public int getCategory() {
		System.out.println("카테고리 "+category_num);
		return category_num;
	}

	@Override
	public int getAuthor() {
		// TODO Auto-generated method stub
		return return_for_getauthor;
	}
}