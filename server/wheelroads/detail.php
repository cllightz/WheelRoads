<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 指定した低アクセシビリティ道路の口コミ情報を取得
	// JSON

	$road_id = get( 'road_id' );
	$lang = get_string( 'lang' );

	if ( strlen( $lang ) != 2 ) {
		status( "INVALID LENGTH LANG PARAMETER $lang" );
	}

	$lang = strtolower( $lang );

	$user_id = auth();

	try {
		$dbh = connect();

		$data = array();
		$flag = false;

		$sql = "SELECT post_id, post_time, lang, comment, level,
									 is_steps, is_difference, is_steep, is_rough,
									 is_narrow, is_cant, is_bikes
						FROM posts
						WHERE road_id = $road_id
							AND lang IN ('en'".($lang != NULL ? ", '$lang'" : '' ).")
						ORDER BY post_time DESC";

		foreach ( $dbh->query( $sql ) as $post ) {
			$post_id = $post['post_id'];
			$post_time = $post['post_time'];
			$lang = $post['lang'];
			$comment = $post['comment'];
			$level = $post['level'];
			$category = get_category( $post );

			$tmp = array(
				'post_id' => $post_id,
				'post_time' => $post_time,
				'lang' => $lang,
				'comment' => $comment,
				'level' => $level,
				'category' => $category
			);

			array_push( $data, $tmp );

			$flag = true;
		}

		if ( $flag ) {
			success( $data );
		} else {
			status( 'NO POSTS OF THE ROAD' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}

	function get_category( $post ) {
		$res = array();
		if ( $post['is_steps'] )      { array_push( $res, "is_steps" ); }
		if ( $post['is_difference'] ) { array_push( $res, "is_difference" ); }
		if ( $post['is_steep'] )      { array_push( $res, "is_steep" ); }
		if ( $post['is_rough'] )      { array_push( $res, "is_rough" ); }
		if ( $post['is_narrow'] )     { array_push( $res, "is_narrow" ); }
		if ( $post['is_cant'] )       { array_push( $res, "is_cant" ); }
		if ( $post['is_bikes'] )      { array_push( $res, "is_bikes" ); }
		return $res;
	}
?>
