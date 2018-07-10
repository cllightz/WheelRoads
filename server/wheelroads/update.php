<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 当該低アクセシビリティ道路のルートの新規登録・訂正案登録
	// JSON

	$road_id = get( 'road_id' );
	$lat = get_array( 'lat' );
	$lng = get_array( 'lng' );

	$user_id = auth();

	if ( count( $lat ) != count( $lng ) ) {
		status( 'UNEQUAL ARRAY SIZE' );
	}

	if ( count( $lat ) < 2 ) {
		status( 'TOO SHORT ARRAY SIZE' );
	}

	try {
		$dbh = connect();

		$dbh->beginTransaction();

		try {
			$sql_insert_update = "INSERT INTO updates ( road_id, user_id )
														VALUES ( :road_id, :user_id )";

			$stmt = $dbh->prepare( $sql_insert_update );
			$stmt->bindValue( ':road_id', $road_id, PDO::PARAM_INT );
			$stmt->bindValue( ':user_id', $user_id, PDO::PARAM_INT );
			$flag = $stmt->execute();
			$update_id = $dbh->lastInsertId();

			if ( !$flag ) {
				status( 'REGISTRATION FAILED' );
			}

			for ( $i = 0; $i < count( $lat ); ++$i ) {
				$sql_insert_point = "INSERT INTO points ( update_id, point_num, lat, lng )
														 VALUES ( :update_id, :point_num, :lat, :lng )";

				$stmt = $dbh->prepare( $sql_insert_point );
				$stmt->bindValue( ':update_id', $update_id, PDO::PARAM_INT );
				$stmt->bindValue( ':point_num', $i, PDO::PARAM_INT );
				$stmt->bindParam( ':lat', $lat[$i], PDO::PARAM_STR );
				$stmt->bindParam( ':lng', $lng[$i], PDO::PARAM_STR );
				$flag = $stmt->execute();

				if ( !$flag ) {
					//$error = $stmt->errorInfo();
					//print_r( $error );
					$dbh->rollBack();
					status( 'COULD NOT INSERT POINTS' );
				}
			}

			$dbh->commit();

			success( array( 'update_id' => $update_id ) );
		} catch ( Exception $e ) {
			$dbh->rollBack();
			status( 'TRANSACTION ERROR' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
