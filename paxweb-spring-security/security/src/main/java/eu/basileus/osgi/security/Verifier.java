package eu.basileus.osgi.security;

import com.nimbusds.jwt.JWTClaimsSet;

@FunctionalInterface
public interface Verifier {
	String verify(JWTClaimsSet claims);
}
