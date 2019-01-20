package com.marketmaking;

import java.util.ArrayList;
import java.util.List;


public class MarketMakingGameCode {
    static List<AI> players;
    public static void main(String[] args) {
//        players = new ArrayList<>();
//        //Deal Cards
//        int[] Cards = uniformWithoutReplacement();
//        for (int c:Cards) {
//            System.out.print(c+", ");
//        }
//        System.out.println();
//        players.add(new AI(0, Cards[0]));
//        players.add(new AI(1, Cards[1]));
//        players.add(new AI(2, Cards[2]));
//        //Cards[3] is human's card, 4 to 6 are to be revealed
        players.get(0).observe(0, 12);
//        System.out.println(players.get(0).guessCardValue(1,53));
        System.out.println(players.get(0).myMarketValueMu);
        System.out.println(players.get(0).myMarketValueSigma);
    }


    public void revealCard(int cardIndex, int value){
        players.get(0).observe(cardIndex, value);
        players.get(1).observe(cardIndex, value);
        players.get(2).observe(cardIndex, value);
    }

}
