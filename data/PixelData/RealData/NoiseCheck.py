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
    
    for w in range(width):
        for h in range(height):
            greyValues.append(pixelMap[w,h])
    
    image.close()
    return greyValues
    


##################################################################
#   noiseCheck 
#-----------------------------------------------------------------
#
###################################################################
def noiseCheck4AllPeople():
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
        #Generate Output
        NoisePic = Image.open(z0dir).convert('RGB') 
        NoisePicDir = 'NoiseData/' + imgDir[23:] + '.png'
        width = NoisePic.size[0]
        height = NoisePic.size[1]
        pixelMap = NoisePic.load()
        #put zkPixelSum list to pixelMap
        for i in range(width * height):
            pixelMap[i % width, i / width] = (std[i],0,0)
    
        NoisePic.save(NoisePicDir)
        NoisePic.close()
        
    
##################################################################
#   main 
#-----------------------------------------------------------------
#   Driver
###################################################################
def main():    
    noiseCheck4AllPeople()
    
    
    print 'done'
if __name__ == '__main__':
    main()