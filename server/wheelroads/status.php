<?php
  function status( $status ) {
    $json = array(
      'status' => $status,
      'data' => NULL
    );

    echo json_encode( $json );
    die();
  }
?>
