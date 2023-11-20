import tkinter as tk
import page_main

window = tk.Tk()

# 화면 : 메인
def Show_Main():
    # UI 생성
    lab_logo = tk.Label(window, text="[LOGO]")
    etr_userName = tk.Entry(window)
    lab_reuslt = tk.Label(window, text="")
    btn_search = tk.Button(window, text='search', command=lambda: SearchUser(etr_userName.get()))

    # UI 배치
    lab_logo.pack()
    etr_userName.pack()
    lab_reuslt.pack()
    btn_search.pack()

def SearchUser(userName):
    print(userName)



# 윈도우 제목 지정
window.title('ERHistoryViwer')
# 윈도우 아이콘 지정
# window.iconphoto(False, tk.PhotoImage(file="icons/winicon.png"))
# 윈도우 크기 지정
window.geometry("600x600+50+50")
window.resizable(False, True)

Show_Main()

# 윈도우 시작
window.mainloop()
