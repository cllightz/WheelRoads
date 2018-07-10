<?php
  // WheelRoadsデータベースへの接続

  function connect() {
    try {
      $dsn = 'mysql:host=localhost;dbname=u215980831_wlrd';
      $user = 'u215980831_wlrd';
      $password = 'bokuhazakoi';
      return new PDO( $dsn, $user, $password );
    } catch ( PDOException $e ) {
  		status( 'DATABASE ERROR' );
  	} catch ( Exception $e ) {
  		status( 'UNKNOWN ERROR' );
  	}
  }
?>
