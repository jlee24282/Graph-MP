# -*- coding: utf-8 -*-
"""
Created on Mon Apr 10 18:05:07 2017

@author: JLee
"""

from PIL import Image
import glob
import json
import os
import re


NAME = 'an2i_left_happy_sunglasses'
DOWNSIZENUM = '4'
PICINDEX = '0'

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
def rankAll():  
    for imdir in glob.glob('ResultData/*'):
        NAME = re.search('ResultData/(.*)_4', imdir)
        NAME = NAME.group(1)
        
        with open('ResultData/'+ NAME+ '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
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
            im = Image.open('Images/ImageData/pngFiles/AllSunglasses/'+NAME+'_'+DOWNSIZENUM+'.png').convert('RGB') 
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
#   resultPictureRank 
#-----------------------------------------------------------------
#   Generate png files with each subgraph, from graphMP result
#       INPUT:  /Users/JLee/Documents/workspace/Graph-MP/data/PixelData/RealData/ResultData/NAME_DOWNSIZENUM_PICINDEX
#           graphMP result txt file
#       INPUT:  Images/ImageData/pngFiles/faces/NAME/NAME_straight_neutral_sunglasses_DOWNSIZENUM.png
#           neutral sunglasses pictuer to put subgraph
#       OUTPUT: ResultPics/NAME_DOWNSIZENUM/NAME_Rank(i)_funcVal_funcValues[i]_DOWNSIZENUM_Sparse sparsity[i]).png 
###################################################################
def rankAllforManual():        
    with open('ResultData/'+ NAME+ '_' +DOWNSIZENUM + '_' + PICINDEX) as f:
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
        im = Image.open('Images/ImageData/pngFiles/AllSunglasses/'+NAME+'_'+DOWNSIZENUM+'.png').convert('RGB') 
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
    
    for imdir in glob.glob('ResultPics/*'):
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
    
    
            PERSON = NAME.split('_')
            PERSON = PERSON[0]
            print PERSON
            im = Image.open('Images/ImageData/pngFiles/faces/'+ PERSON  +'/'+ NAME+'_'+DOWNSIZENUM+'.png').convert('RGB')            
            pixelMap = im.load()
            w=im.size[0]   
            
            for i in range(3):
                resultNodes = results[i] 
                for item in resultNodes:
                    pixelMap[item%w, int(item/w)] = (255, 0, 0)
                    
            #im.show()       
            im.save('ResultPics/result_best3_'+ NAME +'_'+ DOWNSIZENUM+ '_' + PICINDEX+'.png') 
            im.close()
            
            
##################################################################
#   main 
#-----------------------------------------------------------------
#   Driver
###################################################################
def main():    
    #rankAll()    
    resultPicturePrintBest3Rank()
    print 'done'
    
if __name__ == '__main__':
    main()
    
