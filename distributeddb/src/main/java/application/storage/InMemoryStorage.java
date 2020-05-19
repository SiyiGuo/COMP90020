package application.storage;

import raft.concurrentutil.Cu;

import java.util.HashMap;

public class InMemoryStorage implements Storage {
    private HashMap<String, String> storage;
    public InMemoryStorage() {
        this.storage = new HashMap<>();
    }
    @Override
    public String get(String key) {
        if (!(this.storage.containsKey(key))) {
            throw new RuntimeException("Key: " + key +"not in store");
        }
        return this.storage.get(key);
    }

    @Override
    public boolean update(String key, String value) {
        if (!(this.storage.containsKey(key))) {
            throw new RuntimeException("Key: " + key +"not in store");
        }
        this.storage.put(key, value);
        return true;
    }

    @Override
    public boolean put(String key, String value) {
        this.storage.put(key, value);
        return true;
    }

    @Override
    public boolean delete(String key) {
        this.storage.remove(key);
        return true;
    }
}
