package com.sakrafux.realworld.core.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.token.reader.HttpHeaderTokenReader;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
@Replaces(HttpHeaderTokenReader.class)
public class RealWorldTokenReader extends HttpHeaderTokenReader {

    @Override
    protected String getPrefix() {
        return "Token";
    }

    @Override
    protected String getHeaderName() {
        return "Authorization";
    }

}
