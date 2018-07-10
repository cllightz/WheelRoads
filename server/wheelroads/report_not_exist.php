<?php
	require_once "auth.php";
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 低アクセシビリティ道路が存在しない(既に工事により改善されている等)ことを報告
	// not_existテーブルへ登録する
	// statusのみを返す
	// JSON

	$road_id = get( 'road_id' );
	$user_id = auth();

	try {
		$dbh = connect();

		$sql = "SELECT * FROM not_exist
						WHERE road_id = $road_id AND user_id = $user_id";

		foreach ( $dbh->query( $sql ) as $existing_report ) {
			status( 'ALREADY REPORTED' );
		}

		$insert = "INSERT INTO not_exist ( road_id, user_id )
							 VALUES ( $road_id, $user_id )";

		$insert_stmt = $dbh->prepare( $insert );
		$insert_flag = $insert_stmt->execute();

		if ( !$insert_flag ) {
			status( 'COULD NOT INSERT REPORT' );
		}

		success( NULL );
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
