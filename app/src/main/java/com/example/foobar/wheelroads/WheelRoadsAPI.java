package com.example.foobar.wheelroads;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class WheelRoadsAPI {
	private Globals mGlobals;
	private String mToken;

	private Response.ErrorListener mErrorListener;

	public WheelRoadsAPI( Globals globals, String token ) {
		this.mGlobals = globals;
		this.mToken = token;

		mErrorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse( VolleyError error ) {
				Log.e( "volley", "wheelroads error" );
			}
		};
	}

	public WheelRoadsAPI( Globals globals ) {
		this.mGlobals = globals;

		mErrorListener = new Response.ErrorListener() {
			@Override
			public void onErrorResponse( VolleyError error ) {
				Log.e( "volley", "wheelroads error" );
			}
		};

		Uri.Builder builder = getBuilder( "signup" );
		String uri = builder.build().toString();
		Log.e( "signup", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "reg", response.getString( "status" ) );
				mToken = response.getJSONObject( "data" ).getString( "token" );

				mGlobals.saveToken( mToken );

				Log.e( "signup", mToken );
			}
		};

		mGlobals.mQueue.add( new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener ) );
	}

	public JsonObjectRequest getDetailRequest( final ListView listView, final ArrayAdapter< String > adapter, final Toast toast ) {
		Uri.Builder builder = getBuilder( "detail" );
		builder.appendQueryParameter( "road_id", String.valueOf( mGlobals.road_id ) );
		builder.appendQueryParameter( "lang", "ja" );
		String uri = builder.build().toString();
		Log.e( "detail", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "detail", response.getString( "status" ) );

				mGlobals.mPostIdList.clear();

				try {
					JSONArray posts = response.getJSONArray( "data" );

					for ( int i = 0; i < posts.length(); ++i ) {
						JSONObject post = posts.getJSONObject( i );
						int post_id = post.getInt( "post_id" );
						String comment = post.getString( "comment" );
						int level = post.getInt( "level" );
						String lang = post.getString( "lang" );
						String post_time = post.getString( "post_time" );
						JSONArray category = post.getJSONArray( "category" );

						HashMap< String, Boolean > types = new HashMap<>();
						types.put( "is_steps", false );
						types.put( "is_difference", false );
						types.put( "is_steep", false );
						types.put( "is_rough", false );
						types.put( "is_narrow", false );
						types.put( "is_cant", false );
						types.put( "is_bikes", false );

						for ( int j = 0; j < category.length(); ++j ) {
							String key = category.getString( j );
							types.put( key, true );
						}

						adapter.add( new Post( post_id, comment, level, lang, post_time, types ).toString() );
						mGlobals.mPostIdList.add( post_id );
					}

					listView.setAdapter( adapter );
				} catch ( Exception e ) {
					toast.show();
				}
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getGetRequest() {
		Projection projection = mGlobals.mMap.getProjection();
		VisibleRegion region = projection.getVisibleRegion();
		LatLngBounds bounds = region.latLngBounds;
		LatLng sw = bounds.southwest;
		LatLng ne = bounds.northeast;
		String s = Double.toString( sw.latitude );
		String n = Double.toString( ne.latitude );
		String w = Double.toString( sw.longitude );
		String e = Double.toString( ne.longitude );

		Uri.Builder builder = getBuilder( "get" );
		builder.appendQueryParameter( "s", s );
		builder.appendQueryParameter( "n", n );
		builder.appendQueryParameter( "w", w );
		builder.appendQueryParameter( "e", e );

		String uri = builder.build().toString();
		Log.e( "get", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "get", response.getString( "status" ) );

				mGlobals.clearRoads();

				JSONArray roads = response.getJSONArray( "data" );

				for ( int i = 0; i < roads.length(); ++i ) {
					JSONObject road = roads.getJSONObject( i );
					int road_id     = road.getInt( "road_id" );
					int update_id   = road.getInt( "update_id" );

					double level;

					try {
						level = road.getDouble( "level" );
					} catch ( Exception e ) {
						level = -1;
					}

					PolylineOptions options = new PolylineOptions()
															.width( 10 )
															.color( MyColor.levelToColor( level ) );

					JSONArray points = road.getJSONArray( "points" );

					for ( int j = 0; j < points.length(); ++j ) {
						JSONObject point = points.getJSONObject( j );
						double lat = point.getDouble( "lat" );
						double lng = point.getDouble( "lng" );
						options.add( new LatLng( lat, lng ) );
					}

					Polyline polyline = mGlobals.mMap.addPolyline( options );
					mGlobals.mRoads.add( new Road( polyline, road_id, update_id ) );

				}
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getPostRequest( Post post, final PostActivity activity ) throws JSONException {
		Uri.Builder builder = getBuilder( "post" );
		post.append( builder );
		String uri = builder.build().toString();
		Log.e( "post", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "post", response.getString( "status" ) );
				int post_id = response.getJSONObject( "data" ).getInt( "post_id" );
				Log.e( "post", Integer.toString( post_id ) );

				activity.finish();
			}
		};

		// JSONObject parameters = post.getJsonObject( mToken );
		// Log.e( "json", parameters.toString() );

		// return new JsonObjectRequest( Request.Method.POST, uri, parameters, listener, mErrorListener );
		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getRegRequest() {
		Uri.Builder builder = getBuilder( "reg" );
		String uri = builder.build().toString();
		Log.e( "reg", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "reg", response.getString( "status" ) );
				int road_id = response.getJSONObject( "data" ).getInt( "road_id" );

				if ( mGlobals.mNewRoad ) {
					mGlobals.road_id = road_id;
				}

				Log.e( "reg", Integer.toString( road_id ) );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getReportNotExistRequest() {
		Uri.Builder builder = getBuilder( "report_not_exist" );
		builder.appendQueryParameter( "road_id", String.valueOf( mGlobals.road_id ) );
		String uri = builder.build().toString();
		Log.e( "report_not_exist", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "report_not_exist", response.getString( "status" ) );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getUpdateRequest( int roadId, final MapsActivity activity ) {
		Uri.Builder builder = getBuilder( "update" );
		builder.appendQueryParameter( "road_id", Integer.toString( roadId ) );

		List<LatLng> points = mGlobals.mTemporaryPolyline.getPoints();

		for ( LatLng point : points ) {
			builder.appendQueryParameter( "lat[]", Double.toString( point.latitude ) );
			builder.appendQueryParameter( "lng[]", Double.toString( point.longitude ) );
		}

		String uri = builder.build().toString();
		Log.e( "update", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "update", response.getString( "status" ) );
				int update_id = response.getJSONObject( "data" ).getInt( "update_id" );
				mGlobals.update_id = update_id;
				Log.e( "update",  Integer.toString( update_id ) );

				// Intent to PostActivity
				activity.startPostActivity();
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getUpdateRequest( int roadId, final MapsActivity activity, final Intent intent ) {
		Uri.Builder builder = getBuilder( "update" );
		builder.appendQueryParameter( "road_id", Integer.toString( roadId ) );

		List<LatLng> points = mGlobals.mTemporaryPolyline.getPoints();

		for ( LatLng point : points ) {
			builder.appendQueryParameter( "lat[]", Double.toString( point.latitude ) );
			builder.appendQueryParameter( "lng[]", Double.toString( point.longitude ) );
		}

		String uri = builder.build().toString();
		Log.e( "update", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "update", response.getString( "status" ) );
				int update_id = response.getJSONObject( "data" ).getInt( "update_id" );
				mGlobals.update_id = update_id;
				Log.e( "update",  Integer.toString( update_id ) );

				// Intent to PostActivity
				activity.startPostActivity();
				activity.startActivity( intent );

				mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getUpdatesRequest() {
		Uri.Builder builder = getBuilder( "updates" );
		builder.appendQueryParameter( "road_id", Integer.toString( mGlobals.road_id ) );
		String uri = builder.build().toString();
		Log.e( "updates", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				try {
					Log.e( "updates", response.getString( "status" ) );
					JSONArray updates = response.getJSONArray( "data" );

					mGlobals.clearUpdates();

					for ( int i = 0; i < updates.length(); ++i ) {
						JSONObject update = updates.getJSONObject( i );
						int update_id = update.getInt( "update_id" );

						PolylineOptions options = new PolylineOptions()
						.width( 10 )
						.color( MyColor.GREEN )
						.visible( false );

						JSONArray points = update.getJSONArray( "points" );

						for ( int j = 0; j < points.length(); ++j ) {
							JSONObject point = points.getJSONObject( j );
							double lat = point.getDouble( "lat" );
							double lng = point.getDouble( "lng" );
							options.add( new LatLng( lat, lng ) );
						}

						Polyline polyline = mGlobals.mMap.addPolyline( options );

						mGlobals.mUpdates.add( new Update( update_id, polyline ) );
					}

					mGlobals.firstUpdate();
				} catch ( Exception e ) {
					Log.e( "updates", e.toString() );
				}
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getVotePostRequest( int post_id, boolean vote ) {
		Uri.Builder builder = getBuilder( "vote_post" );
		builder.appendQueryParameter( "post_id", Integer.toString( post_id ) );
		builder.appendQueryParameter( "vote", vote ? "1" : "0" );
		String uri = builder.build().toString();
		Log.e( "vote_update", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "vote_update", response.getString( "status" ) );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	public JsonObjectRequest getVoteUpdateRequest( boolean vote ) {
		Uri.Builder builder = getBuilder( "vote_update" );
		builder.appendQueryParameter( "update_id", Integer.toString( mGlobals.update_id ) );
		builder.appendQueryParameter( "vote", vote ? "1" : "0" );
		String uri = builder.build().toString();
		Log.e( "vote_update", uri );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "vote_update", response.getString( "status" ) );
			}
		};

		return new JsonObjectRequest( Request.Method.GET, uri, null, listener, mErrorListener );
	}

	private Uri.Builder getBuilder( String name ) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme( "http" );
		builder.authority( mGlobals.getResources().getString( R.string.wheelroads_api_server_domain ) );
		builder.path( "/wheelroads/" + name + ".php" );

		if ( ! name.equals( "signup" ) /*&& ! name.equals( "post" )*/ ) {
			builder.appendQueryParameter( "token", mToken );
		}

		return builder;
	}
}
