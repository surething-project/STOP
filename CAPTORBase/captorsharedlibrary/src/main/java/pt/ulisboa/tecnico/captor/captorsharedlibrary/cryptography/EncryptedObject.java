package pt.ulisboa.tecnico.captor.captorsharedlibrary.cryptography;

import java.io.Serializable;

import javax.crypto.SealedObject;

public class EncryptedObject implements Serializable {

    private byte[] key;
    private SealedObject sealedObject;

    public EncryptedObject(byte[] key, SealedObject sealedObject) {
        this.key = key;
        this.sealedObject = sealedObject;
    }

    public byte[] getKey() {
        return key;
    }

    public SealedObject getSealedObject() {
        return sealedObject;
    }
}
