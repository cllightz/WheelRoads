package com.example.foobar.wheelroads;

import android.net.Uri;

import java.security.InvalidParameterException;
import java.util.HashMap;

public class Post {
	public boolean isNew;
	public int post_id;
	public int road_id;
	public String comment;
	public int level;
	public String lang;
	public String post_time;
	public boolean is_steps;
	public boolean is_difference;
	public boolean is_steep;
	public boolean is_rough;
	public boolean is_narrow;
	public boolean is_cant;
	public boolean is_bikes;

	public Post( int road_id, String comment, int level, String lang,
	             HashMap< String, Boolean > types ) {
		this.isNew = true;

		this.road_id = road_id;
		this.comment = comment;
		this.level   = level;
		this.lang    = lang;
		is_steps      = types.get( "is_steps" );
		is_difference = types.get( "is_difference" );
		is_steep      = types.get( "is_steep" );
		is_rough      = types.get( "is_rough" );
		is_narrow     = types.get( "is_narrow" );
		is_cant       = types.get( "is_cant" );
		is_bikes      = types.get( "is_bikes" );
	}

	public Post( int post_id, String comment, int level, String lang, String post_time,
	             HashMap< String, Boolean > types ) {
		this.isNew = false;

		this.post_id = post_id;
		this.comment = comment;
		this.level   = level;
		this.lang    = lang;
		this.post_time    = post_time;
		is_steps      = types.get( "is_steps" );
		is_difference = types.get( "is_difference" );
		is_steep      = types.get( "is_steep" );
		is_rough      = types.get( "is_rough" );
		is_narrow     = types.get( "is_narrow" );
		is_cant       = types.get( "is_cant" );
		is_bikes      = types.get( "is_bikes" );
	}

	public String toString() {
		String res = "";

		switch ( level ) {
			case 0:
				res += "付き添いなしでも快適に通行可";
				break;

			case 1:
				res += "付き添いなしでは労力を要する\n";
				break;

			case 2:
				res += "付き添いが必要\n";
				break;

			case 3:
				res += "通行不能\n";
				break;
		}

		res += is_steps ? "階段 " : "";
		res += is_difference ? "段差 " : "";
		res += is_steep ? "急な坂 " : "";
		res += is_rough ? "路面が悪い " : "";
		res += is_narrow ? "狭い " : "";
		res += is_cant ? "横方向の傾斜 " : "";
		res += is_bikes ? "塞がれている " : "";
		res += "\n";

		res += comment + "\n";
		res += post_time;

		return res;
	}

	public void append( Uri.Builder builder ) {
		if ( ! isNew ) {
			throw new InvalidParameterException();
		}

		builder.appendQueryParameter( "road_id", String.valueOf( road_id ) );
		builder.appendQueryParameter( "comment", comment );
		builder.appendQueryParameter( "level", String.valueOf( level ) );
		builder.appendQueryParameter( "lang", lang );
		builder.appendQueryParameter( "is_steps", is_steps ? "1" : "0" );
		builder.appendQueryParameter( "is_difference", is_difference ? "1" : "0" );
		builder.appendQueryParameter( "is_steep", is_steep ? "1" : "0" );
		builder.appendQueryParameter( "is_rough", is_rough ? "1" : "0" );
		builder.appendQueryParameter( "is_narrow", is_narrow ? "1" : "0" );
		builder.appendQueryParameter( "is_cant", is_cant ? "1" : "0" );
		builder.appendQueryParameter( "is_bikes", is_bikes ? "1" : "0" );
	}
}
