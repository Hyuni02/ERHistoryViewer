from flask import Flask, request, jsonify
import numpy as np
import os
import matplotlib.pyplot as plt


app = Flask(__name__)

@app.route('/')
def upload_MMR():
    return 'Upload mmr success'

@app.route('/connectioncheck',methods=['GET'])
def connectCheck():
    return 'connection check success'

@app.route('/upload',methods=['POST','GET'])
def upload_file():
    # file = request.files['file']
    #
    # if file:
    #     filename = 'data/mmr_raw.csv'
    #     file.save(filename)
    #     # todo mmr분석 시작
    #
    # return file.name
    uploaded_file = request.files['file']

    if uploaded_file.filename != '':
        file_path = os.path.join('data', uploaded_file.filename)
        uploaded_file.save(file_path)
        return jsonify({'file_name': uploaded_file.filename}), 200
    else:
        return jsonify({'error': 'No file selected'}), 400



if __name__ == '__main__':
    app.run(debug=True,port=8080,host='0.0.0.0')