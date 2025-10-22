package com.teambiund.bander.auth_server.auth.util.generator.token;

import com.teambiund.bander.auth_server.auth.enums.Role;

/**
 * TokenProvider defines the contract for issuing and validating JWT (or token-like) strings used by
 * the Auth Server. Implementations should be fast and dependency-light.
 */
public interface TokenUtil {
  /**
   * Generate a short‑lived access token for the given user.
   *
   * @param userId user identifier to place in the token (subject)
   * @param role role granted to the user
   * @param deviceId device identifier used to tie the token to a client device
   * @return signed JWT string
   */
  String generateAccessToken(String userId, Role role, String deviceId);

  /**
   * Generate a long‑lived refresh token for the given user.
   *
   * @param userId user identifier to place in the token (subject)
   * @param role role granted to the user
   * @param deviceId device identifier used to tie the token to a client device
   * @return signed JWT string
   */
  String generateRefreshToken(String userId, Role role, String deviceId);

  /**
   * Verify signature and expiration of the token.
   *
   * @param token JWT string
   * @return true if signature is valid and token not expired; false otherwise
   */
  boolean isValid(String token);

  /**
   * Extract the user id (subject) from a token without throwing.
   *
   * @param token JWT string
   * @return user id or null when not present/invalid
   */
  String extractUserId(String token);

  /**
   * Extract role from a token without throwing.
   *
   * @param token JWT string
   * @return Role enum value or null when not present/invalid
   */
  Role extractRole(String token);

  /**
   * Extract device id from a token without throwing.
   *
   * @param token JWT string
   * @return device id or null when not present/invalid
   */
  String extractDeviceId(String token);

  long extractExpiration(String token);
}
