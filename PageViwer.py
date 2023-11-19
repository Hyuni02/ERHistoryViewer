import tkinter as tk

window = tk.Tk()

# 윈도우 제목 지정
window.title('ERHistoryViwer')
# 윈도우 아이콘 지정
# window.iconphoto(False, tk.PhotoImage(file="icons/???.png"))
# 윈도우 크기 지정
window.geometry("600x600")
window.resizable(False,True)

# 버튼 추가
style1 = {'bg':'green', 'fg':'black'}
style2 = {'bg':'black', 'fg':'white'}
btn_TL = tk.Button(window, text="btn_TL", **style1)
btn_TR = tk.Button(window, text="btn_TR", **style2)

# UI 배치
btn_TL.pack(side="top")
btn_TR.pack(side="right", anchor="nw")
# 윈도우 시작
window.mainloop()

