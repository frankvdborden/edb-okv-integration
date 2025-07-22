export KMIP_DIR=/var/lib/edb/pg_okv
export OKV_HOME=/opt/oracle/okvpostgres
export UUID=$1
export KEYFILE=$2

read CLEAR_TEXT

java -cp $KMIP_DIR/jsdk/lib/okvjsdk.jar:$KMIP_DIR/kmip KmipClient encrypt $UUID "$CLEAR_TEXT" $KEYFILE
