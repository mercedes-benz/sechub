#!/usr/bin/env sh
# SPDX-License-Identifier: MIT

JAVA_DEBUG_OPTIONS=""

wait_loop() {
    while true
    do
	    echo "Press [CTRL+C] to stop.."
	    sleep 120
    done
}

localserver() {
    check_setup

    profiles="dev,real_products,mocked_notifications"
    database_options=""
    storage_options="-Dsechub.storage.sharedvolume.upload.dir=$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"

    if [ "$POSTGRES_ENABLED" = true ]
    then
        echo "Using database: Postgres"

        profiles="$profiles,postgres"
        database_options="-Dspring.datasource.url=$DATABASE_CONNECTION -Dspring.datasource.username=$DATABASE_USERNAME  -Dspring.datasource.password=$DATABASE_PASSWORD"

        echo "Database connection:"
        echo " * URL: $DATABASE_CONNECTION"
        echo " * Username: $DATABASE_USERNAME"
        echo " * Password: $DATABASE_PASSWORD"
    else
        echo "Using database: H2"
        profiles="$profiles,h2"
    fi

    if [ "$S3_ENABLED" = true ]
    then
        echo "Using object storage"

        storage_options="-Dsechub.storage.s3.endpoint=$S3_ENDPOINT"
        storage_options="$storage_options -Dsechub.storage.s3.bucketname=$S3_BUCKETNAME"
        storage_options="$storage_options -Dsechub.storage.s3.accesskey=$S3_ACCESSKEY"
        storage_options="$storage_options -Dsechub.storage.s3.secretkey=$S3_SECRETKEY"

        echo "Object storage:"
        echo " * Endpoint: $S3_ENDPOINT"
        echo " * Bucketname: $S3_BUCKETNAME"
        echo " * Accesskey: $S3_ACCESSKEY"
    fi

    echo "Upload source code maximum bytes: $SECHUB_MAX_FILE_UPLOAD_SIZE"
    echo "Upload binaries maximum bytes: $SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES"
    echo "Activated profiles: $profiles"

    echo "Java version:"
    java --version

    java $JAVA_DEBUG_OPTIONS $database_options \
        $storage_options \
        -Dfile.encoding=UTF-8 \
        -Dspring.profiles.active="$profiles" \
        -Dsechub.targettype.detection.intranet.hostname.endswith=intranet.example.org \
        -Dsechub.config.trigger.nextjob.initialdelay=0 \
        -Dsechub.initialadmin.userid="$ADMIN_USERID" \
        -Dsechub.initialadmin.email=sechubadm@example.org \
        -Dsechub.initialadmin.apitoken="$ADMIN_APITOKEN" \
        -Dspring.servlet.multipart.max-file-size="$SECHUB_MAX_FILE_UPLOAD_SIZE" \
        -Dspring.servlet.multipart.max-request-size="$SECHUB_MAX_FILE_UPLOAD_SIZE" \
        -Dsechub.upload.binaries.maximum.bytes="$SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES" \
        -Dsechub.adapter.netsparker.userid=abc \
        -Dsechub.adapter.netsparker.apitoken=xyz \
        -Dsechub.adapter.netsparker.baseurl=https://example.org \
        -Dsechub.adapter.netsparker.defaultpolicyid=example \
        -Dsechub.adapter.netsparker.licenseid=example \
        -Dsechub.adapter.nessus.defaultpolicyid=example \
        -Dsechub.notification.email.administrators=example@example.org \
        -Dsechub.notification.email.from=example@example.org \
        -Dsechub.notification.smtp.hostname=example.org \
        -Dserver.port=8443 \
        -Dserver.address=0.0.0.0 \
        -jar /sechub/sechub-server*.jar
}

check_setup () {
    check_variable "$ADMIN_USERID" "ADMIN_USERID"
    check_variable "$ADMIN_APITOKEN" "ADMIN_APITOKEN"
    check_variable "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR" "SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
}

check_variable () {
    value="$1"
    name="$2"

    if [ -z "$value" ]
    then
        echo "Environment variable $name not set."
        exit 1
    fi
}

debug () {
    wait_loop
}

if [ "$JAVA_ENABLE_DEBUG" = "true" ]
then
    # By using `address=*:15023` the server will bind 
    # all available IP addresses to port 15023
    # otherwise the container cannot be accessed from outside
    JAVA_DEBUG_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,address=*:15023"
fi

if [ "$SECHUB_START_MODE" = "localserver" ]
then
    localserver
else
    debug
fi