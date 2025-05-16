export KMIP_DIR=/var/lib/edb/kmip
export OKV_HOME=$KMIP_DIR/okv
export KEYFILE=$1
export UUID=$2

read CLEAR_TEXT

java -cp $KMIP_DIR/jsdk/lib/okvjsdk.jar:$KMIP_DIR/kmip KmipClient encrypt $UUID "$CLEAR_TEXT" $KEYFILE
