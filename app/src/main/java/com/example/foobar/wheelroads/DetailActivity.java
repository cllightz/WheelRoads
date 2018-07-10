package com.example.foobar.wheelroads;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {
	private Globals mGlobals;

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_detail );
		Toolbar toolbar = (Toolbar) findViewById( R.id.toolbar );
		setSupportActionBar( toolbar );

		mGlobals = (Globals)this.getApplication();

		FloatingActionButton fab = (FloatingActionButton) findViewById( R.id.fab_new_post );
		fab.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick( View view ) {
				Intent intent = new Intent( DetailActivity.this, PostActivity.class );
				intent.putExtra( "new", false );
				startActivity( intent );
			}
		} );
	}

	@Override
	protected void onResume() {
		ListView listView = ( ListView )findViewById( R.id.detail_list );
		listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick( AdapterView< ? > parent, View view, final int position, long id ) {
				DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick( DialogInterface dialog, int which ) {
						mGlobals.mQueue.add(
						mGlobals.mWrapi.getVotePostRequest( mGlobals.mPostIdList.get( position ), true )
						);
					}
				};

				DialogInterface.OnClickListener negativeListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick( DialogInterface dialog, int which ) {
						mGlobals.mQueue.add(
						mGlobals.mWrapi.getVotePostRequest( mGlobals.mPostIdList.get( position ), false )
						);
					}
				};

				AlertDialog.Builder builder = new AlertDialog.Builder( DetailActivity.this );
				builder.setTitle( "投票" );
				builder.setMessage( "この投稿の評価を投稿してください。" );
				builder.setPositiveButton( "高評価", positiveListener );
				builder.setNegativeButton( "低評価", negativeListener );
				builder.setNeutralButton( "キャンセル", null );
				builder.show();
			}
		} );

		ArrayAdapter< String > adapter = new ArrayAdapter<>( this, android.R.layout.simple_list_item_1 );
		Toast toast = Toast.makeText( this, "NO POSTS OF THE ROAD", Toast.LENGTH_LONG );
		mGlobals.mQueue.add( mGlobals.mWrapi.getDetailRequest( listView, adapter, toast ) );

		super.onResume();
	}
}
