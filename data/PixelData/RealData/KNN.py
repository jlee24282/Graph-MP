# -*- coding: utf-8 -*-
"""
Created on Fri Apr  7 15:23:25 2017

@author: JLee
"""
from sklearn.neighbors import NearestNeighbors
import numpy as np
import glob
from PIL import Image

"""
X = np.array([[-1, -1], [-2, -1], [-3, -2], [1, 1], [2, 1], [3, 2]])
nbrs = NearestNeighbors(n_neighbors=2, algorithm='ball_tree').fit(X)
distances, indices = nbrs.kneighbors(X)
"""

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
    for folder in glob.glob('Images/ImageData/faces/*'):
        for filename in glob.glob(folder + '/*'):
            if 'open_4' in filename:
                X.append(greyValues(filename))
    print len(X)    
    nbrs = NearestNeighbors(n_neighbors=2, algorithm='ball_tree').fit(X)
    distances, indices = nbrs.kneighbors(X)
    print distances
    print indices
            
##################################################################
#   main 
#-----------------------------------------------------------------
#
###################################################################
def main():
    readData()

if __name__ == '__main__':
    main()