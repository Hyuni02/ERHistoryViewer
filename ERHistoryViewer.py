import requests
import pprint

key = "AcUmvv9Rtp2aOoVKiDnqP4gdVzeqiTVYahP9Xi6U"
baseURL="https://open-api.bser.io/"
header = {"x-api-key":f"{key}"}

currentSeasonId = 19

# nickName으로 userNum 찾기
def GetUserNum(nickName):
    response_userNum = requests.get(f"{baseURL}v1/user/nickname?query={nickName}", headers=header)
    print("userNum :", response_userNum.json()["user"]["userNum"])
    return response_userNum.json()["user"]["userNum"]


# 유저의 최근 10게임 기록
def GetUserGames(userNum):
    response_userGame = requests.get(f"{baseURL}v1/user/games/{userNum}", headers=header)
    print(response_userGame.json())
    return

# 유저 통계 얻기
def GetUserStats(userNum, seasonId):
    response_userStats = requests.get(f"{baseURL}v1/user/stats/{userNum}/{seasonId}", headers=header)
    print(response_userStats.json())
    return

# 시즌별 1000위 랭커 정보 얻기
def GetRanker(seasonId):
    response_ranker = requests.get(f"{baseURL}rank/top/{seasonId}/3")
    return

# 유저의 최근 90일간 전투기록 얻기

# 한 매치에 대한 정보 얻기

# 게임 내부 정보 얻기



#
#def GetCharacterName(characterNum):
    # response_characterName = requests.get()