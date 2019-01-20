package com.example.starter

import org.junit.Assert.assertEquals
import org.junit.Test

class TestKotlinMain {

    @Test
    fun shouldDoSomethingIExpectKotlin() {

        //Load data from a csv file
        val data = KotlinData.load("data_example.csv")

        //create my model using the data
        val model = KotlinModel(data)
        model.run()

        //make some assertions about the results of your model
        assertEquals(2.16, model.results!!, 0.1)
    }

    @Test
    fun shouldDoSomethingIExpectJava() {

        //Load data from a csv file
        val data = Data.load("data_example.csv")

        //create my model using the data
        val model = Model(data)
        model.run()

        //make some assertions about the results of your model
        assertEquals(2.16, model.results, 0.1)
    }
}