<?php
	require_once 'auth.php';
	require_once 'connect.php';
	require_once 'get_post.php';
	require_once 'status.php';
	require_once 'success.php';
	error_reporting( E_ALL );

	// 当該低アクセシビリティ道路への情報(口コミ)の投稿
	// postsテーブルへ登録する
	// post_idのみを返す
	// JSON

	$road_id = get( 'road_id' );
	$is_steps = get_bool( 'is_steps' );
	$is_difference = get_bool( 'is_difference' );
	$is_steep = get_bool( 'is_steep' );
	$is_rough = get_bool( 'is_rough' );
	$is_narrow = get_bool( 'is_narrow' );
	$is_cant = get_bool( 'is_cant' );
	$is_bikes = get_bool( 'is_bikes' );
	$level = get( 'level' );
	$comment = get_string( 'comment' );
	$lang = get_string( 'lang' );

	// $image_flag = isset( $_FILE['upfile']['tmp_name'] );
	// echo $_FILE['upfile']['tmp_name'];

	if ( strlen( $lang ) != 2 ) {
		status( "INVALID LENGTH LANG PARAMETER $lang" );
	}

	$lang = strtolower( $lang );

	$user_id = auth();

	try {
		$dbh = connect();

		$dbh->beginTransaction();

		try {
			$sql = "INSERT INTO posts (
								road_id, user_id,
								is_steps, is_difference, is_steep, is_rough,
								is_narrow, is_cant, is_bikes,
								level, comment, lang
							)
			 				VALUES (
								:road_id, :user_id,
								:is_steps, :is_difference, :is_steep, :is_rough,
								:is_narrow, :is_cant, :is_bikes,
								:level, :comment, :lang
							)";

			$stmt = $dbh->prepare( $sql );
			$stmt->bindValue( ':road_id', $road_id, PDO::PARAM_INT );
			$stmt->bindValue( ':user_id', $user_id, PDO::PARAM_INT );
			$stmt->bindValue( ':is_steps', $is_steps, PDO::PARAM_INT );
			$stmt->bindValue( ':is_difference', $is_difference, PDO::PARAM_INT );
			$stmt->bindValue( ':is_steep', $is_steep, PDO::PARAM_INT );
			$stmt->bindValue( ':is_rough', $is_rough, PDO::PARAM_INT );
			$stmt->bindValue( ':is_narrow', $is_narrow, PDO::PARAM_INT );
			$stmt->bindValue( ':is_cant', $is_cant, PDO::PARAM_INT );
			$stmt->bindValue( ':is_bikes', $is_bikes, PDO::PARAM_INT );
			$stmt->bindValue( ':level', $level, PDO::PARAM_INT );
			$stmt->bindParam( ':comment', $comment, PDO::PARAM_STR );
			$stmt->bindParam( ':lang', $lang, PDO::PARAM_STR );

			/*
			if ( $image_flag ) {
				//$uploaddir = '/srv/http/wheelroads/';
				//$uploadfile = $uploaddir.basename( $_FILES['upfile']['name'] );
				$image = file_get_contents( $_FILES['upfile']['tmp_name'] );
				$stmt->bindParam( ':image', $image, PDO::PARAM_STR );
			}
			*/

			$flag = $stmt->execute();
			$post_id = $dbh->lastInsertId();

			if ( !$flag ) {
				//print_r( $stmt->errorInfo() );
				$dbh->rollBack();
				status( 'COULD NOT INSERT POST' );
			}

			$dbh->commit();
		} catch ( Exception $e ) {
			$dbh->rollBack();
			status( 'TRANSACTION ERROR' );
		}

		success( array( 'post_id' => $post_id ) );
	} catch ( PDOException $e ) {
		status( 'DATABASE ERROR' );
	} catch ( Exception $e ) {
		status( 'UNKNOWN ERROR' );
	}
?>
