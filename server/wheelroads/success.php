<?php
  function success( $data ) {
    $json = array(
      'status' => 'SUCCESS',
      'data' => $data
    );

    echo json_encode( $json );
  }
?>
