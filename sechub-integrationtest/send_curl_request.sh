#!/bin/bash 

url=$1
http_verb=$2

function usage(){
    echo "usage: send_curl_request <url> <http_verb>"
}

if [ -z "$url" ] ; then
    echo "url is missing as first parameter!"
    usage
    exit 1
fi

if [ -z "$http_verb" ] ; then
    echo "request method is missing as second parameter"
    usage
    exit 1
fi

curl --insecure -X $http_verb -H "Content-Type: application/json" $url