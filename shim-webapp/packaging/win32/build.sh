#!/bin/bash

TOMCAT_VERSION=6.0.29
P="$( cd "$(dirname "$0")"; pwd )"

cd "$P"

if [ ! -f ../../target/shim.war ] ; then
	echo "Create shim.war"
	( cd ../.. ; mvn package || exit 1 )
fi

if [ ! -f apache-tomcat-${TOMCAT_VERSION}-windows-x86.zip ] ; then
	echo "Grabbing tomcat ${TOMCAT_VERSION} binary package"
	wget -c http://apache.ziply.com/tomcat/tomcat-6/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}-windows-x86.zip
fi

if [ ! -d apache-tomcat-${TOMCAT_VERSION} ]; then
	echo "Decompression tomcat archive"
	unzip apache-tomcat-${TOMCAT_VERSION}-windows-x86.zip
fi

echo "Building package"
makensis shim.nsi

