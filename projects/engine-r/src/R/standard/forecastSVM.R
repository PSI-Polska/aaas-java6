# Title     : TODO
# Objective : TODO
# Created by: kskitek
# Created on: 2017-08-31
library(timeDate)
library(dplyr)
library(TTR)
library(caret)
library(e1071)
library(randomForest)

prepareDataForForecast <- function(dfData, dfParameters){
    forecastBeg <- as.character(dfParameters %>%
        filter(name == "forecastBeg") %>%
        summarise(value))
    forecastEnd <- as.character(dfParameters %>%
        filter(name == "forecastEnd") %>%
        summarise(value))
    resolution <- as.character(dfParameters %>%
        filter(name == "resolution") %>%
        summarise(value))


    print("Preparing vector of predictors")
    dfPredictors <- as.data.frame(seq(from = as.POSIXct(forecastBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(forecastEnd, format = "%Y-%m-%dT%H:%M") - 1, by = resolution))
    #print(nrow(dfPredictors))
    #print(dfPredictors)
    #print(nrow(dfData))
    colnames(dfPredictors) <- c("DateTime")
    dfPredictors$Temperature <- dfData$Temperature
    dfPredictors$WorkingDay <- as.factor(dfData$WorkingDay)
    
    #print(dfPredictors)
    return(dfPredictors)
}

run <- function(dfData, dfParameters){
    print("Entering forecast")
    filePath <- as.character(dfParameters %>% filter(name == "pathModelIn") %>% summarise(value))
    print("Entering prepareDataForForecast")
    dfData <- prepareDataForForecast(dfData, dfParameters)
    print("Left prepareDataForForecast")
    
    #calendar preciction creation
    str(dfData)
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))
    dfData$month <- as.factor(format(dfData$DateTime, "%m"))
    print("calendar predictors: done")
    str(dfData)
    
    #one-hot encoding
    #definition of the size of space
    levels(dfData$WorkingDay) <- c("0", "1")
    levels(dfData$dayOfWeek) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    levels(dfData$hour) <- c("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
    levels(dfData$month) <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    
    DVobject <- dummyVars(~ dayOfWeek + hour + month + Temperature + WorkingDay, data = dfData)
    featuresResponse.DF <- as.data.table(predict(DVobject, newdata = dfData))
    print("one-hot encoding: done")
    
    print("Read model")
    model <- readRDS(filePath)
    #NdataSetCols <- ncol(dataset)

    print("Predict")
    Prediction <- predict(model, featuresResponse.DF)
    return(data.frame(Prediction))
}


