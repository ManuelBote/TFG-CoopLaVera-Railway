package com.example.tfg_cooperativa.api;

public interface ApiCallback<T> {
    void onSuccess(T result);
    void onError(String message);
}
