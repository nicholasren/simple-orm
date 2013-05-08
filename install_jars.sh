#!/bin/sh -e
cd ../simple-di && \
echo "uploading simple-di" && \
gradle uploadArchives && \
cd ../simple-mvc && \
echo "uploading simple-mvc" && \
gradle uploadArchives
cd ../simple-orm && \
echo "installing simple-xx jars" && \
gradle idea