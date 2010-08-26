#!/bin/bash

mvn install:install-file -DgroupId=commapi -DartifactId=commapi -Dversion=2.0.3 -Dpackaging=jar -Dfile=deps/commapi-2.0.3.jar
mvn install:install-file -DgroupId=sigplus -DartifactId=sigplus -Dversion=2.52 -Dpackaging=jar -Dfile=deps/sigplus-2.52.jar

