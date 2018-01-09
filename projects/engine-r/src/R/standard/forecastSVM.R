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

prepareDataForForecast <- function(dfParameters){
    forecastBeg <- as.character(dfParameters %>%
        filter(name == "forecastBeg") %>%
        summarise(value))
    forecastEnd <- as.character(dfParameters %>%
        filter(name == "forecastEnd") %>%
        summarise(value))
    resolution <- as.character(dfParameters %>%
        filter(name == "resolution") %>%
        summarise(value))
    seasonLevel <- as.integer(dfParameters %>%
        filter(name == "seasonLevel") %>%
        summarise(value))


    dfTimeseries <- as.data.frame(seq(from = as.POSIXct(forecastBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(forecastEnd, format = "%Y-%m-%dT%H:%M"), by = resolution))
    colnames(dfTimeseries) <- c("DateTime")
    dfTimeseries$seasonLevels <- factor(x = rep(seasonLevel, nrow(dfTimeseries)), levels = c("1", "2", "3"))
    return(dfTimeseries)
}

run <- function(dfData, dfParameters){
    filePath <- as.character(dfParameters %>%
        filter(name == "pathModelIn") %>%
        summarise(value))

    dfData <- prepareDataForForecast(dfParameters)

    # create day of week feature
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    # create hour feature
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))

    dataset <- data.frame(predict(dummyVars(~ dayOfWeek + hour + seasonLevels + DateTime, data = dfData), newdata = dfData))
    dataset$DateTime <- as.POSIXct(dataset$DateTime, origin = "1970-01-01", tz = "GMT")

    model <- readRDS(filePath)
    NdataSetCols <- ncol(dataset)
    Prediction <- predict(model, dataset[, 1 : (NdataSetCols - 1)])

    return(data.frame(Prediction))
}


