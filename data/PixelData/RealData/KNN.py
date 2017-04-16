# -*- coding: utf-8 -*-
"""
Created on Fri Apr  7 15:23:25 2017

@author: JLee
"""
from sklearn.neighbors import NearestNeighbors
import numpy as np
import glob
from PIL import Image
import re


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
#   readData 
#-----------------------------------------------------------------
#
###################################################################
def readData():
    X = []
    y= []
    files = []
    # load all open eye imagess
    for folder in glob.glob('Images/ImageData/faces/*'):
        for filename in glob.glob(folder + '/*'):
            if 'open_4' in filename:
                files.append(filename)
                X.append(greyValues(filename))
    print len(X)    
    
    #load y image (z0 = sunglasses image)
    y = greyValues('Images/ImageData/pngFiles/AllSunglasses/steffi_straight_neutral_sunglasses_4.png')
    
    nbrs = NearestNeighbors(n_neighbors=10).fit(X, y)
    distances, indices = nbrs.kneighbors(y, return_distance=True)
    #print distances
    print distances
    print indices
    
    distances = distances[:,1]

    #files, distances = (list(x) for x in zip(*sorted(zip(files, distances), key=lambda pair: pair[1])))
    print indices[0]
    for i in indices[0]:
        imgDir = re.sub('faces/.*?/', 'pngFiles/KNeighbors/', files[i])
        imgDir = imgDir.replace('.pgm', '.png')

        im = Image.open(files[i])
        im.save(imgDir)
        im.close()

##################################################################
#   sunglassImages
#-----------------------------------------------------------------
#
###################################################################
def sunglassImages():
    count = 0
    for folder in glob.glob('Images/ImageData/faces/*'):
        for filename in glob.glob(folder + '/*'):
            if 'sunglasses_4' in filename:
                #print filename
                count = count+1
                imgDir = re.sub('faces/.*?/', 'pngFiles/AllSunglasses/', filename)
                imgDir = imgDir.replace('.pgm', '.png')

                im = Image.open(filename)
                im.save(imgDir)
                im.close()
    print count


##################################################################
#   main 
#-----------------------------------------------------------------
#
###################################################################
def main():
    readData()
    #sunglassImages()

if __name__ == '__main__':
    main()