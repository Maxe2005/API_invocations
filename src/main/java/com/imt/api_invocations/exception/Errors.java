package com.imt.api_invocations.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class Errors {

    private List<CustomError> theErrorsYOUMade;

    public void addError(CustomError error) {
        this.theErrorsYOUMade.add(error);
    }

}
