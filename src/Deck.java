import java.util.Collections;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;

public class Deck {
	
    private static final String attributes = "FOREST 0 FOREST 0\n" +
            "FOREST 0 FOREST 0\n" +
            "WHEAT 0 WHEAT 0\n" +
            "WHEAT 0 WHEAT 0\n" +
            "LAKE 0 LAKE 0\n" +
            "LAKE 0 LAKE 0\n" +
            "FOREST 0 FOREST 0\n" +
            "FOREST 0 FOREST 0\n" +
            "SWAMP 0 SWAMP 0\n" +
            "MEADOW 0 MEADOW 0\n" +
            "MEADOW 0 MEADOW 0\n" +
            "LAKE 0 LAKE 0\n" +
            "WHEAT 0 SWAMP 0\n" +
            "WHEAT 0 MEADOW 0\n" +
            "WHEAT 0 LAKE 0\n" +
            "WHEAT 0 FOREST 0\n" +
            "WHEAT 1 LAKE 0\n" +
            "WHEAT 1 FOREST 0\n" +
            "FOREST 0 MEADOW 0\n" +
            "FOREST 0 LAKE 0\n" +
            "FOREST 1 WHEAT 0\n" +
            "WHEAT 1 MINES 0\n" +
            "WHEAT 1 SWAMP 0\n" +
            "WHEAT 1 MEADOW 0\n" +
            "FOREST 1 LAKE 0\n" +
            "FOREST 1 WHEAT 0\n" +
            "FOREST 1 WHEAT 0\n" +
            "FOREST 1 WHEAT 0\n" +
            "LAKE 1 FOREST 0\n" +
            "LAKE 1 WHEAT 0\n" +
            "LAKE 1 WHEAT 0\n" +
            "FOREST 1 MEADOW 0\n" +
            "WHEAT 0 MEADOW 1\n" +
            "LAKE 1 FOREST 0\n" +
            "LAKE 1 FOREST 0\n" +
            "LAKE 1 FOREST 0\n" +
            "MINES 1 WHEAT 0\n" +
            "MEADOW 0 SWAMP 1\n" +
            "WHEAT 0 SWAMP 1\n" +
            "LAKE 0 MEADOW 1\n" +
            "MEADOW 0 SWAMP 2\n" +
            "WHEAT 0 SWAMP 2\n" +
            "LAKE 0 MEADOW 2\n" +
            "WHEAT 0 MEADOW 2\n" +
            "WHEAT 0 MINES 3\n" +
            "SWAMP 0 MINES 2\n" +
            "SWAMP 0 MINES 2\n" +
            "MINES 2 WHEAT 0";
    
    private final Stack<Domino> DECK;

    public Deck() {
        Scanner sc;
        DECK = new Stack<>();
        try {
            sc = new Scanner(attributes);
            for (int i = 1; i <= 48; i++) {
                Tile one = new Tile(Tile.TileTypes.valueOf(sc.next()), Integer.parseInt(sc.next()), "" + i + "0");
                Tile two = new Tile(Tile.TileTypes.valueOf(sc.next()), Integer.parseInt(sc.next()), "" + i + "1");
                DECK.add(new Domino(i, one, two));
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Collections.shuffle(DECK);
    }

    public Domino pop() {
        return DECK.pop();
    }

    public TreeMap<Domino, Player> getHand(int numOfPlayers) {
        TreeMap<Domino, Player> map = new TreeMap<>();
        for (int i = 0; i < numOfPlayers; i++)
            map.put(pop(), null);
        return map;
    }

    public int cardsRemaining() {
        return DECK.size();
    }
}
