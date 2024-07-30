#!/bin/sh
# SPDX-License-Identifier: MIT

SLEEP_TIME_IN_WAIT_LOOP="2h"

check_variable() {
  value="$1"
  name="$2"

  if [ -z "$value" ]
  then
    echo "Mandatory environment variable $name not set."
    exit 1
  fi
}

check_setup() {
  check_variable "$ADMIN_USERID" "ADMIN_USERID"
  check_variable "$ADMIN_APITOKEN" "ADMIN_APITOKEN"
  check_variable "$SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR" "SECHUB_STORAGE_SHAREDVOLUME_UPLOAD_DIR"
}

wait_loop() {
  while true
  do
    echo "wait_loop(): Sleeping for $SLEEP_TIME_IN_WAIT_LOOP."
    sleep $SLEEP_TIME_IN_WAIT_LOOP
  done
}

init_scheduler_settings() {
  if [ -z "$SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY" ] ; then
    export SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY="10000"
    echo "---> SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY is undefined. Falling back to $SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY"
  fi
  # Randomize initial delay used for trigger next job (a random tenth of the scheduler delay)
  export SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY=$(( $SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY / 10 * $(shuf -i 0-10 -n 1) ))
}

localserver() {
  check_setup

  profiles="dev,real_products,mocked_notifications"

  if [ "$POSTGRES_ENABLED" = true ] ; then
    profiles="$profiles,postgres"
    check_variable "$DATABASE_CONNECTION" "DATABASE_CONNECTION"
    check_variable "$DATABASE_USERNAME" "DATABASE_USERNAME"
    check_variable "$DATABASE_PASSWORD" "DATABASE_PASSWORD"

    # Set datasource variables for Java Spring app:
    export SPRING_DATASOURCE_URL="$DATABASE_CONNECTION"
    export SPRING_DATASOURCE_USERNAME="$DATABASE_USERNAME"
    export SPRING_DATASOURCE_PASSWORD="$DATABASE_PASSWORD"

    echo "Using database: Postgres"
    echo " * connection string: $DATABASE_CONNECTION"
    echo " * database username: $DATABASE_USERNAME"
  else
    echo "Using database: H2"
    profiles="$profiles,h2"
  fi

  echo "Upload source code maximum bytes: $SECHUB_MAX_FILE_UPLOAD_SIZE"
  echo "Upload binaries maximum bytes: $SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES"
  echo "Activated Spring profiles: $profiles"

  # Check variables for Java Spring app:
  check_variable "$SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES" "SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES"

  # Set variables for Java Spring app:
  export SPRING_PROFILES_ACTIVE="$profiles"
  export SECHUB_TARGETTYPE_DETECTION_INTRANET_HOSTNAME_ENDSWITH="intranet.example.org"
  export SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY="0"
  export SECHUB_INITIALADMIN_USERID="$ADMIN_USERID"
  export SECHUB_INITIALADMIN_APITOKEN="$ADMIN_APITOKEN"
  export SECHUB_INITIALADMIN_EMAIL="sechubadm@example.org"
  export SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE="$SECHUB_MAX_FILE_UPLOAD_SIZE"
  export SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE="$SECHUB_MAX_FILE_UPLOAD_SIZE"
  export SECHUB_ADAPTER_NETSPARKER_USERID="abc"
  export SECHUB_ADAPTER_NETSPARKER_APITOKEN="xyz"
  export SECHUB_ADAPTER_NETSPARKER_BASEURL="https://example.org"
  export SECHUB_ADAPTER_NETSPARKER_DEFAULTPOLICYID="example"
  export SECHUB_ADAPTER_NETSPARKER_LICENSEID="example"
  export SECHUB_ADAPTER_NESSUS_DEFAULTPOLICYID="example"
  export SECHUB_NOTIFICATION_EMAIL_ADMINISTRATORS="example@example.org"
  export SECHUB_NOTIFICATION_EMAIL_FROM="example@example.org"
  export SECHUB_NOTIFICATION_SMTP_HOSTNAME="example.org"

  echo "Starting up SecHub server"
  java $java_debug_options \
    -Dfile.encoding=UTF-8 \
    -Dserver.port=8443 \
    -Dserver.address=0.0.0.0 \
    -jar /sechub/sechub-server*.jar
}

startup_server() {
  check_variable "$SPRING_PROFILES_ACTIVE" "SPRING_PROFILES_ACTIVE"

  # Initial job scheduling settings
  init_scheduler_settings

  if [ -n "$SPRING_DATASOURCE_URL" ] ; then
    check_variable "$SPRING_DATASOURCE_USERNAME" "SPRING_DATASOURCE_USERNAME"
    check_variable "$SPRING_DATASOURCE_PASSWORD" "SPRING_DATASOURCE_PASSWORD"
    cat - <<EOF
Database:
- connection string: $SPRING_DATASOURCE_URL
- database user: $SPRING_DATASOURCE_USERNAME
EOF
  fi

  if [ -n "${SECHUB_SERVER_SSL_KEYSTORE_ALIAS}" -a "${SECHUB_SERVER_SSL_KEYSTORE_ALIAS}" != "undefined" ] ; then
    # Create symlink to .p12 keystore file
    ln -s /sechub/secrets/secret-ssl/sechub_server_ssl_keystore_file ${SECHUB_SERVER_SSL_KEYSTORE_LOCATION}
    cat - <<EOF
SSL server certificate:
- alias: $SECHUB_SERVER_SSL_KEYSTORE_ALIAS
- location: $SECHUB_SERVER_SSL_KEYSTORE_LOCATION
EOF
  fi

  cat - <<EOF
SecHub server settings:
- Activated Spring profiles: $SPRING_PROFILES_ACTIVE
- base url: $SECHUB_SERVER_BASEURL
- administration url: $SECHUB_SERVER_ADMINISTRATION_BASEURL
- Upload source code maximum size: $SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE
- Upload binaries maximum bytes: $SECHUB_UPLOAD_BINARIES_MAXIMUM_BYTES
- Job scheduling is activated every ${SECHUB_CONFIG_TRIGGER_NEXTJOB_DELAY}ms
- Job scheduling initial delay: ${SECHUB_CONFIG_TRIGGER_NEXTJOB_INITIALDELAY}ms

Starting up SecHub server
EOF
  java $java_debug_options \
    -Dfile.encoding=UTF-8 \
    -XX:InitialRAMPercentage=50 \
    -XX:MaxRAMPercentage=80 \
    -XshowSettings:vm \
    -jar /sechub/sechub-server*.jar
}

#####################################
echo "Starting run script: run.sh $@"
echo "Java version:"
java --version

if [ "$JAVA_ENABLE_DEBUG" = "true" ] ; then
  JAVA_DEBUG_PORT=15023
  echo "# Opening port $JAVA_DEBUG_PORT for Java debugging"
  java_debug_options="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:$JAVA_DEBUG_PORT"
fi

if [ "$S3_ENABLED" = "true" ] ; then
  # Set storage variables for Java Spring app:
  export SECHUB_STORAGE_S3_ENDPOINT="$S3_ENDPOINT"
  export SECHUB_STORAGE_S3_BUCKETNAME="$S3_BUCKETNAME"
  export SECHUB_STORAGE_S3_ACCESSKEY="$S3_ACCESSKEY"
  export SECHUB_STORAGE_S3_SECRETKEY="$S3_SECRETKEY"

  cat - <<EOF
Using S3 object storage:
- Endpoint: $S3_ENDPOINT
- Bucket: $S3_BUCKETNAME
EOF
fi

# Startup SecHub server
case "$SECHUB_START_MODE" in
  localserver) localserver ;;
  server) startup_server ;;
  *) wait_loop ;;
esac

if [ "$KEEP_CONTAINER_ALIVE_AFTER_CRASH" = "true" ] ; then
  wait_loop
fi
