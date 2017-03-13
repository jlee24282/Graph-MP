# -*- coding: utf-8 -*-
"""
Created on Mon Feb  6 12:40:00 2017

@author: JLee
"""

from PIL import Image
import glob
import json
import os

NAME = 'cheyer'
DOWNSIZENUM = '4'
PICINDEX = '0'

def pgmTopng():
    for imgDir in glob.glob('Images/ImageData/faces/'+NAME+'/*'):
        try:
            img = Image.open(imgDir)
            imgFile = imgDir.replace('pgm','png').replace('ImageData/','ImageData/pngFiles/')
            
            imgDir = imgFile[:32] + NAME
            if not os.path.exists(imgDir):
                os.makedirs(imgDir)
            
            print imgFile
            img.save(imgFile)
        except:
            pass
        
def pgmTopngTest():
    for imgDir in glob.glob('Images/ImageData/ImageFiles/faces/'+NAME+'/*'):
        try:
            img = Image.open(imgDir)
            imgFile = imgDir.replace('pgm','png').replace('ImageData/','ImageData/pngFiles/')
            
            imgDir = imgFile.replace(NAME, '').replace('png', '')
            if not os.path.exists(imgDir):
                os.makedirs(imgDir)
            
            print imgFile
            img.save(imgFile)
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
    im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')         
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
    im.save('Results/result_All_'+ NAME +'_'+ DOWNSIZENUM+'.png') 
    im.close()


def resultPicturePrintBest3():
    im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')         
    pixelMap = im.load()
    with open('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/'+ NAME+ '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
        text_file = f.readlines()
    results = []
    funcValues = []

    for line in text_file:
        if line.startswith('Current result: '):
            jsonList = json.loads(line.replace('Current result: ', '').replace('{', '[').replace('}', ']').replace('\n', ''))
            results.append(jsonList)
            
        if line.startswith('Current function value: '):
            jsonList = json.loads(line.replace('Current function value: ', '').replace('\n', ''))
            funcValues.append(jsonList)
    
            
    w=im.size[0]    
    results, funcValues = (list(x) for x in zip(*sorted(zip(results, funcValues), key=lambda pair: pair[1])))
    
    for i in range(3):
        resultNodes = results[i]
        print funcValues[i]
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
    
    im.show()       
    im.save('Results/result_best3_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
    im.close()
    
def resultPicturePrintBest():
    im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')         
    pixelMap = im.load()
    with open('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/'+ NAME + '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
        text_file = f.readlines()
    results = []

    for line in text_file:
        if line.startswith('result subgraph is: '):
            jsonList = json.loads(line.replace('result subgraph is: ', '').replace('\n', ''))
            results.append(jsonList)
            
    w=im.size[0]    
    
    for i in range(len(results)):
        resultNodes = results[i]
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
    
    im.show()       
    im.save('Results/result_Best1_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
    im.close()
    
def resultPicturePrintSingle():
    im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')         
    pixelMap = im.load()

    resultNodes = [428,429,434,458,459,460,461,462,463,465,466,490,491,493,495,496,497,498,523,524,525,554,555,557,586,587,589,618,650]
    
    w=im.size[0]
    h=im.size[1]
    
    
    for item in resultNodes:
        pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('Results/result_single_'+NAME+'.png') 
    im.close()

def resultPictureRank():        
    with open('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/'+ NAME+ '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
        text_file = f.readlines()
        
    results = []
    funcValues = []
    for line in text_file:
        if line.startswith('Current result: '):
            jsonList = json.loads(line.replace('Current result: ', '').replace('{', '[').replace('}', ']').replace('\n', ''))
            results.append(jsonList)
            
        if line.startswith('Current function value: '):
            jsonList = json.loads(line.replace('Current function value: ', '').replace('\n', ''))
            funcValues.append(jsonList)
    
    results, funcValues = (list(x) for x in zip(*sorted(zip(results, funcValues), key=lambda pair: pair[1])))
    
    for i in range(len(results)):
        im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB') 
        w=im.size[0]    
        pixelMap = im.load()
        resultNodes = results[i]
        print funcValues[i]
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
        im.show()       
        imgDir = 'Results/'+NAME + '/'
        if not os.path.exists(imgDir):
            os.makedirs(imgDir)
        im.save(imgDir + NAME+'_Rank'+str(i)+'_funcVal_'+str(funcValues[i]) +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
        
    im.close()
    
def main():    
    #pgmTopng()
    #resultPicturePrintAll()
    resultPicturePrintBest3()
    #resultPicturePrintBest()
    resultPictureRank()    
    #resultPicturePrintSingle()
    """
    for i in range(12):
        PICINDEX = str(i)
        resultPicturePrintBest3()
        resultPicturePrintBest()
    """     
    
    print 'done'
if __name__ == '__main__':
    main()