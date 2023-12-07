from flask import Flask, request, jsonify
import numpy as np
import os
import json
import matplotlib.pyplot as plt
import Debugger2 as d2

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

    # 주어진 (x, y) 좌표 데이터
    x = parse[0][1] - mmrGiven[:, 0] + 1  # x 값
    y = mmrGiven[:, 1]  # y 값

    # 로그 변환을 수행합니다.
    x_log = np.log(1 / x)  # 1/x 대칭을 적용

    # 선형 회귀를 위해 데이터를 변형합니다.
    A = np.vstack((x_log, np.ones(len(x)))).T
    coefficients = np.linalg.pinv(A) @ y

    # 계산된 계수를 이용하여 좌우 대칭된 로그 함수의 파라미터를 구합니다.
    a, b = coefficients

    # 좌우 대칭된 로그 함수 그래프를 그리기 위해 x 값의 범위를 정의합니다.
    x_range = np.linspace(min(x), parse[0][1], 100)
    # 좌우 대칭된 로그 함수의 y 값을 계산합니다.
    y_log_symmetric = a * np.log(1 / x_range) + b  # 1/x 대칭을 적용

    pred = []
    for i in range(min(x), parse[0][1]):
        mmr = int(a * np.log(1 / i) + b)
        pred.append([i, mmr])
    graph = np.array(pred)

    return graph


if __name__ == '__main__':
    app.run(debug=True, port=8080, host='0.0.0.0')
