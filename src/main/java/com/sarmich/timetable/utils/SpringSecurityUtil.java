package com.sarmich.timetable.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class SpringSecurityUtil {

  public static int getProfileId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
    return userDetails.getProfileEntity().getId();
  }

  public static String getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }

  public static UserDetails getUserDetails() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (UserDetails) authentication.getPrincipal();
  }
}
