<?php
	require_once "auth.php";
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 投稿情報の評価
	// post_votesテーブルへ登録する
	// statusのみを返す
	// JSON

	$post_id = get( 'post_id' );
	$vote = get( 'vote' );

	if ( $vote < -1 || 1 < $vote ) {
		status( 'INVALID VOTE RANGE '.$vote );
	}

	$user_id = auth();

	try {
		$dbh = connect();

		$sql = "SELECT user_id FROM posts WHERE post_id = $post_id";

		foreach ( $dbh->query( $sql ) as $post ) {
			if ( $post['user_id'] == $user_id ) {
				status( 'CANNOT VOTE YOURSELF' );
			}
		}

		$sql = "SELECT vote FROM post_votes
						WHERE post_id = $post_id AND user_id = $user_id";

		$exist = false;

		foreach ( $dbh->query( $sql ) as $existing_vote ) {
			if ( $existing_vote['vote'] == $vote ) {
				status( 'ALREADY VOTED' );
			}

			$update = "UPDATE post_votes
								 SET vote = $vote
								 WHERE post_id = $post_id AND user_id = $user_id";

			$update_stmt = $dbh->prepare( $update );
			$update_flag = $update_stmt->execute();

			if ( !$update_flag ) {
				status( 'COULD NOT UPDATE VOTE' );
			}

			$exist = true;
		}

		if ( !$exist ) {
			$insert = "INSERT INTO post_votes ( post_id, user_id, vote )
								 VALUES ( $post_id, $user_id, $vote )";

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
