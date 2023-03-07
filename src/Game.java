import java.awt.*;
import java.util.List;
import java.util.*;

public class Game {
    public final int NUMPLAYERS, HANDSIZE;
    public final int GAME_GRID_SIZE; // All players in a game have the same grid size. Therefore, grid size is an attribute of the game.
    private final Deck deck;
    private final Player[] players;
    private final Player[] scoreBoard;

    public boolean readyToContinue = false; // if true, player can press any key to move to the next player's turn

    private TreeMap<Domino, Player> currentHand, nextHand;
    private int currentDominoIndex;
    private boolean pastStartUp, gameEnd;


    public Game(int NUMPLAYERS, int GLOBAL_GRID_SIZE, String[] playerIcons, String[] playerNames, Boolean[] arePlayersBots, Integer[] botLevels) {

        Debug.threadCheck("Game > Game", false);

        this.NUMPLAYERS = NUMPLAYERS;
        this.GAME_GRID_SIZE = GLOBAL_GRID_SIZE;
        HANDSIZE = GLOBAL_GRID_SIZE == 7 ? 4 : NUMPLAYERS;
        gameEnd = false;
        pastStartUp = false;
        currentDominoIndex = 0;
        deck = new Deck();

        List<Player> playersList = new ArrayList<>();
        for (int i = 0; i < NUMPLAYERS; i++) {
            Player p;
            if (arePlayersBots[i]) {
                p = new AiPlayer(this, playerIcons[i], playerNames[i], botLevels[i]);
            } else {
                p = new Player(this, playerIcons[i], playerNames[i]);
            }
            playersList.add(p);
        }

        //Shuffle players
        Collections.shuffle(playersList);
        players = playersList.toArray(new Player[playersList.size()]);

        currentHand = deck.getHand(HANDSIZE);
        if (GLOBAL_GRID_SIZE != 7)
            for (int i = 0; i < 4 - NUMPLAYERS; i++)
                deck.getHand(12);
        scoreBoard = players.clone();
    }

    public void panelInitialized() {
        goAIifAIturn();
    }

    public void goAIifAIturn() {
        //System.out.println("brooo");
        if (getCurrentPlayer() instanceof AiPlayer) {
            ((AiPlayer) getCurrentPlayer()).play();
        }
    }

    public void rotateCurrentDomino() {

        Debug.threadCheck("Game > rotateCurrentPlayerDomin", false);

        getCurrentPlayer().getCurrentDomino().rotate();
        //System.out.println("Rotated current domino");

        Player p = getCurrentPlayer();
        Grid playerGrid = p.getGrid();
        Point mouseoveredTileIndices = p.getMouseoveredTileIndices();

        p.validate();

        playerGrid.autoHighlight(p.getCurrentDomino(), mouseoveredTileIndices.x, mouseoveredTileIndices.y);
    }

    public void playTurnPick(int index) {
        Debug.threadCheck("Game > playTurnPick", false);
        if (nextHand == null && cardsRemaining() == 0) {
            if (getPlayerWhoPickedDomino(currentDominoIndex) == getPlayerWhoPickedDomino(HANDSIZE - 1)) {
                updateScoreBoard();
                gameEnd = true;
            }
            incrementNextPlayer();
            if (!isGameEnd())
                goAIifAIturn();
            return;
        }

        ArrayList<Map.Entry<Domino, Player>> list = new ArrayList<>(isPastStartUp() ? nextHand.entrySet() : currentHand.entrySet());
        if (list.get(index).getValue() == null) {
            getCurrentPlayer().setCurrentDomino(list.get(index).getKey());
            list.get(index).setValue(getCurrentPlayer());
        } else {
            return;
        }

        if (!isPastStartUp()) {
            incrementNextPlayer();
            goAIifAIturn();
        } else if (getCurrentPlayer().getTurnphase() == TurnPhases.PUT) {
            incrementNextPlayer();
            if (isReadyToGoOn()) {
                nextRound();
            }
            goAIifAIturn();
        }
    }

    public void nextRound() {
        try {
            currentHand = nextHand;
            nextHand = deck.getHand(HANDSIZE);
            for (Player p : players)
                p.reassignChosenDominoes();
        } catch (EmptyStackException E) {
            nextHand = null;
            System.out.println(E.getMessage());
        }
    }

    private void incrementNextPlayer() {
        currentDominoIndex = (currentDominoIndex + 1) % HANDSIZE;
        //System.out.println("Next playa");
    }

    public int getCurrentDominoIndex() {
        return currentDominoIndex;
    }

    public Player getCurrentPlayer() {
        if (!isPastStartUp())
            return players[(NUMPLAYERS == 2 && GAME_GRID_SIZE == 7) ? currentDominoIndex / 2 : currentDominoIndex];
        return getPlayerWhoPickedDomino(currentDominoIndex);
    }

    public Player[] getPlayers() {
        return players;
    }

    public TreeMap<Domino, Player> getCurrentHand() {
        return currentHand;
    }

    public TreeMap<Domino, Player> getNextHand() {
        return nextHand;
    }

    public boolean isGameEnd() {
        return gameEnd;
    }

    public boolean isPastStartUp() {
        if (pastStartUp)
            return true;
        if (pastStartUp = !currentHand.containsValue(null))
            nextHand = deck.getHand(HANDSIZE);
        return pastStartUp;
    }

    public boolean isReadyToGoOn() {
        updateScoreBoard();
        return isPastStartUp() ? !nextHand.containsValue(null) : !currentHand.containsValue(null);
    }

    public Player getPlayerWhoPickedDomino(int index) {
        ArrayList<Map.Entry<Domino, Player>> list = new ArrayList<>(currentHand.entrySet());
        return list.get(index).getValue();
    }

    public int cardsRemaining() {
        return deck.cardsRemaining();
    }

    public Player getPlayerAbs(int index) {
        return players[index];
    }

    public void updateScoreBoard() {

        Debug.threadCheck("Game > updateScoreBoard", false);

        int n = scoreBoard.length;

        for (int i = 0; i < n - 1; i++) {
            int max = i;
            for (int j = i + 1; j < n; j++)
                if (scoreBoard[j].compareTo(scoreBoard[max]) > 0)
                    max = j;

            Player temp = scoreBoard[max];
            scoreBoard[max] = scoreBoard[i];
            scoreBoard[i] = temp;
        }

        for (int i = 0; i < scoreBoard.length; i++) {
            if (i > 0 && scoreBoard[i].compareTo(scoreBoard[i - 1]) == 0)
                scoreBoard[i].setRank(scoreBoard[i - 1].getRank());
            else {
                scoreBoard[i].setRank(i + 1);
            }
        }
    }

    public Player[] getScoreBoard() {

        return scoreBoard;
    }

    public boolean canDiscard() {
        return getCurrentPlayer().canDiscard();
    }

    public enum TurnPhases {
        PICK, PUT
    }
}
