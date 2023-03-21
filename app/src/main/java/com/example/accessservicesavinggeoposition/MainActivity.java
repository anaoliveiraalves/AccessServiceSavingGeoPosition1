package com.example.accessservicesavinggeoposition;

/*Exemplo de uma pequena aplicação que guarda cada localização e o instante temporal em que foi visitada.
 * Caso haja acesso WiFi obtém informação sobre que endereço se trata - GeoCoding (Geonames service)
 */


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;



@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity {
	private BufferedWriter bw = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//Location 
		// Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
		// Here, thisActivity is the current activity

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		//LocationProvider provider = locationManager.getProvider(LocationManager.GPS_PROVIDER);;
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setCostAllowed(false);
		String providerName = locationManager.getBestProvider(criteria, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                }
            }
        }
        // If no suitable provider is found, null is returned.
		if (providerName == null) {	
			Toast.makeText(getApplicationContext(), "Error: No location provider", Toast.LENGTH_SHORT).show();
		}
		else{
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		            1000,          // 1-second interval.
		            10,             // 10 meters.	            
		            listener);
		}
	}
	protected void onDestroy(Bundle savedInstanceState){
			closeAndFlushFile();
	}
	
	@SuppressLint("NewApi")
	final LocationListener listener = new LocationListener() {

	    @Override
	    public void onLocationChanged(Location location) {
	            // A new location update is received.  Do something useful with it.  In this case,
	            // we're sending the update to a handler which then updates the UI with the new
	            // location.
	    	
	    	
	         
	         String text= "";
	    
	    
	        	 //Test if there is WiFi connection
	        	
	       	 // Gets the URL from the UI's text field.
	               String stringUrl = "http://api.geonames.org/findNearbyPlaceName?lat="+location.getLatitude()+"&lng="+location.getLongitude()+"&username=meiaistudent";
	               ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	               NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	               if (networkInfo != null && networkInfo.isConnected()) {
	                   new DownloadWebpageText().execute(stringUrl);
	               }





                           text="Location:"+location.getLatitude()+","+location.getLongitude();
	    	     //Display position or address depending on WiFi enabled
	    	    TextView output = (TextView) findViewById(R.id.textView2);
	    	    output.setText(text);

	    	    if(bw != null){
	    	    	try {
	    	    		Log.d("Writing file:",text);
						bw.write(text);
						bw.newLine();	
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        		        	
	    	    }
	    }

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
	       
	    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
		public void registarEmFicheiro(View view){
			abrirFicheiro(true);
		}
		public void abrirFicheiro(boolean append){
			boolean mExternalStorageAvailable = false;
			boolean mExternalStorageWriteable = false;
			String state = Environment.getExternalStorageState();
	
			if (Environment.MEDIA_MOUNTED.equals(state)) {
			    // We can read and write the media
			    mExternalStorageAvailable = mExternalStorageWriteable = true;
			} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			    // We can only read the media
			    mExternalStorageAvailable = true;
			    mExternalStorageWriteable = false;
			} else {
			    // Something else is wrong. It may be one of many other states, but all we need
			    //  to know is we can neither read nor write
			    mExternalStorageAvailable = mExternalStorageWriteable = false;
			}
	    	if(bw == null && mExternalStorageAvailable && mExternalStorageWriteable ){
	    		
	        	File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "valores.txt");     	
				try {
					bw = new BufferedWriter(new FileWriter(file, append));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	//Nao esquecer:  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    	}
	
		}
			
			
			
		
		private void closeAndFlushFile(){
			if(bw != null){
				try {
					bw.close();
					bw.flush();
					bw = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        	
			}
		}
		public void paraRegistoEmFicheiro(View view){
			closeAndFlushFile();
		}
		public void limparRegistoFicheiro(View view){
			closeAndFlushFile();
			abrirFicheiro(false);
			
		}
		 private class DownloadWebpageText extends AsyncTask <String,String, String> {
		        protected String doInBackground(String... urls) {
		              
		            // params comes from the execute() call: params[0] is the url.
		            try {
		                return downloadUrl(urls[0]);
		            } catch (IOException e) {
		                return "Unable to retrieve web page. URL may be invalid.";
		            }
		        }
		        // onPostExecute displays the results of the AsyncTask.
		        protected void onPostExecute(String result) {
	        	TextView textView = (TextView) findViewById(R.id.textView3);
	            textView.setText(result);
					if(bw != null){
						try {
							bw.write(result);
							bw.newLine();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
		
	    
	}
	 private String downloadUrl(String myurl) throws IOException {
		    InputStream is = null;
		    // Only display the first 500 characters of the retrieved
		    // web page content.
		    int len = 500;
		        
		    try {
		        URL url = new URL(myurl);
		        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		        conn.setReadTimeout(10000 /* milliseconds */);
		        conn.setConnectTimeout(15000 /* milliseconds */);
		        conn.setRequestMethod("GET");
		        conn.setDoInput(true);
		        // Starts the query
		        conn.connect();
		        //int response = conn.getResponseCode(); 
		       // Log.d(DEBUG_TAG, "The response is: " + response); Toast.makeText(getApplicationContext(),  "The response is: " + response, Toast.LENGTH_SHORT).show();
		        is = conn.getInputStream();

		        // Convert the InputStream into a string
		        String contentAsString = readIt(is, len);
		        return contentAsString;
		        
		    // Makes sure that the InputStream is closed after the app is
		    // finished using it.
		    } finally {
		        if (is != null) {
		            is.close();
		        } 
		    }
		}
	 
	// Reads an InputStream and converts it to a String.
	 public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	     Reader reader = null;
	     reader = new InputStreamReader(stream, "UTF-8");        
	     char[] buffer = new char[len];
	     reader.read(buffer);
	     return new String(buffer);
	 }
		 }
}
