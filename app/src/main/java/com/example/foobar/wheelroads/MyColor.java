package com.example.foobar.wheelroads;

import android.graphics.Color;
import android.support.annotation.ColorInt;

public class MyColor extends Color {
	@ColorInt public static final int BLACK       = 0xFF000000;
	@ColorInt public static final int DKGRAY      = 0xFF444444;
	@ColorInt public static final int GRAY        = 0xFF888888;
	@ColorInt public static final int LTGRAY      = 0xFFCCCCCC;
	@ColorInt public static final int WHITE       = 0xFFFFFFFF;

	@ColorInt public static final int RED         = 0xFFFF0000;
	@ColorInt public static final int ORANGE      = 0xFFFF7F00; // Original
	@ColorInt public static final int YELLOW      = 0xFFFFFF00;
	@ColorInt public static final int GREEN       = 0xFF00FF00;
	@ColorInt public static final int CYAN        = 0xFF00FFFF;
	@ColorInt public static final int BLUE        = 0xFF0000FF;
	@ColorInt public static final int MAGENTA     = 0xFFFF00FF;
	@ColorInt public static final int TRANSPARENT = 0;

	static public int levelToColor( double level ) {
		int R;
		int G;
		int B;

		if ( level < 0.0f ) {
			R =   0;
			G =   0;
			B =   0;
		} else if ( level <= 0.5f ) {
			R =   0 + (int)( 191.0f * (level-0.0f) * 2.0f );
			G = 127 + (int)(  64.0f * (level-0.0f) * 2.0f );
			B =   0;
		} else if ( level <= 1.0f ) {
			R = 191 + (int)(  64.0f * (level-0.5f) * 2.0f );
			G = 191 - (int)(  64.0f * (level-0.5f) * 2.0f );
			B =   0;
		} else if ( level <= 2.0f ) {
			R = 255;
			G = 127 - (int)( 127.0f * (level-1.0f) );
			B =   0;
		} else if ( level <= 3.0f ) {
			R = 255 - (int)( 128.0f * (level-2.0f) );
			G =   0;
			B =   0 + (int)( 127.0f * (level-2.0f) );
		} else {
			R = 127;
			G = 127;
			B = 127;
		}

		return Color.argb( 0xBF, R, G, B );
	}
}
