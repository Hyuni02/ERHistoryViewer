import numpy as np
import matplotlib.pyplot as plt
import json
import pprint
from flask import request, jsonify


def prediction():
    parse = np.loadtxt('data/mmr.csv', delimiter=',')
    # Load score data
    mmrGiven = parse[1:]
    rangex = np.array([mmrGiven[0][0], mmrGiven[-1][0]])
    rangey = np.array([min(mmrGiven[:, 1]) - 100, max(mmrGiven[:, 1]) + 100])
    start = int(min(mmrGiven[:, 0]))
    end = int(parse[0][1])
    data = mmrGiven
    # Estimate a line, final = slope * midterm + y_intercept
    line = [0, 0]

    # TODO) Please find the best [slope, y_intercept] from 'data'
    A = np.vstack((data[:, 0], np.ones(len(data)))).T
    line = np.linalg.pinv(A) @ data[:, 1]
    prediction = lambda x: line[0] * x + line[1]

    pred = []
    print(start, end)
    for i in range(start, end):
        y = int(line[0] * i + line[1])
        pred.append([i, y])
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


g = prediction()
x = g[:,0]
y = g[:,1]
x1 = []
y2 = []
for i in x:
    x1.append(int(i))
for j in y:
    y2.append(int(j))
point = {
    'x': x1,
    'y': y2
}
r = json.dumps(point)
print(r)
