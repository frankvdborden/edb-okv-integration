export KMIP_DIR=/var/lib/edb/kmip
export OKV_HOME=$KMIP_DIR/okv
export KEY_FILE=$1
export DBNAME=$2
export UUID=$3

echo $KEY_FILE >> /tmp/decrypt.out
java -cp $KMIP_DIR/jsdk/lib/okvjsdk.jar:$KMIP_DIR/kmip KmipClient decrypt $UUID  $KEY_FILE $DBNAME
