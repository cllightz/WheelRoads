package com.example.foobar.wheelroads;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PolylineDecoder {
	public static ArrayList< LatLng > decodePoints( String points )
	{
		ArrayList< LatLng > res = new ArrayList<>();

		try {
			for ( int index = 0, lat = 0, lng = 0; index < points.length(); ) {
				int shift = 0;
				int result = 0;

				for ( ; ; ) {
					int b = points.charAt( index ) - '?';
					++index;
					result |= ( (b & 31) << shift );
					shift += 5;

					if ( b < 32 ) {
						break;
					}
				}

				lat += ( (result & 1) != 0 ? ~(result >> 1) : result >> 1 );

				shift = 0;
				result = 0;

				for ( ; ; ) {
					int b = points.charAt( index ) - '?';
					++index;
					result |= ( (b & 31) << shift );
					shift += 5;

					if ( b < 32 ) {
						break;
					}
				}

				lng += ( (result & 1) != 0 ? ~(result >> 1) : result >> 1);

				// Add the new Lat/Lng to the Array
				res.add( new LatLng( (double)lat / 100000.0, (double)lng / 100000.0 ) );
			}

			return res;
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return res;
	}
}
