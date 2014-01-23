package lets.taxi.onlinetaksimetre;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	// Google Map
	private GoogleMap googleMap;
	Button button;
	ArrayList<Marker> markerList = new ArrayList<Marker>();
	List<Polyline> lines;
	TextView distanceText ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		distanceText = (TextView) findViewById(R.id.distance);
		
		try {
			// Loading map
			initilizeMap();

		} catch (Exception e) {
			e.printStackTrace();
		}

		button = (Button) findViewById(R.id.button1);
		lines = new ArrayList<Polyline>();
	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
		googleMap.setMyLocationEnabled(true);

		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				if (markerList.size() <2){
				
					MarkerOptions marker = new MarkerOptions().position(
							new LatLng(arg0.latitude, arg0.longitude)).title(
							"Hello Maps ");
					marker.draggable(true);
					
					// adding marker
					markerList.add(googleMap.addMarker(marker));
					
			}

			}
		});

		
		
		googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			
			@Override
			public void onMarkerDragStart(Marker arg0) {
				markerList.remove(arg0);
				
			}
			
			@Override
			public void onMarkerDragEnd(Marker arg0) {
				markerList.add(arg0);
				if(markerList.size()==2)
					button.callOnClick();
			}
			
			@Override
			public void onMarkerDrag(Marker arg0) {
				
			}
		});
		
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this,String.valueOf(markerList.size()), Toast.LENGTH_LONG).show();

				Location loc1 = new Location("point 1");
				Location loc2 = new Location("point 2");
				
				loc1.setLatitude(markerList.get(0).getPosition().latitude);
				loc1.setLongitude(markerList.get(0).getPosition().longitude);
				
				loc2.setLatitude(markerList.get(1).getPosition().latitude);
				loc2.setLongitude(markerList.get(1).getPosition().longitude);
				
				distanceText.setText(String.valueOf(loc1.distanceTo(loc2)));
				
				String url = makeURL(markerList.get(0).getPosition().latitude,
						markerList.get(0).getPosition().longitude, markerList
								.get(1).getPosition().latitude,
						markerList.get(1).getPosition().longitude);
				
				connectAsyncTask as = new connectAsyncTask(url);
				
				as.execute();
			}
		});

	}
	
	private boolean markerEquals(MarkerOptions marker1 , Marker marker2){
		if(marker1.getPosition().latitude != marker2.getPosition().latitude)
			return false;
		if(marker1.getPosition().longitude != marker2.getPosition().longitude)
			return false;
		
		return true;
	}

	public String makeURL(double sourcelat, double sourcelog, double destlat,
			double destlog) {
		StringBuilder urlString = new StringBuilder();
		urlString.append("http://maps.googleapis.com/maps/api/directions/json");
		urlString.append("?origin=");// from
		urlString.append(Double.toString(sourcelat));
		urlString.append(",");
		urlString.append(Double.toString(sourcelog));
		urlString.append("&destination=");// to
		urlString.append(Double.toString(destlat));
		urlString.append(",");
		urlString.append(Double.toString(destlog));
		urlString.append("&sensor=false&mode=driving&alternatives=true");
		return urlString.toString();
	}

	public void drawPath(String result) {

		try {
			Log.d("distance", result);
			
			// Tranform the string into a json object
			final JSONObject json = new JSONObject(result);
			JSONArray routeArray = json.getJSONArray("routes");
			JSONObject routes = routeArray.getJSONObject(0);
			JSONObject overviewPolylines = routes
					.getJSONObject("overview_polyline");
			String encodedString = overviewPolylines.getString("points");
			
			
			List<LatLng> list = decodePoly(encodedString);

			for(Polyline line: lines){
				line.remove();
			}
			lines.clear();
			
			for (int z = 0; z < list.size() - 1; z++) {
				LatLng src = list.get(z);
				LatLng dest = list.get(z + 1);

				
				Polyline line = googleMap.addPolyline(new PolylineOptions()
						.add(new LatLng(src.latitude, src.longitude),
								new LatLng(dest.latitude, dest.longitude))
						.width(8).color(Color.RED
								).geodesic(true));
				lines.add(line);
				
			}

		} catch (JSONException e) {

		}
	}

	private List<LatLng> decodePoly(String encoded) {

		List<LatLng> poly = new ArrayList<LatLng>();
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			LatLng p = new LatLng((((double) lat / 1E5)),
					(((double) lng / 1E5)));
			poly.add(p);
		}

		return poly;
	}

	private class connectAsyncTask extends AsyncTask<Void, Void, String> {
		private ProgressDialog progressDialog;
		String url;

		connectAsyncTask(String urlPass) {
			url = urlPass;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressDialog = new ProgressDialog(MainActivity.this);
			progressDialog.setMessage("Fetching route, Please wait...");
			progressDialog.setIndeterminate(true);
			progressDialog.show();
		}

		@Override
		protected String doInBackground(Void... params) {
			JSONParser jParser = new JSONParser();
			String json = jParser.getJSONFromUrl(url);
			return json;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progressDialog.hide();
			if (result != null) {
				drawPath(result);
			}
		}
	}
}