package com.imt.api_invocations.exception;

public record CustomError(int statusCode, String message) {
}
