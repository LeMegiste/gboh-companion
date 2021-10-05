# gboh-companion
A commande line tool to handle 99% of the dice rolls of GMT Great Battles of History games

## Overview
This is a command line interface to handle all the dice rolls and charts when playing GMT Games's Great Battles of History Games.
Indeed, currently only SPQR Deluxe edition's rules are supported.

This companion helps the players with reviewing the charts, handling the dice rolls, counting the hits, the missile status...

## How to use ? 
Prepare the battle sheets (using excel). Each unit must be identified by a unique code, easy to remember
(see the folders for Zama, Great Plains and Lautulae for examples)

Launch

``java -jar gboh-compagnion.jar <yourBattleDirectory>``

## Two types of commands
### Generic game commands
* **LOAD** : Loads the battle in the folder provided
* **DUMP** : Dumps the armies in their current state in the game folder.
* **END_TURN** : Ends the current turn
* **ROUT_POINTS** : Computes the current rout points for both armies
* **UNDO** : Undoes one of the last 10 commands
* **EXIT** : SAVE the game and exits
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

