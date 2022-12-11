import requests
import urllib3
import random
import subprocess
import os

from qscd.Organization_pb2 import Organization
from qscd.signature_pb2 import Signature
from qscd.LocationProofs_pb2 import CitizenCard
from qscd.LocationProofs_pb2 import CitizenCardSigned

urllib3.disable_warnings(urllib3.exceptions.SecurityWarning)

localHostUrl = "https://localhost"
qscdUrl = "https://qscd-heroku.herokuapp.com"
portQSCD = 8445

createSessionUrl = "{}/session/create".format(qscdUrl)
citizenCardSignedUrl = "{}/citizenCard/register/signed".format(qscdUrl)

token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBdXRoZW50aWNhdGlvbiIsIm9yZ2FuaXphdGlvbiI6Imluc3BlY3RvciIsImlzcyI6InN1cmVxdWFsaWZpZWQucXNjZCIsInNlc3Npb25JZCI6ImRlZmF1bHQifQ.AOSL0BnhofeZC44HfXytqmcV99Ka2oBhJD14Du0qXizzP-pAfvJ9JXSnZ6DZfrC5jK0bqfs4VMbx-Mb6Gd3XhQ"
qscd_cert = "./qscd.crt"

def serialize(protoClass):
    return protoClass.SerializeToString()

def sign(dataToSign):
    subprocess.run('javac sign.java', shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    subprocess.Popen(args=["java", "-Djava.library.path=/usr/local/lib", "sign", dataToSign], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, preexec_fn=os.setsid)

    signatureProto = Signature()
    signatureProto.value = open("signature.txt", "rb").read()
    signatureProto.cryptoAlgo = "SHA256withRSA"
    signatureProto.nonce = random.randint(0, 100000000)

    return signatureProto

def createSession():
    organization = Organization()
    organization.name = "inspector"
    return post(organization, createSessionUrl, qscd_cert).json()

def sendCitizenCardSigned(citizenCardId, sessionToken, ccSignature, publicKey):
    citizenCard = CitizenCard()
    citizenCard.id = citizenCardId

    citizenCardSignature = sign(citizenCardId)
    citizenCard.signature.CopyFrom(citizenCardSignature)

    citizenCardSigned = CitizenCardSigned()
    citizenCardSigned.citizenCard.CopyFrom(citizenCard)

    signature = Signature()
    signature.value = ccSignature
    signature.cryptoAlgo = "SHA256withRSA"
    signature.nonce = random.randint(0, 100000000)

    citizenCardSigned.signature.CopyFrom(signature)
    citizenCardSigned.publicKey = publicKey

    print(citizenCardSigned)

    return postWithToken(citizenCardSigned, citizenCardSignedUrl, sessionToken, qscd_cert)

def post(proto, url, cert):
    print(url)
    if proto is None:
        return requests.post(url, verify=False)
    return requests.post(url, data=serialize(proto), verify=False)

def postWithToken(proto, url, token, cert):
    print(url)
    headers_proto = {"Authorization": "Bearer " + token}
    if proto is None:
        return requests.post(url, headers=headers_proto, verify=False)
    return requests.post(url, data=serialize(proto), headers=headers_proto, verify=False)
