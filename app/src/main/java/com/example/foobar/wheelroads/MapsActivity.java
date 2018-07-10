package com.example.foobar.wheelroads;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
	public Globals mGlobals;

	private LocationManager mManager;

	private boolean mGPSChanged;

	private LatLng mOrigin;
	private Marker mMarkerOrigin;
	private LatLng mDestination;
	private Marker mMarkerDestination;

	private JsonObjectRequest mTmpRequest;

	private FloatingActionButton mFabAdd;
	private FloatingActionButton mFabCancel;
	private FloatingActionButton mFabPrevious;
	private FloatingActionButton mFabNext;
	private FloatingActionButton mFabFinish;

	private boolean mMapReady;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_maps );

		mGlobals = (Globals)this.getApplication();
		mGlobals.init();

		Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		// FAB
		mFabAdd = (FloatingActionButton)findViewById( R.id.fab_add );
		mFabAdd.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				switch ( mGlobals.mMode ) {
					case INITIAL:
						mGlobals.mNewRoad = true;
						break;

					case UPDATES:
						mGlobals.mNewRoad = false;
						mGlobals.clearUpdates();
						break;
				}

				setMode( Mode.ORIGIN );
				mGlobals.mQueue.add( mGlobals.mWrapi.getRegRequest() );
			}
		} );

		mFabCancel = (FloatingActionButton)findViewById( R.id.fab_cancel );
		mFabCancel.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				switch ( mGlobals.mMode ) {
					case ORIGIN:
						mMarkerOrigin.setVisible( false );
						mMarkerDestination.setVisible( false );
						mGlobals.removeTemporaryPolyline();
						break;

					case UPDATES:
						mGlobals.clearUpdates();
						break;
				}

				setMode( Mode.INITIAL );
			}
		} );

		mFabPrevious = (FloatingActionButton)findViewById( R.id.fab_previous );
		mFabPrevious.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				switch ( mGlobals.mMode ) {
					case DESTINATION:
						setMode( Mode.ORIGIN );
						mMarkerDestination.setVisible( false );
						mGlobals.removeTemporaryPolyline();
						break;

					case UPDATES:
						mGlobals.previousUpdate();
						break;
				}
			}
		} );

		mFabNext = (FloatingActionButton)findViewById( R.id.fab_next );
		mFabNext.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				switch ( mGlobals.mMode ) {
					case ORIGIN:
						setMode( Mode.DESTINATION );
						break;

					case UPDATES:
						mGlobals.nextUpdate();
						break;
				}
			}
		} );

		mFabFinish = (FloatingActionButton)findViewById( R.id.fab_finish );
		mFabFinish.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				setMode( Mode.INITIAL );

				mMarkerOrigin.setVisible( false );
				mMarkerDestination.setVisible( false );
				mGlobals.removeTemporaryPolyline();

				if ( mGlobals.mNewRoad ) {
					Intent intent = new Intent( MapsActivity.this, PostActivity.class );
					intent.putExtra( "new", true );

					mGlobals.mQueue.add( mGlobals.mWrapi.getUpdateRequest( mGlobals.road_id, MapsActivity.this, intent ) );
				} else {
					mGlobals.mQueue.add( mGlobals.mWrapi.getUpdateRequest( mGlobals.road_id, MapsActivity.this ) );
				}
			}
		} );

		// Map
		SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
												.findFragmentById( R.id.map );

		mapFragment.getMapAsync( this );

		// LocationManager
		mManager = (LocationManager)getSystemService( LOCATION_SERVICE );

		LocationListener mListener = new LocationListener() {
			@Override
			public void onLocationChanged( Location location ) {
				double lat = location.getLatitude();
				double lng = location.getLongitude();
				LatLng current = new LatLng( lat, lng );
				Log.e( "GPS", "changed: " + lat + ", " + lng );

				// GPSを初めて掴んだ時にカメラを移動
				if ( !mGPSChanged ) {
					// Move Camera
					float zoom = 17.0f;
					float tilt = 0.0f;
					float bear = 0.0f;
					CameraPosition position = new CameraPosition( current, zoom, tilt, bear );
					CameraUpdate update = CameraUpdateFactory.newCameraPosition( position );
					mGlobals.mMap.animateCamera( update );

					mGPSChanged = true;
				}

				// mLogを更新
				if ( mGlobals.isLogging() ) {
					mGlobals.mLogList.add( current );
					PolylineOptions options = new PolylineOptions().color( MyColor.CYAN );

					for ( LatLng point : mGlobals.mLogList ) {
						options.add( point );
					}

					if ( mGlobals.mLog != null ) {
						mGlobals.mLog.remove();
					}

					mGlobals.mLog = mGlobals.mMap.addPolyline( options );
				}
			}

			@Override
			public void onStatusChanged( String provider, int status, Bundle extras ) {
				switch ( status ) {
					case LocationProvider.AVAILABLE:
						break;

					case LocationProvider.OUT_OF_SERVICE:
						break;

					case LocationProvider.TEMPORARILY_UNAVAILABLE:
						break;
				}
			}

			@Override
			public void onProviderEnabled( String provider ) {
			}

			@Override
			public void onProviderDisabled( String provider ) {
			}
		};

		// Permission Check
		if ( ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
			return;
		}

		mManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1_000/*ms*/, 1/*m*/, mListener );
		mManager.getProvider( LocationManager.GPS_PROVIDER );

		// Other
		mGPSChanged = false;
		mMapReady = false;

		// All Roads
		mGlobals.mRoads = new ArrayList<>();
	}

	@Override
	protected void onResume() {
		if ( mMapReady ) {
			mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
		}

		super.onResume();
	}

	@Override
	public boolean onPrepareOptionsMenu( Menu menu ) {
		menu.clear();

		String text = mGlobals.mLogVisibility ? "ロギングを終了" : "ロギングを開始";
		menu.add( 0, Menu.FIRST, Menu.NONE, text );

		return super.onCreateOptionsMenu( menu );
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		if ( item.getItemId() == Menu.FIRST ) {
			if ( mGlobals.mLogVisibility ) {
				if ( 1 < mGlobals.mLogList.size() ) {
					setMode( Mode.INITIAL );
				}
			} else {
				mGlobals.mLogList = new ArrayList<>();
			}

			setLogMode( !mGlobals.mLogVisibility );
		}

		return super.onOptionsItemSelected( item );
	}

	@Override
	public void onMapReady( GoogleMap googleMap ) {
		mGlobals.mMap = googleMap;

		setLogMode( false );
		setMode( Mode.INITIAL );

		// Set Map Visual
		mGlobals.mMap.setMapType( GoogleMap.MAP_TYPE_TERRAIN );

		// Disable Rotate & Tilt
		UiSettings settings = mGlobals.mMap.getUiSettings();
		settings.setTiltGesturesEnabled( false );
		settings.setRotateGesturesEnabled( false );

		// Enable My Location Layer
		try {
			mGlobals.mMap.setMyLocationEnabled( true );
		} catch ( Exception e ) {
			Log.e( "Exception", "setMyLocation" );
			mGlobals.mMap.setMyLocationEnabled( false );
		}

		// Disable Indoor Layer
		mGlobals.mMap.setIndoorEnabled( false );
		mGlobals.mMap.getUiSettings().setIndoorLevelPickerEnabled( false );

		// Disable Compass
		mGlobals.mMap.getUiSettings().setCompassEnabled( false );

		// Disable Map Toolbar
		mGlobals.mMap.getUiSettings().setMapToolbarEnabled( false );

		mGlobals.mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener() {
			@Override
			public void onCameraChange( CameraPosition cameraPosition ) {
				if ( mGlobals.mMode != Mode.UPDATES ) {
					mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				}
			}
		} );

		float zoom = 1.0f;
		float tilt = 0.0f;
		float bear = 0.0f;
		LatLng defaultLocation = new LatLng( 35.0, 139.0 );
		CameraPosition defaultPosition = new CameraPosition( defaultLocation, zoom, tilt, bear );
		CameraUpdate defaultUpdate = CameraUpdateFactory.newCameraPosition( defaultPosition );
		mGlobals.mMap.moveCamera( defaultUpdate );

		// Move Camera & Render Circle
		LatLng latlng = new LatLng( 35.681061, 139.767096 );
		zoom = 10.0f;

		try {
			if ( ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
				return;
			}

			Location location = mManager.getLastKnownLocation( mManager.getBestProvider( new Criteria(), true ) );
			latlng = new LatLng( location.getLatitude(), location.getLongitude() );
			zoom = 15.0f;

			mGPSChanged = true;
		} catch ( Exception e ) {
			Log.e( "getLastKnownLocation", "Exception" );

			mGPSChanged = false;
		}

		CameraPosition position = new CameraPosition( latlng, zoom, tilt, bear );
		CameraUpdate update = CameraUpdateFactory.newCameraPosition( position );
		mGlobals.mMap.moveCamera( update );

		MarkerOptions options = new MarkerOptions()
			.position( latlng )
			.visible( false )
			.draggable( false );

		mMarkerOrigin      = mGlobals.mMap.addMarker( options );
		mMarkerDestination = mGlobals.mMap.addMarker( options );

		mGlobals.mMap.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick( Marker marker ) {
				return true;
			}
		} );

		// Set OnClickListener of Polylines
		GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick( LatLng clicked ) {
				switch ( mGlobals.mMode ) {
					case INITIAL:
						for ( int i = 0; i < mGlobals.mRoads.size(); ++i ) {
							Road road = mGlobals.mRoads.get( i );

							if ( PolyUtil.isLocationOnPath( clicked, road.getPath(), true, 20.0 ) ) {
								// Intent -> Road DetailActivity
								// Log.e( "tap", "polyline" );
								mGlobals.road_id = road.road_id;
								startActivity( new Intent( MapsActivity.this, DetailActivity.class ) );
							}
						}

						break;

					case ORIGIN:
						mOrigin = clicked;
						mMarkerOrigin.setVisible( true );
						mMarkerOrigin.setPosition( clicked );
						break;

					case DESTINATION:
						mDestination = clicked;
						mMarkerDestination.setVisible( true );
						mMarkerDestination.setPosition( clicked );

						if ( mGlobals.mTemporaryPolyline != null ) {
							mGlobals.removeTemporaryPolyline();
						}

						// Directions API
						mTmpRequest = mGlobals.mDapi.getRequest( mOrigin, mDestination, MyColor.GREEN );
						mGlobals.mQueue.add( mTmpRequest );

						break;

					case UPDATES:
						DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick( DialogInterface dialog, int which ) {
								mGlobals.mQueue.add( mGlobals.mWrapi.getVoteUpdateRequest( true ) );
							}
						};

						DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick( DialogInterface dialog, int which ) {
								mGlobals.mQueue.add( mGlobals.mWrapi.getVoteUpdateRequest( false ) );
							}
						};

						AlertDialog.Builder builder = new AlertDialog.Builder( MapsActivity.this );
						builder.setTitle( "投票" );
						builder.setMessage( "この多角線の候補を評価してください。" );
						builder.setPositiveButton( "高評価", positiveListener );
						builder.setNegativeButton( "低評価", negativeListener );
						builder.setNeutralButton( "キャンセル", null );
						builder.show();

						break;
				}
			}
		};

		mGlobals.mMap.setOnMapClickListener( onMapClickListener );

		// Set OnLongClickListener of Polylines
		GoogleMap.OnMapLongClickListener onMapLongClickListener = new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick( LatLng clicked ) {
				// Log.e( "long tap", "tapped" );

				if ( mGlobals.mMode == Mode.INITIAL ) {
					for ( int i = 0; i < mGlobals.mRoads.size(); ++i ) {
						final Road road = mGlobals.mRoads.get( i );

						if ( PolyUtil.isLocationOnPath( clicked, road.getPath(), true, 20.0 ) ) {
							DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick( DialogInterface dialog, int which ) {
									// Intent -> Road DetailActivity
									mGlobals.road_id = road.road_id;
									setMode( Mode.UPDATES );
								}
							};

							DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick( DialogInterface dialog, int which ) {
									mGlobals.road_id = road.road_id;
									mGlobals.mQueue.add( mGlobals.mWrapi.getReportNotExistRequest() );
								}
							};

							AlertDialog.Builder builder = new AlertDialog.Builder( MapsActivity.this );
							builder.setTitle( "操作" );
							builder.setMessage( "この区間に対する操作を選んでください。" );
							builder.setPositiveButton( "この区間の範囲の候補を表示", positiveListener );
							builder.setNegativeButton( "すでに無いことを報告", negativeListener );
							builder.setNeutralButton( "キャンセル", null );
							builder.show();
						}
					}
				}
			}
		};

		mGlobals.mMap.setOnMapLongClickListener( onMapLongClickListener );
		mMapReady = true;
	}

	public void setMode( Mode mode ) {
		mGlobals.mMode = mode;

		switch ( mode ) {
			case INITIAL:
				mFabAdd     .setVisibility( View.VISIBLE );
				mFabCancel  .setVisibility( View.GONE );
				mFabPrevious.setVisibility( View.GONE );
				mFabNext    .setVisibility( View.GONE );
				mFabFinish  .setVisibility( View.GONE );
				setLogVisibility( true );
				mGlobals.mNewRoad = false;
				mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				break;

			case ORIGIN:
				mFabAdd     .setVisibility( View.GONE );
				mFabCancel  .setVisibility( View.VISIBLE );
				mFabPrevious.setVisibility( View.GONE );
				mFabNext    .setVisibility( View.VISIBLE );
				mFabFinish  .setVisibility( View.GONE );
				setLogVisibility( false );
				mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				break;

			case DESTINATION:
				mFabAdd     .setVisibility( View.GONE );
				mFabCancel  .setVisibility( View.GONE );
				mFabPrevious.setVisibility( View.VISIBLE );
				mFabNext    .setVisibility( View.GONE );
				mFabFinish  .setVisibility( View.VISIBLE );
				setLogVisibility( false );
				mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				break;

			case DETAIL:
				mFabAdd     .setVisibility( View.GONE );
				mFabCancel  .setVisibility( View.GONE );
				mFabPrevious.setVisibility( View.GONE );
				mFabNext    .setVisibility( View.GONE );
				mFabFinish  .setVisibility( View.GONE );
				setLogVisibility( false );
				mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				break;

			case UPDATES:
				mFabAdd     .setVisibility( View.VISIBLE );
				mFabCancel  .setVisibility( View.VISIBLE );
				mFabPrevious.setVisibility( View.VISIBLE );
				mFabNext    .setVisibility( View.VISIBLE );
				mFabFinish  .setVisibility( View.GONE );
				setLogVisibility( false );
				mGlobals.clearRoads();
				mGlobals.mQueue.add( mGlobals.mWrapi.getUpdatesRequest() );
				break;
		}
	}

	private void setLogMode( boolean enabled ) {
		mGlobals.mLogVisibility = enabled;

		if ( enabled ) {
			if ( mGlobals.mLog != null ) {
				mGlobals.mLog.setVisible( false );
				mGlobals.mLog.remove();
			}

			mGlobals.mLogList = new ArrayList<>();

			// Enable GPS
			if ( ! mManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
				Intent settingsIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
				startActivity( settingsIntent );
			}
		}
	}

	private void setLogVisibility( boolean enable ) {
		if ( enable ) {
			setLogMode( mGlobals.mLogVisibility );
		}
	}

	public void startPostActivity() {
		if ( mGlobals.mNewRoad ) {
			mGlobals.mNewRoad = false;
			startActivity( new Intent( this, PostActivity.class ) );
		}
	}
}
