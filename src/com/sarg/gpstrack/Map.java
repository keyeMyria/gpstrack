package com.sarg.gpstrack;

import java.io.File;

import java.io.IOException;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity; 
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class Map extends MapActivity {
    private static final int MENU_TRACK = 0;
	private static final int MENU_PLAY = 1;
	private static final int MENU_POSITION = 2;
	private static final int MENU_LOAD_TRACK = 3;
	private MyLocationOverlay myLocation;
	
	LocationManager mLocationManager;
	LocationListener mLocationListener;
	
	MapView mapView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Debug.startMethodTracing("gpstrack");
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);           
        
        mapView = (MapView) findViewById(R.id.mapview);
        myLocation = new MyLocationOverlay(this, mapView);
        myLocation.enableMyLocation();
        myLocation.enableCompass();
        
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);
 
        mapView.getOverlays().add(myLocation);
        mapView.getOverlays().add(new TrackOverlay("/sdcard/default.gpx"));
    }
    
    @Override
    protected void onDestroy() {
    	//Debug.stopMethodTracing(); 
    	super.onDestroy();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_TRACK, 0, "New track").setIntent(new Intent(this, com.sarg.gpstrack.Menu.class));
    	
    	Intent fileChooser = new Intent(Intent.ACTION_GET_CONTENT);
    	fileChooser.addCategory(Intent.CATEGORY_OPENABLE);
    	fileChooser.setType("*/*");
    	menu.add(0, MENU_LOAD_TRACK, 0, "Load track").setIntent(Intent.createChooser(fileChooser, null));
    	menu.add(0, MENU_PLAY, 0, "Play");
    	menu.add(0, MENU_POSITION, 0, "My position");
    	
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub
    	switch (item.getItemId()) {
		case MENU_POSITION:
			GeoPoint p = myLocation.getMyLocation();
			if (p != null) {
				mapView.getController().animateTo(p);
			}
			break;
		}
    	return super.onMenuItemSelected(featureId, item);
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
	class TrackOverlay extends Overlay {
		private GeoPoint[] track;
		private Paint paint;
		
		private class DownloadTrack extends AsyncTask<String, Integer, GeoPoint[]> {

			@Override
			protected GeoPoint[] doInBackground(String... params) {
				try {
					return GpxReader.readTrack(new File(params[0]));
				} catch (IOException e) {
					Log.println(Log.DEBUG, "SARG", e.toString());
					return null;
				}
			}
			
			@Override
			protected void onPostExecute(GeoPoint[] result) {
				if (result != null) {
					track = result;
				} else {
					Toast.makeText(
							getApplicationContext(), 
							"Failed to load track", 
							Toast.LENGTH_LONG
					).show();
				}
			}
		}

		private Path trackToPath(GeoPoint[] track, Projection p) {
			Path path = new Path();
			
			if (track.length > 0) {
				Point start = p.toPixels(track[0], null);
				path.moveTo(start.x, start.y);
				
				for (int i = 1; i < track.length; i++) {
					Point pnt = p.toPixels(track[i], null);
					path.lineTo(pnt.x, pnt.y);
				}
			}
			
			return path;
		}
		
		public TrackOverlay(String fileName) {
			new DownloadTrack().execute(fileName);
			
			paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(4);
            paint.setARGB(255, 255, 80, 80);
            paint.setPathEffect(new CornerPathEffect(4));
		}
		
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if (track != null) {
				canvas.drawPath(
						trackToPath(track, mapView.getProjection()),
						paint
				);
			}
			super.draw(canvas, mapView, shadow);
		}	
	}
	
	GeoPoint Location2GeoPoint(Location location) {
		return new GeoPoint(
				(int)(location.getLatitude()*1E6), 
				(int)(location.getLongitude()*1E6)
		);
	}
}