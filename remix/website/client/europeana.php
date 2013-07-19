<?php

//from http://jquery-howto.blogspot.com/2009/04/cross-domain-ajax-querying-with-jquery.html

include_once('../settings.php');

$id = isset( $_GET['id'] ) ? $_GET['id'] : null;

if ( $id && preg_match('/[0-9A-Za-z]+\/[0-9A-Za-z]+/', $id ) ) {
	// Website url to open
	$daurl = 'http://www.europeana.eu/portal/record/' . $id .  '.srw?wskey=' . EUROPEANA_API_KEY;
} else {
	exit();
}

$file_cache_name = 'data/' . str_replace( '/', '-', $id ) . '.xml';

//europeana queries should last 30 days
$expires = 60*60*24*30;

if ( file_exists( $file_cache_name ) &&
	filemtime( $file_cache_name ) > time() - $expires
) {

	//read file from cache
	@$handle = fopen( $file_cache_name, "r" );

	// If there is something, read and return
	if ( $handle ) {

		// Set your return content type
		header('Content-type: text/xml');
		header('Expires: ' . gmdate('D, d M Y H:i:s', time() + $expires) . ' GMT');

		while (!feof($handle)) {

			$buffer = fgets($handle, 4096);
			echo $buffer;

		}

		fclose($handle);

	} else {

		header("HTTP/1.0 404 Not Found");

	}

} else {

	// Get that website's content
	@$handle = fopen( $daurl, "r" );

	// If there is something, read and return
	if ( $handle && !feof( $handle ) ) {

		@$cache_handle = fopen( $file_cache_name, "w" );

		if ( $cache_handle ) {

			// Set your return content type
			header('Content-type: text/xml');
			header('Expires: ' . gmdate('D, d M Y H:i:s', time()+$expires) . ' GMT');

			while ( !feof( $handle ) ) {

				$buffer = fgets( $handle, 4096 );
				fputs( $cache_handle, $buffer );
				echo $buffer;

			}

			fclose( $cache_handle );

		} else {

			error_log( 'could not write to cache directory. probably need to chown www on the directory [' . $file_cache_name . ']' );

		}

		fclose( $handle );

	} else {

		header("HTTP/1.0 404 Not Found");
		error_log('no content found at [' . $daurl . ']');

	}

}
