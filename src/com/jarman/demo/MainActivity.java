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

public class MainActivity extends ListActivity {
    private static final String ADDRESS = "http://jarman.homedns.org/playlists.xml";
    private static final String PATH = "/sdcard/Jarman/";
    private static final String FILENAME = "playlist.xml";
    private List<Map<String, String>> items;
    
    
    public void onCreate(Bundle savedInstanceState) {
		Log.d("OnCreate", "onCreate Called");
        super.onCreate(savedInstanceState);

        new FetchDataTask().execute();
    }
    
    
    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (PlayerActivity.mediaPlayer != null)
		{
			PlayerActivity.mediaPlayerPrepared = false;
			PlayerActivity.mediaPlayer.release();
		}
	}


    private class FetchDataTask extends AsyncTask<Void, Void, Void> {
    	
    	@Override
    	protected void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    	}

		@Override
		protected Void doInBackground(Void... params) {
    		Log.d("doInBackground", "Called");
    		if (Helpers.updateFile(PATH, FILENAME))
    		{
    			Helpers.DownloadFromUrl(ADDRESS, PATH, FILENAME);
            	Log.d("doInBackground", "file was written");
    		}
    		else
    		{
            	Log.d("doInBackground", "not updating the file");
    		}
        	items = Helpers.ReadFolderItemsFromFile(PATH, FILENAME, null);
        	Log.d("doInBackground", "Completed");
        	return null;
        	
        }

        @Override public void onPostExecute(Void voids) {
            if (items == null)
            {
            	Toast.makeText(getBaseContext(), "Internet Unavailable", Toast.LENGTH_SHORT);
            }
            else
            {
            	SimpleAdapter adapter = new SimpleAdapter(MainActivity.this,
            			items, android.R.layout.simple_list_item_2, new String[] {"name", "type"}, new int[] {android.R.id.text1, android.R.id.text2});

            	MainActivity.this.setListAdapter(adapter);
            	Log.d("onPostExecute", "View Adapter Set");

            	final ListView lv = getListView();

            	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            		public void onItemClick(AdapterView<?> parent, View v,
            				int position, long id) {

            			String currentID = (String) items.get(position).get("id");
            			String currentType = (String) items.get(position).get("type");

            			if (currentType.equals("folder")) {
            				//start my folders activity
            				Intent myIntent = new Intent(MainActivity.this, FolderActivity.class);

            				myIntent.putExtra("ID", currentID);

            				MainActivity.this.startActivity(myIntent);
            			} else {
            				// start the playlist activity
            				Intent myIntent = new Intent(MainActivity.this, PlaylistActivity.class);

            				myIntent.putExtra("ID", currentID);

            				MainActivity.this.startActivity(myIntent);
            			}

            		}
            	});
            }
        }

    }
}