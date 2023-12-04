import urllib.request
import csv

f = open("characterindex.csv",'r', encoding="utf-8")
rdr = csv.reader(f)
for line in rdr:
    url = f"https://cdn.dak.gg/assets/er/game-assets/1.9.0/CharResult_{line[2]}_S000.png"
    print(url)
    savelocation = f"./imgs/{line[2].lower()}.png"  # 내컴퓨터의 저장 위치
    print(savelocation)
    urllib.request.urlretrieve(url, savelocation)  # 해당 url에서 이미지를 다운로드 메소드
