package com.imt.api_invocations.exception;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Errors {

  private List<CustomError> errors;

  public void addError(CustomError error) {
    this.errors.add(error);
  }
}
