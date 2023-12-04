import ERHistoryViewer as er
from pprint import pprint
import time
import json
import Debugger as deb

usernum = er.GetUserNum("errorcode02")
userstat = er.GetUserStats(usernum, 20)

deb.ExportJson(userstat, "season20stats")


