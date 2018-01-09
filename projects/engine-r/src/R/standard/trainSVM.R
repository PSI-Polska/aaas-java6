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

run <- function(dfData, dfParameters){
    print("reading parameters")
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

    print("Preparing data")
    DateTime <- seq(from = as.POSIXct(subsetBeg, format = "%Y-%m-%dT%H:%M"), to = as.POSIXct(subsetEnd, format = "%Y-%m-%dT%H:%M"), by = resolution)
    dfData <- cbind(dfData, DateTime)
    str(dfData)
    dfData <- dfData %>% filter(! is.na(Energy) & ! is.na(DateTime))

    print("Create features")
    nL <- 24 * 28
    dfData$season <- EMA(dfData$Energy, n = nL)
    dfData$season[1 : nL] <- dfData$season[nL + 1]

    nBins <- 3
    dfData$seasonLevels <- cut(dfData$season, breaks = nBins, labels = seq(1, nBins))

    # create day of week feature
    dfData$dayOfWeek <- as.factor(dayOfWeek(timeDate(dfData$DateTime)))
    # create hour feature
    dfData$hour <- as.factor(format(dfData$DateTime, "%H"))

    dataset <- data.frame(predict(dummyVars(~ dayOfWeek +
        hour +
        seasonLevels +
        Energy +
        DateTime, data = dfData), newdata = dfData))
    dataset$DateTime <- as.POSIXct(dataset$DateTime, origin = "1970-01-01", tz = "GMT")

    featuresResponseCols <- c(seq(1, 7), seq(8, 8 + 23), seq(32, 34), 35)
    NdataSetCols <- length(featuresResponseCols)

    trainindex <- which(dataset$DateTime >= as.POSIXct(subsetBeg, format = "%Y-%m-%dT%H:%M", tz = "GMT") & dataset$DateTime < as.POSIXct(subsetEnd, format = "%Y-%m-%dT%H:%M", tz = "GMT"))

    training <- as.data.frame(dataset[trainindex, featuresResponseCols])
    rownames(training) = NULL

    type <- "eps-regression" #regression
    u <- - 2 # -3,-2,-1,0,1,2,3
    gam <- 10 ^ {u}
    w <- 4.5 #1.5,-1,0.5,2,3,4
    cost <- 10 ^ {w}

    print("Training SVM")
    str(training)
    head(training)
    # support vector machine
    svmFit <- svm(training[, 1 : (NdataSetCols - 1)],
    training[, NdataSetCols],
    type = type,
    kernel = "radial",
    gamma = gam,
    cost = cost)

    print("Saving SVM results")
    saveRDS(svmFit, file = filePath)
    return(1)
}
