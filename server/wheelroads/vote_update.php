<?php
	require_once "auth.php";
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// ルート候補の評価
	// update_votesテーブルへ登録する
	// statusのみを返す
	// JSON

	$update_id = get( 'update_id' );
	$vote = get( 'vote' );

	if ( $vote < -1 || 1 < $vote ) {
		status( 'INVALID VOTE RANGE '.$vote );
	}

	$user_id = auth();

	try {
		$dbh = connect();

		$sql = "SELECT user_id FROM updates WHERE update_id = $update_id";

		foreach ( $dbh->query( $sql ) as $update ) {
			if ( $update['user_id'] == $user_id ) {
				status( 'CANNOT VOTE YOURSELF' );
			}
		}

		$sql = "SELECT vote FROM update_votes
						WHERE update_id = $update_id AND user_id = $user_id";

		$exist = false;

		foreach ( $dbh->query( $sql ) as $existing_vote ) {
			if ( $existing_vote['vote'] == $vote ) {
				status( 'ALREADY VOTED' );
			}

			$update = "UPDATE update_votes
								 SET vote = $vote
								 WHERE update_id = $update_id AND user_id = $user_id";

			$update_stmt = $dbh->prepare( $update );
			$update_flag = $update_stmt->execute();

			if ( !$update_flag ) {
				status( 'COULD NOT UPDATE VOTE' );
			}

			$exist = true;
		}

		if ( !$exist ) {
			$insert = "INSERT INTO update_votes ( update_id, user_id, vote )
								 VALUES ( $update_id, $user_id, $vote )";

			$insert_stmt = $dbh->prepare( $insert );
			$insert_flag = $insert_stmt->execute();

			if ( !$insert_flag ) {
				status( 'COULD NOT INSERT VOTE' );
			}
		}

		success( NULL );
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
