import java.security.interfaces.RSAPrivateKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class sign {
    public static void main(String[] args){
        String s = sign(args[0]);
        if(s == null){
            System.out.println("null");
        }
        else{
            System.out.println(s);
        }
    }

    public static String sign(String message) {
        PrivateKey privateKey = loadPrivateKey();

        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());
            byte[] generatedSignature = signature.sign();

            try (FileOutputStream fos = new FileOutputStream("signature.txt", false)) {
                fos.write(generatedSignature);
            }

            return "";

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PrivateKey loadPrivateKey() {
        try {
            File file = new File("crypto/inspect_private_key.pem");
            InputStream input = new FileInputStream(file);

            byte[] data = new byte[input.available()];
            input.read(data);
            String key = new String(data);
            String privateKeyPEM = key
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);

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

    public static PublicKey loadPublicKey() {
        try {
            File file = new File("crypto/inspect_public_key.pem");
            InputStream input = new FileInputStream(file);

            byte[] data = new byte[input.available()];
            input.read(data);
            String key = new String(data);
            String publicKeyPEM = key
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
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
}
