package com.sionic.demo.config.jwt.exception

class NotFoundTokenException : RuntimeException("토큰을 헤더에서 찾을 수 없습니다.")