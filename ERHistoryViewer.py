import requests

key = "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U"

baseURL="https://open-api.bser.io/"
header = {"x-api-key":f"{key}"}

# nickName으로 userNum 찾기
def GetUserNum(nickName):
    response_userNum = requests.get(f"{baseURL}v1/user/nickname?query={nickName}", headers=header)
    print("userNum :", response_userNum.json()["user"]["userNum"])
    return response_userNum.json()["user"]["userNum"]

