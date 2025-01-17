package application.storage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.UUID;
import raft.logmodule.RaftLogEntry;

public class JsonLogStorage implements LogStorage {

    private ArrayList<JSONObject> logs = new ArrayList<>();
    private String logName = UUID.randomUUID().toString() + ".json";
    private JSONObject historyLogs = new JSONObject();

    @Override
    public void add(long timestamp, RaftLogEntry logEntry, int leaderId) {
        JSONObject log = logEntry.toJson();
        log.put("timestamp", timestamp);
        log.put("leader", leaderId);
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

        this.historyLogs.put(this.logs.get(this.logs.size() - 1).get("timestamp"), jsLogs);

        try (FileWriter file = new FileWriter("history_"+this.logName)){
            file.write(historyLogs.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
