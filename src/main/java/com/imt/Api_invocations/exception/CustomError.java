package com.imt.Api_invocations.exception;

public record CustomError(int statusCode, String message) {
}
