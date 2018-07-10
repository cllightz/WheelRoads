package com.example.foobar.wheelroads;

import android.util.Log;

import com.google.android.gms.maps.model.Polyline;

public class Update {
	public int update_id;
	private Polyline mPolyline;

	public Update( int update_id, Polyline polyline ) {
		this.update_id = update_id;
		this.mPolyline = polyline;
	}

	public void enable() {
		Log.e( "enable", Integer.toString( update_id ) );
		mPolyline.setVisible( true );
	}

	public  void disable() {
		Log.e( "disable", Integer.toString( update_id ) );
		mPolyline.setVisible( false );
	}

	public void remove() { mPolyline.setVisible( false ); mPolyline.remove(); }
}
