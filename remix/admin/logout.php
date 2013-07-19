<?php
	/*
		UserCake Version: 1.4
		http://usercake.com

		Developed by: Adam Davis
	*/
	//include("../../models/config.php");
	require(dirname(__DIR__) . '/website/models/config.php');

	// Check if admin i/f is active
	if ( !defined( 'ADMIN_IF_ACTIVE' ) || !ADMIN_IF_ACTIVE ) { header('Location: /'); exit(); }

	//Log the user out
	if(isUserLoggedIn()) $loggedInUser->userLogOut();

	if(!empty($websiteUrl))
	{
		$add_http = "";

		if(strpos($websiteUrl,"http://") === false)
		{
			$add_http = "http://";
		}

		header("Location: ".$add_http.$websiteUrl);
		die();
	}
	else
	{
		header("Location: http://".$_SERVER['HTTP_HOST']);
		die();
	}
