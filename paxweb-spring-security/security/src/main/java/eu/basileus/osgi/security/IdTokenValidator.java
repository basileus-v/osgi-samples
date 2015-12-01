package eu.basileus.osgi.security;

import java.text.ParseException;
import java.util.Arrays;
import java.util.function.BiFunction;

import org.mitre.jwt.signer.service.JWTSigningAndValidationService;
import org.mitre.openid.connect.client.service.ServerConfigurationService;
import org.mitre.openid.connect.config.ServerConfiguration;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@AllArgsConstructor
public class IdTokenValidator {

	private ServerConfigurationService serverConfigurationService;
	private JwtValidationServiceProvider validationServiceProvider;
	//private ClientConfigurationService clientConfigurationService;
	private int timeSkewAllowance = 1;

	public String validateCommon(JWT token, BiFunction<JWTClaimsSet, ServerConfiguration, String> customValidator) {

		JWTClaimsSet claims = null;

		try {
			claims = token.getJWTClaimsSet();
		} catch (ParseException e) {
			return "Could not parse";
		}

		String issuer = claims.getIssuer();

		ServerConfiguration serverConfig = serverConfigurationService.getServerConfiguration(issuer);

		// check the signature
		JWTSigningAndValidationService jwtValidator = null;

		Algorithm tokenAlg = token.getHeader().getAlgorithm();

		if (token instanceof PlainJWT) {
			return "Unsigned tokens are not supported";
		} else if (token instanceof SignedJWT) {

			SignedJWT signedToken = (SignedJWT) token;

			if (tokenAlg.equals(JWSAlgorithm.HS256) || tokenAlg.equals(JWSAlgorithm.HS384)
					|| tokenAlg.equals(JWSAlgorithm.HS512)) {
				return "Symmetric signing algorithms are not supported";
			} else {
				// otherwise load from the server's public key
				jwtValidator = validationServiceProvider.getValidator(serverConfig.getJwksUri());
			}

			if (jwtValidator != null) {
				if (!jwtValidator.validateSignature(signedToken)) {
					return "Signature validation failed";
				}
			} else {
				return "Unable to find an appropriate signature validator";
			}
		}

		return customValidator.apply(claims, serverConfig);
	}
	
	private String idTokenCustomValidate(JWTClaimsSet claims, ServerConfiguration serverConfig) {
		//RegisteredClient clientConfig = clientConfigurationService.getClientConfiguration(serverConfig);
		PrefixedVerifier verifier = new PrefixedVerifier("", Arrays.asList(
												Verifiers.issuer(serverConfig), 
												Verifiers.expirationTime(timeSkewAllowance),
												Verifiers.notBeforeTime(timeSkewAllowance), 
												Verifiers.issueTime(timeSkewAllowance)));
												//Verifiers.audience(clientConfig)));
		return verifier.verify(claims);
	}
	
	public String validate(JWT idToken) {
		String error = validateCommon(idToken, this::idTokenCustomValidate);
		return error != null ? "Id token " + error : null; 
	}
}
