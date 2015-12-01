package eu.basileus.osgi.security;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;

public interface JwtValidationServiceProvider {
	JWTSigningAndValidationService getValidator(String jwksUri);
}
