package com.example.simplecrud.Exception;

public class DuplicateException extends Exception{
    public DuplicateException() {
        super("동일한 ID가 존재합니다. 다른아이디로 진행해주십시오.");
    }
}
