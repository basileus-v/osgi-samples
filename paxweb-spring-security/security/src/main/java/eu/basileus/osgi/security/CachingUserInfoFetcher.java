package eu.basileus.osgi.security;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.mitre.openid.connect.client.UserInfoFetcher;
import org.mitre.openid.connect.model.PendingOIDCAuthenticationToken;
import org.mitre.openid.connect.model.UserInfo;
import org.springframework.security.authentication.AuthenticationServiceException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CachingUserInfoFetcher extends UserInfoFetcher {
	
	private LoadingCache<PendingOIDCAuthenticationToken,UserInfo> cache;
	public CachingUserInfoFetcher() {
		cache = CacheBuilder.newBuilder()
			.maximumSize(10000)
			.expireAfterAccess(100, TimeUnit.MINUTES)
			.build(CacheLoader.from(super::loadUserInfo));
	}
	
	@Override
	public UserInfo loadUserInfo(PendingOIDCAuthenticationToken token) {
		try {
			return cache.get(token);
		} catch (ExecutionException e) {
			throw new AuthenticationServiceException("Could not load userinfo", e);
		}
	}
}
