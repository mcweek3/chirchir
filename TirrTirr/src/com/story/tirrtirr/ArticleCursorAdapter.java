package com.story.tirrtirr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ArticleCursorAdapter extends CursorAdapter {
	public static HashMap<Integer , String> map = new HashMap<Integer , String>();
	private static final String audioPath = Environment.getExternalStorageDirectory().getPath() + "/data/";
	private MediaPlayer mp = new MediaPlayer();
    private Handler mHandler = new Handler();
    private SeekBar ProgressBar_audio = null;
    saveCookie pref = null;
    Context context = null;
    String[] comments_context;
    ListView comment_list;
    
	public ArticleCursorAdapter(Context context, Cursor c, int flags) {
		super(context, c, flags);
		this.context = context;
		pref = new saveCookie(context);
	}
	
	@Override
	public View newView(Context _context, Cursor _cursor, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.item_list_article, parent, false);
	    int id = _cursor.getInt(_cursor.getColumnIndex("_id"));
	    updateComments(id);
	    return view;
	}
	
	public String getNickname(Integer author){
		if(map.containsKey(author) && map.get(author)!="")
			return map.get(author);
		else{
			String value = "";
			if(!map.containsValue(value)){
				value = ((Main)context).nickMid(author);
			}
			map.put(author, value);
			return value;
		}
	}

	@Override
	protected void onContentChanged() {
		// TODO Auto-generated method stub
		super.onContentChanged();
		this.notifyDataSetChanged();
	}
	
	@Override
	public void bindView(View view, Context Context, Cursor cursor) {
	    final int id = cursor.getInt(cursor.getColumnIndex("_id"));
	    String content = cursor.getString(cursor.getColumnIndex("content"));
	    int likes = cursor.getInt(cursor.getColumnIndex("like_count"));
	    final int author = cursor.getInt(cursor.getColumnIndex("author"));
	    Button btn_comment = (Button) view.findViewById(R.id.btn_comment);
	    ImageButton btn_play = (ImageButton) view.findViewById(R.id.btn_play);
	    ImageButton btn_like = (ImageButton) view.findViewById(R.id.btn_like);
	    TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
	    TextView txt_like_count = (TextView) view.findViewById(R.id.txt_like_count);
	    TextView txt_author = (TextView) view.findViewById(R.id.author);
	    final EditText comment_txt = (EditText) view
	            .findViewById(R.id.txt_comment);
	    comment_list = (ListView) view.findViewById(R.id.comment_list);

	    txt_content.setText(content);
	    txt_like_count.setText(Integer.valueOf(likes).toString());
	    txt_author.setText(getNickname(author));
	    
	    txt_author.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				pref.put("view_id", author);
//				pref.put("view_id", Integer.parseInt(author));
				Intent myIntent2 = new Intent(context,
						MyPage.class);
				context.startActivity(myIntent2);
			}
		});
	    
	    btn_comment.setOnClickListener(new View.OnClickListener() {
	         @Override
	         public void onClick(View view) {
	            new AsyncTask<Void, Void, Void>() {
	               HttpResponse response = null;

	               @Override
	               protected Void doInBackground(Void... params) {
	                  HttpClient httpClient = new DefaultHttpClient();
	                  String urlString = "http://54.65.81.18:9000/comment/"
	                        + id;
	                  HttpPost httpPost = new HttpPost(urlString);

	                  try {
	                     List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
	                           2);
	                     nameValuePairs.add(new BasicNameValuePair("id", ""
	                           + pref.getValue("id", 0)));
	                     nameValuePairs
	                           .add(new BasicNameValuePair("context",
	                                 comment_txt.getText().toString()));

	                     httpPost.setEntity(new UrlEncodedFormEntity(
	                           nameValuePairs, "utf-8"));
	                     response = httpClient.execute(httpPost);
	                  } catch (ClientProtocolException e) {
	                  } catch (IOException e) {
	                  }
	                  return null;
	               }

	               protected void onPostExecute(Void result) {
	                  super.onPostExecute(result);


	               }

	            }.execute(null, null, null);
	         }
	      });
	    
	    btn_like.setOnClickListener(new View.OnClickListener() {
	        @Override
	        public void onClick(View view) {
	        	new AsyncTask<Void, Void, Void> () {
	        		HttpResponse response = null;

	        		@Override
	        	    protected Void doInBackground(Void... params) {
	        			HttpClient httpClient = new DefaultHttpClient();
	        	        String urlString = "http://54.65.81.18:9000/like";
	        	        HttpPost httpPost = new HttpPost(urlString);
	        	   
	        	        try {
	        	        	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);

	        	            nameValuePairs.add(new BasicNameValuePair("id", Integer.valueOf(id).toString()));
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
	        		}
	        	}.execute(null, null, null);
	        }
	    });
	    
	    
	       final SeekBar bar_audio = (SeekBar) view.findViewById(R.id.bar_audio);
	       
	       // pressing Play Button
	       btn_play.setOnClickListener(new View.OnClickListener() {
	         @Override
	         public void onClick(View v) {
	            final BarUpdateRunnable run = new BarUpdateRunnable(bar_audio);
	            final File file = new File(audioPath + id + ".mp4");
	               
	            new AsyncTask<Void, Void, Void> () {
	               HttpResponse response = null;

	               @Override
	               protected Void doInBackground(Void... params) {
	                  if (!file.exists()) {
	                     HttpClient httpClient = new DefaultHttpClient();
	                     String urlString = "http://54.65.81.18:9000/upload/" + id +".mp4";
	                     HttpGet httpGet = new HttpGet(urlString);
	            
	                     try {
	                        response = httpClient.execute(httpGet);
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
	                  }
	                  return null;
	               }
	               
	                 protected void onPostExecute(Void result) {
	                     super.onPostExecute(result);
	                     
	                     try {
	                     mp.reset();
	                     mp.setDataSource(audioPath + id + ".mp4");
	                     mp.prepare();
	                     mp.start();
	                     bar_audio.setProgress(0);
	                     bar_audio.setMax(100);
	                     mHandler.removeCallbacksAndMessages(null);
	                     updateProgressBar(run);
	                     bar_audio.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
	                        @Override
	                        public void onProgressChanged(SeekBar seekBar,
	                              int progress, boolean fromUser) {
	                        }

	                        @Override
	                        public void onStartTrackingTouch(SeekBar seekBar) {
	                        }

	                        @Override
	                        public void onStopTrackingTouch(SeekBar seekBar) {
	                           mHandler.removeCallbacks(run);

	                           int totalDuration = mp.getDuration();
	                           int currentPosition = (int) (((float) seekBar.getProgress() / 100) * totalDuration);

	                           mp.seekTo(currentPosition);
	                           updateProgressBar(run);
	                        }
	                     });
	                     mp.setOnCompletionListener(new OnCompletionListener() {
	                        @Override
	                        public void onCompletion(MediaPlayer mp) {
	                           finishSong(bar_audio);
	                        }
	                     });
	                     } catch (Exception e) {
	                        e.printStackTrace();
	                     }

	                 }
	            }.execute(null, null, null);
	         }
	      });
	  }
	   
		
	 public void updateComments(final int id) {
	      new AsyncTask<Void, Void, Void>() {
	         HttpResponse response = null;
	         
	         JSONArray comments_json;
	         
	         @Override
	         protected Void doInBackground(Void... params) {
	            HttpClient httpClient = new DefaultHttpClient();
	            String urlString = "http://54.65.81.18:9000/comment_read/" + id;
	            HttpGet httpget = new HttpGet(urlString);

	            try {
	               response = httpClient.execute(httpget);
	               Log.e("updating1", "updating1");
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
	               BufferedReader reader = new BufferedReader(
	                     new InputStreamReader(entity.getContent()), 65728);
	               String line = null;

	               while ((line = reader.readLine()) != null)
	                  sb.append(line);
	            } catch (IOException e) {
	               e.printStackTrace();
	            } catch (Exception e) {
	               e.printStackTrace();
	            }
	            Log.e("updating", "updating");
	            Log.i("ASDF", sb.toString());

	            String res_str = sb.toString();
	            Log.e("comments",res_str);

	            try {
	               comments_json = new JSONArray(res_str);
	            } catch (JSONException e) {
	               e.printStackTrace();
	            }

	            JSONObject temp = null;
	            ContentValues values = null;

	            List<String> str_list = new ArrayList<String> ();
	            for (int i = 0; i < comments_json.length(); i++) {
	               try {
	                  temp = comments_json.getJSONObject(i);
	                  String temp_context = temp.getString("context");
	                  str_list.add(temp_context);
	   
	                  
	               } catch (JSONException e) {
	                  e.printStackTrace();
	               }
	            }
	            comments_context = new String[str_list.size()];
	            comments_context = str_list.toArray(comments_context);

	            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
	                  R.layout.comment_item, R.id.comment_item_text, comments_context);
	            comment_list.setAdapter(adapter); // ListView와 ArrayList를 연결합니다.
	         }

	      }.execute(null, null, null);

	   }
	
	
	   public void updateProgressBar(Runnable run) {
	      mHandler.postDelayed(run, 100);
	   }   
	   
	   private void finishSong(SeekBar bar) {
	      bar.setProgress(0);
	   }
	   
	   private class BarUpdateRunnable implements Runnable {
	      private SeekBar bar;

	      public BarUpdateRunnable(SeekBar _bar) {
	         this.bar = _bar;
	      }
	      
	      @Override
	      public void run() {
	         int totalDuration = mp.getDuration();
	         int currentDuration = mp.getCurrentPosition();
	         int progress = (int) ((float) currentDuration / totalDuration * 100);

	         bar.setProgress(progress);
	         if (currentDuration != totalDuration - 20)
	            mHandler.postDelayed(this, 100);
	      }
	   }
	   
	
}
