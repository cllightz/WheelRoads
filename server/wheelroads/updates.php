<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 指定した低アクセシビリティ道路のパスの候補を取得
	// JSON

	$road_id = get( 'road_id' );
	$user_id = auth();

	try {
		$dbh = connect();

		$data = array();
		$flag = false;

		$sql_get_update_id = "SELECT u.update_id
													FROM updates AS u LEFT JOIN update_votes AS v
														ON u.update_id = v.update_id
													WHERE road_id = $road_id
													GROUP BY u.update_id
													ORDER BY SUM( v.vote ) DESC, u.update_time ASC";

		foreach ( $dbh->query( $sql_get_update_id ) as $update ) {
			$update_id = $update['update_id'];

			$sql_get_points = "SELECT p.lat, p.lng
												 FROM points as p
												 WHERE p.update_id = $update_id
												 ORDER BY point_num";
												 $points = array();

			$flag = true;
			$flag_path = false;

			foreach ( $dbh->query( $sql_get_points ) as $latlng ) {
				$point = array( 'lat' => (float)$latlng['lat'],
												'lng' => (float)$latlng['lng'] );

				array_push( $points, $point );

				$flag_path = true;
			}

			if ( $flag_path ) {
				$road = array( 'update_id' => $update_id,
											 'points' => $points );

				array_push( $data, $road );

				$flag_data = true;
			}
		}

		if ( $flag ) {
			success( $data );
		} else {
			status( 'NO PATHS OF THE ROAD' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}

	function get_category( $post ) {
		$res = array();
		if ( $post['is_steps'] )      { array_push( $res,  is_steps ); }
		if ( $post['is_difference'] ) { array_push( $res,  is_difference ); }
		if ( $post['is_steep'] )      { array_push( $res,  is_steep ); }
		if ( $post['is_rough'] )      { array_push( $res,  is_rough ); }
		if ( $post['is_narrow'] )     { array_push( $res,  is_narrow ); }
		if ( $post['is_cant'] )       { array_push( $res,  is_cant ); }
		if ( $post['is_bikes'] )      { array_push( $res,  is_bikes ); }
		return $res;
	}
?>
