package application.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.UUID
import raft.logmodule.RaftLogEntry;

public class JsonLogStorage implements LogStorage {

    private ArrayList<JSONObject> logs = new ArrayList<JSONObject>();
    private String logName = UUID.randomUUID().toString() + ".json";

    @Override
    public void add(long timestamp, RaftLogEntry logEntry) {
        JSONObject log = logEntry.toJson();
        log.put("timestamp", timestamp);
        this.logs.add(log);
        this.writeJsonToFile();
    }

    @Override
    public void removeOnStartIndex(long timestamp, Long startIndex) {
        this.logs.remove(startIndex);
        this.writeJsonToFile();
    }

    private void writeJsonToFile() {
        JSONArray jsLogs = new JSONArray(this.logs);
        try {
            StringWriter out = new StringWriter();
            jsLogs.writeJSONString(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
