<?php
  // status
  function missing( $var ) {
    status( "MISSING PARAMETER $var" );
  }

  function invalid( $var, $val ) {
    status( "INVALID PARAMETER $var = $val" );
  }

  // isset, is_array, , is_01, is_numeric, is_string
  function is_01( $arg ) {
    return $arg == 0 || $arg == 1;
  }

  function check( $array, $key ) {
    if ( !isset( $array[$key] ) ) {
      missing( $key );
    }
  }

  function check_array( $array, $key ) {
    check( $array, $key );

    if ( !is_array( $array[$key] ) ) {
      invalid( $array, $array[$key] );
    }

		foreach( $array[$key] as $i => $v ) {
			if ( !is_numeric( $v ) ) {
				invalid( "{$key}['{$i}']", $v );
			}
		}
  }

  function check_bool( $array, $key ) {
    check( $array, $key );

    if ( !is_01( $array[$key] ) ) {
      invalid( $key, $array[$key] );
    }
  }

  function check_numeric( $array, $key ) {
    check( $array, $key );

    if ( !is_numeric( $array[$key] ) ) {
      invalid( $key, $array[$key] );
    }
  }

  function check_string( $array, $key ) {
    check( $array, $key );

    if ( !is_string( $array[$key] ) ) {
      invalid( $key, $array[$key] );
    }
  }

  // $_GET[]
  function get( $key ) {
    check_numeric( $_GET, $key );
    return $_GET[$key];
  }

  function get_bool( $key ) {
    check_bool( $_GET, $key );
    return $_GET[$key];
  }

	function get_array( $key ) {
		check_array( $_GET, $key );
		return $_GET[$key];
	}

  function get_string( $key ) {
    check_string( $_GET, $key );
    return $_GET[$key];
  }

  // $_POST[]
  function post( $key ) {
    check_numeric( $_POST, $key );
    return $_POST[$key];
  }

  function post_bool( $key ) {
    check_bool( $_POST, $key );
    return $_POST[$key];
  }

  function post_string( $key ) {
    check_string( $_POST, $key );
    return $_POST[$key];
  }
?>
