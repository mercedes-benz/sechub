#!/bin/bash 

# Usage: bad_request <url><verb>

function usage(){
    echo "usage: send_curl_request <url> <request method>"
}

if [ -z "$1" ] ; then
    echo "url is missing as first parameter!"
    usage
    exit 1
fi

if [ -z "$2" ] ; then
    echo "request method is missing as second parameter"
    usage
    exit 1
fi


curl --insecure -X $2 -H "Content-Type: application/json" $1