package com.sarmich.timetable.service;

import com.sarmich.timetable.exception.InvalidOperationException;
import com.sarmich.timetable.exception.handler.ErrorCode;
import com.sarmich.timetable.model.GetCodeResponse;
import com.sarmich.timetable.model.SmsCache;
import com.sarmich.timetable.utils.CacheName;
import com.sarmich.timetable.utils.CommonUtils;
import java.time.Instant;
import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class VerificationCodeService {
  private final CacheManager cacheManager;
  private final EmailService emailService;

  public GetCodeResponse generateAndStore(String email, Integer time) {
    Cache cache = cacheManager.getCache(CacheName.SMS_CACHE);
    if (cache == null) return null;
    SmsCache smsCache = cache.get(email.toLowerCase(), SmsCache.class);
    if (smsCache == null) {
      Integer code = CommonUtils.generateRandomDigits();
      smsCache = new SmsCache(email, code, Instant.now().plusSeconds(time));
      cache.put(email.toLowerCase(), smsCache);
      emailService.sendVerificationEmail(smsCache.email(), code);
    }
    return new GetCodeResponse(smsCache.email(), smsCache.time());
  }

  public void validate(String email, Integer code) {
    Cache cache = cacheManager.getCache(CacheName.SMS_CACHE);
    if (cache == null)
      throw new InvalidOperationException(
          ErrorCode.INVALID_OPERATION_ERROR_CODE, "Cache not found");
    SmsCache smsCache = cache.get(email.toLowerCase(), SmsCache.class);
    if (smsCache == null) {
      throw new InvalidOperationException(ErrorCode.INVALID_OPERATION_ERROR_CODE, "Time expired");
    }
    if (!smsCache.code().equals(code)) {
      throw new InvalidOperationException(ErrorCode.INVALID_OPERATION_ERROR_CODE, "Invalid code");
    }
  }
}
