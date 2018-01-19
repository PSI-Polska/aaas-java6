# Title     : TODO
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

prepareDataForForecast <- function(dfData, dfParameters){
  
    flog.threshold(INFO)
  
    forecastBeg <- as.character(dfParameters %>%
        filter(name == "forecastBeg") %>%
        summarise(value))
    forecastEnd <- as.character(dfParameters %>%
        filter(name == "forecastEnd") %>%
        summarise(value))
    resolution <- as.character(dfParameters %>%
        filter(name == "resolution") %>%
        summarise(value))

    
    flog.info("forecastSVM: preparing vector of predictors")
    #print(dfData)
    
    dfPredictors <- as.data.frame(seq(from = as.POSIXct(forecastBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(forecastEnd, format = "%Y-%m-%dT%H:%M"), by = resolution))
    
    #print(dfPredictors)
    
    colnames(dfPredictors) <- c("DateTime")
    dfPredictors$Temperature[1] <- dfData$Temperature
    dfPredictors$WorkingDay <- as.factor(dfData$WorkingDay)
    
    return(dfPredictors)
}

run <- function(dfData, dfParameters){
    flog.info("forecastSVM: entering forecastSVM")
    filePath <- as.character(dfParameters %>% filter(name == "pathModelIn") %>% summarise(value))
    flog.info("forecastSVM: entering prepareDataForForecast")
    dfData <- prepareDataForForecast(dfData, dfParameters)
    flog.info("forecastSVM: left prepareDataForForecast")

    #calendar preciction creation
    flog.info("forecastSVM: one-hot encoding")
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))
    dfData$month <- as.factor(format(dfData$DateTime, "%m"))
    
    #one-hot encoding
    #definition of the size of space
    levels(dfData$WorkingDay) <- c("0", "1")
    levels(dfData$dayOfWeek) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    levels(dfData$hour) <- c("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
    levels(dfData$month) <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    
    DVobject <- dummyVars(~ dayOfWeek + hour + month + Temperature + WorkingDay, data = dfData)
    featuresResponse.DF <- as.data.table(predict(DVobject, newdata = dfData))
    
    flog.info("forecastSVM: reding in the model")
    model <- readRDS(filePath)

    flog.info("forecastSVM: predicting")
    Prediction <- predict(model, featuresResponse.DF)
    print(data.frame(Prediction))
    return(data.frame(Prediction))
}


