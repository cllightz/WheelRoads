<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 画面上の低アクセシビリティ道路を取得
	// 3で得られた全てのroad_idに対して，4-5を繰り返し行う

	// 1. 画面上に入るpoint(pointsテーブル)を取得
	// 2. 1のpointが属する全てのupdate(updatesテーブル)のupdate_idを取得(重複を除く)
	// 3. 2のupdateが属するroad(roadsテーブル)のroad_idをそれぞれ取得
	// 4. 3のroadに属する全てのupdateの内，最も評価(votesテーブルのvoteのSUM)が
	//    高いもupdateのupdate_idを取得(同評価の場合はupdateの古いものを優先)
	// 5. 4のupdateに属する全てのpointを取得
	// 6. 3のroadに属するpostのlevelの平均をそれぞれ取得

	// JSON

	$s = get( 's' );
	$n = get( 'n' );
	$w = get( 'w' );
	$e = get( 'e' );

	$user_id = auth();

	try {
		$dbh = connect();

		$data = array();
		$flag_data = false;

		// 1-3
		$sql_get_road_id = "SELECT DISTINCT u.road_id AS road_id
												FROM
													( points AS p INNER JOIN updates AS u
															ON p.update_id = u.update_id
													) INNER JOIN roads AS r
														ON u.road_id = r.road_id
												WHERE p.lat BETWEEN $s AND $n
													AND p.lng BETWEEN $w AND $e
													AND r.enable = 1";

		foreach ( $dbh->query( $sql_get_road_id ) as $road ) {
			$road_id = $road['road_id'];
			$update_id;

			// 4
			$sql_get_update_id = "SELECT u.update_id
														FROM updates AS u LEFT JOIN update_votes AS v
															ON u.update_id = v.update_id
														WHERE u.road_id = $road_id
														GROUP BY v.update_id
														ORDER BY SUM( v.vote ) DESC, u.update_time ASC
														LIMIT 1";

			foreach ( $dbh->query( $sql_get_update_id ) as $update ) {
				$update_id = $update['update_id'];

				// 5
				$sql_get_points = "SELECT p.lat, p.lng
													FROM points as p
													WHERE p.update_id = $update_id
													ORDER BY point_num";

				$points = array();
				$flag_road = false;

				foreach ( $dbh->query( $sql_get_points ) as $latlng ) {
					$point = array( 'lat' => (float)$latlng['lat'],
													'lng' => (float)$latlng['lng'] );

					array_push( $points, $point );

					$flag_road = true;
				}

				if ( $flag_road ) {
					// 6
					$sql_get_level = "SELECT AVG( level ) AS avg
														FROM posts
														WHERE road_id = $road_id";

					$flag_level = false;

					foreach ( $dbh->query( $sql_get_level ) as $row ) {
						$level = $row['avg'];
						$flag_level = true;
					}

					$road = array( 'road_id' => $road_id,
												 'update_id' => $update_id,
												 'level' => $flag_level ? $level : NULL,
												 'points' => $points );

					array_push( $data, $road );

					$flag_data = true;
				}
			}
		}

		if ( $flag_data ) {
			success( $data );
		} else {
			status( 'NO DATA IN THIS LOCATION' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
