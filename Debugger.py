import ERHistoryViewer as er
from pprint import pprint
import json


def ExportJson(js, fileName="temp"):
    file_path = f"{fileName}.json"
    pprint(js)
    file = open(file_path, "w")
    file.write(json.dumps(js, indent="\t"))

ExportJson(er.GetUserGames(er.GetUserNum("ErrorCode02")))
# ExportJson(er.GetGameData("LoadingTip"))
