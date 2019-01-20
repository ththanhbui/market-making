package com.example.starter;


import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.PosteriorSamplingAlgorithm;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.algorithms.variational.optimizer.KeanuOptimizer;
import io.improbable.keanu.algorithms.variational.optimizer.Optimizer;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.bool.BooleanVertex;
import io.improbable.keanu.vertices.dbl.DoubleVertex;
import io.improbable.keanu.vertices.dbl.probabilistic.GaussianVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.nonprobabilistic.ConstantIntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex;

/**
 * See accompanying workshop material for an intro
 */
public class HackCambridgeExamples {

    public static void main(String[] args) {
//        System.out.println("Running Thermometers example");
//        runThermometers();
        System.out.println("Running Guess Who Example");
        runGuessWho();
    }

    public static void runThermometers() {
        /*
         * Create the True Temp node
         */
        DoubleVertex trueTemp = new GaussianVertex(20.0, 5.0);

        /*
         * Now create our two thermometers, and apply our observations.
         */
        DoubleVertex therm1 = new GaussianVertex(trueTemp, 1.0);
        DoubleVertex therm2 = new GaussianVertex(trueTemp, 2.0);
        therm1.observe(21.0);
        therm2.observe(23.0);

        /*
         * Now, convert to a Bayes Net and run the Gradient Optimiser (our variables are continuous and differentiable
         * so a gradient based approach will be quicker and more accurate).  Luckily Keanu will choose the appropriate
         * optimiser automatically for us.
         */
        BayesianNetwork net = new BayesianNetwork(trueTemp.getConnectedGraph());
        Optimizer optimizer = KeanuOptimizer.of(net);
        optimizer.maxAPosteriori();

        /*
         * Now output the most likely temperature given the information we had
         */
        System.out.println("Most likely True Temp: " + trueTemp.getValue().scalar());
    }

    public static void runGuessWho() {
        /*
         * 0 = Alice
         * 1 = Bob
         * 2 = Cat
         * 3 = Derek
         *
         * We use a uniform here as we have no prior knowledge regarding which character our opponent has chosen
         *
         * Note - this is also our only random variable for this model, most models will have more random variables than
         * this!
         */
        IntegerVertex who = new UniformIntVertex(0, 4);

        /*
         * Now build up the rest of graph based on the rules of the game.
         */
        BooleanVertex isAlice = who.equalTo(new ConstantIntegerVertex(0));
        BooleanVertex isBob = who.equalTo(new ConstantIntegerVertex(1));
        BooleanVertex isCat = who.equalTo(new ConstantIntegerVertex(2));
        BooleanVertex isDerek = who.equalTo(new ConstantIntegerVertex(3));
        BooleanVertex glassesWearer = isDerek.or(isCat);
        BooleanVertex blondeHaired = isAlice.or(isCat);

        /*
         * Build our BayesianNetwork from the Keanu PPL Model and find an initial state
         */
        BayesianNetwork net = new BayesianNetwork(who.getConnectedGraph());

        /*
         * First - get some samples from our initial Posterior (although given there are no observations this should
         * just match the prior.
         */
        PosteriorSamplingAlgorithm sampler = MetropolisHastings.withDefaultConfig();
        NetworkSamples samples = sampler.getPosteriorSamples(net, isCat, 20000).drop(1000);

        /*
         * We'd expect this to produce a probability close to 25% as it's just a 1/4 chance at this point
         */
        System.out.println("Initial Probabilty it's Cat: "
                + samples.get(isCat).probability(val -> val.scalar() == true));

        /*
         * Let's say we check if the opponents character has glasses and they say Yes.  We can observe that node in the
         * graph and now check the posterior.  There are only two people with glasses, so we'd expect that probability
         * would now be 50%
         */
        glassesWearer.observe(true);
        net.probeForNonZeroProbability(1000);
        samples = sampler.getPosteriorSamples(net, isCat, 20000).drop(1000);
        System.out.println("After Observation the person wears glasses: "
                + samples.get(isCat).probability(val -> val.scalar() == true));

        /*
         * Finally observe that the person is blonde haired - only one person has the combination of blonde hair and
         * glasses (and that's Cat) so the posterior probability at this point should be 1.0.
         */
        blondeHaired.observe(true);
        net.probeForNonZeroProbability(1000);
        samples = sampler.getPosteriorSamples(net, isCat, 20000).drop(1000);
        System.out.println("After Blonde Haired observation: "
                + samples.get(isCat).probability(val -> val.scalar() == true));
    }

}
