package eu.basileus.osgi.security;

import com.nimbusds.jwt.JWT;

@FunctionalInterface
public interface JwtVerifier {
	String verify(JWT jwt);
}
