package com.inno.userservice.cache;

import static com.inno.userservice.cache.CacheConstants.USERS_CACHE;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCacheEvictor {

  private final CacheManager cacheManager;

  public void evictUserById(Long userId) {
    if (userId == null) {
      return;
    }

    Cache cache = cacheManager.getCache(USERS_CACHE);
    if (cache != null) {
      cache.evict(userId);
    }
  }
}
