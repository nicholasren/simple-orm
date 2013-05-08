#!/bin/sh -e
cd ../simple-di && \
echo "uploading simple-di" && \
gradle uploadArchives && \
cd ../simple-mvc && \
echo "uploading simple-mvc" && \
gradle simple-mvc:uploadArchives
cd ../simple-orm && \
echo "installing simple-xx jars" && \
gradle idea