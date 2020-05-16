package application.storage;

public interface Storage {
    String get(String key);
    boolean update(String key, String value);
    boolean put(String key, String value);
    boolean delete(String key);
}
