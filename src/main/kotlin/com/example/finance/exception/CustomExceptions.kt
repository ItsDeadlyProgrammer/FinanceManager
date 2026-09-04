package com.example.finance.exception

class ResourceNotFoundException(message: String) : RuntimeException(message)

class ResourceAlreadyExistsException(message: String) : RuntimeException(message)

class ForbiddenAccessException(message: String) : RuntimeException(message)

class BadRequestException(message: String) : RuntimeException(message)

class UnauthorizedException(message: String) : RuntimeException(message)
