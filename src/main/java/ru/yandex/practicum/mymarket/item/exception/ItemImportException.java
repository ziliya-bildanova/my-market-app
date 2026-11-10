package ru.yandex.practicum.mymarket.item.exception;

public class ItemImportException extends RuntimeException {

    public ItemImportException(String message) {
        super(message);
    }

    public ItemImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
