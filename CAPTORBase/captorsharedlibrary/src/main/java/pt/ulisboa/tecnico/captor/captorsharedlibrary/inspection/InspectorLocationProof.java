package pt.ulisboa.tecnico.captor.captorsharedlibrary.inspection;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class InspectorLocationProof implements Serializable {

	@JsonProperty("proofMessage")
    @Embedded
    private InspectorLocationProofMessage proofMessage;

	@Column(columnDefinition="TEXT")
	@JsonProperty("signature")
    private String signature;

    @Column(columnDefinition="TEXT")
    @JsonProperty("sessionId")
    private String sessionId;

    @Column(columnDefinition="TEXT")
    @JsonProperty("proverId")
    private String proverId;

    @Column(columnDefinition="TEXT")
    @JsonProperty("ccId")
    private String ccId;

    @Column(columnDefinition="TEXT")
    @JsonProperty("citizenCardSignature")
    private String citizenCardSignature;

    @Column(columnDefinition="TEXT")
    @JsonProperty("publicKey")
    private String publicKey;

	public InspectorLocationProof() {
		proofMessage = new InspectorLocationProofMessage();
	}
	
    public InspectorLocationProof(InspectorLocationProofMessage proofMessage, String signature, String sessionId,
                                  String proverId, String ccId, String citizenCardSignature, String publicKey) {
        this.proofMessage = proofMessage;
        this.signature = signature;
        this.sessionId = sessionId;
        this.proverId = proverId;
        this.ccId = ccId;
        this.citizenCardSignature = citizenCardSignature;
        this.publicKey = publicKey;
    }

    @JsonProperty("proofMessage")
    public InspectorLocationProofMessage getProofMessage() {
        return proofMessage;
    }
    public void setProofMessage(InspectorLocationProofMessage proofMessage) {
    	this.proofMessage = proofMessage;
    }

    @JsonProperty("signature")
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
    	this.signature = signature;
    }

    @JsonProperty("sessionId")
    public String getSessionId() {
        return sessionId;
    }
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @JsonProperty("proverId")
    public String getProverId() {
        return proverId;
    }
    public void setProverId(String sessionId) {
        this.proverId = proverId;
    }

    @JsonProperty("ccId")
    public String getCcId() { return ccId; }
    public void setCcId(String token) { this.ccId = ccId; }

    @JsonProperty("citizenCardSignature")
    public String getCitizenCardSignature() {
        return citizenCardSignature;
    }
    public void setCitizenCardSignature(String citizenCardSignature) {
        this.citizenCardSignature = citizenCardSignature;
    }

    @JsonProperty("publicKey")
    public String getPublicKey() {
        return publicKey;
    }
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    @Override
    public String toString() {
        return "ccId: " + " " + this.ccId + "\n" +
                "citizenCardSignature: " + " " + this.citizenCardSignature + "\n" +
                "signature: " + " " + this.signature + "\n" +
                "sessionId: " + " " + this.sessionId + "\n" +
                "proverId: " + " " + this.proverId + "\n" +
                "publicKey: " + " " + this.publicKey;
    }
}
