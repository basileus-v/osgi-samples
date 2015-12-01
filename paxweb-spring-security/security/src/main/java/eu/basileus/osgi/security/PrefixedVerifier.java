package eu.basileus.osgi.security;

import java.util.List;

import com.nimbusds.jwt.JWTClaimsSet;

public class PrefixedVerifier implements Verifier {
	private String prefix;
	private List<Verifier> verifiers;

	public PrefixedVerifier(String prefix, List<Verifier> verifiers) {
		this.prefix = prefix;
		this.verifiers = verifiers;
	}

	@Override
	public String verify(JWTClaimsSet claims) {
		for (Verifier v : verifiers) {
			String s = v.verify(claims);
			if (s != null) {
				return prefix + ": " + s;
			}
		}
		return null;
	}

}
