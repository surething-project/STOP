import base64

import os
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import rsa

AES_key_length = 256

def generateKeyPair():
    private_key = rsa.generate_private_key(public_exponent=65537, key_size=2048, backend=default_backend())
    storeKeyPair(private_key, private_key.public_key())

def storeKeyPair(privateKey, publicKey):
    private_pem = privateKey.private_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PrivateFormat.PKCS8,
        encryption_algorithm=serialization.NoEncryption()
    )
    with open('crypto/inspector_private_key.pem', 'wb') as f:
        f.write(private_pem)

    public_pem = publicKey.public_bytes(
        encoding=serialization.Encoding.PEM,
        format=serialization.PublicFormat.SubjectPublicKeyInfo)

    with open('crypto/inspector_public_key.pem', 'wb') as f:
        f.write(public_pem)

def generate_AES_secret_key():
    secret_key = os.urandom(AES_key_length)
    print(len(secret_key))
    encoded_secret_key = base64.b64encode(secret_key)
    return encoded_secret_key

def store_AES_key(key):
    with open('AESKey_256.psk', 'wb') as f:
        f.write(key)


if __name__ == '__main__':
    generateKeyPair()