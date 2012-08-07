package com.jarman.demo;

import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jarman.helpers.Helpers;

public class PlaylistActivity extends ListActivity {
    private static String ADDRESS = "http://jarman.homedns.org/songs.xml?Playlist=";
    private static final String PATH = "/sdcard/Jarman/";
    public static List<Map<String, String>> songList;
    private static List<Map<String, String>> items;
    
    private String playlistID;
    
    
    public void onCreate(Bundle savedInstanceState) {
		Log.d("OnCreate", "onCreate Called");
        super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		playlistID = intent.getStringExtra("ID");
		
        new FetchDataTask().execute();
    }


    private class FetchDataTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    	}

        public FetchDataTask() {
    		Log.d("FetchDataTask", "Called");
    		
    		// it's the constructor!
        }

		@Override
		protected Void doInBackground(Void... params) {
    		Log.d("doInBackground", "Called");
    		if (Helpers.updateFile(PATH, playlistID + ".xml"))
    		{
    			Helpers.DownloadFromUrl(ADDRESS + playlistID, PATH, playlistID + ".xml");
            	Log.d("doInBackground", "file was written");
    		}
    		else
    		{
            	Log.d("doInBackground", "file cache was used");
    		}
        	items = Helpers.ReadPlaylistItemsFromFile(PATH, playlistID + ".xml");
        	Log.d("doInBackground", "Completed");
        	return null;
        	
        }

        @Override public void onPostExecute(Void voids) {
        	Log.d("onPostExecute", "Started");
            if (items == null)
            {
            	Log.d("onPostExecute", "Null items");
            	Toast.makeText(getBaseContext(), "Internet Unavailable", Toast.LENGTH_SHORT).show();
            	onBackPressed();
            }
            else
            {
            	SimpleAdapter adapter = new SimpleAdapter(PlaylistActivity.this,
            			items, android.R.layout.simple_list_item_2, new String[] {"name", "artist"}, new int[] {android.R.id.text1, android.R.id.text2});

            	PlaylistActivity.this.setListAdapter(adapter);
            	Log.d("onPostExecute", "View Adapter Set");
        	
            	final ListView lv = getListView();

            	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            		public void onItemClick(AdapterView<?> parent, View v,
            				int position, long id) {


            			//start my folders activity
            			Intent myIntent = new Intent(PlaylistActivity.this, PlayerActivity.class);

            			myIntent.putExtra("url", (String) items.get(position).get("url"));
            			myIntent.putExtra("startTime", (String) items.get(position).get("startTime"));
            			myIntent.putExtra("artist", (String) items.get(position).get("artist"));
            			myIntent.putExtra("name", (String) items.get(position).get("name"));
            			myIntent.putExtra("playlistPosition", position);

            			songList = items;

            			PlaylistActivity.this.startActivity(myIntent);

            		}
            	});
            }
        }
    }
}
