from flask import Flask, request, jsonify
import numpy as np
import os
import json
import matplotlib.pyplot as plt

app = Flask(__name__)


@app.route('/')
def upload_MMR():
    return 'Upload mmr success'


@app.route('/connectioncheck', methods=['GET'])
def connectCheck():
    return 'connection check success'


@app.route('/upload', methods=['POST', 'GET'])
def upload_file():
    uploaded_file = request.files['file']

    if uploaded_file.filename != '':
        file_path = os.path.join('data', uploaded_file.filename)
        uploaded_file.save(file_path)

        pred = prediction()
        x = []
        y = []
        for i in pred[:, 0]:
            x.append(int(i))
        for j in pred[:, 1]:
            y.append(int(j))
        return jsonify({'x': x, 'y': y}), 200
    else:
        return jsonify({'error': 'No file selected'}), 400


def prediction():
    parse = np.loadtxt('data/mmr.csv', delimiter=',')
    # Load score data
    mmrGiven = parse[1:]
    start = int(min(mmrGiven[:, 0]))
    end = int(parse[0][1])
    data = mmrGiven

    A = np.vstack((data[:, 0], np.ones(len(data)))).T
    line = np.linalg.pinv(A) @ data[:, 1]

    pred = []
    for i in range(start, end):
        y = int(line[0] * i + line[1])
        pred.append([i, y])
    graph = np.array(pred)
    return graph


if __name__ == '__main__':
    app.run(debug=True, port=8080, host='0.0.0.0')
