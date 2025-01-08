package edu.iastate.cs472.proj2;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Abhay Prasanna Rao
 *
 * This class implements the Monte Carlo tree search method to find the best
 * move at the current state.
 */

public class MonteCarloTreeSearch extends AdversarialSearch {
    /**
     * The input parameter legalMoves contains all the possible moves.
     * It contains four integers:  fromRow, fromCol, toRow, toCol
     * which represents a move from (fromRow, fromCol) to (toRow, toCol).
     * It also provides a utility method `isJump` to see whether this
     * move is a jump or a simple move.
     *
     * Each legalMove in the input now contains a single move
     * or a sequence of jumps: (rows[0], cols[0]) -> (rows[1], cols[1]) ->
     * (rows[2], cols[2]).
     *
     * @param legalMoves All the legal moves for the agent at current step.
     */
    private static final int SIMULATION_COUNT = 1000;
    private static final double EXPLORATION_PARAM = Math.sqrt(2);
    private MCTree<CheckersData> tree;
    private Random random;

    public MonteCarloTreeSearch() {
        random = new Random();
    }

    @Override
    protected CheckersMove[] legalMoves() {
        return board.getLegalMoves(CheckersData.BLACK); // Agent plays as BLACK
    }

    @Override
    public CheckersMove makeMove(CheckersMove[] legalMoves) {
        if (legalMoves == null || legalMoves.length == 0) return null;
        if (legalMoves.length == 1) return legalMoves[0];

        // Create root node with current board state
        tree = new MCTree<>();
        MCNode<CheckersData> root = new MCNode<>(copyBoard(board));
        tree.root = root;

        // Run MCTS iterations
        for (int i = 0; i < SIMULATION_COUNT; i++) {
            // Selection
            MCNode<CheckersData> selected = selection(root);

            // Expansion
            MCNode<CheckersData> expanded = expansion(selected, legalMoves);
            if (expanded == null) continue;

            // Simulation
            int result = simulation(expanded);

            // Backpropagation
            backpropagation(expanded, result);
        }

        // Select best move
        return getBestMove(root, legalMoves);
    }

    private MCNode<CheckersData> selection(MCNode<CheckersData> node) {
        while (!isTerminal(node.getState()) && !node.isLeaf()) {
            node = selectUCT(node);
        }
        return node;
    }

    private MCNode<CheckersData> selectUCT(MCNode<CheckersData> node) {
        MCNode<CheckersData> selected = null;
        double bestUCT = Double.NEGATIVE_INFINITY;

        for (MCNode<CheckersData> child : node.getChildren()) {
            if (child.getVisits() == 0) return child;

            double uct = child.getWins() / child.getVisits() +
                    EXPLORATION_PARAM * Math.sqrt(Math.log(node.getVisits()) / child.getVisits());

            if (uct > bestUCT) {
                selected = child;
                bestUCT = uct;
            }
        }

        return selected;
    }

    private MCNode<CheckersData> expansion(MCNode<CheckersData> node, CheckersMove[] legalMoves) {
        if (isTerminal(node.getState())) return node;

        // Get unexpanded moves
        ArrayList<CheckersMove> unexpandedMoves = new ArrayList<>();
        for (CheckersMove move : legalMoves) {
            boolean isExpanded = false;
            for (MCNode<CheckersData> child : node.getChildren()) {
                if (movesEqual(child.getMove(), move)) {
                    isExpanded = true;
                    break;
                }
            }
            if (!isExpanded) {
                unexpandedMoves.add(move);
            }
        }

        if (unexpandedMoves.isEmpty()) return null;

        // Create new child node with random unvisited move
        CheckersMove move = unexpandedMoves.get(random.nextInt(unexpandedMoves.size()));
        CheckersData newState = copyBoard(node.getState());
        newState.makeMove(move);

        MCNode<CheckersData> child = new MCNode<>(newState, node, move);
        node.addChild(child);

        return child;
    }

private int simulation(MCNode<CheckersData> node) {
    if (node == null || node.getState() == null) {
        throw new IllegalArgumentException("Invalid node for simulation");
    }
    CheckersData simulationBoard = copyBoard(node.getState());
    int currentPlayer = CheckersData.BLACK;
    int moveCount = 0;
    final int MAX_MOVES = 100;

    while (!isTerminal(simulationBoard) && moveCount < MAX_MOVES) {
        CheckersMove[] moves = simulationBoard.getLegalMoves(currentPlayer);
        if (moves == null || moves.length == 0) {
            // Game over
            if (currentPlayer == CheckersData.BLACK) {
                return -1; // BLACK loses
            } else {
                return 1;  // BLACK wins
            }
        }

        // Make random move (uniform distribution as required)
        CheckersMove move = moves[random.nextInt(moves.length)];
        simulationBoard.makeMove(move);

        currentPlayer = (currentPlayer == CheckersData.RED) ?
                CheckersData.BLACK : CheckersData.RED;
        moveCount++;
    }

    // If max moves reached, it's a draw
    if (moveCount >= MAX_MOVES) {
        return 0; // Draw
    }

    // Evaluate final position
    double score = evaluateBoard(simulationBoard);
    if (score > 0) return 1;      // BLACK wins
    else if (score < 0) return -1; // BLACK loses
    else return 0;                 // Draw
}
    private void backpropagation(MCNode<CheckersData> node, int result) {
        while (node != null) {
            node.incrementVisits();
            if (result == 1) { // Win
                node.addWin();
            } else if (result == 0) { // Draw
                node.addDraw(); // Add 0.5
            }
            node = node.getParent();
        }
    }


private CheckersMove getBestMove(MCNode<CheckersData> root, CheckersMove[] legalMoves) {
    MCNode<CheckersData> bestChild = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    for (MCNode<CheckersData> child : root.getChildren()) {
        // Use win ratio for final decision instead of just visits
        double winRatio = child.getVisits() > 0 ?
                child.getWins() / child.getVisits() : 0;

        if (winRatio > bestScore) {
            bestScore = winRatio;
            bestChild = child;
        }
    }

    return bestChild != null ? bestChild.getMove() : legalMoves[0];
}

    private boolean isTerminal(CheckersData state) {
        return state.getLegalMoves(CheckersData.BLACK) == null &&
                state.getLegalMoves(CheckersData.RED) == null;
    }

    private double evaluateBoard(CheckersData state) {
        double score = 0;
        int blackPieces = 0;
        int redPieces = 0;
        int blackKings = 0;
        int redKings = 0;

        // Piece counting and position evaluation
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                switch (state.pieceAt(row, col)) {
                    case CheckersData.BLACK:
                        blackPieces++;
                        // Bonus for advancement
                        score += (row * 0.1); // Encourage forward movement
                        // Bonus for protecting back row
                        if (row == 0) score += 0.5;
                        // Bonus for center control
                        if (row >= 2 && row <= 5 && col >= 2 && col <= 5) {
                            score += 0.3;
                        }
                        break;

                    case CheckersData.BLACK_KING:
                        blackKings++;
                        score += 1.5; // Kings are worth more
                        // Bonus for center control
                        if (row >= 2 && row <= 5 && col >= 2 && col <= 5) {
                            score += 0.4;
                        }
                        break;

                    case CheckersData.RED:
                        redPieces++;
                        // Penalize opponent advancement
                        score -= ((7 - row) * 0.1);
                        // Penalize opponent back row protection
                        if (row == 7) score -= 0.5;
                        // Penalize opponent center control
                        if (row >= 2 && row <= 5 && col >= 2 && col <= 5) {
                            score -= 0.3;
                        }
                        break;

                    case CheckersData.RED_KING:
                        redKings++;
                        score -= 1.5;
                        // Penalize opponent center control
                        if (row >= 2 && row <= 5 && col >= 2 && col <= 5) {
                            score -= 0.4;
                        }
                        break;
                }
            }
        }

        // Endgame evaluation adjustments
        int totalPieces = blackPieces + redPieces + blackKings + redKings;
        if (totalPieces <= 6) { // Endgame situation
            score += (blackKings - redKings) * 0.5; // Kings become more valuable
            // Encourage piece concentration in endgame
            score += evaluateEndgamePosition(state);
        }

        // Mobility evaluation
        CheckersMove[] blackMoves = state.getLegalMoves(CheckersData.BLACK);
        CheckersMove[] redMoves = state.getLegalMoves(CheckersData.RED);

        // Add mobility score
        if (blackMoves != null) score += blackMoves.length * 0.1;
        if (redMoves != null) score -= redMoves.length * 0.1;

        return score;
    }

    private double evaluateEndgamePosition(CheckersData state) {
        double score = 0;
        int blackCenterX = 0, blackCenterY = 0;
        int blackCount = 0;

        // Calculate center of mass for black pieces
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (state.pieceAt(row, col) == CheckersData.BLACK ||
                        state.pieceAt(row, col) == CheckersData.BLACK_KING) {
                    blackCenterX += col;
                    blackCenterY += row;
                    blackCount++;
                }
            }
        }

        if (blackCount > 0) {
            blackCenterX /= blackCount;
            blackCenterY /= blackCount;

            // Reward pieces being closer together in endgame
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (state.pieceAt(row, col) == CheckersData.BLACK ||
                            state.pieceAt(row, col) == CheckersData.BLACK_KING) {
                        double distance = Math.sqrt(Math.pow(col - blackCenterX, 2) +
                                Math.pow(row - blackCenterY, 2));
                        score += (4 - distance) * 0.1; // Reward closer pieces
                    }
                }
            }
        }

        return score;
    }

    private CheckersData copyBoard(CheckersData original) {
        CheckersData copy = new CheckersData();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                copy.board[row][col] = original.board[row][col];
            }
        }
        return copy;
    }

    private boolean movesEqual(CheckersMove move1, CheckersMove move2) {
        if (move1 == null || move2 == null) return false;
        if (move1.rows.size() != move2.rows.size()) return false;

        for (int i = 0; i < move1.rows.size(); i++) {
            if (!move1.rows.get(i).equals(move2.rows.get(i)) ||
                    !move1.cols.get(i).equals(move2.cols.get(i))) {
                return false;
            }
        }
        return true;
    }
}
