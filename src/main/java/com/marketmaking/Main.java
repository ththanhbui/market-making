package com.marketmaking;

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
                players.add(new Player(name, seven_cards[0]));
                players.add(new Player("Bob", seven_cards[1]));
                players.add(new Player("Charlie", seven_cards[2]));
                players.add(new Player("Dave", seven_cards[3]));
                break;
            case 2:
                players.add(new Player("Alice", seven_cards[0]));
                players.add(new Player(name, seven_cards[1]));
                players.add(new Player("Charlie", seven_cards[2]));
                players.add(new Player("Dave", seven_cards[3]));
                break;
            case 3:
                players.add(new Player("Alice", seven_cards[0]));
                players.add(new Player("Bob", seven_cards[1]));
                players.add(new Player(name, seven_cards[2]));
                players.add(new Player("Dave", seven_cards[3]));
                break;
            case 4:
                players.add(new Player("Alice", seven_cards[0]));
                players.add(new Player("Bob", seven_cards[1]));
                players.add(new Player("Charlie", seven_cards[2]));
                players.add(new Player(name, seven_cards[3]));
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
                System.out.printf("%s makes the market at %d-%d.\n",
                        players.get(mm_pos-1).getPlayerName(),
                        (int) Math.floor(players.get(mm_pos-1).getExpected()),
                        (int) Math.ceil(players.get(mm_pos-1).getExpected()));
                sleep(1);

                // The other 3 players decide to buy/sell
                int bs_count=0;
                for (int bs_pos=mm_pos==4?1:mm_pos+1; bs_count<3; bs_pos=bs_pos==4?1:bs_pos+1) {
                    if (bs_pos != playerPos) { // not user's turn
                        // analyze the qty to buy/sell - AI
                        int qty = players.get(bs_pos-1).analyze();
                        players.get(bs_pos-1).transaction(players.get(mm_pos-1), qty);
                        System.out.printf("%s decided to %s %d at %d.\n",
                                players.get(bs_pos-1).getPlayerName(),
                                qty < 0 ? "sell" : "buy",
                                Math.abs(qty),
                                qty < 0 ? (int) Math.floor(players.get(mm_pos-1).getExpected()) : (int) Math.ceil(players.get(mm_pos-1).getExpected()));
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
                    }
                    bs_count++;
                }
            } else { // user is the MM
                System.out.println("Please make your market with a spread of 1 by entering the lower limit (e.g. 52-53 --> enter '52'): ");
                String low;
                while (!isNumeric(low = sc.nextLine())) {
                    System.out.println("Please enter a valid number representing the lower limit of the spread: ");
                }
                System.out.printf("You have set the market at %d-%d.\n", Integer.parseInt(low), Integer.parseInt(low)+1);
                players.get(mm_pos-1).setExpected(Integer.parseInt(low) + 0.5);
                sleep(1);

                // The other 3 players decide to buy/sell
                int bs_count=0;
                for (int bs_pos=mm_pos==4?1:mm_pos+1; bs_count<3; bs_pos=bs_pos==4?1:bs_pos+1) {
                    // analyze the qty to buy/sell - AI
                    int qty = players.get(bs_pos-1).analyze();
                    players.get(bs_pos-1).transaction(players.get(mm_pos-1), qty);
                    System.out.printf("%s decided to %s %d at %d.\n",
                            players.get(bs_pos-1).getPlayerName(),
                            qty < 0 ? "sell" : "buy",
                            Math.abs(qty),
                            qty < 0 ? (int) Math.ceil(players.get(mm_pos-1).getExpected()) : (int) Math.floor(players.get(mm_pos-1).getExpected()));
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
            p.updateKnowledge(seven_cards[round+3]);
        System.out.println();
        sleep(1);

        System.out.println("You have 1 minute(s) to revise your strategy.");
        System.out.println();
        sleep(60);

        System.out.println("==================== Start of round "+(round+1)+" ====================");
        System.out.println();
    }

    public void calculateResult() {
        // Calculate each player's balance
        for (Player p : players) {
            p.calculateFinalBalance(marketValue);
        }

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
        for(int i=0; i<7; i++) {
            result[i] = randomNum.nextInt(15);
            for(int j=0; j<i; j++){
                if(result[i] == result[j]){
                    result[i] = randomNum.nextInt(15);
                    break;
                }
            }
        }

        for (int i=0; i<7; i++)
            result[i]++;
        return result;
    }

    public static void main(String args[]) {
        Scanner scanner = new Scanner(System.in);
        // ask user name
        System.out.println("What is your name?");
        String name = scanner.nextLine();

        // ask user which position they want to be in
        System.out.println("Hello "+name+". Which position do you want to start? (1-4)");
        String pos;
        // check that it's between 1-4
        while (!isNumeric(pos = scanner.nextLine()) || Integer.parseInt(pos) < 1 || Integer.parseInt(pos) > 4) {
            System.out.println("Please enter a valid number between 1-4: ");
        }

        // initialise the game
        Main game = new Main(name, Integer.parseInt(pos));
        int round = 1;

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
        game.calculateResult();
    }
}
