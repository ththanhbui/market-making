package com.marketmaking;

import io.improbable.keanu.algorithms.NetworkSamples;
import io.improbable.keanu.algorithms.mcmc.MetropolisHastings;
import io.improbable.keanu.network.BayesianNetwork;
import io.improbable.keanu.vertices.bool.BooleanVertex;
import io.improbable.keanu.vertices.intgr.IntegerVertex;
import io.improbable.keanu.vertices.intgr.probabilistic.UniformIntVertex;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

public class AI {
    List<IntegerVertex> dealtCards = new ArrayList<>();
    List<BooleanVertex> collisionPreventionNodes = new ArrayList<>();
    List<BooleanVertex> hiddenCardComparison = new ArrayList<>();
    IntegerVertex MarketValue;
    BayesianNetwork net;
    int ownCardValue;
    int playerNumber;
    double myMarketValueMu = 56;
    double myMarketValueSigma = 7;
    double riskThreshold = 0.25;

    public AI(int playerNumber, int playerCardValue) {
        ownCardValue = playerCardValue;
        this.playerNumber = playerNumber;
        for (int i = 0; i < 7; i++) {
            dealtCards.add(new UniformIntVertex(1, 16));
            // dealtCards.get(0) is A, and so on
        }
        // Big assumption here: if A buys from B means A has higher card than B, if A sells to B means A has lower card
        for (int i = 0; i < 7; i++) {
            for (int j = i + 1; j < 7; j++) {
                collisionPreventionNodes.add(dealtCards.get(i).equalTo(dealtCards.get(j)));
            }
        }
        for (int i = 0; i < 4; i++) {
            for (int j = i + 1; j < 4; j++) {
                hiddenCardComparison.add(dealtCards.get(i).equalTo(IntegerVertex.max(dealtCards.get(i), dealtCards.get(j))));
            }
        }
        MarketValue = dealtCards.get(0).plus(dealtCards.get(1).plus(dealtCards.get(2).plus(dealtCards.get(3).plus(dealtCards.get(4).plus(dealtCards.get(5).plus(dealtCards.get(6)))))));
        net = new BayesianNetwork(MarketValue.getConnectedGraph());
        // Imposing the condition that all 7 cards are unique
        for (int i = 0; i < collisionPreventionNodes.size(); i++) {
            collisionPreventionNodes.get(i).observe(false);
        }
        observe(playerNumber, ownCardValue);
        net.probeForNonZeroProbability(10);

    }

    public static double getVarianceOfList(NetworkSamples myNS, IntegerVertex myV) {
        double xbar = 0;
        for (int i = 0; i < myNS.get(myV).asList().size(); i++) {
            xbar += myNS.get(myV).asList().get(i).scalar();
        }
        xbar /= myNS.get(myV).asList().size();
        double acc = 0;
        for (int i = 0; i < myNS.get(myV).asList().size(); i++) {
            acc += (myNS.get(myV).asList().get(i).scalar() - xbar) * (myNS.get(myV).asList().get(i).scalar() - xbar);
        }
        return acc / myNS.get(myV).asList().size();
    }

    public static double getAverageOfIntegerVertexSampleValues(NetworkSamples myNS, IntegerVertex v) {
        double acc = 0;
        int numSamples = myNS.get(v).asList().size();
        for (int i = 0; i < numSamples; i++) {
            acc += myNS.get(v).asList().get(i).scalar();
        }
        acc = acc / numSamples;
        return acc;
    }

    public int guessCardValue(int enemyPlayerCardIndex, int playedMktVal) {
        net.probeForNonZeroProbability(100);
        dealtCards.get(playerNumber).unobserve();

        IntegerVertex enemyPlayerCard = dealtCards.get(enemyPlayerCardIndex);
        int desiredMu = playedMktVal;
        int lowerBound = 1;
        int upperBound = 15;
        int tmp = 8;
        for (int i = 0; i < 4; i++) {
            enemyPlayerCard.unobserve();
            enemyPlayerCard.observe(tmp);
            net.probeForNonZeroProbability(100);
            if (getAverageOfIntegerVertexSampleValues(MetropolisHastings.withDefaultConfig().getPosteriorSamples(net, MarketValue, 10000), MarketValue) > desiredMu) {
                upperBound = tmp;
            } else {
                lowerBound = tmp;
            }
            tmp = (upperBound + lowerBound) / 2;
        }
        enemyPlayerCard.unobserve();
        dealtCards.get(playerNumber).observe(ownCardValue);

        return tmp;
    }

    public void enemyTransactedAt(int enemyIndex, double transactionValue) {
        if (enemyIndex == this.playerNumber) return;

//        System.out.println("enemy " + enemyIndex + " transacted at " + transactionValue);
        try {
            hiddenCardComparison.get(getHiddenCardListIndex(enemyIndex)).observe((enemyIndex - playerNumber) * (transactionValue - myMarketValueMu) < 0);
            net.probeForNonZeroProbability(100);
        } catch (Exception e) {
            System.out.println("that was weird");
            hiddenCardComparison.get(getHiddenCardListIndex(enemyIndex)).unobserve();
        }
        //true means the smaller indexed player has the bigger card
        net.probeForNonZeroProbability(100);

        myMarketValueMu = getAverageOfIntegerVertexSampleValues(MetropolisHastings.withDefaultConfig().getPosteriorSamples(net, MarketValue, 10000), MarketValue);
        myMarketValueSigma = Math.sqrt(getVarianceOfList(MetropolisHastings.withDefaultConfig().getPosteriorSamples(net, MarketValue, 10000), MarketValue));
    }

    public void observe(int cardIndex, int value) {
        dealtCards.get(cardIndex).observe(value);
        net.probeForNonZeroProbability(100);

        myMarketValueMu = getAverageOfIntegerVertexSampleValues(MetropolisHastings.withDefaultConfig().getPosteriorSamples(net, MarketValue, 10000), MarketValue);
        myMarketValueSigma = Math.sqrt(getVarianceOfList(MetropolisHastings.withDefaultConfig().getPosteriorSamples(net, MarketValue, 10000), MarketValue));
    }

    public int getHiddenCardListIndex(int enemyPlayerIndex) {
        if (min(playerNumber, enemyPlayerIndex) == 0) {
            return playerNumber + enemyPlayerIndex - 1;
        }
        return playerNumber + enemyPlayerIndex;

    }

    public int buyQuantity(double transactionPrice) {
        return Math.max((int) ((transactionPrice - myMarketValueMu) / myMarketValueSigma * riskThreshold * 5), 1);
    }

    public int sellQuantity(double transactionPrice) {
        return Math.min((int) -((transactionPrice - myMarketValueMu) / myMarketValueSigma * riskThreshold * 5), -1);
    }
}