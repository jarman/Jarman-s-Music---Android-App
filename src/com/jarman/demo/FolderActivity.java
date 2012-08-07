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

import com.jarman.helpers.Helpers;

public class FolderActivity extends ListActivity {
    private static final String PATH = "/sdcard/Jarman/";
    private static final String FILENAME = "playlist.xml";
    private List<Map<String, String>> items;
    
	public String folderID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		folderID = intent.getStringExtra("ID");

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
        	items =  Helpers.ReadFolderItemsFromFile(PATH, FILENAME, folderID);
        	Log.d("doInBackground", "Completed");
        	return null;
        	
        }

        @Override public void onPostExecute(Void voids) {
            SimpleAdapter adapter = new SimpleAdapter(FolderActivity.this,
            items, android.R.layout.simple_list_item_2, new String[] {"name", "type"}, new int[] {android.R.id.text1, android.R.id.text2});

            FolderActivity.this.setListAdapter(adapter);
        	Log.d("onPostExecute", "View Adapter Set");
        	
        	final ListView lv = getListView();
        	
        	lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					
        		    String currentID = (String) items.get(position).get("id");
        		    String currentType = (String) items.get(position).get("type");
        		    
        		    if (currentType.equals("folder")) {
        		    	//start my folders activity
        		    	Intent myIntent = new Intent(FolderActivity.this, FolderActivity.class);
        		    	
        		    	myIntent.putExtra("ID", currentID);
        		    	
        		    	FolderActivity.this.startActivity(myIntent);
        		    } else {
        		    	// start the playlist activity
        		    	Intent myIntent = new Intent(FolderActivity.this, PlaylistActivity.class);
        		    	
        		    	myIntent.putExtra("ID", currentID);
        		    	
        		    	FolderActivity.this.startActivity(myIntent);
        		    }
					
				}
        		});
        }

    }
}
