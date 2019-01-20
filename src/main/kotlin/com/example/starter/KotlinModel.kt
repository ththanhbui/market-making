package com.example.starter

import io.improbable.keanu.algorithms.variational.optimizer.KeanuOptimizer
import io.improbable.keanu.network.BayesianNetwork
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex
import java.util.*

/**
 * This is a simple example of using Keanu that is intended to be used
 * as a starting point for new Keanu projects. It loads data from
 * a csv file found in the resources folder and uses it to build
 * a simple model where we have a prior belief of two random variables
 * and we noisily observe their sum. The model prints out the most
 * probable values for the two random variables given the observation
 * and the most probable true value of their sum given the prior belief.
 */
class KotlinModel(var data: KotlinData) {
    var results: Double? = null

    fun main(args: List<String>) {
        val data = KotlinData.load("data_example.csv")

        val model = KotlinModel(data)
        model.run()
    }

    fun run() {

        //Create a random and set its seed if you want your model to run the same each time
        val random = Random(1)

        //Use lines from your csv data file
        val firstCsvLine = data.csvLines[0]
        val secondCsvLine = data.csvLines[1]

        //Create your model as a bayesian network
        val vertexA = GaussianVertex(firstCsvLine.mu, firstCsvLine.sigma)
        val vertexB = GaussianVertex(secondCsvLine.mu, secondCsvLine.sigma)

        //Noisily observe that the gaussian defined in the first line plus the gaussian in the
        //second line sums to 2.0
        val vertexC = GaussianVertex(vertexA.plus(vertexB), 1.0)
        vertexC.observe(2.0)

        //Create a BayesNet object from your model
        val bayesNet = BayesianNetwork(vertexA.connectedGraph)

        //Find the most probable value for A and B given we've taken a
        //noisy observation of 2.0
        val optimizer = KeanuOptimizer.of(bayesNet)
        optimizer.maxAPosteriori()

        //Expose model results
        results = vertexA.value.scalar() + vertexB.value.scalar()

        println("A is most probably " + vertexA.value.scalar())
        println("B is most probably " + vertexB.value.scalar())
        println("Most probable actual value of the sum $results")
    }
}