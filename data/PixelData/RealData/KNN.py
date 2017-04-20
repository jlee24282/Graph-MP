# -*- coding: utf-8 -*-
"""
Created on Fri Apr  7 15:23:25 2017

@author: JLee
"""
from sklearn.neighbors import NearestNeighbors
import numpy as np
import glob
import os
from PIL import Image
import re


NEIGHBOR_NUM = 7
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
def readData(z0ImageDir):
    X = []
    y = []
    files = []
    
    # load all open eye imagess
    for folder in glob.glob('Images/ImageData/faces/*'):
        for filename in glob.glob(folder + '/*'):
            if 'open_4' in filename:
                files.append(filename)
                X.append(greyValues(filename))
    print len(X)    
    
    #load y image (z0 = sunglasses image)
    y = greyValues(z0ImageDir)
    
    nbrs = NearestNeighbors(n_neighbors=NEIGHBOR_NUM).fit(X, y)
    distances, indices = nbrs.kneighbors(y, return_distance=True)
    #print distances
    print distances
    print indices
    
    distances = distances[:,1]
    
    NAME = re.search('AllSunglasses/(.*)_4', z0ImageDir)
    NAME = NAME.group(1)
    print NAME

    #files, distances = (list(x) for x in zip(*sorted(zip(files, distances), key=lambda pair: pair[1])))
    print indices[0]
    for i in indices[0]:
        imgDir = re.sub('faces/.*?/', 'pngFiles/KNeighbors/'+NAME + '/', files[i])
        imgDir = imgDir.replace('.pgm', '.png')
        
        imDir = re.sub('sunglasses/(.*?).png', 'sunglasses/', imgDir)
        #print imDir
        print imgDir
        
        if not os.path.exists(imDir):
            os.makedirs(imDir)

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
    
def KNN():
    for filename in glob.glob('Images/ImageData/pngFiles/AllSunglasses/*'):
        if 'sunglasses_4' in filename:
            readData(filename)


##################################################################
#   main 
#-----------------------------------------------------------------
#
###################################################################
def main():
    KNN()
    #sunglassImages()

if __name__ == '__main__':
    main()