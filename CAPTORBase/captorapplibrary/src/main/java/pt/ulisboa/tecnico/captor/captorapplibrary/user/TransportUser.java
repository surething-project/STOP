package pt.ulisboa.tecnico.captor.captorapplibrary.user;

import android.app.Activity;

import java.security.KeyPair;

import pt.ulisboa.tecnico.captor.captorapplibrary.clclients.AuthAPIClient;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography.CryptographyUtil;
import pt.ulisboa.tecnico.captor.captorsharedlibrary.auth.User;

public class TransportUser extends User {
    private static final String TAG = "TransportUser";

    private KeyPair keyPair;

    public TransportUser() {
        keyPair = CryptographyUtil.generateKeyPair();
        String publicKeyString =  CryptographyUtil.publicKeyToString(keyPair.getPublic());
        super.setPublicKey(publicKeyString);
    }

    /**
     * Registration is confirmed when AuthAPIClient.USER_CREATED intent is broadcast
     * @param activity
     */
    public void registerUser(Activity activity) {
        AuthAPIClient.getInstance().registerTransport(activity);
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
}
