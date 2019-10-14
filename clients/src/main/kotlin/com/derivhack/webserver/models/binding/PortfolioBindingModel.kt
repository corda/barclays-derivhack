package com.derivhack.webserver.models.binding

class PortfolioBindingModel(val transferRefs : List<String>,
                            val executionRefs : List<String>,
                            val pathToInstructions : String) //you should copy the instructions json into the resources folder of the cordapp package and the the path should be something like "/UC6_Portfolio_Instructions_20191017.json"