package com.tonnfccard;

@FunctionalInterface
public interface CardApiInterface<T> {
    String accept(T t) throws Exception;
}
