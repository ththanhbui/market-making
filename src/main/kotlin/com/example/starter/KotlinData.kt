package com.example.starter

import io.improbable.keanu.util.csv.ReadCsv

class KotlinData(var csvLines: List<CsvLine>) {

    /**
     * An example class to load the csv lines into.
     * This expects the csv to have column labels of
     * mu and sigma.
     */
    class CsvLine {
        var mu: Double = 0.toDouble()
        var sigma: Double = 0.toDouble()
    }

    companion object {
        /**
         * @param fileName the name of the csv file in the resource folder
         * @return a KotlinData object with the contents of the csv file
         */
        fun load(fileName: String): KotlinData {

            //Load a csv file from src/main/resources
            val csvLines = ReadCsv
                    .fromResources(fileName)
                    .expectHeader(true)
                    .asRowsDefinedBy(CsvLine::class.java)
                    .load()

            //create new Data object from csv
            return KotlinData(csvLines)
        }
    }

}

