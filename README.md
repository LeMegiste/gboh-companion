# gboh-companion
A command line tool to handle 99% of the dice rolls of GMT Great Battles of History games.

## Overview
This is a command line interface to handle all the dice rolls and charts when playing GMT Games's Great Battles of History Games.
Indeed, currently only SPQR Deluxe edition's rules are supported.

This companion helps the players with reviewing the charts, handling the dice rolls, counting the hits, the missile status... If you are using it, you don't need to keep track of cohesion levels, missile levels, etc.

This tool is a companion for the solitaire player (using paper counters and maps, or Vassal) or can be used in a face to face games if both players agree to use it.

The tool won't replace the knowledge of the rules and does not pretend to cover all edge cases. It is more of an assistant for people knowing the rules well.

## What this tool does not do

* the GBOH Companion is not integrated within Vassal (but a clever programmer could certainly make the two softwares working together)
* the GBOH Companion is not following Leader activations and leader rules.
* the rules as so rich and complex, that you cannot trust the GBOH Companion entirely. The **D** and **SET** commands are here to allow you to *manually* handle edge cases.

## Unit codes
This tool relies on the usage of Unit codes. Unit codes are simple sequences of letters uniquely designating units in a given battle. The idea is to have them short and explicit and easy to memorize.

Examples, from *Great plains* (provided in the Battles directory):
* The Carthaginian levy troups are CMI1 to CMI16
* The Hastati of the first legion are 1Haa to 1Had
* The robust Iberian mercenaries are MHI1 to MHI8
etc.


## How to use ? 
Prepare the battle sheets (using excel). Each unit must be identified by a unique code, easy to remember
(see the folders for Zama, Great Plains and Lautulae as examples, in the ``battles`` directory.)

Launch (from the root of the project)

``java -jar target/gboh-compagnion.jar <yourBattleDirectory>``

Example, for the battle of Zama

``java -jar target/gboh-compagnion.jar battles/Zama``


## Two types of commands
### Generic game commands
* **LOAD** : Loads the battle in the folder provided
* **DUMP** : Dumps the armies in their current state in the game folder.
* **END_TURN** : Ends the current turn
* **ROUT_POINTS** : Computes the current rout points for both armies
* **D** : Just throws a dice
* **ND** : record the values passed as parameters (ex: ND 5,6,2) as next dice throws.
* **NEAR** : Lists all units (or of a given army) that are 2 or 1 hit from collapsing.
* **UNDO** : Undoes one of the last 10 commands
* **EXIT** : saves the game and exits
* **SAVE** : Persists the game in a backup file
* **HELP** : Get some help. Usage: HELP <some specific command>

### Unit commands
The syntax for those commands is always the same:

``<actingUnit1>[,<actingUnit2>,...] CMD <-modifier1> <-modifier1> [<defendingUnit>,<defendingUnit>,...]``

Possible modifiers can be listed using ``HELP <myCommand>``

Example: Balearic Skirmishers 1 fires at Legio I Hastati a.

``BSK1 M 1Haa`` 

Example: Legio I Hastati a assited by Principes a shocks (flank attack) Mercenary HI 3.

``1Haa,1Pra S -f MHI3`` 

**List of all commands**
* **+** : Adds 1 hit to the unit
* **++** : Adds 2 hits to the unit
* **+++** : Adds 3 hits to the unit
* **-** : Removes -1 hit from the unit
* **--** : Removes -2 hit from the unit
* **E** : Eliminates the unit(s)
* **F** : Fight! Resolves the shock combat between attackers and defenders
* **LF** : Line fight - NOT YET IMPLEMENTED
* **LOG** : Logs the units
* **M** : The attacker fires on the defender
* **R** : Rallies the current unit
* **S** : Shock ! Full shock combat (fire, pre-shock, shock) between attackers and defenders
* **SET** : Sets the counter values for the units.

The game is automatically saved after each command. (do not hesitate to open the file game.xml to see the latest commands)

## And now?
I wrote this program to help me during my solitaire replay of some battles. I'm sharing it if it can be of any use to anybody even if I'm conscious that, due to its command line nature, it may be a little hard to adopt by people who are not old geeks.

If you find bugs, if you want evolutions, if you need help to use it, please write me: dev [at] megiste.ch, I'll do my best to help you.


