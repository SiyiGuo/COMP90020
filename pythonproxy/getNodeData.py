import json
import glob
from flask import Flask , send_file

app = Flask (__name__)
@app.route('/')
def DownloadMergedJson() -> str:
    result = []
    for f in glob.glob("*.json"):
        with open(f, "rb") as infile:
            result.append(json.load(infile))

    with open("merged_node_data.json", "wb") as outfile:
        json.dump(result, outfile)

    return send_file("merged_node_data.json")


app.run()
