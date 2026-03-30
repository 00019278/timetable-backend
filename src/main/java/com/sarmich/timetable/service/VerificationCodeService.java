package com.sarmich.timetable.service;

import com.sarmich.timetable.exception.InvalidOperationException;
import com.sarmich.timetable.model.SmsCache;
import com.sarmich.timetable.utils.CacheName;
import com.sarmich.timetable.utils.CommonUtils;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class VerificationCodeService {
    private final CacheManager cacheManager;
    private final Random random = new Random();

    public SmsCache generateAndStore(String email) {
        Cache cache = cacheManager.getCache(CacheName.SMS_CACHE);
        if (cache == null) return null;
        SmsCache smsCache = cache.get(email.toLowerCase(), SmsCache.class);
        if (smsCache == null) {
            Integer code = CommonUtils.generateRandomDigits();
            smsCache = new SmsCache(email, code);
            cache.put(email.toLowerCase(), smsCache);
        }
        return smsCache;
    }

    public void validate(String email, Integer code) {
        Cache cache = cacheManager.getCache(CacheName.SMS_CACHE);
        if (cache == null) throw new InvalidOperationException("Cache not found");
        SmsCache smsCache = cache.get(email.toLowerCase(), SmsCache.class);
        if (smsCache == null) {
            throw new InvalidOperationException("Time expired");
        }
        if (!smsCache.code().equals(code)) {
            throw new InvalidOperationException("Invalid code");
        }
    }
}
