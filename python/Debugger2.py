import ERHistoryViewer as er
import Debugger as deb

input = "이이이이이이이이"
usernum = er.GetUserNum(input)
userstat = er.GetUserStats(usernum, 21)

deb.ExportJson(userstat, f"season_{input}_userstats")


