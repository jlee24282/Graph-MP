# -*- coding: utf-8 -*-
"""
Created on Mon Feb  6 12:40:00 2017

@author: JLee
"""

from PIL import Image
import glob
import json

NAME = 'testDoubleCircle'
DOWNSIZENUM = '2'

def pgmTopng():
    #for imgDir in glob.glob("Images/ImageData/test/*"):
    for imgDir in glob.glob("Images/ImageData/faces/"+ NAME +"/*"):
        try:
            img = Image.open(imgDir)
            imgDir = imgDir.replace('pgm','png').replace('ImageData/','ImageData/pngFiles/')
            #imgDir = 'pngFiles/'+ imgDir
            #print imgDir
            print imgDir
            img.save(imgDir)
        except:
            pass
        
def pgmTopngTest():
    #for imgDir in glob.glob("Images/ImageData/test/*"):
    for imgDir in glob.glob("Images/ImageData/ImageFiles/Test/*"):
        try:
            img = Image.open(imgDir)
            imgDir = imgDir.replace('pgm','png').replace('ImageData/','ImageData/pngFiles/')
            #imgDir = 'pngFiles/'+ imgDir
            #print imgDir
            print imgDir
            img.save(imgDir)
        except:
            pass
        
def resultPicture():
    im = Image.open('Results/output_'+NAME+'_'+DOWNSIZENUM+'.png').convert('RGB')         
    pixelMap = im.load()

    resultNodes = [1541,1542,1543,1544,1545,1546,1547,1548,1549,1550,1551,1552,1553,1554,1555,1556,1557,1558,1559,1560,1561,1562,1563,1564,1565,1566,1567,1631,1632,1696,1760,1761,1825,1826,1827,1828,1892,1956,2020,2021,2085,2149,2150,2151,2152,2216,2280,2344,2408,2472,2536,2600,2664,2728,2792,2856,2920,2984,2985,3049,3050,3051,3115,3179,3243,3307,3369,3370,3371,3433,3495,3496,3497,3559,3561,3623,3687,3751,3752,3753,3754,3818]
    
    w=im.size[0]
    h=im.size[1]
    
    
    for item in resultNodes:
        pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('Results/result_'+NAME+'_'+DOWNSIZENUM+'.png') 
    im.close()

        
def resultPictureTest():
    im = Image.open('Results/testDoubleCircle.png').convert('RGB')         
    pixelMap = im.load()
    with open("/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/TEST") as f:
        text_file = f.readlines()
    line_num = 0
    results = []
    for i in range(len(text_file)):
        if text_file[i][30:40]==" test star":
            line_num = i
    for i in range(line_num, len(text_file)):
        if i%3 == 0:
            if text_file[i+1][0] != "-":
                print text_file[i+1]
                
                results.append([text_file[i].replace("}", "").split("{")[1].rstrip(), float(text_file[i+1].split(":")[1])])
    results = sorted(results, key=lambda x: x[1])
    
    w=im.size[0]
    h=im.size[1]    
    
    for i in range(10):
        resultNodes = []
        for item in results[i][0].split(","):
            resultNodes.append(int(item))
        print resultNodes
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
    # resultNodes = [145,173,174,175,202,203,232,259,260,261,262,288,289,318,348,374,375,376,377,378,404]
    # print results
    
    
    # for item in resultNodes:
    #     pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('Results/result_'+ NAME +'.png') 
    im.close()


def resultPicturePrintAll():
    im = Image.open('Results/'+ NAME + '.png').convert('RGB')         
    pixelMap = im.load()
    with open('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/'+ NAME) as f:
        text_file = f.readlines()
    results = []

    for line in text_file:
        if line.startswith('Current result: '):
            jsonList = json.loads(line.replace('Current result: ', '').replace('{', '[').replace('}', ']').replace('\n', ''))
            results.append(jsonList)
            
    w=im.size[0]    
    
    for i in range(len(results)):
        resultNodes = results[i]
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
    
    im.show()       
    im.save('Results/result_'+ NAME +'.png') 
    im.close()

    
def resultPicturePrintBest():
    im = Image.open('Results/'+NAME+'.png').convert('RGB')         
    pixelMap = im.load()

    resultNodes = [48, 77, 78, 79, 108, 109, 110, 113, 140, 141, 142, 143, 144, 170, 172, 202]
    
    w=im.size[0]
    h=im.size[1]
    
    
    for item in resultNodes:
        pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('Results/result_'+NAME+'.png') 
    im.close()

def main():    
    #pgmTopngTest()
    resultPicturePrintAll()
    #resultPicturePrintBest()#resultPictureTest()
    
    
if __name__ == '__main__':
    main()