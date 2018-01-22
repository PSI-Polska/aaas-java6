# Title     : Forecasting with the SVM method
# Objective : TODO
# Created by: kskitek
# Created on: 2017-08-31
# Major rewrite: 2018-01-19 (rbachorz()

library(timeDate)
library(dplyr)
library(TTR)
library(caret)
library(e1071)
library(futile.logger)
library(data.table)

run <- function(dfData, dfParameters){
    flog.threshold(INFO)
  
    flog.info("TrainSVM: reading parameters")
    filePath <- as.character(dfParameters %>%
        filter(name == "pathModelOut") %>%
        summarise(value))
    subsetBeg <- as.character(dfParameters %>%
        filter(name == "trainBeg") %>%
        summarise(value))
    subsetEnd <- as.character(dfParameters %>%
        filter(name == "trainEnd") %>%
        summarise(value))
    resolution <- as.character(dfParameters %>%
        filter(name == "resolution") %>%
        summarise(value))

    
    flog.info("TrainSVM: preparing the data")
    DateTime <- seq(from = as.POSIXct(subsetBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(subsetEnd, format = "%Y-%m-%dT%H:%M"), by = resolution)
    dfData <- cbind(dfData, DateTime)
    dfData <- dfData %>% filter(! is.na(Load) & ! is.na(DateTime))

    #calendar preciction creation
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))
    dfData$month <- as.factor(format(dfData$DateTime, "%m"))

    dfData$WorkingDay <- as.factor(dfData$WorkingDay)
    
    flog.info("TrainSVM: structure of the data frame")
    str(dfData) 
        
    flog.info("TrainSVM: head of the data frame")
    print(head(dfData))
    
    flog.info("TrainSVM: summary of the data frame")
    print(summary(dfData))
    
    #one-hot encoding
    #definition of the size of space
    levels(dfData$WorkingDay) <- c("0", "1")
    levels(dfData$dayOfWeek) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    levels(dfData$hour) <- c("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
    levels(dfData$month) <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    
    
    DVobject <- dummyVars(~ dayOfWeek + hour + Temperature + WorkingDay + Load, data = dfData)
    featuresResponse.DF <- as.data.table(predict(DVobject, newdata = dfData))
    featuresResponse.DF$Date <- dfData$DateTime
    
    featureResponseColumns <- 1:(length(featuresResponse.DF) - 1)
    nFeatureResponse <- ncol(featuresResponse.DF) - 1  
    
    trainingData <- featuresResponse.DF[, featureResponseColumns, with = FALSE]

    flog.info("TrainSVM: training - beginning")
    # SVM
    type <- "eps-regression" #regression
    u <- -4 # -3,-2,-1,0,1,2,3
    gam <- 10^{u} 
    w <- 4.5 #1.5,-1,0.5,2,3,4
    #w <- 1
    cost <- 10^{w}
    trainingData <- featuresResponse.DF[, featureResponseColumns, with = FALSE]
    # support vector machine
    svmFit <- svm(formula = Load ~ .,
                  data = trainingData,
                  type = type,
                  kernel = "radial",
                  gamma = gam,
                  cost = cost)

    flog.info("TrainSVM: saving the result")
    saveRDS(svmFit, file = filePath)
    flog.info("TrainSVM: leaving...")
    return(1)
}
