export KMIP_DIR=/var/lib/edb/pg_okv
export OKV_HOME=/opt/oracle/okvpostgres
export UUID=$1
export KEY_FILE=$2

java -cp $KMIP_DIR/jsdk/lib/okvjsdk.jar:$KMIP_DIR/kmip KmipClient decrypt $UUID  $KEY_FILE 
