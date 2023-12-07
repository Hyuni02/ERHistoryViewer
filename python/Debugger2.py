import numpy as np
import matplotlib.pyplot as plt
import json
import pprint
from flask import request, jsonify


def prediction():
    parse = np.loadtxt('data/mmrRaw.csv', delimiter=',')
    # Load score data
    mmrGiven = parse[1:]
    rangex = np.array([parse[0][0], mmrGiven[-1][0]])
    rangey = np.array([min(mmrGiven[:, 1]) - 100, max(mmrGiven[:, 1]) + 100])
    start = int(min(mmrGiven[:, 0]))
    start = 0
    end = int(parse[0][1])
    data = mmrGiven
    line = [0, 0]
    A = np.vstack((data[:, 0], np.ones(len(data)))).T
    line = np.linalg.pinv(A) @ data[:, 1]

    if abs(line[0]) > 3:
        print("rapid data", line)
        # return "rapid data"

    prediction = lambda x: line[0] * x + line[1]

    pred = []
    for i in range(start, end):
        y = int(line[0] * i + line[1])
        pred.append([i, y])
        print(i,y)
    graph = np.array(pred)

    # Plot scores and the estimated line
    plt.figure()
    plt.plot(data[:, 0], data[:, 1], 'r.', label='The given data')
    plt.plot(rangex, prediction(rangex), 'b-', label='Prediction')
    plt.plot(graph[:, 0], graph[:, 1], 'y.', label='Prediction')
    plt.xlabel('Midterm scores')
    plt.ylabel('Final scores')
    plt.xlim(rangex)
    plt.ylim(rangey)
    plt.grid()
    plt.legend()
    plt.show()
    return graph

def prediction2():
    parse = np.loadtxt('data/mmrRaw.csv', delimiter=',')
    # Load score data
    mmrGiven = parse[1:]

    # 주어진 (x, y) 좌표 데이터
    x = parse[0][1] - mmrGiven[:,0] + 1  # x 값
    y = mmrGiven[:,1]  # y 값

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

    # todo 정점 뽑기
    # 자바에서 파싱할 때 <시작날짜 + 0번값 - 1 , mmr>

    # 실제 데이터와 좌우 대칭된 로그 함수의 그래프를 그립니다.
    plt.scatter(x, y, label='Original Data')
    plt.plot(x_range, y_log_symmetric, color='green', label='Symmetric Log Function')
    plt.plot(x_range, y_log_symmetric, color='yellow', label='points')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.legend()
    plt.show()

prediction2()

