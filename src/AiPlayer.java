import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class AiPlayer extends Player {
	
	private GamePanel gui;
	
	final int ABILITY_LEVEL;
	
	public static final String[] difficultyNames = {"Beginner", "Intermediate", "Master"};
	
    public AiPlayer(Game game, String icon, String name, int difficulty) {
    	super(game, icon, name);
    	ABILITY_LEVEL = difficulty;
    }
    
    public void connectGUI(GamePanel panel) {
    	gui = panel;
	}
    
    
    
    //========= above: logistical stuff; below: algorithm ==========

	
    
	static final int CORE_COUNT = Runtime.getRuntime().availableProcessors();
	ExecutorService EXEC = Executors.newFixedThreadPool(CORE_COUNT);
	
	private final AtomicInteger iterCounter = new AtomicInteger(0);
	private int totalIters = 0;
    private PutMove processPutMove(PutMove m, Grid g, Domino d, boolean computeNextPick, boolean addProgress) {
    		
		int x = m.dominoPosition.x;
		int y = m.dominoPosition.y;
		
		d.setRotation(m.dominoRotation);
		if (!g.canPlay(d, x, y))
			return null;
		
		Grid trialGrid = g.clone();
		playDomino(d, trialGrid, x, y);
		
		m.score = trialGrid.getScoreWithPotentialBonuses();
		m.biggestContiguousRegionSize = trialGrid.getLargestRegion();
		//m.totalTileCountOfConnectedRegions =
		
		if (computeNextPick && getPickableHand() != null) {
			
			//System.err.println("about to find the best pick for the next move");
			
			PickMove pick = bruteForcePick(trialGrid, game.getNextHand(), m, false);
			if (pick.next == null) {
				pick.apply(m);
				pick.score = m.score - Grid.HARMONY_BONUS;
			}
			
			m.setNextIfBetter(pick);
			m.receive();
		}

		if (addProgress) {
			iterCounter.getAndIncrement();
		}
		
		return m;
    }
    
    private PickMove bruteForcePick(Grid grid, Map<Domino, Player> hand, Move<?, PickMove> rootMove, boolean multithread) {
    	
		Domino[] dominoesArray = hand.keySet().toArray(new Domino[hand.size()]);
		
    	for (int i = dominoesArray.length - 1; i >= 0; i--) {
    		
    		if (hand.get(dominoesArray[i]) != null) continue; // domino already picked

			PickMove pick = new PickMove(i, dominoesArray[i].ONE.CROWNS + dominoesArray[i].TWO.CROWNS);
			pick.next = bruteForcePut(grid, dominoesArray[i], pick, multithread);
			
			if (pick.next == null) {
				pick.apply(rootMove);
			}
			
			rootMove.setNextIfBetter(pick);
    		
			System.gc();
		}
    	
		rootMove.receive();
    	
    	return rootMove.next;
    }
    
    private PutMove bruteForcePut(Grid grid, Domino domino, Move<?, PutMove> rootMove, boolean multithread) {


		Grid g = grid.clone();
    	Domino d = domino.clone();
    	Rect bounds = g.getBounds();
    	
    	List<PutMove> allLegalPlays = new ArrayList<>();
    	
    	boolean isRoot = rootMove instanceof Origin;
    	
	    	for (int x = bounds.x1; x < bounds.x2; x++) {
	    		for (int y = bounds.y1; y < bounds.y2; y++) {
				for (int r = 0; r < 4; r++) {
					d.setRotation(r);
					if (!g.canPlay(d, x, y)) {
						continue;
					}
					allLegalPlays.add(new PutMove(new Point(x, y), r));
				}
			}
	    	}
    	boolean canPlay = allLegalPlays.size() > 0;
	    	
    	if (ABILITY_LEVEL == 0) {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			PutMove randomMove = null;
			if (canPlay) {
				randomMove = allLegalPlays.get(rng.nextInt(0, allLegalPlays.size()));
			}
			allLegalPlays.clear();
			if (canPlay) {
				allLegalPlays.add(randomMove);
			}
			
			System.gc();
    	}
    	else if (ABILITY_LEVEL == 1) {
    	
    		Origin<PutMove> temp = new Origin<PutMove>();
    		for (final PutMove m : allLegalPlays) {
				PutMove processedMove = processPutMove(m, g, d, false, false);
				temp.setNextIfBetter(processedMove);
				
				System.gc();
			}
    		
    		allLegalPlays.clear();
    		
    		if (canPlay)
    			allLegalPlays.add(temp.next);
			
			System.gc();
    	}
    	
    	if (isRoot) {
    		totalIters = allLegalPlays.size();
    	}
    	
    	if (multithread) {
    		
    		if (EXEC.isShutdown())
        			EXEC = Executors.newFixedThreadPool(CORE_COUNT);
    		
		    	List<Callable<PutMove>> tasks = new ArrayList<>();
		    	for (final PutMove m : allLegalPlays) {
		    	    Callable<PutMove> c = () -> processPutMove(m, g, d, isRoot, isRoot);
		    	    tasks.add(c);
		    	}

		    	try {
				List<Future<PutMove>> results = EXEC.invokeAll(tasks);
				for (Future<PutMove> a : results) {
					rootMove.setNextIfBetter(a.get());
				}

				results = null;
				System.gc();

			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
    	}
    	else {
    		for (final PutMove m : allLegalPlays) {
    			PutMove processedMove = processPutMove(m, g, d, isRoot, isRoot);
				rootMove.setNextIfBetter(processedMove);
				
				System.gc();
			}
		}

		allLegalPlays.clear();
		System.gc();

		return rootMove.next;
	}
    
    private Map<Domino, Player> getPickableHand() {
    	return game.isPastStartUp() ? game.getNextHand() : game.getCurrentHand();
    }
    
    public void play() {
    	(new SwingWorker<Void, Void>() {
			@Override
			public Void doInBackground() {
					boolean canDiscard = canDiscard();
					
					switch (getTurnphase()) {
					
						case PUT:
							if (canDiscard) {
								try {
									SwingUtilities.invokeAndWait(() -> gui.discard.doClick());
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
							else {
								PutMove bestPlay = bruteForcePut(grid, getCurrentDomino(), new Origin<PutMove>(), true);
								getCurrentDomino().setRotation(bestPlay.dominoRotation);
								playDomino(bestPlay.dominoPosition.x, bestPlay.dominoPosition.y);
			                    grid.getCachedGraphicalMeasurements(gui);
			                    grid.getScore();
			                    
			                    	if (bestPlay.next == null) {
										game.playTurnPick(-1);
			                    	}
			                    	else {
			                    		game.playTurnPick(bestPlay.next.dominoIndex);
			                    	}
			                    	
								break;
							}
						
						case PICK:
							if (game.getNextHand() == null && game.cardsRemaining() == 0) {
								game.playTurnPick(-1);
							}
							else {
								PickMove bestPlay = bruteForcePick(grid, getPickableHand(), new Origin<PickMove>(), true);
								game.playTurnPick(bestPlay.dominoIndex);
							}
							
							break;
					}
				iterCounter.set(0);
				totalIters = 0;
				return null;
			}
			@Override
			public void done() {
				// The following try-catch is needed because exceptions that occur in SwingWorker background tasks don't automatically print error messages.
				
				try { 
		            get(); // this line can throw InterruptedException or ExecutionException
		        } 
		        catch (ExecutionException e) {
		            Throwable cause = e.getCause(); // if SomeException was thrown by the background task, it's wrapped into the ExecutionException
		            cause.printStackTrace();
		        }
		        catch (InterruptedException ie) {
		            // TODO handle the case where the background task was interrupted as you want to
		        }
				
				gui.repaint();
			}
    	}).execute();
    }
}

class PickMove extends Move<PickMove, PutMove> {
	
	final int dominoIndex;
	final int crowns;
	
	public PickMove(int dominoIndex, int crowns) {
		this.dominoIndex = dominoIndex;
		this.crowns = crowns;
	}
	
	@Override
	public int compareTo(PickMove c) {
		receive();
		
		if (next != null && c.next == null)
			return 1;
		else if (next == null && c.next != null)
			return -1;
		else if (next != null) {
			return next.compareTo(c.next);
		}
		
		if (crowns != c.crowns) {
			return Integer.compare(crowns, c.crowns);
		}
		
		if (dominoIndex < c.dominoIndex) return 1;
		else if (dominoIndex > c.dominoIndex) return -1;
		return 0;
	}
}

class PutMove extends Move<PutMove, PickMove> {
	
	final Point dominoPosition;
	final int dominoRotation;
	
	public PutMove(Point dominoPosition, int dominoRotation) {
		this.dominoPosition = dominoPosition;
		this.dominoRotation = dominoRotation;
	}
	
	@Override
	public int compareTo(PutMove c) {
		receive();
		
		if (c == null) {
			return 1;
		}
		
		if (score + bonus > c.score + c.bonus) return 1;
		else if (score + bonus < c.score + c.bonus) return -1;
		else if (totalTileCountOfConnectedRegions > c.totalTileCountOfConnectedRegions) return 1;
		else if (totalTileCountOfConnectedRegions < c.totalTileCountOfConnectedRegions) return -1;
		else if (biggestContiguousRegionSize > c.biggestContiguousRegionSize) return 1;
		else if (biggestContiguousRegionSize < c.biggestContiguousRegionSize) return -1;
		else if (numberOfConnectedRegions > c.numberOfConnectedRegions) return 1;
		else if (numberOfConnectedRegions < c.numberOfConnectedRegions) return -1;
		else if (numberOfSidesConnected > c.numberOfSidesConnected) return 1;
		else if (numberOfSidesConnected < c.numberOfSidesConnected) return -1;
		return 0;
	}
}

class Origin<N extends Move> extends Move<Origin, N> {

	@Override
	public int compareTo(Origin o) {
		receive();
		return 0;
	}
}

abstract class Move<T extends Move, N extends Move> implements Comparable<T> {
	
	int score;
	int bonus = score = (int)Double.NEGATIVE_INFINITY;
	int totalTileCountOfConnectedRegions = 0;
	int numberOfConnectedRegions = 0;
	int numberOfSidesConnected = 0;
	int biggestContiguousRegionSize = 0;
	
	int randomOverrideCount;
	N next;
	
	void setNextIfBetter(N move) {
		if (move == null) return;
		if (next == null) next = move;
		
		int comp = move.compareTo(next);
		if (comp == 1) {
			next = move;
			randomOverrideCount = 0;
		}
		else if (comp == 0) {
			ThreadLocalRandom rng = ThreadLocalRandom.current();
			if (rng.nextDouble() < 1.0 / (randomOverrideCount + 2.0)) {
				next = move;
				randomOverrideCount++;
			}
		}
	}
	
	void apply(Move move) {
		score = move.score;
		bonus = move.bonus;
		totalTileCountOfConnectedRegions = move.totalTileCountOfConnectedRegions;
		biggestContiguousRegionSize = move.biggestContiguousRegionSize;
	}
	
	void receive() {
		Move move = this;
		while (move.next != null) {
			move = move.next;
		}
		apply(move);
	}
}

