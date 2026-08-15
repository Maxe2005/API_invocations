package com.imt.api_invocations.config;

import org.slf4j.MDC;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class XRequestIdHolder {

  private static final ThreadLocal<String> X_REQUEST_ID = new ThreadLocal<>();

  public static void setRequestId(String requestId) {
    X_REQUEST_ID.set(requestId);
    if (requestId != null) {
      MDC.put("X-Request-Id", requestId);
    }
  }

  public static String getRequestId() {
    String id = X_REQUEST_ID.get();
    if (id != null) {
      return id;
    }
    // Fall back to extracting from current HTTP request attributes
    try {
      ServletRequestAttributes attributes =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attributes != null) {
        return attributes.getRequest().getHeader("X-Request-Id");
      }
    } catch (Exception e) {
      // Ignore - will return null below
    }
    return null;
  }

  public static void clearRequestId() {
    X_REQUEST_ID.remove();
    MDC.remove("X-Request-Id");
  }
}