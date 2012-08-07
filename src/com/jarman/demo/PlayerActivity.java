package com.jarman.demo;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity {
	private String songUrl;
	public static MediaPlayer mediaPlayer;
	public static boolean mediaPlayerPrepared = false;
	private static boolean seekBarDragging = false;
    private static List<Map<String, String>> songList;
    private static int playlistPosition;
    AsyncTask<Void, Void, Void> updateThread;
    AsyncTask<Void, Void, Boolean> runMediaPlayerThread;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// get info sent to the activity
		Intent intent = getIntent();
		int newPlaylistPosition = intent.getIntExtra("playlistPosition", 0);
		
		List<Map<String, String>> newSongList = PlaylistActivity.songList;
		
		// set the view
		setContentView(R.layout.mediaplayer);
		
		// add some listeners
		((Button) findViewById(R.id.playButton)).setOnClickListener(new PlayPressed());
		((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(new SeekBarReleased());
		((Button) findViewById(R.id.backButton)).setOnClickListener(new BackPressed());
		((Button) findViewById(R.id.forwardButton)).setOnClickListener(new ForwardPressed());
		
		if (songList != null) {
			Log.d("songlist", newSongList.get(newPlaylistPosition).get("url"));
			Log.d("songlist", songList.get(playlistPosition).get("url"));
		}
		
		if (songList != null && newSongList.get(newPlaylistPosition).get("url").equals(songList.get(playlistPosition).get("url")))
		{
			playSong(false);
		}
		else
		{
			playlistPosition = newPlaylistPosition;
			songList = newSongList;
			playSong(true);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (updateThread != null)
		{
			updateThread.cancel(true);
		}
	}
		
		
	protected void playSong(boolean startMusic)
	{
		songUrl = songList.get(playlistPosition).get("url");
		String songName = songList.get(playlistPosition).get("name");
		String songArtist = songList.get(playlistPosition).get("artist");
		final String startTime = songList.get(playlistPosition).get("startTime");
		
		// setup the view
		((TextView) findViewById(R.id.name)).setText(songName);
		((TextView) findViewById(R.id.artist)).setText(songArtist);
		((Button) findViewById(R.id.playButton)).setText("loading...");
		((SeekBar) findViewById(R.id.seekBar)).setProgress(0);
		
		if (startMusic)
		{
			runMediaPlayerThread = new RunMediaPlayer(startTime);
			runMediaPlayerThread.execute();
		}
		else
		{
	        updateThread = new UpdateMetadata().execute();
		}
	}
	
	private class RunMediaPlayer extends AsyncTask<Void, Void, Boolean>{
		String startTime;
		
		private RunMediaPlayer(String time) {
			startTime = time;
		}
		
		public Boolean doInBackground(Void...voids) {
			mediaPlayerPrepared = false;
			if (mediaPlayer != null)
			{
				try {
					updateThread.wait(500);
				}
				catch (Exception e) {}
				
				if (updateThread != null)
				{
					updateThread.cancel(true);
				}
				mediaPlayer.release();
			}
			mediaPlayer  = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			try {
				mediaPlayer.setDataSource(songUrl);
				mediaPlayer.prepare(); // might take long! (for buffering, etc)
				mediaPlayerPrepared = true;
			}
			catch (IOException e) {
				Log.d("MediaThread", e.toString());
        		return false;
			}

			mediaPlayer.seekTo(Integer.parseInt(startTime));

			mediaPlayer.start();
	        updateThread = new UpdateMetadata().execute();
			mediaPlayer.setOnCompletionListener(new OnMusicComplete());
			return true;
		}
		
		public void onPostExecute(Boolean doInBackgroundSucceeded) {
			if (!doInBackgroundSucceeded)
			{
				Toast.makeText(getBaseContext(), "Internet Unavailable", Toast.LENGTH_SHORT).show();
				songList = null;
				onBackPressed();
			}
		}
	}
	

	private class UpdateMetadata extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			
			try {
				Thread.sleep(500);
			} catch (Exception e) {}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mediaPlayerPrepared && mediaPlayer != null && mediaPlayer.isPlaying() && !seekBarDragging) {
				int dur = mediaPlayer.getDuration();
				int cur = mediaPlayer.getCurrentPosition();
				
				// setup times
				SimpleDateFormat df = new SimpleDateFormat("m:ss");
				df.setTimeZone(TimeZone.getTimeZone("GMT+0"));
				((TextView) findViewById(R.id.totalTime)).setText(df.format(new Date(dur)));
				((TextView) findViewById(R.id.currentTime)).setText(df.format(new Date(cur)));
				
				// update progress bar
				((SeekBar) findViewById(R.id.seekBar)).setProgress(cur);
				((SeekBar) findViewById(R.id.seekBar)).setMax(dur);
				
				// update other buttons
				((TextView) findViewById(R.id.playButton)).setText("Pause");
			}
			try {
				notify();
				Log.d("UpdateMetadata", "something was notified");
			} catch (Exception e) {}
			
	        new UpdateMetadata().execute();
			
		}
		
	}
	
	private class PlayPressed implements View.OnClickListener {

		public void onClick(View arg0) {
			if (mediaPlayer != null && mediaPlayer.isPlaying())
			{
				mediaPlayer.pause();
				((Button) findViewById(R.id.playButton)).setText(" Play ");
			}
			else if (mediaPlayerPrepared && mediaPlayer != null)
			{
				mediaPlayer.start();
				((Button) findViewById(R.id.playButton)).setText("Pause");
			}
			
		}
		
	}	
	
	private class BackPressed implements View.OnClickListener {
		public void onClick(View arg0) {
			playlistPosition = (playlistPosition - 1 + songList.size()) % songList.size();
			playSong(true);
		}
	}	
	
	private class ForwardPressed implements View.OnClickListener {
		public void onClick(View arg0) {
			playlistPosition = (playlistPosition + 1) % songList.size();
			playSong(true);
		}
	}	

	private class SeekBarReleased implements OnSeekBarChangeListener {

		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}

		public void onStartTrackingTouch(SeekBar bar) {
			seekBarDragging = true;
		}

		public void onStopTrackingTouch(SeekBar bar) {
			seekBarDragging = false;
			// TODO set this up so it handles states when the media player isn't ready yet
			if (mediaPlayerPrepared)
			{
				mediaPlayer.seekTo(bar.getProgress());
			}
		}
		
	}
	
	private class OnMusicComplete implements MediaPlayer.OnCompletionListener {

		public void onCompletion(MediaPlayer mp) {
			playlistPosition = (playlistPosition + 1) % songList.size();
			playSong(true);
		}
	}
}
