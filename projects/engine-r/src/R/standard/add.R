# Title     : TODO
# Objective : TODO
# Created by: kskitek
# Created on: 2017-08-30

#' Adds two series
#' @param A, B
#' @return C
run <- function(dfIn, additionalParameters) {
    str(dfIn)
    C = dfIn$A + dfIn$B
    data.frame(C)
}
