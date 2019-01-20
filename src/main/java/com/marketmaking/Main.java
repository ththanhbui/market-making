package com.marketmaking;

import io.improbable.keanu.util.ProgressBar;

import java.util.*;

public class Main {
    // general game states
    private int[] seven_cards;
    private int playerPos;
    private int marketValue;

    // list of players
    ArrayList<Player> players;

    public Main(String name, int pos) {
        this.seven_cards = dealUniformWithoutReplacement();
        for (int i : seven_cards)
            this.marketValue+= i;

//        for (int t : seven_cards)
//            System.out.println(t);
//
//        System.out.println(marketValue);

        this.players = new ArrayList<>();
        this.playerPos = pos;
        switch (playerPos) {
            case 1:
                players.add(new Player(name, seven_cards[0], 3));
                players.add(new Player("Bob", seven_cards[1], 0));
                players.add(new Player("Charlie", seven_cards[2], 1));
                players.add(new Player("Dave", seven_cards[3], 2));
                break;
            case 2:
                players.add(new Player("Alice", seven_cards[0], 0));
                players.add(new Player(name, seven_cards[1], 3));
                players.add(new Player("Charlie", seven_cards[2], 1));
                players.add(new Player("Dave", seven_cards[3], 2));
                break;
            case 3:
                players.add(new Player("Alice", seven_cards[0], 0));
                players.add(new Player("Bob", seven_cards[1], 1));
                players.add(new Player(name, seven_cards[2], 3));
                players.add(new Player("Dave", seven_cards[3], 2));
                break;
            case 4:
                players.add(new Player("Alice", seven_cards[0], 0));
                players.add(new Player("Bob", seven_cards[1], 1));
                players.add(new Player("Charlie", seven_cards[2], 2));
                players.add(new Player(name, seven_cards[3], 3));
                break;
            default:
                break;
        }
    }

    public void advance(Scanner sc, int start) {
        int mm_count=0;
        for (int mm_pos=start; mm_count < 4; mm_pos=mm_pos==4?1:mm_pos+1) {
            // user is NOT the MM
            if (mm_pos != playerPos) {
                // Player #mm_pos makes the market
                System.out.printf("%s made the market at %d-%d.\n",
                        players.get(mm_pos-1).getPlayerName(),
                        (int) Math.floor(players.get(mm_pos-1).getExpected()+0.5),
                        (int) Math.ceil(players.get(mm_pos-1).getExpected()+0.5));
                sleep(1);

                // The other 3 players decide to buy/sell
                int bs_count=0;
                for (int bs_pos=mm_pos==4?1:mm_pos+1; bs_count<3; bs_pos=bs_pos==4?1:bs_pos+1) {
                    if (bs_pos != playerPos) { // not user's turn
                        // analyze the qty to buy/sell - AI
                        int qty = players.get(bs_pos-1).analyze((int) Math.floor(players.get(mm_pos-1).getExpected()+0.5),
                                (int) Math.ceil(players.get(mm_pos-1).getExpected()+0.5));
                        players.get(bs_pos-1).transaction(players.get(mm_pos-1), qty);

                        for (Player p : players) {
                            p.brain.enemyTransactedAt(bs_pos - 1, qty < 0 ? (int) Math.floor(players.get(mm_pos - 1).getExpected() + 0.5) : (int) Math.ceil(players.get(mm_pos - 1).getExpected() + 0.5));
                            System.out.println("Guess "+p.brain.guessCardValue(bs_pos-1, (int) Math.floor(players.get(mm_pos - 1).getExpected() + 0.5)));
                        }
                        System.out.printf("%s decided to %s %d at %d.\n",
                                players.get(bs_pos-1).getPlayerName(),
                                qty < 0 ? "sell" : "buy",
                                Math.abs(qty),
                                qty < 0 ? (int) Math.floor(players.get(mm_pos-1).getExpected()+0.5) : (int) Math.ceil(players.get(mm_pos-1).getExpected()+0.5));
                        sleep(1);
                    } else {
                        // ask player to buy/sell
                        System.out.println("Do you want to 'BUY' or 'SELL'? (buy/sell)");
                        String decision = sc.nextLine();
                        while (!decision.equals("buy") && !decision.equals("sell")) {
                            System.out.println("Please enter 'buy' or 'sell': ");
                            decision = sc.nextLine();
                        }
                        // quantity
                        String qty;
                        System.out.println("How many do you want to "+decision.toUpperCase()+"?");
                        while (!isNumeric(qty = sc.nextLine()) || Integer.parseInt(qty) < 1 || Integer.parseInt(qty) > 3) {
                            System.out.println("Please enter a valid number between 1-3: ");
                        }
                        int quantity = decision.equals("buy") ? Integer.parseInt(qty) : Integer.parseInt(qty)*-1;

                        players.get(playerPos-1).transaction(players.get(mm_pos-1), quantity);
                        for (Player p : players) {
                            p.brain.enemyTransactedAt(bs_pos - 1, quantity < 0 ? (int) Math.floor(players.get(mm_pos - 1).getExpected() + 0.5) : (int) Math.ceil(players.get(mm_pos - 1).getExpected() + 0.5));
                        }
                    }
                    bs_count++;
                }
            } else { // user is the MM
                System.out.println("Please make your market with a spread of 1 by entering the lower limit (e.g. 52-53 --> enter '52'): ");
                String low;
                while (!isNumeric(low = sc.nextLine())) {
                    System.out.println("Please enter a valid number representing the lower limit of the spread: ");
                }
                System.out.printf("You have made the market at %d-%d.\n", Integer.parseInt(low), Integer.parseInt(low)+1);
                players.get(mm_pos-1).setExpected(Integer.parseInt(low) + 0.5);
                sleep(1);

                // The other 3 players decide to buy/sell
                int bs_count=0;
                for (int bs_pos=mm_pos==4?1:mm_pos+1; bs_count<3; bs_pos=bs_pos==4?1:bs_pos+1) {
                    // analyze the qty to buy/sell - AI
                    int qty = players.get(bs_pos-1).analyze(Integer.parseInt(low), Integer.parseInt(low)+1);
                    players.get(bs_pos-1).transaction(players.get(mm_pos-1), qty);
                    for (Player p : players)
                        p.brain.enemyTransactedAt(bs_pos-1, qty < 0 ? (int) Math.floor(players.get(mm_pos-1).getExpected()+0.5) : (int) Math.ceil(players.get(mm_pos-1).getExpected()+0.5));

                    System.out.printf("%s decided to %s %d at %d.\n",
                            players.get(bs_pos-1).getPlayerName(),
                            qty < 0 ? "sell" : "buy",
                            Math.abs(qty),
                            qty < 0 ? (int) Math.floor(players.get(mm_pos-1).getExpected()+0.5) : (int) Math.ceil(players.get(mm_pos-1).getExpected()+0.5));
                    sleep(1);
                    bs_count++;
                }
            }
            mm_count++;
            System.out.println();
        }
    }

    public void pause(int round) {
        System.out.println("==================== End of round "+round+" ====================");
        System.out.println();
        sleep(1);

        System.out.println("This is your current balance sheet.");
        System.out.println();
        sleep(1);

        players.get(playerPos-1).printBalanceSheet();
        System.out.println();
        sleep(1);

        System.out.println("Revealing a card of "+seven_cards[round+3]);
        // all AI update their expected
        for (Player p : players)
            p.brain.observe(round+3, seven_cards[round+3]);
        System.out.println();
        sleep(1);

        System.out.println("You have 1 minute(s) to revise your strategy.");
        System.out.println();
        sleep(1); // changes for demo, 60s for real

        System.out.println("==================== Start of round "+(round+1)+" ====================");
        System.out.println();
    }

    public void calculateResult() {
        // Print out all cards & Calculate each player's balance
        for (Player p : players) {
            System.out.println(p.getPlayerName()+" has a card of "+p.getCard());
            p.calculateFinalBalance(marketValue);
        }

        System.out.println("The market value is "+marketValue);

        // Rank according to balance
        Collections.sort(players);
        int position = 1;
        for (Player p : players) {
            System.out.println((position++)+". "+p.getPlayerName()+": "+p.getBalance());
        }
    }

    private static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  // match a number with optional '-' and decimal.
    }

    private void sleep(int sec) {
        try {
            Thread.sleep(sec * 1000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     *
     * @return deal 7 cards between 1-15
     */
    private int[] dealUniformWithoutReplacement(){
        Random randomNum = new Random();
        int[] result = new int[7];
        HashSet<Integer> set = new HashSet<>();
        for(int i=0; i<7; i++) {
            int temp = randomNum.nextInt(15);
            while(set.contains(temp)){
                temp = randomNum.nextInt(15);
            }
            result[i] = temp;
            set.add(result[i]);
        }

        for (int i=0; i<7; i++)
            result[i]++;
        return result;
    }

    public static void main(String args[]) {
        ProgressBar.disable();
        Scanner scanner = new Scanner(System.in);
        // ask user name
        System.out.println("What is your name?");
        String name = scanner.nextLine();

        // ask user which position they want to be in
        System.out.println("Hello "+name+". Which Player do you want to be? (1-4)");
        String pos;
        // check that it's between 1-4
        while (!isNumeric(pos = scanner.nextLine()) || Integer.parseInt(pos) < 1 || Integer.parseInt(pos) > 4) {
            System.out.println("Please enter a valid number between 1-4: ");
        }

        // initialise the game
        Main game = new Main(name, Integer.parseInt(pos));
        int round = 1;
        System.out.println("Your card is "+game.players.get(Integer.parseInt(pos)-1).getCard());

        // Debug
        for (Player p : game.players) {
            System.out.println(p.getPlayerName()+" "+p.getCard());
        }

        System.out.println("==================== Start of round "+round+" ====================");
        System.out.println();
        // round 1
        game.advance(scanner, round);
        game.pause(round++);

        // round 2
        game.advance(scanner, round);
        game.pause(round++);

        // round 3
        game.advance(scanner, round);
        game.pause(round++);

        // round 4
        game.advance(scanner, round);

        // result
        System.out.println("The game has ended. The result is as follows: ");
        System.out.print("All the cards are ");
        for (int i : game.seven_cards)
            System.out.print(i+" ");
        System.out.println();
        game.calculateResult();

    }
}
