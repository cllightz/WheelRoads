<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=" />
	</head>
	<body>
		<?php
			require_once 'connect.php';
			error_reporting( E_ALL );

			// report_not_existが送信されたroadの一覧
			// HTML

			try {
				$dbh = connect();

				$sql = "SELECT road_id, COUNT(*) AS count
								FROM not_exist
								GROUP BY road_id
								ORDER BY COUNT(*) DESC, road_id ASC";

				$exist = false;

				foreach ( $dbh->query( $sql ) as $row ) {
					echo "{$row['road_id']}: {$row['count']}<br />\n";
				}
			} catch ( PDOException $e ) {
				echo 'DATABASE ERROR';
			} catch ( Exception $e ) {
				echo 'UNKNOWN ERROR';
			}
		?>
	</body>
</html>
