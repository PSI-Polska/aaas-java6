# Title     : TODO
# Objective : TODO
# Created by: kskitek
# Created on: 2017-08-22

#' Multiplies series
#' @param A, B
#' @return C
run <- function(dfIn, additionalParameters) {
    C = dfIn$A * dfIn$B
    data.frame(C)
}
