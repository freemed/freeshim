#!/bin/bash
#
#	$Id$
#	jeff@freemedsoftware.org
#

# Build a Debian "free" version of FreeSHIM.

echo "Building changelog ... "
./git2cl > ChangeLog

echo "Exporting from Subversion ... "
git archive --remote git://github.com/freemed/freeshim.git master | tar -x -C BUILD

echo "Removing non-free component(s) ... "
xsltproc remove-nonfree.xsl BUILD/shim-webapp/pom.xml > BUILD/shim-webapp/pom.xml.tmp
mv BUILD/shim-webapp/pom.xml.tmp BUILD/shim-webapp/pom.xml -f
xsltproc remove-nonfree.xsl BUILD/shim-drivers/pom.xml > BUILD/shim-drivers/pom.xml.tmp
mv BUILD/shim-drivers/pom.xml.tmp BUILD/shim-drivers/pom.xml -f
rm -rf BUILD/shim-drivers/shim-driver-signature-topaz

echo "Moving in changelog ... "
mv ChangeLog BUILD/ -vf

echo "Creating source archives"

VERSION=$( cat project.properties | grep 'shim.version' | cut -d= -f2 )
echo "Version $VERSION"

cp BUILD shim-${VERSION} -Rf

zip -r shim-free-${VERSION}-src.zip shim-${VERSION}
tar czvf shim-free-${VERSION}-src.tar.gz shim-${VERSION}
rm -rf shim-${VERSION}

(

cd BUILD

echo "Building!"
mvn

ls -1 shim-*.*

ls -1 shim-*.* | while read X; do
	Y="${X/shim/shim-free}"
	mv "$X" "$Y" -vf
	mv "$Y" .. -vf
done

cd ..

)

echo "Cleaning..."
rm BUILD -rf
echo "done"

