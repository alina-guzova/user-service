package com.inno.userservice.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static com.inno.userservice.cache.CacheConstants.USERS_CACHE;

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
