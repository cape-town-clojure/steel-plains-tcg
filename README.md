# Steel Plains

The home for the trading card game we're building in the Cape Town Clojure User Group.

Join us on `#clojure.za` at `irc.freenode.net`!

## Getting set up

1. Clone this repo and `cd` into it.
2. Run `mkdir -p target/generated`. You only need to do this once.
3. In three different terminal windows, run:
	- `lein cljx auto`.
	- `lein cljsbuild auto`.
	- `lein run`.

You can now browse to `http://localhost:8080`, and you can modify any
cljx/cljs file and have the changes appear when you refresh the
browser.

You can press `ctrl-esc` to see a draggable debug window showing the
state of the deck builder.

## About the game

Steel Plains is part Deck Building Collectable Card Game, part strategy board game where players design and vote for up to X new cards to be cycled into the game very month. Up to Y cards can be cycled out of the game each month.

Inspiration for the actual gameplay mechanics are drawn heavily from Dvorak, Magic: The Gathering and Mojang's Scrolls

Overarching Gameplay Goals:
 - A players turn has to be entirely uninterruptible by other players so that games can be played asynchronously and over possibly longer periods of time.
 - Have enough definition in the factions of the game so that players can vote correctly on what cards suit which colours.
 - Hexagonal shared board where pieces get placed and played as time progresses

The setting is general, family friendly, light fantasy.

Some specific plans:
 - Have a three month cycle for cards:
   - In the first month, a card is in concept status, and only the top X voted in (more "in" votes than "out") can make it to the next stage
   - The second month of card cycle is art and coding, and again an "in" or "out" vote applies.
   - The third month is beta testing of all cards that made it in, and again, an "in" or "out" vote applies - this will include any previously removed cards that have regained an "in" vote
   - All cards that made it in at the beta testing stage gets accepted and "printed" into the main game.

 - Card removal cycle:
   - Every month, all cards currently in "print" are available to be voted out, the top Y voted out will be pushed into an area where they can be revoted and third-stage beta tested before possibly bringing it back into the game.

 - Card printing cycles
   - Every card "printed" and handed to a player will have its edition stamped onto it so that it can increase in value over time
   - Still thinking about how to limit print cycles of cards otherwise.

 - Factions
   - Green: Nature
   - Blue: Willpower
   - Red: Chaos
   - White: Light

Primary Card Types:
 - Creature: Summon a movable unit on the board
 - Structure: Summon an unmovable unit on the board
 - Land: Place a land type on a friendly or Steel Plain (unclaimed) hex
 - Spell: A once off effect that changes the state of the world in some way
 - Enchantment: An local or global effect that sticks around as long as the enchantment is in play
 - Equipment: A physical item, placed on a creature and can be moved, dropped and have no specific owner.

Decks & Mechanics:
 - Dual decks:
   - Main deck, minimum 40 cards, limit of 3 per kind
   - Land deck, minimum 30 cards, limit of 4 per non-basic type, no limit for basic land types.
 - Discarded cards get added to the discard pile
 - If you need to draw a card from your deck and there are no cards left, shuffle discard pile and it becomes your deck. Effectively bottomless card pile
 - All cards, when played has an effect and goes to the discard pile, so the card itself is only a 'spell' as such, never the actual artifact.

Land Placement:
 - Inversely proportional to base size:
   - 1 hex    : 3 per turn
   - 2-3 hexes: 2 per turn
   - 4-5 hexes: 1 per turn
 - Separate land 'hand' of 4 cards, replenishes to 4 every turn.
 - Each land hex has an amount of space
 - Land types:
   - Plains: White/Green/Fast/Big
   - Mountains: Red/Green/Slow/Small
   - Rivers: Blue/White/Medium/Medium
   - Hills: White/Red/Medium/Medium
   - Swamp: Red/Blue/Slow/Big
   - Tundra: Green/Blue/Fast/Medium
   - Forest: Green/Red/Slow/Medium
   - Mudflats: Blue/White/Slow/Small

Subtypes:
 - Any card can have arbitrary keyworded subtypes that can be referenced by other cards
 - Basic lands implictly have the same subtype as its title, along with 'land'
 - Other lands can optionally share a subtype of a basic land, along with 'land'

Mana:
 - The function of mana is to provide resources for your spells in hand and activated abilities of units on the board.
 - A land can be sapped as a resource for mana, once per game.
 - Sapped mana expands your total accessible mana pool for every round, and doesn't rely on the land any longer.
 - You can only sap a single land per turn.
 - You can only sap lands that you own.

Bases:
 - Players start with a single base and can expand up to 5 blocks (normal 7-sized map)
 - Each expansion costs gold
 - Each base produces a certain amount of gold per turn
 - Each base has its own health counter.
 - Once a base is destroyed, it cannot be rebuilt by normal means.
 - Once all of a player's bases are destroyed, they lose the game.

Gold's function:
 - Gold is consumed by units on the board for various activated abilities.
 - Construct extra bases


## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
