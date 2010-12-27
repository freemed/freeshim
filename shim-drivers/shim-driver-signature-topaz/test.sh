#!/bin/bash
# $Id$
#
# Authors:
#      Jeff Buchbinder <jeff@freemedsoftware.org>
#
# FreeMED Electronic Medical Record and Practice Management System
# Copyright (C) 1999-2011 FreeMED Software Foundation
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

# Test script for Topaz driver

P="$( cd "$(dirname "$0")" ; pwd )"
JDK_LOCATION="/usr/lib/jvm/jdk1.6.0_21"
JNI_PLATFORM=i686-unknown-linux-gnu

${JDK_LOCATION}/jre/bin/java -Djava.library.path=native/${JNI_PLATFORM} -cp $P/target/classes:$P/../../shim-library/target/classes:$P/deps/commapi-2.0.3.jar:$P/deps/sigplus-2.52.jar:${HOME}/.m2/repository/log4j/log4j/1.2.16/log4j-1.2.16.jar:${HOME}/.m2/repository/org/tmatesoft/sqljet/sqljet/1.0.3/sqljet-1.0.3.jar org.freemedsoftware.device.impl.SignatureTopazShim

