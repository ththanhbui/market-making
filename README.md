```
 __ __  __  ___ _  _____ _____   __ __  __  _  ___ __  _  __   __  __   __ 
|  V  |/  \| _ \ |/ / __|_   _| |  V  |/  \| |/ / |  \| |/ _] /  |/  \ /  |
| \_/ | /\ | v /   <| _|  | |   | \_/ | /\ |   <| | | ' | [/\ `7 | // |`7 |
|_| |_|_||_|_|_\_|\_\___| |_|   |_| |_|_||_|_|\_\_|_|\__|\__/  |_|\__/  |_|
 
```

## Setting up

1. Install [IntelliJ](https://www.jetbrains.com/idea/).
1. Open a terminal window and `cd` to where you want the project to be located.
1. Run `https://github.com/ttbui11/market-making` to obtain the project.

You should now be ready! To run the game:

1. Open the `market-making` folder in IntelliJ.
1. Tick the box `Use auto-import`.
1. Open `market-making/src/main/java/com/marketmaking/Main.java`.
1. Right click the `main` function, and select `Run`.

## Inspiration
A [**market maker**](https://en.wikipedia.org/wiki/Market_maker) or **liquidity provider** is a company or an individual that quotes both a buy and a sell price in a financial instrument or commodity held in inventory, hoping to make a profit on the bid-offer spread, or turn.

When you want to effect a trade, you’ll need to specify the:
1. thing you want to trade (which is often implied by the context, so let’s ignore this for now) 
2. **direction** - whether you want to buy it or sell it
3. **price** at which you’re willing to transact, and
4. **quantity** - how many shares/contracts/etc.; also called size
If you want to buy something, like a stock or a contract, you can say or otherwise indicate a **bid** to buy. If you want to sell, it’s an **offer** to sell.

The market maker will often set a spread, for example £4-£5, for a product, say a pencil. This means they will buy in at £4/pencil and sell at £5/pencil. The challenge part is to predict the actual value of the pencil so that you can set such a spread. Thus, we made this game in order to simulate that process for anyone who wants to know more about market making and trading in general.

## How to play
There are 4 players and 15 cards, numbered from 1 to 15. 7 cards are drawn from random. The sum of the 7 cards is the `market value` of the product we are making the market for. The other 8 cards will be discarded.
 
At the start of the game, each of the players will receive a card. Each player only knows their own card. There are 4 rounds. After each of the first 3 rounds, one of the remaining 3 cards is revealed to all the players. Each of the players will take turn to make the market for the product (with a spread of 1). The other players will then have to buy/sell between 1-3 products. For example, if Player 1 is making a market of 54-55, other players can buy 1-3 products at 55, or sell 1-3 products at 54.

The order is as follows:
- **ROUND 1:**
  - Player 1 makes the market. The rest decides to buy/sell.
  - Player 2 makes the market. The rest decides to buy/sell.
  - Player 3 makes the market. The rest decides to buy/sell.
  - Player 4 makes the market. The rest decides to buy/sell.

    A card is revealed. Each player has 1 minute to revise their strategy.
- **ROUND 2:**
  - Player 2 makes the market. The rest decides to buy/sell.
  - Player 3 makes the market. The rest decides to buy/sell.
  - Player 4 makes the market. The rest decides to buy/sell.
  - Player 1 makes the market. The rest decides to buy/sell.
    
    A card is revealed. Each player has 1 minute to revise their strategy.
 - **ROUND 3:**
   - Player 3 makes the market. The rest decides to buy/sell.
   - Player 4 makes the market. The rest decides to buy/sell.
   - Player 1 makes the market. The rest decides to buy/sell.
   - Player 2 makes the market. The rest decides to buy/sell.
   
    A card is revealed. Each player has 1 minute to revise their strategy.
 - **ROUND 4:**
   - Player 4 makes the market. The rest decides to buy/sell.
   - Player 1 makes the market. The rest decides to buy/sell.
   - Player 2 makes the market. The rest decides to buy/sell.
   - Player 3 makes the market. The rest decides to buy/sell.
    
    The remaining products are bought/sold at the `market value` price.
    
    The player with the largest balance wins.
   
## How we built it
Market Making 101 runs on the terminal. We built it using Java as the main programming language, with [Keanu](https://improbable-research.github.io/keanu/) to help us represent various probabilistic variables within the game.

## Challenges we ran into
This is the first time we encountered with Probabilistic Programming Language. The API is also currently in active development. Thus, we spent quite a bit of time figuring out how to use many features of the language.

## Accomplishments that we're proud of
Despite spending the first 3 hours struggling to find ideas, we completed the game both in the backend and the UI. Overall, the game is rather polished (e.g. decent visualisation despite Java and is decently challenging for beginner).
                                                             
We hope Market Making 101 is an interesting introduction that potentially inspires many traders-to-be out there.

## What we learned
We learned how to use [Keanu](https://improbable-research.github.io/keanu/) as a probabilistic programming language and how to incorporate it into our project.

## What's next for Market Making 101
We are making the game more difficult by allowing a larger range of values, hence increase the uncertainty of the true value. The spread could also be varied depending on how confident the market maker is of the true value. Lastly, we hope we have time for a more eye-catching UI than the pretty-printing in the terminal.
