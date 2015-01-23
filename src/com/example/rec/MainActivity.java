package com.example.rec;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final MediaRecorder mediaRecorder = new MediaRecorder();
		
		
		Button record = (Button) findViewById(R.id.record);
		record.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				try{
					mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
					mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					mediaRecorder.setOutputFile("/storage/emulated/0/Download/record.mp4");
					mediaRecorder.prepare();
					mediaRecorder.start();
					Toast.makeText(MainActivity.this, "녹음을 시작합니다", Toast.LENGTH_SHORT).show();
				}
				catch(IOException ioe){
					Toast.makeText(MainActivity.this, "IOException", Toast.LENGTH_SHORT).show();;
				}
				
			}});
		
		Button stop = (Button) findViewById(R.id.stop);
		stop.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				mediaRecorder.stop();
				mediaRecorder.release();
				//mediaRecorder = null;
			}});
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
