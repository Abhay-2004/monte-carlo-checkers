package edu.iastate.cs472.proj2;
import java.util.ArrayList;
import java.util.List;
/**
 * @author Abhay Prasanna Rao
 * Node type for the Monte Carlo search tree.
 */
public class MCNode<E>
{
    private E state;  // Current game state
    private MCNode<E> parent;  // Parent node
    private List<MCNode<E>> children;  // Child nodes
    private int visits;  // Number of visits to this node
    private double wins;  // Number of wins from this node
    private CheckersMove move;  // Move that led to this state
    private static final double C = Math.sqrt(2);

    public MCNode(E state) {
        this.state = state;
        this.children = new ArrayList<>();
        this.visits = 0;
        this.wins = 0;
    }

    public MCNode(E state, MCNode<E> parent, CheckersMove move) {
        this(state);
        this.parent = parent;
        this.move = move;
    }

    // Getters and setters
    public E getState() { return state; }
    public MCNode<E> getParent() { return parent; }
    public List<MCNode<E>> getChildren() { return children; }
    public int getVisits() { return visits; }
    public double getWins() { return wins; }
    public CheckersMove getMove() { return move; }

    public void addChild(MCNode<E> child) {
        children.add(child);
    }

    public void incrementVisits() {
        visits++;
    }

    public void addWin() {
        wins++;
    }

    public double getUCT() {
        if (visits == 0) return Double.MAX_VALUE;
        double exploitation = wins / visits;
        double exploration = C * Math.sqrt(Math.log(parent.visits) / visits);
        return exploitation + exploration;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    //method to handle draws
    public void addDraw() {
        wins += 0.5; // Increment by 0.5 for draws
    }
}

