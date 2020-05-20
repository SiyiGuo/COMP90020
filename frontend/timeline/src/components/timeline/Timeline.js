import React, {Component} from 'react';
import { Timeline } from 'antd';
import 'antd/dist/antd.css';
import './Timeline.css'
import getFakeData from './GetFakeData';

class TimelineComponent extends Component {
  state = {
    all_nodes: [],
    last_logs: {},
    following_logs: {}
  }

  componentDidMount() {
    getFakeData()
      .then(data => {
        let all_nodes = data.all_nodes;
        let last_logs = data.init_status;
        let following_logs = data.following_logs;

        // comcat log entries 
        let timeStamps = Object.keys(following_logs).sort();
        let cum_logs = {0: {}};
        for (const index in all_nodes) {
          cum_logs[0][all_nodes[index]] = [last_logs[all_nodes[index]]];
        }

        let temp = JSON.parse(JSON.stringify(cum_logs[0]));
        for (let i = 0; i < timeStamps.length; i++) {
          let logs = following_logs[timeStamps[i]];
          for (let node_id in logs) {
            temp[node_id].push(logs[node_id]);
          }
          cum_logs[timeStamps[i]] = temp;
          temp = JSON.parse(JSON.stringify(cum_logs[timeStamps[i]]));
        }

        this.setState({
          all_nodes: all_nodes,
          last_logs: last_logs,
          following_logs: cum_logs,
        });
      })
  }

  render() {
    return (
      <div className="timeline">
        <Timeline>
          {this.drawTimeline()}
        </Timeline>
      </div>
    );
  }

  drawTimeline =  () => {
    const timestamps = Object.keys(this.state.following_logs).sort()
    return (
      timestamps.map(key => {
        const logs = this.state.following_logs[key];
        return (
          <Timeline.Item>
            <p>timestamp: {key === 0 ? 'Initial' : key}</p>
            {
              Object.keys(logs).map((node_id, index) => {
                const entries = logs[node_id];
                return (
                  <div className="Row">
                    <div 
                      className="Column" 
                      style={{
                          width:'70px', 
                          backgroundColor: entries[entries.length - 1].state == 'leader' ? 'red' : ''
                          }}>
                      <p>
                          {entries[entries.length - 1].state}<br/>{node_id}
                      </p>
                    </div>
                    {
                      this.drawNode(entries)
                    }
                  </div>
                );
              })
            }
          </Timeline.Item> 
        );
      })
    );
  }

  drawNode = (entries) => {
    return entries.map(
      logEntry => {
        if (!logEntry) return "";
        let currentCommittedIndex = entries[entries.length - 1].committed_index;
        return this.drawLogEntry(logEntry, currentCommittedIndex)
      }
    )
  }

  drawLogEntry = (log, currentCommittedIndex) => {
    return (
      <div className="rectangle" style={{backgroundColor: log.index > currentCommittedIndex ? 'yellow' : '#BBFF33'}}>
        <p style={{fontSize: '10px', margin: 0}}>term: {log.term}</p>
        <p style={{fontSize: '10px', margin: 0}}>index: {log.index}</p>
        <p style={{fontSize: '20px', margin: 0}}>{log.log_entry.command}</p>
        <p style={{fontSize: '10px', margin: 0}}>
          {log.log_entry.key}: {log.log_entry.value}
        </p>
      </div>
    )
  }
  
}
  
export default TimelineComponent;