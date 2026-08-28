package com.maciej.spredex;

import com.maciej.spredex.Errors.ErrorType;

public record CellError(ErrorType type, String message) {}
