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

##################################################################
#   noiseCheck 
#-----------------------------------------------------------------
#
###################################################################
def noiseCheck4AllPeople():
    for imgDir in glob.glob('Images/ImageData/faces/*'):
        for OneImage in glob.glob(imgDir + '/*'):
            print OneImage[23:]
            
            
            
            
    """
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
            """
    
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