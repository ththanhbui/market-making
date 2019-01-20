package com.marketmaking;

import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;

public class Player implements Comparable<Player> {
    // general player info
    private String playerName;
    private int playerNumber;
    private int balance;
    private ArrayList<Transactions> balanceSheet;

    // bots only
    public AI brain;
    private ArrayList<Integer> knownCards;
    private double expected;

    public Player(String name, Integer card, int playerNumber) {
        this.playerName = name;
        this.playerNumber = playerNumber;
        this.balance = 0;
        this.balanceSheet = new ArrayList<>();

        // useful for bots only
        brain = new AI(playerNumber, card);
        this.knownCards = new ArrayList<>();
        knownCards.add(card);
        findExpected();
    }

    public void findExpected() {
        this.expected = this.brain.myMarketValueMu;
    }

    /**
     *
     * @return analyze whether to buy or sell
     */
    public int analyze(int low, int high) {
        double mid = (low + high)/2;
        if (this.expected < mid) {
            return this.brain.sellQuantity(low);
        } else {
            return this.brain.buyQuantity(high);
        }
    }

    /**
     *
     * @param marketMaker - the market maker
     * @param qty - quantity: negative - sell to market maker, positive - buy from market maker
     *
     */
    public void transaction(Player marketMaker, int qty) {
        if (qty < 0) { // sell to mm
            this.balanceSheet.add(new Transactions("sell", qty*-1, (int) Math.floor(marketMaker.getExpected())));
            marketMaker.balanceSheet.add(new Transactions("buy", qty*-1, (int) Math.floor(marketMaker.getExpected())));
        } else { // buy from mm
            this.balanceSheet.add(new Transactions("buy", qty, (int) Math.ceil(marketMaker.getExpected())));
            marketMaker.balanceSheet.add(new Transactions("sell", qty, (int) Math.ceil(marketMaker.getExpected())));
        }
    }

    public void printBalanceSheet(){
        System.out.println("______________________________________________________");
        System.out.println("|    Buy    |    Sell    |    Price    |    Total    |");
        System.out.println("|====================================================|");

//        // Comment for real
//        balanceSheet.add(new Transactions("buy", 3, 54));
//        balanceSheet.add(new Transactions("sell", 4, 56));
//        balanceSheet.add(new Transactions("sell", 4, 56));

        // printing the entries
        int total = 0;
        int buy = 0;
        int sell = 0;
        for (Transactions txn : balanceSheet) {
            if (txn.buy_sell.equals("buy")) {
                System.out.printf("|%s|            |%s|%s|\n",
                        StringUtils.center(String.valueOf(txn.qty), 11),
                        StringUtils.center(String.valueOf(txn.price), 13),
                        StringUtils.center(String.valueOf(txn.qty * txn.price * -1), 13));
                total += txn.qty * txn.price * -1;
                buy += txn.qty;
            } else {
                System.out.printf("|           |%s|%s|%s|\n",
                        StringUtils.center(String.valueOf(txn.qty), 12),
                        StringUtils.center(String.valueOf(txn.price), 13),
                        StringUtils.center(String.valueOf(txn.qty * txn.price), 13));
                total += txn.qty * txn.price;
                sell += txn.qty;
            }
        }
        System.out.println("|====================================================|");
        System.out.printf("|%s|%s|             |%s|\n",
                StringUtils.center(String.valueOf(buy), 11),
                StringUtils.center(String.valueOf(sell), 12),
                StringUtils.center(String.valueOf(total), 13));
        System.out.println("|====================================================|");
    }

    public void calculateFinalBalance(int market_price) {
        int total_buy = 0;
        int total_sell = 0;
        for (Transactions txn : balanceSheet) {
            if (txn.buy_sell.equals("buy")) {
                total_buy += txn.qty;
                balance += txn.qty * txn.price * -1;
            } else {
                total_sell += txn.qty;
                balance += txn.qty * txn.price;
            }
        }

        // buy/sell remaining at market value
        balance += market_price * (total_buy - total_sell);
    }

    @Override
    public int compareTo(Player p) {
        return p.balance - this.balance;
    }

    public double getExpected() {
        return this.expected;
    }

    public void setExpected(double val) {
        this.expected = val;
    }

    public int getBalance() {
        return this.balance;
    }

    public int getCard() {
        return this.knownCards.get(0);
    }

    public String getPlayerName() {
        return this.playerName;
    }

    private class Transactions {
        public String buy_sell;
        public int qty;
        public int price;

        public Transactions(String buy_sell, int quantity, int price) {
            this.buy_sell = buy_sell;
            this.qty = quantity;
            this.price = price;
        }
    }

    public static void main(String args[]) {
        Player test = new Player("test", 1, 1);
        test.printBalanceSheet();
        test.calculateFinalBalance(55);
    }
}
