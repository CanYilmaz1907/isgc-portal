package com.isgc.portal.security;

import java.util.Base64;

final class Base64Url {
  private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

  private Base64Url() {}

  static String encode(byte[] bytes) {
    return URL_ENCODER.encodeToString(bytes);
  }
}


