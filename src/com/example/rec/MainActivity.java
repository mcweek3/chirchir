package com.example.rec;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity  {

	public ProgressBar pb;
	public Button record;
	public Button stop;
	public MediaRecorder mediaRecorder = new MediaRecorder();
	public EditText edit_title;

	private static final String MEDIA_PATH = new String(
			"/storage/emulated/0/test/"); // ROOT 경로를 지정합니다.
	private List<String> songs = new ArrayList<String>();
	private MediaPlayer mp = new MediaPlayer();
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();

	public int Position = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pb = (ProgressBar) findViewById(R.id.progressBar);
		pb.setVisibility(ProgressBar.GONE);
		edit_title = (EditText) findViewById(R.id.title);
		edit_title.setHint("제목을 입력하세요");
		record = (Button) findViewById(R.id.record);
		stop = (Button) findViewById(R.id.stop);
		stop.setVisibility(View.GONE);

		updateSongList();

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
					mediaRecorder.setOutputFile("/storage/emulated/0/test/"
							+ title + ".mp4");
					mediaRecorder.prepare();
					mediaRecorder.start();
					Toast.makeText(MainActivity.this, "녹음을 시작합니다",
							Toast.LENGTH_SHORT).show();
				} catch (IOException ioe) {
					Toast.makeText(MainActivity.this, "IOException",
							Toast.LENGTH_SHORT).show();
					;
				}

			}
		});

		stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "녹음을 정지합니다",
						Toast.LENGTH_SHORT).show();
				pb.setVisibility(ProgressBar.GONE);
				stop.setVisibility(View.GONE);
				record.setVisibility(View.VISIBLE);
				stopProgressBarThread();
				mediaRecorder.stop();
				updateSongList();
				// mediaRecorder.release();
				// mediaRecorder = null;
			}
		});
	}

	public SeekBar songProgressBar;

	public void updateSongList() {
		File home = new File(MEDIA_PATH); // home으로 sd카드의 root를 지정합니다.
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

	public void myPlayHandler(View v) {

		// reset all the listView items background colours
		// before we set the clicked one..

		ListView lvItems = getListView();

		// get the row the clicked button is in
		RelativeLayout vwParentRow = (RelativeLayout) v.getParent();

		TextView child = (TextView) vwParentRow.getChildAt(0);
		ProgressBar pro = (ProgressBar) vwParentRow.getChildAt(1);
		int index = songs.indexOf(child.getText());
		// Position = index;
		playSong(v, MEDIA_PATH + songs.get(index));
		pro.setTag(index + "");

	}

	// List 아이템을 클릭했을 때의 event를 처리합니다.
	// protected void onListItemClick(ListView l, View v, int position, long id)
	// {
	// Position = position;
	// playSong(MEDIA_PATH + songs.get(position));
	// }

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
					Log.e("removeCallback","removeCallback");
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

	private void nextSong() {
		if (++Position >= songs.size()) {
			// 마지막 곡이 끝나면, 재생할 곡을 초기화합니다.
			Position = 0;
			TextView status = (TextView) findViewById(R.id.playStatus);
			status.setText("준비됨");
		} else {
			// 다음 곡을 재생합니다.
			Toast.makeText(getApplicationContext(), "다음 곡을 재생합니다.",
					Toast.LENGTH_SHORT).show();
			// playSong(MEDIA_PATH + songs.get(Position));
		}
	}

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
	int temp;
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = mp.getDuration();
			long currentDuration = mp.getCurrentPosition();

			// Displaying Total Duration time
			// songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			// songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
			// / Log.d("currentDuration", ""+currentDuration);
			// Log.d("totalDuration", ""+totalDuration);
			// Updating progress bar
			int progress = (int) ((float) currentDuration / totalDuration * 100);
			float progress_float = (float) ((float) currentDuration / totalDuration * 100);
			Log.d("Progress", "" + progress);
			songProgressBar.setProgress((int) progress);
			
//			songProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//				@Override
//				public void onProgressChanged(SeekBar seekBar, int progress,
//						boolean fromUser) {
//					// TODO Auto-generated method stub
//				}
//
//				@Override
//				public void onStartTrackingTouch(SeekBar seekBar) {
//					// TODO Auto-generated method stub
//					mHandler.removeCallbacks(mUpdateTimeTask);
//					Log.e("removeCallback","removeCallback");
//				}
//
//				@Override
//				public void onStopTrackingTouch(SeekBar seekBar) {
//					// TODO Auto-generated method stub
//					mHandler.removeCallbacks(mUpdateTimeTask);
//					int totalDuration = mp.getDuration();
//					int currentPosition = (int) (((float) seekBar.getProgress() / 100) * totalDuration);
//
//					// forward or backward to certain seconds
//					mp.seekTo(currentPosition);
//
//					// update timer progress again
//					updateProgressBar();
//
//				}
//						
//			});

			// Running this thread after 100 milliseconds
			if (currentDuration != totalDuration - 20)
				mHandler.postDelayed(this, 100);
		}
	};

	

	private volatile Thread theProgressBarThread1;
	public int CurrentPosition = 0;

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
					Toast.makeText(MainActivity.this, "녹음을 정지합니다",
							Toast.LENGTH_SHORT).show();
					pb.setVisibility(ProgressBar.GONE);
					stop.setVisibility(View.GONE);
					record.setVisibility(View.VISIBLE);
					mediaRecorder.stop();
					updateSongList();
					stopProgressBarThread();
				}
			}
		};
	};

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
