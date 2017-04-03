# -*- coding: utf-8 -*-
"""
Created on Mon Feb  6 12:40:00 2017

@author: JLee

"""

from PIL import Image
import glob
import json
import os
import re

NAME = 'ch4f'
DOWNSIZENUM = '4'
PICINDEX = '0'


##################################################################
#   pgmTopng
#-----------------------------------------------------------------
#   Converting pgm files to png
#       INPUT:  all files from "Images/ImageData/faces/NAME/"
#       OUTPUT: Images/ImageData/pngFiles/faces/NAME/filename.png
###################################################################
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
  

##################################################################
#   resultPictureTest 
#-----------------------------------------------------------------
#   Print all truesubgraph to the Test picture
#       INPUT:  ResultPics/testDoubleCircle.png
#       OUTPUT: /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/TEST
###################################################################
def resultPictureTest():
    im = Image.open('ResultPics/testDoubleCircle.png').convert('RGB')         
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
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('Results/result_'+ NAME +'.png') 
    im.close()


##################################################################
#   resultPicturePrintAll 
#-----------------------------------------------------------------
#   Print all truesubgraph to the sunglasses picture
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           Neutral sunglass picture to draw the glasses
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME
#           GraphMP result txt file
#       OUTPUT: ResultPics/result_All_NAME_DOWNSIZENUM.png
###################################################################
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
    im.save('ResultPics/result_All_'+ NAME +'_'+ DOWNSIZENUM+'.png') 
    im.close()


##################################################################
#   resultPicturePrintBest3 
#-----------------------------------------------------------------
#   Print best3 based on score function truesubgraph to the sunglasses picture
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           Neutral sunglass picture to draw the glasses
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME
#           GraphMP result txt file
#       OUTPUT: ResultPics/result_All_NAME_DOWNSIZENUM.png
###################################################################
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
    im.save('ResultPics/result_best3_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
    im.close()
    
    
##################################################################
#   resultPicturePrintBest3Rank 
#-----------------------------------------------------------------
#   Print best3 subgraphs(judged by human Eye) - uses rank number (on file name)
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           Neutral sunglass picture to draw the glasses
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME
#           GraphMP result txt file
#       OUTPUT: ResultPics/result_All_NAME_DOWNSIZENUM.png
###################################################################
def resultPicturePrintBest3Rank():
    
    for imdir in glob.glob('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultPics/*'):
        NAME = re.search('ResultPics/(.*)_4', imdir)
        NAME = NAME.group(1)
        results = []
        ranks = []
        
        if not NAME.startswith('result'):            
            print NAME
            for filename in glob.glob(imdir + '/*'):
                onesubset = []             
                rank = re.search('_Rank(.*)_funcVal', filename)
                rank = int(rank.group(1))
                ranks.append(rank)
                        
                oneImage = Image.open(filename)
                pixelMap2 = oneImage.load()
                w = oneImage.size[0] 
                h = oneImage.size[1]
                for index in  range(960):
                    #print pixelMap2[index% oneImage.size[0], int(index/oneImage.size[1])]
                    if pixelMap2[index% oneImage.size[0], int(index/oneImage.size[0])] == (255, 0, 0):
                        onesubset.append(index)

                results.append(onesubset)
                #print onesubset
                oneImage.close()
                #print results
            
            #print results
                results, ranks = (list(x) for x in zip(*sorted(zip(results, ranks), key=lambda pair: pair[1])))
    
    
            im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')            
            pixelMap = im.load()
            w=im.size[0]   
            
            for i in range(3):
                resultNodes = results[i] 
                for item in resultNodes:
                    pixelMap[item%w, int(item/w)] = (255, 0, 0)
                    
            im.show()       
            im.save('ResultPics/result_best3_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
            im.close()
            
    
##################################################################
#   resultPicturePrintBest 
#-----------------------------------------------------------------
#   Print best(based on the score function) truesubgraph to the sunglasses picture
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           Neutral sunglass picture to draw the glasses
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME
#           GraphMP result txt file
#       OUTPUT: ResultPics/result_All_NAME_DOWNSIZENUM.png
###################################################################
    
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
    im.save('ResultPics/result_Best1_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
    im.close()
    
##################################################################
#   resultPicturePrintSingle 
#-----------------------------------------------------------------
#   Print single subset to the sunglasses picture
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           Neutral sunglass picture to draw the glasses
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME
#           GraphMP result txt file
#       OUTPUT: ResultPics/result_All_NAME_DOWNSIZENUM.png
###################################################################
def resultPicturePrintSingle():
    im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB')         
    pixelMap = im.load()

    resultNodes = [428,429,434,458,459,460,461,462,463,465,466,490,491,493,495,496,497,498,523,524,525,554,555,557,586,587,589,618,650]
    
    w=im.size[0]
    h=im.size[1]
    
    
    for item in resultNodes:
        pixelMap[item%w, int(item/w)] = (255, 0, 0)
    im.show()       
    im.save('ResultPics/result_single_'+NAME+'.png') 
    im.close()

##################################################################
#   resultPictureRank 
#-----------------------------------------------------------------
#   Generate png files with each subgraph, from graphMP result
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME_DOWNSIZENUM_PICINDEX
#           graphMP result txt file
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           neutral sunglasses pictuer to put subgraph
#       OUTPUT: ResultPics/NAME_DOWNSIZENUM/NAME_Rank(i)_funcVal_funcValues[i]_DOWNSIZENUM_Sparse sparsity[i]).png 
###################################################################
def resultPictureRank():        
    with open('/Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/'+ NAME+ '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
        text_file = f.readlines()
        
    results = []
    funcValues = []
    sparsity = []
    
    for line in text_file:
        if line.startswith('Current result: '):
            jsonList = json.loads(line.replace('Current result: ', '').replace('{', '[').replace('}', ']').replace('\n', ''))
            results.append(jsonList)
            
        if line.startswith('Current function value: '):
            jsonList = json.loads(line.replace('Current function value: ', '').replace('\n', ''))
            funcValues.append(jsonList)
            
        if line.startswith('s**********************************************: '):
            jsonList = json.loads(line.replace('s**********************************************: ', '').replace('\n', ''))
            sparsity.append(jsonList)
    
    results, funcValues, sparsity = (list(x) for x in zip(*sorted(zip(results, funcValues, sparsity), key=lambda pair: pair[1])))
    
    for i in range(len(results)):
        im = Image.open('Images/ImageData/pngFiles/faces/'+ NAME + '/'+ NAME+'_straight_neutral_sunglasses_'+DOWNSIZENUM+'.png').convert('RGB') 
        w=im.size[0]    
        pixelMap = im.load()
        resultNodes = results[i]
        print funcValues[i]
        for item in resultNodes:
            #pixelMap[item%w, int(item/w)] = (255, i*30, i*30)
            pixelMap[item%w, int(item/w)] = (255, 0, 0)
        #im.show()       
        imgDir = 'ResultPics/'+ NAME + '_'+str(DOWNSIZENUM)+ '/'
        if not os.path.exists(imgDir):
            os.makedirs(imgDir)
        im.save(imgDir + NAME+'_Rank'+str(i)+'_funcVal_'+str(funcValues[i]) +'_'+ DOWNSIZENUM+ '_Sparse' + str(sparsity[i])+'.png') 
        
    im.close()
    
    
##################################################################
#   main 
#-----------------------------------------------------------------
#   Driver
###################################################################
def main():    
    #pgmTopng()
    #resultPicturePrintAll()
    resultPictureRank()    
    resultPicturePrintBest3Rank()
    #resultPicturePrintBest()
    #resultPicturePrintSingle()
    
    print 'done'
if __name__ == '__main__':
    main()