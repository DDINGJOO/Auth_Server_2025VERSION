package com.teambiund.bander.auth_server.auth.enums;

public enum AppType {
  GENERAL,       // 일반 앱 (USER, GUEST, PLACE_OWNER 모두 로그인 가능)
  PLACE_MANAGER  // 공간관리자 앱 (PLACE_OWNER만 로그인 가능)
}
