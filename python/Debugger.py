import ERHistoryViewer as er
from pprint import pprint
import time
import json


def ExportJson(js, fileName="temp", printjs=False):
    file_path = f"{fileName}.json"
    if printjs: pprint(js)
    file = open(file_path, "w")
    file.write(json.dumps(js, indent="\t"))
    print(file_path)
    time.sleep(1)


# ExportJson(er.GetUserGames(er.GetUserNum("ErrorCode02")))
# ExportJson(er.GetGameData("LoadingTip"))

input_username = '한동그라미'
usernum = er.GetUserNum(input_username)
season = er.GetSeason()
ExportJson(season, "./jsons/season")

lastseason = season["data"][len(season["data"]) - 1]["seasonID"]
userstat = er.GetUserStats(usernum, lastseason)
username = userstat["userStats"][0]["nickname"]

ExportJson(userstat, f"./jsons/userstat_{username}_{lastseason}")
next = None
for i in range(99):
    usergames = er.GetUserGames(usernum, next)
    # ExportJson(usergames, f"./jsons/usergames_{input_username}_{i}")
    ExportJson(usergames, f"./jsons/usergames_{username}_{i}")
    try:
        next = usergames["next"]
    except:
        print("no next")
        break

print("done")