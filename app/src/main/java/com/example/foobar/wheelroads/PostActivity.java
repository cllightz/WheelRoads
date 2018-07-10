package com.example.foobar.wheelroads;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import org.json.JSONException;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
	private Globals mGlobals;

	private RadioButton level0;
	private RadioButton level1;
	private RadioButton level2;
	private RadioButton level3;

	private CheckBox steps;
	private CheckBox difference;
	private CheckBox steep;
	private CheckBox rough;
	private CheckBox narrow;
	private CheckBox cant;
	private CheckBox bikes;

	private EditText comment;
	private RadioButton en;
	private RadioButton ja;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_post );
		Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar_detail );
		setSupportActionBar( toolbar );

		Intent intent = getIntent();
		boolean isNew = intent.getBooleanExtra( "new", true );

		mGlobals = (Globals)this.getApplication();
		final int road_id = mGlobals.road_id;

		if ( road_id < 0 ) {
			Toast.makeText( this, "ERROR OCCURRED", Toast.LENGTH_LONG ).show();
			Log.e( "PostActivity", "road_id < 0" );
			finish();
		}

		level0 = (RadioButton)findViewById( R.id.level0 );
		level0.setEnabled( !isNew );
		level0.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				steps.setEnabled( false );
				steps.setChecked( false );
				difference.setEnabled( false );
				difference.setChecked( false );
				steep.setEnabled( false );
				steep.setChecked( false );
				rough.setEnabled( false );
				rough.setChecked( false );
				narrow.setEnabled( false );
				narrow.setChecked( false );
				cant.setEnabled( false );
				cant.setChecked( false );
				bikes.setEnabled( false );
				bikes.setChecked( false );
			}
		} );

		level1 = (RadioButton)findViewById( R.id.level1 );
		level1.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				steps     .setEnabled( false );
				steps     .setChecked( false );
				difference.setEnabled( true );
				steep     .setEnabled( true );
				rough     .setEnabled( true );
				narrow    .setEnabled( true );
				cant      .setEnabled( true );
				bikes     .setEnabled( true );
			}
		});

		level2 = (RadioButton)findViewById( R.id.level2 );
		level2.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				steps     .setEnabled( false );
				steps     .setChecked( false );
				difference.setEnabled( true );
				steep     .setEnabled( true );
				rough     .setEnabled( true );
				narrow    .setEnabled( true );
				cant      .setEnabled( true );
				bikes     .setEnabled( true );
			}
		});

		level3 = (RadioButton)findViewById( R.id.level3 );
		level3.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {
				steps     .setEnabled( true );
				difference.setEnabled( true );
				steep     .setEnabled( true );
				rough     .setEnabled( true );
				narrow    .setEnabled( true );
				cant      .setEnabled( true );
				bikes     .setEnabled( true );
			}
		});

		steps = (CheckBox)findViewById( R.id.steps );
		steps.setOnCheckedChangeListener( new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
				if ( isChecked ) {
					difference.setEnabled( false );
					difference.setChecked( false );
					steep     .setEnabled( false );
					steep     .setChecked( false );
					rough     .setEnabled( false );
					rough     .setChecked( false );
					narrow    .setEnabled( false );
					narrow    .setChecked( false );
					cant      .setEnabled( false );
					cant      .setChecked( false );
					bikes     .setEnabled( false );
					bikes     .setChecked( false );
				} else {
					difference.setEnabled( true );
					steep     .setEnabled( true );
					rough     .setEnabled( true );
					narrow    .setEnabled( true );
					cant      .setEnabled( true );
					bikes     .setEnabled( true );
				}
			}
		} );

		difference = (CheckBox)findViewById( R.id.difference );
		steep = (CheckBox)findViewById( R.id.steep );
		rough = (CheckBox)findViewById( R.id.rough );
		narrow = (CheckBox) findViewById( R.id.narrow );
		cant = (CheckBox)findViewById( R.id.cant );
		bikes = (CheckBox)findViewById( R.id.bikes );

		comment = (EditText)findViewById( R.id.comment );
		comment.setFocusableInTouchMode( true );

		//en = (RadioButton)findViewById( R.id.en );
		//ja = (RadioButton)findViewById( R.id.ja );

		Button post = (Button)findViewById( R.id.post );
		post.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View v ) {

				if ( level0.isChecked() ||
						 ( level1.isChecked() ||
				       level2.isChecked() ||
				       level3.isChecked() ) &&
				     ( steps.isChecked() ||
							 difference.isChecked() ||
							 steep.isChecked() ||
							 rough.isChecked() ||
							 narrow.isChecked() ||
							 cant.isChecked() ||
							 bikes.isChecked() ) ) {

					int level = level0.isChecked() ? 0 :
											level1.isChecked() ? 1 :
											level2.isChecked() ? 2 :
											level3.isChecked() ? 3 : -1;

					String lang = "ja";

					/*
					String lang = en.isChecked() ? "en" :
												ja.isChecked() ? "ja" : "--";
					*/

					HashMap< String, Boolean > types = new HashMap<>();
					types.put( "is_steps",      steps.isChecked() );
					types.put( "is_difference", difference.isChecked() );
					types.put( "is_steep",      steep.isChecked() );
					types.put( "is_rough",      rough.isChecked() );
					types.put( "is_narrow",     narrow.isChecked() );
					types.put( "is_cant",       cant.isChecked() );
					types.put( "is_bikes",      bikes.isChecked() );

					Log.e( "road_id", Integer.toString( mGlobals.road_id ) );
					Post newPost = new Post( mGlobals.road_id, comment.getText().toString(), level, lang, types );

					try {
						mGlobals.mQueue.add( mGlobals.mWrapi.getPostRequest( newPost, PostActivity.this ) );
					} catch ( JSONException e ) {
						e.printStackTrace();
					}

					mGlobals.mQueue.add( mGlobals.mWrapi.getGetRequest() );
				}
			}
		});
	}
}
