# Title     : TODO
# Objective : TODO
# Created by: kskitek
# Created on: 2017-08-31
# Major rewrite: 2018-01-19 (rbachorz()

# Various stuff
library(timeDate)
library(dplyr)
library(data.table)
#library(TTR)
# Machine Learning
library(caret)
library(randomForest)
# Logger
library(futile.logger)


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

    
    flog.info("forecastRF: preparing vector of predictors")
    #print(dfData)
    
    dfPredictors <- as.data.frame(x = seq(from = as.POSIXct(forecastBeg, format = "%Y-%m-%dT%H:%M"), length.out = nrow(dfData), by = resolution))
    
    #print(nrow(dfPredictors))
    #print(nrow(dfData))
    #print(dfPredictors[1:nrow(dfData), ])
    #dfPredictors <- dfPredictors[1:nrow(dfData), ]
    #print(dfPredictors)
    colnames(dfPredictors) <- c("DateTime")
    dfPredictors$Temperature <- dfData$Temperature
    dfPredictors$WorkingDay <- as.factor(dfData$WorkingDay)
    flog.info("forecastRF: leaving prepareDataForForecast")
    return(dfPredictors)
}

run <- function(dfData, dfParameters){
    flog.info("forecastRF: entering forecast...")
    filePath <- as.character(dfParameters %>% filter(name == "pathModelIn") %>% summarise(value))
    flog.info("forecastRF: entering prepareDataForForecast")
    dfData <- prepareDataForForecast(dfData, dfParameters)
    flog.info("forecastRF: left prepareDataForForecast")

    #calendar preciction creation
    flog.info("forecastRF: one-hot encoding")
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))
    dfData$month <- as.factor(format(dfData$DateTime, "%m"))
    
    #one-hot encoding
    #definition of the size of space
    levels(dfData$WorkingDay) <- c("0", "1")
    levels(dfData$dayOfWeek) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    levels(dfData$hour) <- c("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
    levels(dfData$month) <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
    
    DVobject <- dummyVars(~ dayOfWeek + hour + Temperature, data = dfData)
    featuresResponse.DF <- as.data.table(predict(DVobject, newdata = dfData))
    
    flog.info(paste("forecastRF: reading in the model: ", filePath,  sep = ""))
    model <- readRDS(filePath)

    flog.info("forecastRF: predicting...")
    Prediction <- predict(model, featuresResponse.DF)
    #flog.info("Data frame with predicted values:")
    #print(data.frame(Prediction))
    flog.info("forecastRF: leaving...")
    return(data.frame(Prediction))
}


