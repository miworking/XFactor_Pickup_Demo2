# XFactor_PickupRecognition

##[Analysis of the result](https://www.dropbox.com/s/bnvwc62nh7kt24q/Pickup.pptx?dl=0)


##Summary:
 
1. 104 features
{Accelerometer, Magnetic} * {X,Y,Z,Magnitude} * {Mean, Std, Min, Max, Percentile25,50,75, FTT[0,1,2]}
2. data of 6 people, 547 instances
3. Cross -validation on training set: 98% correctly classified (RandomForest, 10 folders)
4. Performance on android phone: ANR android not responding....  GC_FOR_ALLOC 
 
<<<<<<< HEAD
## What's next?

###[default] fix ANR problem
###[work around] Move the prediction process to cloud
###[backup plan] choose another pattern, like the meeting pattern
=======
What's next?
[default] fix ANR problem
[work around] change for a better device
[backup plan] choose another pattern, like the meeting pattern
>>>>>>> 7d2cb2b732e2c7c07c3a442d20bd80eca5a5bd66
