import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.security.Signature;

import pt.gov.cartaodecidadao.PTEID_ByteArray;
import pt.gov.cartaodecidadao.PTEID_Certificate;
import pt.gov.cartaodecidadao.PTEID_EIDCard;
import pt.gov.cartaodecidadao.PTEID_Pin;
import pt.gov.cartaodecidadao.PTEID_Pins;
import pt.gov.cartaodecidadao.PTEID_PublicKey;
import pt.gov.cartaodecidadao.PTEID_ReaderContext;
import pt.gov.cartaodecidadao.PTEID_ReaderSet;
import pt.gov.cartaodecidadao.PTEID_ulwrapper;

public class mainPIN {
    public static void main(String[] args){
        String s = readCard(args[0]);
        if(s == null){
            System.out.println("null");
        }
        else{
            System.out.println(s);
        }
    }

    public static String readCard(String message) {
        String fields;
        try {
            System.loadLibrary("pteidlibj");
            PTEID_ReaderSet.initSDK(); //initiate the middleware

            PTEID_ReaderSet readerSet = PTEID_ReaderSet.instance();
            PTEID_ReaderContext context = readerSet.getReader();;
            PTEID_EIDCard card = context.getEIDCard();

            PTEID_ulwrapper triesLeft = new PTEID_ulwrapper(-1);
            PTEID_Pins pins = card.getPins();
            PTEID_Pin pin = pins.getPinByPinRef(PTEID_Pin.SIGN_PIN);

            if (pin.verifyPin("", triesLeft, true)) {
                // generate message hash SHA256
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] data_hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));

                // convert hash in PTEID object
                PTEID_ByteArray byteArray = new PTEID_ByteArray(data_hash, data_hash.length);

                try {
                    // sign the hash with the card
                    PTEID_ByteArray signature = card.Sign(byteArray, true);

                    // get signature certificate
                    //PTEID_EIDCard eidCard = context.getEIDCard();
                    //PTEID_Certificate signatureCertificate = eidCard.getAuthentication();
                    byte[] eidCertificate = card.getSignature().getCertData().GetBytes();

                    // type of certificate
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    InputStream in = new ByteArrayInputStream(eidCertificate);

                    // generate certificate (according to Java API)
                    Certificate certif = certFactory.generateCertificate(in);

                    // get certificate public key
                    PublicKey publicKey = certif.getPublicKey();

                    try (FileOutputStream fos = new FileOutputStream("signatureCC.txt")) {
                        fos.write(signature.GetBytes());
                    }

                    fields = card.getID().getDocumentNumber() + " " + Base64.getEncoder().encodeToString(signature.GetBytes()) + " " + Base64.getEncoder().encodeToString(publicKey.getEncoded());

                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    fields = null;
                }

            } else {
                System.out.println("Wrong pin");
                fields = null;
            }

            PTEID_ReaderSet.releaseSDK(); //terminate the middleware
            return fields;

        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
            return null;

        } catch (Throwable e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
