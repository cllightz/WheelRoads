package com.example.foobar.wheelroads;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

public class Road {
	private Polyline mPolyline;
	public int road_id;
	public int update_id;

	public Road( Polyline polyline, int road_id, int update_id ) {
		mPolyline = polyline;
		this.road_id = road_id;
		this.update_id = update_id;
	}

	public void remove() { mPolyline.setVisible( false ); mPolyline.remove(); }

	public List<LatLng> getPath() { return mPolyline.getPoints(); }
}
