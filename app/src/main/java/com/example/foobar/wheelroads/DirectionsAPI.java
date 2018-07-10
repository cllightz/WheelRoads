package com.example.foobar.wheelroads;

import android.net.Uri;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectionsAPI {
	private Globals mGlobals;
	private String mKey;

	public DirectionsAPI( Globals globals, String key ) {
		this.mGlobals = globals;
		this.mKey = key;
	}

	public JsonObjectRequest getRequest( LatLng origin, LatLng destination, final int color ) {
		// Build an URI
		Uri.Builder builder = new Uri.Builder();
		builder.scheme( "https" );
		builder.authority( "maps.googleapis.com" );
		builder.path( "/maps/api/directions/json" );
		builder.appendQueryParameter( "origin", origin.latitude + "," + origin.longitude );
		builder.appendQueryParameter( "destination", destination.latitude + "," + destination.longitude );
		builder.appendQueryParameter( "mode", "walking" );
		builder.appendQueryParameter( "avoid", "indoor" );
		builder.appendQueryParameter( "units", "metric" );
		builder.appendQueryParameter( "key", mKey );

		Log.e( "uri", builder.build().toString() );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "volley", "success" );

				PolylineOptions options = new PolylineOptions()
														.color( color )
														.width( 10 );

				JSONArray steps = response.getJSONArray( "routes" )
														.getJSONObject( 0 )
														.getJSONArray( "legs" )
														.getJSONObject( 0 )
														.getJSONArray( "steps" );

				for ( int i = 0; i < steps.length(); ++i ) {
					String points = steps.getJSONObject( i )
															.getJSONObject( "polyline" )
															.getString( "points" );

					ArrayList< LatLng > pointList = PolylineDecoder.decodePoints( points );
					Log.e( "decoder", points );

					for ( LatLng point : pointList ) {
						options.add( point );
					}
				}

				Log.e( "poly", "a" );
				mGlobals.mTemporaryPolyline = mGlobals.mMap.addPolyline( options );
			}
		};

		Response.ErrorListener error = new Response.ErrorListener() {
			@Override
			public void onErrorResponse( VolleyError error ) {
				Log.e( "volley", "error" );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, builder.build().toString(), null, listener, error );
	}
}
