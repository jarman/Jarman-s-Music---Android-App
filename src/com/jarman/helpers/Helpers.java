package com.jarman.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class Helpers {
	public static void DownloadFromUrl(String thisUrl, String path, String filename) { 
        Log.d("DownloadFromUrl", "url: " + thisUrl);
        Log.d("DownloadFromUrl", "path: " + path);
        Log.d("DownloadFromUrl", "filename: " + filename);
        try 
        {
            URL url = new URL(thisUrl);
            new File(path).mkdirs();
            File file = new File(path, filename);

            /* Open a connection to that URL. */
            URLConnection ucon = url.openConnection();

            Log.d("DownloadFromUrl", "about to write file");
            /*
             * Define InputStreams to read from the URLConnection.
             */
            InputStream is = ucon.getInputStream();
            try 
            {  
                OutputStream os = new FileOutputStream(file);  
                try 
                {  
                    byte[] buffer = new byte[4096];  
                    for (int n; (n = is.read(buffer)) != -1; )   
                        os.write(buffer, 0, n);  
                } finally { 
                	os.close(); 
                }
            } finally { 
            	is.close(); 
            }  
            Log.d("DownloadFromUrl", "finished");

        } 
        catch (IOException e) 
        {
                Log.d("DownloadFromUrl", "Error: " + e);
        }
    }
    
    public static List<Map<String,String>> ReadFolderItemsFromFile(String path, String filename, String folderID) {
    	
    	List<Map<String,String>> results = new ArrayList<Map<String,String>>();

    	try 
    	{
    		File fXmlFile = new File(path, filename);
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(fXmlFile);
    		doc.getDocumentElement().normalize();
    		
    		NodeList nList = null;

    		if (folderID == null)
    		{
    			// get all the children of the root
    			nList = doc.getChildNodes().item(0).getChildNodes();
    		}
    		else
    		{
    			//Log.d("ReadItemsFromFile", "Current FolderID: " + folderID);
    			
    			NodeList folderList = doc.getElementsByTagName("folder");
    			
    			for (int i = 0; i < folderList.getLength(); i++) 
    			{

    				Node curNode = folderList.item(i);
    				if (curNode.getNodeType() == Node.ELEMENT_NODE) 
    				{
    					String curNodeId = ((Element) curNode).getElementsByTagName("id").item(0).getTextContent();
    	    			//Log.d("ReadItemsFromFile", "Number of items: " + curNodeId);
    				
    					if (curNodeId.equals(folderID))
    					{
    						//Log.d("ReadItemsFromFile", "Found the folder");
    						NodeList folderChildren = curNode.getChildNodes();
    						for (int j = 0; j < folderChildren.getLength(); j++)
    						{
        						//Log.d("ReadItemsFromFile", "node name: " + folderChildren.item(j).getNodeName());
    							
	    						if (folderChildren.item(j).getNodeName().equals("contents"))
	    						{
	        						//Log.d("ReadItemsFromFile", "found the contents child");
	    							nList = folderChildren.item(j).getChildNodes();
		    						break;
	    						}
    						}
    						break;
    					}
    				}
    			}
    		}

    		if (nList != null)
    		{
    			Log.d("ReadItemsFromFile", "-----------------------");
    			Log.d("ReadItemsFromFile", "Number of items: " + nList.getLength());

    			for (int temp = 0; temp < nList.getLength(); temp++) 
    			{

    				Node nNode = nList.item(temp);
    				//Log.d("ReadItemsFromFile", temp + ". node type: " + nNode.getNodeType());
    				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    					Element eElement = (Element) nNode;

    					Node elementNameNode = eElement.getElementsByTagName("name").item(0);
    					String elementNameString = elementNameNode.getTextContent();
    					//Log.d("ReadItemsFromFile", "Name: " + elementNameString);
    					HashMap<String,String> mapElement = new HashMap<String, String>();
    					mapElement.put("name",elementNameString);
    					mapElement.put("id",eElement.getElementsByTagName("id").item(0).getTextContent());
    					mapElement.put("type", nNode.getNodeName());
    					results.add(mapElement);
    				}
    			}
    		}
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		return null;
    	}
    	return results;

    }
    
    public static List<Map<String,String>> ReadPlaylistItemsFromFile(String path, String filename) {
    	
    	List<Map<String,String>> results = new ArrayList<Map<String,String>>();

    	try 
    	{
    		File fXmlFile = new File(path, filename);
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.parse(fXmlFile);
    		doc.getDocumentElement().normalize();

    		NodeList songList = doc.getElementsByTagName("song");
			Log.d("ReadItemsFromFile", "List Length: " + songList.getLength());

    		for (int i = 0; i < songList.getLength(); i++) 
    		{

    			Node nNode = songList.item(i);
    			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
    				Element eElement = (Element) nNode;
    				
    				HashMap<String,String> mapElement = new HashMap<String, String>();
    				
    				mapElement.put("name", eElement.getElementsByTagName("name").item(0).getTextContent());
    				mapElement.put("artist",eElement.getElementsByTagName("artist").item(0).getTextContent());
    				mapElement.put("startTime", eElement.getElementsByTagName("startTime").item(0).getTextContent());
    				mapElement.put("url", eElement.getElementsByTagName("url").item(0).getTextContent());
    				
    				results.add(mapElement);
    			}
    		}
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		return null;
    	}
    	return results;

    }
    
    public static boolean updateFile(String path,String filename) {
    	File f = new File(path, filename);
    	Log.e("x", f.getAbsolutePath());
    	Log.e("x", "" + f.exists());
        long fileTime = f.lastModified();
        long curTime = System.currentTimeMillis();
        long fileAge = curTime - fileTime;

        Log.d("updateFile", "fileTime: " + fileTime);
        Log.d("updateFile", "curTime: " + curTime);
        Log.d("updateFile", "fileage: " + fileAge);
        
        // return true if file is older than an hour
        return fileAge > (1000 * 60 * 60);
        
    }
}
