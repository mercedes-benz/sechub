#!/bin/bash
# SPDX-License-Identifier: MIT

function eventExists(){
     EVENT_TYPE=$1
     
     local FILE="${PDS_JOB_EVENTS_FOLDER}/${EVENT_TYPE}.json"
     
     if [[ -f "$FILE" ]]; then
       return $FUNCTION_RESULT_TRUE
     else
       return $FUNCTION_RESULT_FALSE
     fi
     
}

function eventNotExists(){
     EVENT_TYPE=$1
     
     if eventExists $EVENT_TYPE; then
       return $FUNCTION_RESULT_FALSE
     else
       return $FUNCTION_RESULT_TRUE
     fi
      
}

# Will wait until the defined event is found. will try this a dedicated time intervall.
# On sucess an info message will be send, in failure case a n error message 
# $1 - event type (e.g. "cancel_requested")
# $2 - time to wait for next check (e.g. 0.3 will wait 0.3 seconds==300 milliseconds)
# $3 - maximum amount of retries
#
function waitForEventAndSendMessage(){
    local EVENT_TYPE=$1
    local TIME_TO_WAIT_BEFORE_NEXT_CHECK=$2
    local MAX_AMOUNT_OF_RETRIES=$3
    
    echo "Check if event can be found: $EVENT_TYPE. Will retry $MAX_AMOUNT_OF_RETRIES times. Wait for next check is:$TIME_TO_WAIT_BEFORE_NEXT_CHECK seconds."
    echo    "  123456789-123456789-123456789-123456789-123456789-123456789 (retries)"
    echo -n "=>"
    while eventNotExists "$EVENT_TYPE"
    do
        sleep $TIME_TO_WAIT_BEFORE_NEXT_CHECK 
        let counter++
        echo -n "_"
        
        if [[ counter -ge $MAX_AMOUNT_OF_RETRIES ]]; then
            echo "|"
            echo "FAILED - event folder contains:"
            ls -l "$PDS_JOB_EVENTS_FOLDER"
            errorMessage "Operation did take too long! Even afer $counter retries the event type: '$EVENT_TYPE' was not recognized!"
            return $FUNCTION_RESULT_FALSE
        fi
     done
     echo ""
     infoMessage "Event type:$EVENT_TYPE was received and handled by script"
     return $FUNCTION_RESULT_TRUE
     
}
