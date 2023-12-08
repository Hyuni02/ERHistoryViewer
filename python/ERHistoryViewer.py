import requests
import pandas as pd
import pprint
import apikey

key = apikey.key
baseURL = "https://open-api.bser.io/"
header = {"x-api-key": f"{key}"}

currentSeasonId = 21


# nickName으로 userNum 찾기
def GetUserNum(nickName: str):
    response_userNum = requests.get(f"{baseURL}v1/user/nickname?query={nickName}", headers=header)
    return response_userNum.json()["user"]["userNum"]


# 유저의 최근 10게임 기록
def GetUserGames(userNum, next=None):
    if next == None:
        response_userGame = requests.get(f"{baseURL}v1/user/games/{userNum}", headers=header)
    else:
        response_userGame = requests.get(f"{baseURL}v1/user/games/{userNum}?next={next}", headers=header)
    return response_userGame.json()


# 유저 통계 얻기
def GetUserStats(userNum, seasonId=currentSeasonId):
    response_userStats = requests.get(f"{baseURL}v1/user/stats/{userNum}/{seasonId}", headers=header)
    return response_userStats.json()


# 시즌별 1000위 랭커 정보 얻기
def GetRanker(seasonId=currentSeasonId):
    response_ranker = requests.get(f"{baseURL}v1/rank/top/{seasonId}/3", headers=header)
    return response_ranker.json()

# 시즌 정보 얻기
def GetSeason():
    response_season = GetGameData("Season")
    return response_season

# 유저의 최근 90일간 전투기록 얻기

# 한 매치에 대한 정보 얻기

# 게임 내부 정보 얻기
# Character : 캐릭터 별 정보
def GetGameData(hash="hash"):
    response_meta = requests.get(f"{baseURL}v2/data/{hash}", headers=header)
    return response_meta.json()


# 게임 언어 정보 획득
def GetLanguageData(language="Korean"):
    response_language = requests.get(f"{baseURL}v1/l10n/{language}",headers=header)
    download(response_language.json()["data"]["l10Path"], f"Language_{language}.txt")

# 게임 언어 파일 다운로드
def download(url, file_name):
    with open(file_name, "wb") as file:   # open in binary mode
        response = requests.get(url)               # get request
        file.write(response.content)      # write to file

# 게임 상세 정보 획득
def GetGameDetail(gameid):
    response_gamedetail = requests.get(f"{baseURL}v1/games/{gameid}", headers=header)
    return response_gamedetail.json()