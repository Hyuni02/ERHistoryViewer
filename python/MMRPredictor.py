from flask import Flask, request, jsonify
import numpy as np
import os
import json
import matplotlib.pyplot as plt

app = Flask(__name__)


@app.route('/')
def upload_MMR():
    return 'Upload mmr success'

@app.route('/upload', methods=['POST', 'GET'])
def upload_file():
    uploaded_file = request.files['file']

    if uploaded_file.filename != '':
        file_path = os.path.join('data', uploaded_file.filename)
        uploaded_file.save(file_path)

        pred = prediction()

        if len(pred) == 0:
            return 'Need More then 10 Games'

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
    parse = np.loadtxt('data/mmrRaw.csv', delimiter=',')
    # Load score data
    mmrGiven = parse[1:]

    if len(mmrGiven) < 10:
        return []

    # 주어진 (x, y) 좌표 데이터
    x = mmrGiven[:, 0] + 1  # x 값
    y = mmrGiven[:, 1]  # y 값

    x_log = np.log(1 / x)  # 1/x 대칭을 적용
    A = np.vstack((x_log, np.ones(len(x)))).T
    coefficients = np.linalg.pinv(A) @ y
    a, b = coefficients

    pred = []
    for i in range(1, int(parse[0][1]), 5):
        mmr = int(a * np.log(1 / i) + b)
        pred.append([i - 1, mmr])
    graph = np.array(pred)

    return graph


if __name__ == '__main__':
    app.run(debug=True, port=8080, host='0.0.0.0')
