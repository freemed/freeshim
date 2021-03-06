# FreeSHIM Opensource Multiplatform Medical Device Interface

* (c) 2011-2014 by the FreeMED Software Foundation

## INSTALLATION

* The generated "shim.war" file needs to be dropped into the webapps
 folder of your Tomcat installation. If you don't have a "shim.war"
  file, please see the "COMPILATION" section.

* If you need to use any of the "native libraries" for any of the 
  device drivers, follow their respective README files for instructions
  on how to install their JNI libraries. (If there are any.) Maven
  *should* handle this for you.

* If you're using a serial port, you may have to run "setserial" on it,
  or even bake that into your /etc/rc.local file to run on boot, otherwise
  some dosing pumps, etc, may not work!

* Really, that's it. It's very simple. A more "in-depth" document for
  shim.war is located in shim-webapp/README !

## CONFIGURATION

* FreeSHIM uses a set of defaults which are included with it. These
  are viewable here (with documentation, of course): https://github.com/freemed/freeshim/blob/master/shim-webapp/src/main/webapp/WEB-INF/shim-default.properties
*  To override these, pass ```-Dshim.properties=/path/to/your/shim.properties``` to your J2EE container.

## COMPILATION

* Install Maven 2 and JDK 1.6 on your system.
* Ask Maven to package the system: `mvn package`
* Voila! You should see the "shim.war" package in `shim-webapps/target/`

## CAVEATS

Due to certain device manufacturers not liking to "play nice" with
open source efforts, there are a certain number of JNI (Java Native
Interface) libraries used. These are architecture specific, and it is
somewhat likely that they may not exist for all target architectures,
so there is always the outside chance that, for example, a 32-bit
Linux JVM might support more drivers than a 64-bit one (same with the
Windows builds). If you don't like that, bother the manufacturers who
have saddled us with fewer than the appropriate number of architecture
builds, or feel free to write an opensource equivalent. We're partial
to the latter solution. ;)

