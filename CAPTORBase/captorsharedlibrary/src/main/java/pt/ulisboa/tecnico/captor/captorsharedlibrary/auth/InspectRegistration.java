package pt.ulisboa.tecnico.captor.captorsharedlibrary.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

public class InspectRegistration {

	@NotNull
	@JsonProperty("publicKey")
	private String publicKey;

	@JsonProperty("publicKey")
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
}
