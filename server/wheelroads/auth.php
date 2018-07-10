<?php
	require_once 'connect.php';
	require_once 'status.php';

	// tokenの認証
	// 該当するuser_idを返す

	function auth() {
		if ( !isset( $_GET['token'] ) ) {
			status( 'NO TOKEN' );
		}

		if ( !is_string( $_GET['token'] ) ) {
			status( 'INVALID TOKEN' );
		}

		return token_to_user_id( $_GET['token'] );
	}

	function auth_post() {
		if ( !isset( $_POST['token'] ) ) {
			status( 'NO TOKEN' );
		}

		if ( !is_string( $_POST['token'] ) ) {
			status( 'INVALID TOKEN' );
		}

		return token_to_user_id( $_POST['token'] );
	}

	function token_to_user_id( $token ) {
		try {
			$dbh = connect();

			$sql = "SELECT u.user_id
							FROM users AS u
							WHERE u.token = '$token'";

			foreach ( $dbh->query( $sql ) as $user ) {
				return $user['user_id'];
			}
		} catch ( PDOException $e ) {
			status( 'DATABASE ERROR' );
		} catch ( Exception $e ) {
			status( 'UNKNOWN ERROR' );
		}

		status( 'TOKEN NOT EXIST' );
	}
?>
