import java.io.Console;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;
import java.util.logging.Logger;

import oracle.okv.exception.OKVException;
import oracle.okv.kmip.OKVCryptoContext;
import oracle.okv.kmip.OKVTagEnum;
import oracle.okv.response.OKVDecryptResponse;
import oracle.okv.response.OKVEncryptResponse;
import oracle.okv.service.OKVService;
public class KmipClient {
    public static final Scanner input = new Scanner(System.in);
    public static final String msg = "\nPress Enter to continue.. ";
    public static final String CLASS_NAME = KmipClient.class.getName();
    public static final Logger logger = Logger.getLogger(CLASS_NAME);

    /**
     * Read endpoint password.
     *
     * @return value read
     */
    public static char[] readPswd() {

        Console console = System.console();
        char[] value = null;
        if (console != null) {
            value = console.readPassword();
        } else {
            String valStr = input.nextLine();
            if (valStr != null && !valStr.isEmpty()) {
                value = valStr.toCharArray();
            }
        }
        if (value != null && value.length == 0) {
            return null;
        }

        return value;
    }

    /**
     * Create a connection to Oracle Key vault using auto-login endpoint
     */

    public static OKVService okvSetup() throws OKVException {

        /* Set the connection configuration */
        OKVService okvService = OKVService.okvEnvSetConfig();

        /* Explicitly open the connection to the Oracle Key Vault server */
        okvService.okvConnect();

        return okvService;
    }

    /*
     * encrypt data with key identified by p_uid
     */

    public static String encrypt(String p_uid, OKVService p_service, String p_plainText)
    {
        StringWriter sw = new StringWriter(); 
        String v_encryptedData = "";

        try {
            /* Set crypto context for encrypt operation */ 
           OKVCryptoContext cryptoContext = OKVCryptoContext.okvCryptoContextCreate(OKVTagEnum.OPERATION_ENCRYPT);
           cryptoContext.setBlockCipherMode(OKVTagEnum.BLOCK_CIPHER_MODE_CBC);
           cryptoContext.setPadding(OKVTagEnum.PADDING_METHOD_PKCS5);
           String iv = "5432109876543210";
           cryptoContext.setIV(iv.getBytes());
           String data = p_plainText;
           
           OKVEncryptResponse encryptedDataResponse = p_service.okvEncrypt(p_uid, data.getBytes(), cryptoContext);
           byte[] encryptedData = encryptedDataResponse.getEncryptedData();
           for (byte encryptedDataByte : encryptedData) {
                v_encryptedData = v_encryptedData + String.format("%02X", encryptedDataByte);
           }
           
        } catch (OKVException e) {
            System.err.println("Failed to complete the Oracle Key Vault " +
                               "operation. " + e.getLocalizedMessage());
            e.printStackTrace(new PrintWriter(sw));
            logger.info(sw.toString());
        }
        return v_encryptedData;
    }

    /*
     * Decrypt data using key with UUID p_uid
     */

    public static String decrypt(String p_uid, OKVService p_service, String p_cipherText)
    {
        StringWriter sw = new StringWriter(); 
        String v_decryptedData = null;
        /*
        *  HEX to byte
        */

        byte[] v_encryptedData = new byte[p_cipherText.length()/2];
        for (int i = 0; i < p_cipherText.length(); ++i) {
        if ((i+1)*2 <= p_cipherText.length()) {
            v_encryptedData[i] = (byte) Integer.parseInt(p_cipherText.substring(i*2, (i+1)*2), 16);
        }
}


        try {
            /* Set crypto context for decrypt operation */
            String iv = "5432109876543210";
            OKVCryptoContext cryptoContext = OKVCryptoContext
            .okvCryptoContextCreate(OKVTagEnum.OPERATION_DECRYPT);
            cryptoContext.setBlockCipherMode(OKVTagEnum.BLOCK_CIPHER_MODE_CBC);
            cryptoContext.setPadding(OKVTagEnum.PADDING_METHOD_PKCS5);
            cryptoContext.setIV(iv.getBytes());

            OKVDecryptResponse decryptedDataResponse = p_service.okvDecrypt(
                   p_uid, v_encryptedData, cryptoContext);
            byte[] decryptedData = decryptedDataResponse.getDecryptedData();
            v_decryptedData = new String(decryptedData);
        }  catch (OKVException e) {
            System.err.println("Failed to complete the Oracle Key Vault " +
                               "operation. " + e.getLocalizedMessage());
            e.printStackTrace(new PrintWriter(sw));
            logger.info(sw.toString());
        }
        return v_decryptedData;
    }

    public static void main(String[] args) throws  Exception {
    
        StringWriter sw = new StringWriter();
	int argsc = args.length;
        String v_output = null;
        String v_uuid = null;
        String v_input = null;
	String v_dbname = null;
        OKVService okvService = null;

	if (argsc < 3)
	       throw new IllegalArgumentException("Usage: KmipClient [encrypt|decrypt] UUID PWD INPUT {OUTPUT}");


        try {
           /* Create a connection to Oracle Key Vault */
           okvService = okvSetup();
           v_uuid = args[1];
	   switch(args[0]) {
	   case "encrypt": 
              v_input = args[2];
	      v_output = args[3];
	      System.out.println("Encrypt input : " + v_input);
              String encryptedMessage = encrypt(v_uuid, okvService,v_input );
	      System.out.println("Encrypt output : " + encryptedMessage);
	      /* overwrite content of key.bin in case of key rotation */
              FileWriter writer = new FileWriter(v_output,false); 
	      writer.write(encryptedMessage);
	      writer.close();
	      break;	
	   case "decrypt":
	      String v_cyphertext = "";
              v_input = args[2];
	      FileReader fileReader = new FileReader(v_input);
              int i;
              while ((i = fileReader.read()) != -1) {
	         v_cyphertext = v_cyphertext + (char)i;
	      }
              String decryptedMessage = decrypt(v_uuid, okvService, v_cyphertext);
              System.out.println(decryptedMessage);
	      break;
	   default:
	   }
        } 
      	// Exception Thrown 
      	catch (IOException e) {
            System.out.println("An error occurred while writing"
                               + " to the file: " + e.getMessage());
        } catch (OKVException e) {
           System.err.println("Failed to complete the Oracle Key Vault " +
                               "operation. " + e.getLocalizedMessage());
           e.printStackTrace(new PrintWriter(sw));
           logger.info(sw.toString());
        } 

    }
}
