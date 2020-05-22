import json
import glob
from flask import Flask , send_file
import os
from flask_cors import CORS

app = Flask (__name__)
cors = CORS(app)
@app.route('/')
def DownloadMergedJson() -> str:
    result = {}
    logs = {}
    node_ids =[]
    for f in glob.glob(os.path.join("..", "history_*.json")):
        print(str(f))
        node_ids.append(str(f).split('.')[2].split('_')[1])

    result["all_nodes"] = node_ids

    for f in glob.glob(os.path.join("..", "history_*.json")):
        node_id = str(f).split('.')[2].split('_')[1]
        with open(f, "rb") as infile:
            result[node_id] = json.load(infile)

    return result


app.run()
