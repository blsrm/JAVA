#!/bin/sh
### Updated By Murugavel Ramachandran (M41569)
JAVA_HOME=/usr/java/jdk1.6.0_12
JAVA=$JAVA_HOME/bin/java
APPROOT=/apps/osm/prod/obs2osm/

LIB=$APPROOT/bin/OBS2OSMDBSYNC.jar:$APPROOT/lib/mail.jar:$APPROOT/lib/ojdbc14.jar:$APPROOT/lib/ojdbc14dms.jar:$APPROOT/lib/mysql-connector-java-5.0.6-bin.jar

CLIENTCLASS=dk.tdc.osm.action.DBSyncMain

$JAVA -DsocksProxyHost=sltarray02.tdk.dk -DsocksProxyPort=1080 -cp $LIB $CLIENTCLASS "$1" $APPROOT/conf/


