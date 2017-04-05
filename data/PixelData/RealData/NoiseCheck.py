# -*- coding: utf-8 -*-
"""
Created on Fri Mar 31 16:09:55 2017

@author: JLee
"""


from PIL import Image
import glob
import json
import os
import re
import numpy as np



##################################################################
#   greyValues 
#-----------------------------------------------------------------
#
###################################################################
def greyValues(imgDir):
    greyValues = []
    image = Image.open(imgDir)
    pixelMap = image.load()
    width = image.size[0] 
    height = image.size[1]
    
    for i in range (width * height):
        greyValues.append(pixelMap[i % width, i / width])
    
    
    image.close()
    return greyValues
    


##################################################################
#   noiseCheck 
#-----------------------------------------------------------------
#
###################################################################
def noiseCheckSTD4AllPeople():
    for imgDir in glob.glob('Images/ImageData/faces/*'):
        z0dir = None
        z0Pixels = []
        std = [0]* 960
        
        # zo pictures
        for OneImage in glob.glob(imgDir + '/*'):
            if '_straight_neutral_sunglasses_4' in OneImage:
                z0dir =OneImage
                z0Pixels = greyValues(OneImage)
                
        # zk pictures     (get standard deviation using z0)            
        for OneImage in glob.glob(imgDir + '/*'):
            if ('_straight_neutral_open_4' in OneImage) or \
                ('_straight_sad_open_4' in OneImage) or \
                ('_straight_angry_open_4' in OneImage) or \
                ('_up_happy_open_4' in OneImage) or \
                ('_up_neutral_open_4' in OneImage) or \
                ('_left_happy_open_4' in OneImage) or \
                ('_left_neutral_open_4' in OneImage) or \
                ('_left_sad_open_4' in OneImage) or \
                ('_right_happy_open_4' in OneImage) or \
                ('_right_sad_open_4' in OneImage) or \
                ('_right_neutral_open_4' in OneImage):
                    
                temp = greyValues(OneImage)
                diff = np.subtract(z0Pixels, temp)
                std = np.add(std, np.abs(diff))
        
        #scale zkPixelSum to 255
        std = np.multiply(np.true_divide(std, max(std)), 255)
        std = [int(i) for i in std]
        
        if 'kk49' in imgDir:
            for i in [237,269,301,332,333,334,335,336,364,368,394,395,396,400,401,402,403,404,405,427,428,437,469,470,502,534,535,567,599,631,663,695,727,759,760]:
                print z0Pixels[i]
                
        #Generate Output
        NoisePic = Image.open(z0dir).convert('RGB') 
        NoisePicDir = 'NoiseData/' + imgDir[23:] + '.png'
        width = NoisePic.size[0]
        height = NoisePic.size[1]
        pixelMap = NoisePic.load()
        #put zkPixelSum list to pixelMap
        for i in range(width * height):
            pixelMap[i % width, i / width] = (std[i],std[i],std[i])
    
        NoisePic.save(NoisePicDir)
        NoisePic.close()
        
##################################################################
#   noiseCheckFunc4AllPeople 
#-----------------------------------------------------------------
#
###################################################################
def noiseCheckFunc4AllPeople():
    for imgDir in glob.glob('Images/ImageData/faces/*'):
        z0dir = None
        z0Pixels = []
        funcVal = [0]* 960
        
        # zo pictures
        for OneImage in glob.glob(imgDir + '/*'):
            if '_straight_neutral_sunglasses_4' in OneImage:
                z0dir =OneImage
                z0Pixels = greyValues(OneImage)
        z0Sums = np.add(z0Pixels, 960)
        funcVal = np.multiply(z0Sums, z0Sums)
        
        # zk pictures     (get standard deviation using z0)            
        for OneImage in glob.glob(imgDir + '/*'):
            if ('_straight_neutral_open_4' in OneImage) or \
                ('_straight_sad_open_4' in OneImage) or \
                ('_straight_angry_open_4' in OneImage) or \
                ('_up_happy_open_4' in OneImage) or \
                ('_up_neutral_open_4' in OneImage) or \
                ('_left_happy_open_4' in OneImage) or \
                ('_left_neutral_open_4' in OneImage) or \
                ('_left_sad_open_4' in OneImage) or \
                ('_right_happy_open_4' in OneImage) or \
                ('_right_sad_open_4' in OneImage) or \
                ('_right_neutral_open_4' in OneImage):
                    
                zkPixels = greyValues(OneImage)
                zkSums = np.subtract(zkPixels, 960)
                zkSums = np.multiply(zkSums, zkSums)
                funcVal = np.add(funcVal, zkSums)
        
        #scale zkPixelSum to 255
        funcVal = np.multiply(np.true_divide(funcVal, max(funcVal)), 255)
        funcVal = [int(i) for i in funcVal]
        
        if 'kk49' in imgDir:
            for i in [857,888,889,920]:
                print funcVal[i]
                
        #Generate Output
        NoisePic = Image.open(z0dir).convert('RGB') 
        NoisePicDir = 'NoiseData/' + imgDir[23:] + '.png'
        width = NoisePic.size[0]
        height = NoisePic.size[1]
        pixelMap = NoisePic.load()
        #put zkPixelSum list to pixelMap
        for i in range(width * height):
            pixelMap[i % width, i / width] = (funcVal[i],funcVal[i],funcVal[i])
    
        NoisePic.save(NoisePicDir)
        NoisePic.close()
        
        
        
    
##################################################################
#   main 
#-----------------------------------------------------------------
#   Driver
###################################################################
def main():    
    noiseCheckFunc4AllPeople()
    
    
    print 'done'
if __name__ == '__main__':
    main()