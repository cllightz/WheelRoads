<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 低アクセシビリティ道路の新規登録
	// roadsテーブルへ登録する
	// road_idのみを返す
	// JSON

	$user_id = auth();

	try {
		$dbh = connect();

		$sql = 'INSERT INTO roads () VALUES ()';

		$stmt = $dbh->prepare( $sql );
		$flag = $stmt->execute();
		$road_id = $dbh->lastInsertId();

		if ( $flag ) {
			success( array( 'road_id' => $road_id ) );
		} else {
			status( 'REGISTRATION FAILED' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
