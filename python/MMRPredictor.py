from flask import Flask

app = Flask(__name__)

@app.route('/')
def upload_MMR():
    return 'Upload mmr success'

@app.route('/connectioncheck',methods=['GET'])
def connectCheck():
    return 'connection check success'

if __name__ == '__main__':
    app.run(debug=True,port=8080,host='0.0.0.0')