import ERHistoryViewer as er
from pprint import pprint
import time
import json
import Debugger as deb

usernum = er.GetUserNum("섹시뽀짝김용주")
userstat = er.GetUserStats(usernum, 19)

deb.ExportJson(userstat, "season20stats")


