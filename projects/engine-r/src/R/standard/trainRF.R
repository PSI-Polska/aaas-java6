# Title     : Forecasting with the SVM method
# Objective : TODO
# Created by: rbachorz
# Created on: 2018-01-22

library(timeDate)
library(dplyr)
library(TTR)
library(caret)
library(randomForest)
library(futile.logger)
library(data.table)

run <- function(dfData, dfParameters){
  flog.threshold(INFO)
  
  flog.info("TrainRF: reading parameters")
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
  
  
  flog.info("TrainRF: preparing the data")
  DateTime <- seq(from = as.POSIXct(subsetBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(subsetEnd, format = "%Y-%m-%dT%H:%M"), by = resolution)
  #DateTime <- DateTime(1:nrow(dfData), )
  dfData <- cbind(dfData, DateTime)
  dfData <- dfData %>% filter(! is.na(Load) & ! is.na(DateTime))
  
  #calendar preciction creation
  dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
  dfData$hour <- as.factor(format(dfData$DateTime, "%H"))
  dfData$month <- as.factor(format(dfData$DateTime, "%m"))
  
  #one-hot encoding
  #definition of the size of space
  levels(dfData$WorkingDay) <- c("0", "1")
  levels(dfData$dayOfWeek) <- c("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
  levels(dfData$hour) <- c("00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23")
  levels(dfData$month) <- c("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")
  
  
  DVobject <- dummyVars(~ dayOfWeek + hour + Temperature + Load, data = dfData)
  featuresResponse.DF <- as.data.table(predict(DVobject, newdata = dfData))
  featuresResponse.DF$Date <- dfData$DateTime
  
  featureResponseColumns <- 1:(length(featuresResponse.DF) - 1)
  nFeatureResponse <- ncol(featuresResponse.DF) - 1  
  
  trainingData <- featuresResponse.DF[, featureResponseColumns, with = FALSE]
  # random forest
  flog.info("TrainRF: training - beginning")
  model <- randomForest(formula = Load ~ .,
                        data = trainingData,
                        ntree=300)
  
  flog.info("TrainRF: saving the result")
  saveRDS(model, file = filePath)
  flog.info("TrainRF: leaving prediction...")
  return(1)
}
