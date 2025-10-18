package com.teambiund.bander.auth_server.service.update;

import com.teambiund.bander.auth_server.exceptions.CustomException;

public interface EmailConfirm {
  boolean confirmEmail(String code, String email);

  Boolean resendEmail(String email) throws CustomException;

  void generateCode(String email);

  boolean checkedConfirmedEmail(String email);
}
