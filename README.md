
# TO-DO

 - **Display the numbers of the drawn dominoes to show that they're ordered from least to greatest**
 - **Fix 7x7 scoring problems**
 - **Fix the incomptency of the master AI**
 - Method that calculates the sum of the sizes of the individual regions connected to a domino, used as an AI move ranking factor
 - Progress bar during AI processing
 - Reduce JAR file size and program memory usage
 - Show backs of dominoes with numbers
 - Domino drafting and flipping animations
 - ✅ ~~**Improve graphics performance (animation frame rate): cache scaled images. Do not rescale all images every repaint**~~
 - ✅ ~~**Fix AI exceptions**~~
 - ✅ ~~**7x7 AI**~~
 - ✅ ~~**Make noob AI more nooby**~~
 - ✅ ~~Add character limit to player name boxes~~
 - ✅ ~~Unset defaults on the start screen before making the final jar file~~
   - ✅ ~~Icon and AI difficulty should initially be set to "---"~~
   - ✅ ~~Player name should be empty~~
   - ✅ ~~Mode should default to human~~
 - ✅ ~~Display player names in game and scoreboard~~
 - ✅ ~~Icon or text like "(AI)" next to player icon or name to indicate that players are AI~~
   - ✅ ~~Show their difficulty levels~~
 - ✅ ~~Show the official Kingdomino logo on the title screen instead of the black text in Times New Roman~~
 
 ---

# Kingdomino Project · Team Christopher
kingdomino-project-team-christopher created by GitHub Classroom

# [Rubric](https://katyisd.instructure.com/courses/340571/files/28450415?module_item_id=13470387) | [Timetable](https://docs.google.com/spreadsheets/d/1Y2sr-q-96gvP5Rszw506G8TEGlhwAp6UnOW1gQSNrIE/edit) | [Prospectus](https://docs.google.com/document/d/1bkiGW1l9FdCjdGb47p7VHJyUJ_xCdPpZoRx48NaN6Pc/edit) | [UML Diagram](https://lucid.app/lucidchart/105b3a56-5193-4488-97e7-701bfa60f804/edit)

> ### Important
>
> **All code that modifies the Swing GUI should run on the [event dispatch thread.](https://stackoverflow.com/questions/7217013/java-event-dispatching-thread-explanation) Run code that doesn't modify the Swing GUI outside the EDT.**

---

## AI Test Results (🤦‍)

### All Difficulties Together

3-player games consisting of a noob, an intermediate, and a master.

| **Level** | **Trial 1** | **Trial 2** | **Trial 3** | **Average** |
|-----------|-------------|-------------|-------------|-------------|
|Noob|64|47|42|52|
|Intermediate|53|50|32|45|
|Master|42|33|35|36.667|

### Noobs vs. Noobs

<sup>Tested by Chris</sup>

| **Rank** | **Trial 1** | **Trial 2** | **Trial 3** | **Trial 4** | **Average** |
|----------|-------------|-------------|-------------|-------------|-------------|
|1st|69|80|64|65|69.5
|2nd|54|61|60|50|56.25
|3rd|52|57|58|39|51.25
|4th|40|43|49|35|41.75

Overall average: 54.6875

Sample standard deviation: 12.030516752548

### Intermediates vs. Intermediates

<sup>Tested by Chris</sup>

| **Rank** | **Trial 1** | **Trial 2** | **Trial 3** | **Trial 4** | **Average** |
|----------|-------------|-------------|-------------|-------------|-------------|
|1st|58|56|61|68|60.75
|2nd|54|54|60|68|59
|3rd|48|45|47|60|50
|4th|40|45|45|60|47.5

Overall average: 54.3125

Sample standard deviation: 8.5300937861198

### Masters vs. Masters

<sup>Tested by Chris</sup>

| **Rank** | **Trial 1** | **Trial 2** | **Trial 3** | **Trial 4** | **Average** |
|----------|-------------|-------------|-------------|-------------|-------------|
|1st|59|41|50|40|47.5
|2nd|33|40|41|39|38.25
|3rd|30|33|34|38|33.75
|4th|29|33|30|37|32.25

Overall average: 37.9375

Sample standard deviation: 7.5537139044314

---

## Progress

 - **83 of 85 required points earned**

 - **21 of 21 bonus points earned** (✅ done)

 - 30 opinion points

| **Requirement**                                                                                                                      |**Max Points**|**Us**|
|----------------------------------------------------------------------------------------------------------------------------------------|------------|----|
| How are the Graphics?                                                                                                                  | 10         |    |
| How is the User Interface?                                                                                                             | 10         |    |
| Can user see all 4 Kingdoms at once?                                                                                                   | 3          | 3  |
| Are the correct number of tiles shown?                                                                                                 | 3          | 3  |
| Are the tiles arranged from lowest to highest?                                                                                         | 5          | 3  |
| On the first round, do the players play in a randomized order and do not place the tiles once they have chosen?                         | 3          | 3   |
|On all other rounds, do the players play in the order of the last round's tile choices, and are they able to immediately place the tiles?| 10         | 10   |
| Can the player rotate the tile?                                                                                                        | 5          | 5  |
| Can the player place the tile exactly where they want to place the tile?                                                               | 5          | 5  |
| Does the placed tile conform to the tile placing rules, including the 5x5 restriction?                                                 | 10         | 10 |
| Is the tile discarded only when the tile is unplayable?                                                                                | 3          | 3   |
| Does the game end once all tiles have been placed or discarded?                                                                        | 3          | 3   |
| Does the game show the scores while also showing each kingdom so that the scores can be verified?                                      | 5          | 5   |
| Does the game show the scores for each category for scoring: plains, water, sheep, swamp, forest, and mines                             | 5          | 5  |
| Does the game correctly calculate each category                                                                                        | 10         | 10   |
| Does the game calculate the Harmony bonus                                                                                              | 3          | 3  |
| Does the game calculate the Middle Kingdom Bonus                                                                                       | 3          | 3   |
| Does the game show all the scores from highest to lowest                                                                               | 3          | 3  |
| Does the game break any ties for the winning scores                                                                                    | 3          | 3  |
| Does the game determine a winner?                                                                                                      | 3          | 3  |
| Bonus: Is there a tile countdown?                                                                                                      | 3          | 3  |
| Bonus: Is there a running total throughout the game                                                                                    | 3          | 3   |
| Bonus: Does the game give suggested placements of tiles                                                                                | 5          | 5  |
| Bonus: Does the game have a working Artificial Intelligence?                                                                           | 10         | 10   |
| Overall opinion | 10 | |

---

## Concept: 3D Kingdomino (4K)

![Concept: 3D Kingdomino (4K)](<https://github.com/Roulette88/Kingdomino/blob/main/Planning/4K%20Kingdomino%203D.png>)

## GUI mockup: board previews for inactive players (✅ done)

Rubric requirement:

> Can user see all 4 Kingdoms at once?

![GUI mockup: board previews for inactive players (✅ done)](https://github.com/Roulette88/Kingdomino/blob/main/Planning/screencapture-file-yitian-killer-gui-mockup-html-2020-11-18-18_21_27.png)
