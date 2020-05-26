import React, {Component} from 'react';
import { Timeline } from 'antd';
import 'antd/dist/antd.css';
import './Timeline.css'
import getFakeData from './GetFakeData';

class TimelineComponent extends Component {
  state = {
    all_nodes: [],
    logs: {},
  }

  componentDidMount() {
    getFakeData()
      .then(data => {
        let all_nodes = data.all_nodes.sort();
        
        // index using timestamp
        let reverse_index_log = {};
        for (let i = 0; i < all_nodes.length; i++) {
          const node_id = all_nodes[i];
          const node_logs = data[node_id];
          for (let timestamp in node_logs) {
            if (!(timestamp in reverse_index_log)) {
              reverse_index_log[timestamp] = {}
            }
            reverse_index_log[timestamp][node_id] = node_logs[timestamp];
          }
        }

        // accumalate logs from the beginning 
        // todo: round timestamp to 100ms precision?
        let timestamps = Object.keys(reverse_index_log).sort();
        let current_logs = {} // record the logs of each node in current timestamp
        for (let i = 0; i < all_nodes.length; i++) {
          current_logs[all_nodes[i]] = []
        }
        
        let final_logs = {}
        for (let i = 0; i < timestamps.length; i++) {
          const node_logs = reverse_index_log[timestamps[i]];
          for (const node_id in node_logs) {
            current_logs[node_id] = node_logs[node_id];
          }
          final_logs[timestamps[i]] = JSON.parse(JSON.stringify(current_logs));
        }

        this.setState({
          all_nodes: all_nodes,
          logs: final_logs
        });
      })
  }

  render() {
    return (
      <div className="timeline" style={{marginLeft: "60px"}}>
        <h1>Timeline of logs</h1>
        {this.drawTimelines()}
      </div>
    );
  }

  drawTimelines =  () => {
    const timestamps = Object.keys(this.state.logs).sort();
    return (
      <Timeline>
        {
          timestamps.map(timestamp => {
            const logs = this.state.logs[timestamp];
            var date = new Date();
            date.setTime(timestamp);
            return (
              <Timeline.Item>
                <p>timestamp: {date.toString().slice(0,24)+ ":" + date.getMilliseconds()}</p>
                  {this.state.all_nodes.map(node_id => 
                    this.drawNode(node_id, logs[node_id])
                  )}
              </Timeline.Item>
            )
          })
        }
      </Timeline>
    )
  }

  drawNode = (node_id, logs) => {
    if (logs.length == 0) {
      return "";
    }
    let isLeader = logs[logs.length - 1].leader == node_id ? true : false;
    return (
      <div className="Row">
        <div className="Column" style={{width:'50px', height: '50px', backgroundColor: isLeader ? '#BCF7F7' : ''}}>
          <p>{isLeader ? 'Leader' : ''}<br/> {node_id}</p>
        </div>
        {logs.map(log => this.drawLogEntry(log))}
      </div>
    );
  }

  drawLogEntry = (log) => {
    return (
      <div className="rectangle">
        <p style={{fontSize: '10px', margin: 0}}>term: {log.term}</p>
        <p style={{fontSize: '10px', margin: 0}}>index: {log.index}</p>
        <p style={{fontSize: '20px', margin: 0}}>{log.command}</p>
        <p style={{fontSize: '10px', margin: 0}}>
          {log.key}: {log.value}
        </p>
      </div>
    )
  }
  
}
  
export default TimelineComponent;