package pt.ulisboa.tecnico.captor.captorapplibrary.proofs;

import java.io.Serializable;

public class SignedProofRequest implements Serializable {

    private String signature;
    private ProofRequest proofRequest;

    public SignedProofRequest(String signature, ProofRequest proofRequest) {
        this.signature = signature;
        this.proofRequest = proofRequest;
    }

    public String getSignature() {
        return signature;
    }

    public ProofRequest getProofRequest() {
        return proofRequest;
    }
}
