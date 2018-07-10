package com.example.foobar.wheelroads;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.Objects;

public class Globals extends Application {
	public GoogleMap mMap;

	public RequestQueue mQueue;
	public DirectionsAPI mDapi;
	public WheelRoadsAPI mWrapi;

	public ArrayList< Road > mRoads;
	public int road_id;
	public int update_id;

	public Polyline mTemporaryPolyline;

	public Polyline mLog;
	public ArrayList< LatLng > mLogList;

	public ArrayList< Update > mUpdates;
	private int mUpdateNumber;

	// Modes
	public Mode mMode;
	public boolean mLogVisibility;
	public boolean mNewRoad;

	public ArrayList< Integer > mPostIdList;

	public void init() {
		String key = getResources().getString( R.string.google_maps_server_key );

		// Volley
		mQueue = Volley.newRequestQueue( this );

		// Directions API
		mDapi = new DirectionsAPI( this, key );

		// WheelRoads API
		String token = getSharedPreferences( "token", MODE_PRIVATE ).getString( "token", "" );

		if ( Objects.equals( token, "" ) ) {
			mWrapi = new WheelRoadsAPI( this );
		} else {
			mWrapi = new WheelRoadsAPI( this, token );
		}

		// Update Polylines
		mUpdates = new ArrayList<>();
		mUpdateNumber = 0;
		mNewRoad = false;

		// List of post_id on ListView
		mPostIdList = new ArrayList<>();

		mRoads = new ArrayList<>();
	}

	public void saveToken( String token ) {
		getSharedPreferences( "token", MODE_PRIVATE ).edit().putString( "token", token ).commit();
	}

	public void clearUpdates() {
		for ( Update update : mUpdates ) {
			update.remove();
		}

		mUpdates.clear();
	}

	public void firstUpdate() {
		if ( mUpdates.isEmpty() ) {
			throw new ArrayIndexOutOfBoundsException();
		}

		mUpdateNumber = 0;
		Update update = mUpdates.get( mUpdateNumber );
		update.enable();
		update_id = update.update_id;
	}

	public void nextUpdate() {
		Update update = mUpdates.get( mUpdateNumber );
		update.disable();

		if ( mUpdateNumber == mUpdates.size() - 1 ) {
			mUpdateNumber = 0;
		} else {
			++mUpdateNumber;
		}

		update = mUpdates.get( mUpdateNumber );
		update.enable();
		update_id = update.update_id;
	}

	public void previousUpdate() {
		Update update = mUpdates.get( mUpdateNumber );
		update.disable();

		if ( mUpdateNumber == 0 ) {
			mUpdateNumber = mUpdates.size() - 1;
		} else {
			++mUpdateNumber;
		}

		update = mUpdates.get( mUpdateNumber );
		update.enable();
		update_id = update.update_id;
	}

	public boolean isLogging() {
		return mLogVisibility;
	}

	public void removeTemporaryPolyline() {
		if ( mTemporaryPolyline != null ) {
			mTemporaryPolyline.setVisible( false );
			mTemporaryPolyline.remove();
		}
	}

	public void clearRoads() {
		for ( Road road : mRoads ) {
			road.remove();
		}

		mRoads.clear();
	}
}
