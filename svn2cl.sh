#!/bin/bash
# $Id: svn2cl.sh 120 2005-06-28 21:42:03Z jeff $
# $Author: jeff $
#
#	Script to generate Changelog from Subversion XML output.
#

svn log -r HEAD:0 --xml --verbose | xsltproc --stringparam strip-prefix dir/subdir "$(cd "$(dirname "$0")" ; pwd)/svn2cl.xsl" - > ChangeLog

