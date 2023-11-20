import tkinter

window = tkinter.Tk()
window.title("Frame_Change")
window.geometry("600x600+200+200")

frame1 = tkinter.Frame(window)
frame2 = tkinter.Frame(window)
frame3 = tkinter.Frame(window)

frame1.grid(row=0, column=0, sticky="nsew")
frame2.grid(row=0, column=0, sticky="nsew")
frame3.grid(row=0, column=0, sticky="nsew")

def openFrame(frame):
    frame.tkraise()


btnToFrame1 = tkinter.Button(frame3,
                             text="Change To Frame1",
                             padx=10,
                             pady=10,
                             command=lambda: [openFrame(frame1)])
btnToFrame2 = tkinter.Button(frame1,
                             text="Change To Frame2",
                             padx=10,
                             pady=10,
                             command=lambda: [openFrame(frame2)])
btnToFrame3 = tkinter.Button(frame2,
                             text="Change To Frame3",
                             padx=10,
                             pady=10,
                             command=lambda: [openFrame(frame3)])

btnToFrame1.pack()
btnToFrame2.pack()
btnToFrame3.pack()

openFrame(frame1)  # 기본메인화면
window.mainloop()