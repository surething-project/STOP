package pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import pt.ulisboa.tecnico.captor.captorsharedlibrary.utils.Base64Util;

public class CryptographyUtil {
    private static final String TAG = "CryptoUtil";

    private static final String CRYPTO_ALGORITHM = "RSA/ECB/PKCS1Padding";
    private static final String CRYPTO_KEY_TYPE = "RSA";
    private static final int CRYPTO_HEY_TYPE_SIZE = 2048;
    private static final String CRYPTO_OBJECT_KEY = "AES";
    private static final int CRYPTO_OBJECT_KEY_SIZE = 256;
    private static final String CRYPTO_SIGNATURE = "SHA256withRSA";

    protected CryptographyUtil() {}

    public static PrivateKey loadPrivateKey(String fileName) {
        try {
            InputStream input = new FileInputStream(new File(fileName));

            byte[] data = new byte[input.available()];
            input.read(data);
            String key = new String(data);
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM.getBytes());

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return (PrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey loadPublicKey(String fileName) {
        try {
            InputStream input = new FileInputStream(new File(fileName));

            byte[] data = new byte[input.available()];
            input.read(data);
            String key = new String(data);
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (PublicKey) keyFactory.generatePublic(keySpec);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(CRYPTO_KEY_TYPE);
            keyGen.initialize(CRYPTO_HEY_TYPE_SIZE, new SecureRandom());
            return keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            //Log.e(TAG, "Invalid algorithm for key gen");
            System.out.println(TAG + "Invalid algorithm for key gen");
        }
        return null;
    }

    public static String publicKeyToString(PublicKey pk) {
        try {       // Transform public key to string
            return Base64Util.encodeString(pk.getEncoded());
        } catch (Exception e) {
            //Log.e(TAG, e.getLocalizedMessage());
            System.out.println(TAG + e.getLocalizedMessage());
        }
        return null;
    }

    public static PublicKey publicKeyFromString(String str) {
        return publicKeyFromByteArray(Base64Util.decodeString(str));
    }

    private static PublicKey publicKeyFromByteArray(byte[] bytes) {
        try {
            return KeyFactory.getInstance(CRYPTO_KEY_TYPE).generatePublic(new X509EncodedKeySpec(bytes));
        } catch (InvalidKeySpecException e) {
            //Log.e(TAG, "public key from array " + e.getLocalizedMessage());
            System.out.println(TAG + ": public key from array " + e.getLocalizedMessage());
        } catch (NoSuchAlgorithmException e) {
            //Log.e(TAG, "public key from array " + e.getLocalizedMessage());
            System.out.println(TAG + ": public key from array " + e.getLocalizedMessage());

        }
        return null;
    }

    public static Object encryptObject(final PublicKey publicKey, final Serializable obj) {
        try {
            /**
             * Generate AES key for data
             */
            KeyGenerator keyGen =  KeyGenerator.getInstance(CRYPTO_OBJECT_KEY);
            keyGen.init(CRYPTO_OBJECT_KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();
            /**
             * Encrypt data with AES
             */
            Cipher c = Cipher.getInstance(CRYPTO_OBJECT_KEY);
            c.init(Cipher.ENCRYPT_MODE, secretKey);
            SealedObject encypted = new SealedObject(obj, c);
            /**
             * Encrypt AES key with RSA public key
             */
            c = Cipher.getInstance(CRYPTO_ALGORITHM);
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] key = c.doFinal(secretKey.getEncoded());
            /**
             * Create and return object with encrypted AES key and encrypted data
             */
            return new EncryptedObject(key, encypted);

            // Convert into byte array
            /*ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            ObjectOutputStream objStream = new ObjectOutputStream(byteArray);
            objStream.writeObject(encypted);
            return byteArray.toByteArray();
            */
        } catch (InvalidKeyException ke) {
            //Log.e(TAG, "Invalid key. More info: " + ke.getLocalizedMessage());
            System.out.println(TAG + ": Invalid key. More info: " + ke.getLocalizedMessage());

        } catch (BadPaddingException be) {
            //Log.e(TAG, "Could not encrypt AES key: " + be.getLocalizedMessage());
            System.out.println(TAG + ": Could not encrypt AES key: " + be.getLocalizedMessage());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException
                | IllegalBlockSizeException | IOException e) {
            //Log.e(TAG, "Caught exception encrypting object: " + e.getLocalizedMessage());
            System.out.println(TAG + ": Caught exception encrypting object: " + e.getLocalizedMessage());

        }
        return null;
    }

    public static Object decryptObject(final PrivateKey privateKey, Object encrypted) {
        try {
            /*ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
            ObjectInputStream objStream = new ObjectInputStream(byteArray);
            SealedObject encrypted = (SealedObject) objStream.readObject();*/
            //SealedObject encrypted = (SealedObject) encrypt;
            //return encrypted.getObject(privateKey);
            /**
             * Decrypt AES key
             */
            EncryptedObject obj = (EncryptedObject) encrypted;
            byte[] key = obj.getKey();
            Cipher c = Cipher.getInstance(CRYPTO_ALGORITHM);
            c.init(Cipher.DECRYPT_MODE, privateKey);
            SecretKey secretKey = new SecretKeySpec(c.doFinal(key), CRYPTO_OBJECT_KEY);
            /**
             * Decrypt Sealed Object with AES key and return object
             */
            return obj.getSealedObject().getObject(secretKey);

        } catch (InvalidKeyException ke) {
            //Log.e(TAG, "Invalid key. More info: " + ke.getLocalizedMessage());
            System.out.println(TAG + ": Invalid key. More info: " + ke.getLocalizedMessage());
        } catch (NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException be) {
            //Log.e(TAG, "Could not decrypt AES key: " + be.getLocalizedMessage());
            System.out.println(TAG + ": Could not decrypt AES key: " + be.getLocalizedMessage());

        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
            //Log.e(TAG, "Caught exception decrypting object: " + e.getLocalizedMessage());
            System.out.println(TAG + ": Caught exception decrypting object: " + e.getLocalizedMessage());

        }
        return null;
    }


    public static String signObject(Object object, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance(CRYPTO_SIGNATURE);
            signer.initSign(privateKey);
            /**
             * Transform object to byte[]
             */
            signer.update(SerializationUtils.serialize((Serializable) object));
            byte[] signature = signer.sign();
            /**
             * return signature byte array in string
             */
            return Base64Util.encodeString(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            //Log.e(TAG, "Cound not create signature: " + e.getLocalizedMessage());
            System.out.println(TAG + ": Could not create signature: " + e.getLocalizedMessage());

        }
        return null;
    }

    public static boolean verifySignature(Object object, PublicKey publicKey, String signature) {
        try {
            Signature verifier = Signature.getInstance(CRYPTO_SIGNATURE);
            verifier.initVerify(publicKey);
            /**
             * Insert byte array of object to be checked
             */
            verifier.update(SerializationUtils.serialize((Serializable) object));
            /**
             * verify with byte array of signature string
             */
            byte[] sig = Base64Util.decodeString(signature);
            return verifier.verify(sig);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            //Log.e(TAG, "Could not validate signature: " + e.getLocalizedMessage());
            System.out.println(TAG + ": Could not validate signature: " + e.getLocalizedMessage());
        } catch (Exception e) {
            //Log.e(TAG, "Unknown exception (probably fake signature?): " + e.getLocalizedMessage());
            System.out.println(TAG + ": Unknown exception (probably fake signature?): " + e.getLocalizedMessage());
        }
        return false;
    }

    /*
    public static byte[] encrypt(byte[] publicKey, byte[] input)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        PublicKey key = KeyFactory.getInstance(CRYPTO_ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));
        Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(input);
    }

    public static byte[] decrypt(byte[] privateKey, byte[] input) throws NoSuchAlgorithmException,
            BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, InvalidKeySpecException {
        PrivateKey key = KeyFactory.getInstance(CRYPTO_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        Cipher cipher = Cipher.getInstance(CRYPTO_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(input);
    }

    public static byte[] encryptObj(byte[] publicKey, Object obj) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objStream = new ObjectOutputStream(byteStream);
        byteStream.
        return null;    // NOT COMPLETE
    }*/

}
