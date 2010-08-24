#!/usr/bin/env php
<?php
/*
 * $Id$
 *
 * Authors:
 *      Jeff Buchbinder <jeff@freemedsoftware.org>
 *
 * FreeMED Electronic Medical Record / Practice Management
 * Copyright (C) 1999-2010 FreeMED Software Foundation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

$protocol = "http";
$url = "localhost:8080/shim";
$username = 'Administrator';
$password = 'password';

print '$Id$' . "\n";
print "SOAP Testing interface for shim\n";
print "\n";

// Hack to deal with Basic Authentication protected WSDL
$temp = tempnam( "/tmp", "wsdl");
print " * Fetching WSDL from $url using $protocol (username $username) ... ";
file_put_contents( $temp, file_get_contents($protocol."://".urlencode($username).":".urlencode($password)."@".$url."/services/ShimService?wsdl") );
print "done.\n\n";

$sc = new SoapClient( $temp, array(
	  'login' => $username
	, 'password' => $password
	, 'compression' => SOAP_COMPRESSION_ACCEPT | SOAP_COMPRESSION_GZIP
));

print "getProtocolVersion : \n";
print_r( $sc->getProtocolVersion() );
print "\n";

print "requestSignature('Patient: Me') : \n";
$id = $sc->requestSignature((object)array("displayInformation" => "Patient: Me"))->return;
print_r( $id );
print "\n";

$x = (object) array( 'return' => '' );

while ($x->return != 'COMPLETE' && $x->return != 'ERROR') {
	print "getJobStatus ( $id ) : \n";
	$x = $sc->getJobStatus((object)array("id" => $id));
	print_r( $x );
	print "\n";
	if ($x->return != 'COMPLETE' && $x->return != 'ERROR') {
		// Only wait if we're not done yet.
		sleep (5);
	}
}

unlink($temp);

?>
