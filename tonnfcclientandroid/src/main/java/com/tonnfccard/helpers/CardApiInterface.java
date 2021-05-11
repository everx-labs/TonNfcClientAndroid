package com.tonnfccard.helpers;

@FunctionalInterface
public interface CardApiInterface<T> {
    String accept(T t) throws Exception;
}
