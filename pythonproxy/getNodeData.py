import json
import glob
from flask import Flask , send_file

app = Flask (__name__)
@app.route('/')
def DownloadMergedJson() -> str:
    result = {}
    logs = {}
    node_ids =[]
    for f in glob.glob("history_*.json"):
        node_ids.append(str(f).split('.')[0].split('_')[1])

    result["all_nodes"] = node_ids

    for f in glob.glob("history_*.json"):
        node_id = str(f).split('.')[0].split('_')[1]
        with open(f, "rb") as infile:
            result[node_id] = json.load(infile)

    return result


app.run()
