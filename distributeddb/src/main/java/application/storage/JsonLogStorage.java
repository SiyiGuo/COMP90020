package application.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.UUID;
import raft.logmodule.RaftLogEntry;

public class JsonLogStorage implements LogStorage {

    private ArrayList<JSONObject> logs = new ArrayList<>();
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

    @Override
    public void setLogName(String nodeId){
        this.logName = nodeId + ".json";
    }

    private void writeJsonToFile() {
        JSONArray jsLogs = new JSONArray();

        for (JSONObject log : this.logs){
            jsLogs.add(log);
        }

        try (FileWriter file = new FileWriter(this.logName)){
            file.write(jsLogs.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
