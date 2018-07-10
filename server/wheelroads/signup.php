<?php
	require_once 'status.php';
	require_once 'connect.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// ユーザの登録
	// tokenのみを返す
	// user_idは返さない
	// JSON
	// 引数なし

	$token = md5( uniqid( rand(), 1 ) );

	try {
		$dbh = connect();

		$sql = "INSERT INTO users ( token )
						VALUES ( :token )";

		$stmt = $dbh->prepare( $sql );
		$stmt->bindParam( ':token', $token, PDO::PARAM_STR );

		if ( $stmt->execute() ) {
			success( array( 'token' => $token ) );
		} else {
			status( 'SIGNUP FAILED' );
		}
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
