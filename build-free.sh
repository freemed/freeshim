#!/bin/bash

svn export https://svn.master.freemedsoftware.org/freemed-utilities/shim/trunk BUILD

(
cd BUILD

xsltproc remove-nonfree.xsl shim-webapp/pom.xml > shim-webapp/pom.xml
xsltproc remove-nonfree.xsl shim-drivers/pom.xml > shim-drivers/pom.xml
rm -rf shim-drivers/

echo "Building!"
mvn

ls -1 shim-* | while read X; do
	Y="${X/shim/shim-free}"
	mv "$X" "$Y" -vf
	mv "$Y" .. -vf
done

cd ..

)

echo "Cleaning..."
rm BUILD -rf
echo "done"

