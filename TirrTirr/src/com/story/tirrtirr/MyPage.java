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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MyPage extends Activity implements ArticlesFragment.onFragmentInteractionListener {

	TextView owner = null;
	Button follow = null;
	int view_id;
	saveCookie pref = new saveCookie(this);
	public JSONArray articles = new JSONArray();
	ArrayList<Integer> local_list = new ArrayList<Integer>();
	
	final String uploadFilePath = "/mnt/sdcard/data/";
	final String uploadFileName = "temp.m4a";
	private List<String> songs = new ArrayList<String> ();
	private MediaPlayer mp = new MediaPlayer();
	private Handler mHandler = new Handler();
	public int Position = 0;
	public SeekBar songProgressBar;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mypage);
		owner = (TextView) findViewById(R.id.tv_owner);
		follow = (Button) findViewById(R.id.bt_follow);
		
		view_id = pref.getValue("view_id", 0);
		//owner.setText(view_id);
		owner.setText(ArticleCursorAdapter.map.get(view_id) + "의 페이지");
		new ProcessUpdateTask().execute(null, null, null);
		
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.btn_record:
			Intent myIntent = new Intent(getApplicationContext(),
					Record.class);
			startActivity(myIntent);
			break;
		case R.id.btn_play:
			RelativeLayout vwParentRow = (RelativeLayout) view.getParent();
			TextView child = (TextView) vwParentRow.getChildAt(0);
			ProgressBar pro = (ProgressBar) vwParentRow.getChildAt(1);
			int index = songs.indexOf(child.getText());
			playSong(view, uploadFilePath + songs.get(index));
			break;
		case R.id.btn_like:
			RelativeLayout vwParentRow2 = (RelativeLayout) view.getParent();
			TextView child2 = (TextView) vwParentRow2.getChildAt(0);
			ImageButton likeButton = (ImageButton) vwParentRow2.getChildAt(3);
			int index2 = songs.indexOf(child2.getText());
			like();
			break;
		case R.id.btn_update:
			new ProcessUpdateTask().execute(null, null, null);
			break;
		case R.id.bt_follow:
			if(view_id == pref.getValue("id", 99)){
				Toast.makeText(getApplicationContext(), "본인은 팔로우할 수 없습니다",Toast.LENGTH_SHORT).show();
				break;
			}
			new ProcessFollowTask().execute(pref.getValue("id", 0), view_id, null);
			break;
		}
	}
	
	private class ProcessUpdateTask extends AsyncTask<Void, Void, Void> {
        HttpResponse response = null;
        StringBuilder sb = null;
        
        @Override
        protected Void doInBackground(Void... params) {
        	HttpClient httpClient = new DefaultHttpClient();
            String urlString = "http://54.65.81.18:9000/download/"+view_id;
            HttpPost httpPost = new HttpPost(urlString);
     
            try {
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
					if(local_list.contains(temp_id)){
						getContentResolver().delete(Uri.parse("content://com.story.tirrtirr.provider/articles"), "_id = ?",new String[] {Integer.toString(temp_id)});
					}
					else
						local_list.add(temp_id);
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
	
	private class ProcessFollowTask extends AsyncTask<Integer, Integer, Void> {
        HttpResponse response = null;
        StringBuilder sb = null;
        
        @Override
        protected Void doInBackground(Integer... params) {
        	HttpClient httpClient = new DefaultHttpClient();
            String urlString = "http://54.65.81.18:9000/follow/";
            HttpPost httpPost = new HttpPost(urlString);
     
            try {
            	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair> (2);

				nameValuePairs.add(new BasicNameValuePair("source",Integer.toString(params[0])));
				nameValuePairs.add(new BasicNameValuePair("destination",Integer.toString(params[1])));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            	
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
            
            if(res_str.equals("nothing"))
            	Toast.makeText(getApplicationContext(), "이미 팔로우 중입니다." ,Toast.LENGTH_SHORT).show();
            else
            	Toast.makeText(getApplicationContext(), "팔로우 되었습니다.",Toast.LENGTH_SHORT).show();
          
            
           
            
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
		// TODO Auto-generated method stub
		return 3;
	}


	@Override
	public int getAuthor() {
		// TODO Auto-generated method stub
		return view_id;
	}
	
}
