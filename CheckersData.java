package edu.iastate.cs472.proj2;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Abhay Prasanna Rao
 *
 * An object of this class holds data about a game of checkers.
 * It knows what kind of piece is on each square of the checkerboard.
 * Note that RED moves "up" the board (i.e. row number decreases)
 * while BLACK moves "down" the board (i.e. row number increases).
 * Methods are provided to return lists of available legal moves.
 */
public class CheckersData {

  /*  The following constants represent the possible contents of a square
      on the board.  The constants RED and BLACK also represent players
      in the game. */

    static final int
            EMPTY = 0,
            RED = 1,
            RED_KING = 2,
            BLACK = 3,
            BLACK_KING = 4;


    int[][] board;  // board[r][c] is the contents of row r, column c.


    /**
     * Constructor.  Create the board and set it up for a new game.
     */
    CheckersData() {
        board = new int[8][8];
        setUpGame();
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_YELLOW = "\u001B[33m";

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < board.length; i++) {
            int[] row = board[i];
            sb.append(8 - i).append(" ");
            for (int n : row) {
                if (n == 0) {
                    sb.append(" ");
                } else if (n == 1) {
                    sb.append(ANSI_RED + "R" + ANSI_RESET);
                } else if (n == 2) {
                    sb.append(ANSI_RED + "K" + ANSI_RESET);
                } else if (n == 3) {
                    sb.append(ANSI_YELLOW + "B" + ANSI_RESET);
                } else if (n == 4) {
                    sb.append(ANSI_YELLOW + "K" + ANSI_RESET);
                }
                sb.append(" ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append("  a b c d e f g h");

        return sb.toString();
    }

    /**
     * Set up the board with checkers in position for the beginning
     * of a game.  Note that checkers can only be found in squares
     * that satisfy  row % 2 == col % 2.  At the start of the game,
     * all such squares in the first three rows contain black squares
     * and all such squares in the last three rows contain red squares.
     */
    void setUpGame() {
        // Initialize empty board
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = EMPTY;
            }
        }

        // Set up black pieces (rows 0-2)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = BLACK;
                }
            }
        }

        // Set up red pieces (rows 5-7)
        for (int row = 5; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = RED;
                }
            }
        }
    }


    /**
     * Return the contents of the square in the specified row and column.
     */
    int pieceAt(int row, int col) {
        return board[row][col];
    }


    /**
     * Make the specified move.  It is assumed that move
     * is non-null and that the move it represents is legal.
     *
     * Make a single move or a sequence of jumps
     * recorded in rows and cols.
     *
     */
    void makeMove(CheckersMove move) {
        int l = move.rows.size();
        for(int i = 0; i < l-1; i++)
            makeMove(move.rows.get(i), move.cols.get(i), move.rows.get(i+1), move.cols.get(i+1));
    }


    /**
     * Make the move from (fromRow,fromCol) to (toRow,toCol).  It is
     * assumed that this move is legal.  If the move is a jump, the
     * jumped piece is removed from the board.  If a piece moves to
     * the last row on the opponent's side of the board, the
     * piece becomes a king.
     *
     * @param fromRow row index of the from square
     * @param fromCol column index of the from square
     * @param toRow   row index of the to square
     * @param toCol   column index of the to square
     */
    void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Move the piece
        board[toRow][toCol] = board[fromRow][fromCol];
        board[fromRow][fromCol] = EMPTY;

        // If this was a jump, remove the jumped piece
        if (Math.abs(fromRow - toRow) == 2) {
            int jumpedRow = (fromRow + toRow) / 2;
            int jumpedCol = (fromCol + toCol) / 2;
            board[jumpedRow][jumpedCol] = EMPTY;
        }

        // King promotion
        if (board[toRow][toCol] == RED && toRow == 0) {
            board[toRow][toCol] = RED_KING;
        }
        if (board[toRow][toCol] == BLACK && toRow == 7) {
            board[toRow][toCol] = BLACK_KING;
        }
    }

    /**
     * Return an array containing all the legal CheckersMoves
     * for the specified player on the current board.  If the player
     * has no legal moves, null is returned.  The value of player
     * should be one of the constants RED or BLACK; if not, null
     * is returned.  If the returned value is non-null, it consists
     * entirely of jump moves or entirely of regular moves, since
     * if the player can jump, only jumps are legal moves.
     *
     * @param player color of the player, RED or BLACK
     */
    CheckersMove[] getLegalMoves(int player) {
        ArrayList<CheckersMove> moves = new ArrayList<>();

        // First check for any available jumps
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (pieceAt(row, col) == player || pieceAt(row, col) == player + 1) {
                    CheckersMove[] jumps = getLegalJumpsFrom(player, row, col);
                    if (jumps != null) {
                        moves.addAll(Arrays.asList(jumps));
                    }
                }
            }
        }

        // If no jumps available, look for regular moves
        if (moves.isEmpty()) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    int piece = pieceAt(row, col);
                    if (piece == player || piece == player + 1) {  // Current player's piece
                        // Handle king moves
                        if (piece == RED_KING || piece == BLACK_KING) {
                            // Kings can move in all directions
                            // Check upward moves
                            if (row > 0) {
                                if (col > 0 && pieceAt(row-1, col-1) == EMPTY) {
                                    moves.add(new CheckersMove(row, col, row-1, col-1));
                                }
                                if (col < 7 && pieceAt(row-1, col+1) == EMPTY) {
                                    moves.add(new CheckersMove(row, col, row-1, col+1));
                                }
                            }
                            // Check downward moves
                            if (row < 7) {
                                if (col > 0 && pieceAt(row+1, col-1) == EMPTY) {
                                    moves.add(new CheckersMove(row, col, row+1, col-1));
                                }
                                if (col < 7 && pieceAt(row+1, col+1) == EMPTY) {
                                    moves.add(new CheckersMove(row, col, row+1, col+1));
                                }
                            }
                        }
                        // Handle regular piece moves
                        else {
                            if (piece == RED) {
                                // Red pieces move up
                                if (row > 0) {
                                    if (col > 0 && pieceAt(row-1, col-1) == EMPTY) {
                                        moves.add(new CheckersMove(row, col, row-1, col-1));
                                    }
                                    if (col < 7 && pieceAt(row-1, col+1) == EMPTY) {
                                        moves.add(new CheckersMove(row, col, row-1, col+1));
                                    }
                                }
                            }
                            else if (piece == BLACK) {
                                // Black pieces move down
                                if (row < 7) {
                                    if (col > 0 && pieceAt(row+1, col-1) == EMPTY) {
                                        moves.add(new CheckersMove(row, col, row+1, col-1));
                                    }
                                    if (col < 7 && pieceAt(row+1, col+1) == EMPTY) {
                                        moves.add(new CheckersMove(row, col, row+1, col+1));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (moves.isEmpty()) return null;
        return moves.toArray(new CheckersMove[0]);
    }


    /**
     * Return a list of the legal jumps that the specified player can
     * make starting from the specified row and column.  If no such
     * jumps are possible, null is returned.  The logic is similar
     * to the logic of the getLegalMoves() method.
     *
     * Note that each CheckerMove may contain multiple jumps. 
     * Each move returned in the array represents a sequence of jumps 
     * until no further jump is allowed.
     *
     * @param player The player of the current jump, either RED or BLACK.
     * @param row    row index of the start square.
     * @param col    col index of the start square.
     */
    CheckersMove[] getLegalJumpsFrom(int player, int row, int col) {
        ArrayList<CheckersMove> jumps = new ArrayList<>();
        ArrayList<Integer> rows = new ArrayList<>();
        ArrayList<Integer> cols = new ArrayList<>();

        // Start with current position
        rows.add(row);
        cols.add(col);

        findJumps(player, rows, cols, jumps);

        if (jumps.isEmpty()) return null;
        return jumps.toArray(new CheckersMove[0]);
    }
    private void findJumps(int player, ArrayList<Integer> rows, ArrayList<Integer> cols,
                           ArrayList<CheckersMove> jumps) {
        int currentRow = rows.get(rows.size() - 1);
        int currentCol = cols.get(cols.size() - 1);
        boolean isKing = (pieceAt(currentRow, currentCol) == RED_KING ||
                pieceAt(currentRow, currentCol) == BLACK_KING);

        // Check all possible jump directions
        int[] rowDirs = (player == RED || isKing) ? new int[]{-1} : new int[]{1};
        if (isKing) rowDirs = new int[]{-1, 1};

        for (int rowDir : rowDirs) {
            for (int colDir : new int[]{-1, 1}) {
                int jumpRow = currentRow + 2 * rowDir;
                int jumpCol = currentCol + 2 * colDir;
                int overRow = currentRow + rowDir;
                int overCol = currentCol + colDir;

                if (jumpRow >= 0 && jumpRow < 8 && jumpCol >= 0 && jumpCol < 8 &&
                        pieceAt(jumpRow, jumpCol) == EMPTY) {

                    int overPiece = pieceAt(overRow, overCol);
                    if ((player == RED && (overPiece == BLACK || overPiece == BLACK_KING)) ||
                            (player == BLACK && (overPiece == RED || overPiece == RED_KING))) {

                        // Create new lists for this jump path
                        ArrayList<Integer> newRows = new ArrayList<>(rows);
                        ArrayList<Integer> newCols = new ArrayList<>(cols);
                        newRows.add(jumpRow);
                        newCols.add(jumpCol);

                        // Save current board state
                        int savedJumpSquare = board[jumpRow][jumpCol];
                        int savedOverSquare = board[overRow][overCol];
                        int savedCurrentSquare = board[currentRow][currentCol];

                        // Make the jump temporarily
                        board[jumpRow][jumpCol] = board[currentRow][currentCol];
                        board[overRow][overCol] = EMPTY;
                        board[currentRow][currentCol] = EMPTY;

                        // Look for additional jumps
                        findJumps(player, newRows, newCols, jumps);

                        // Restore board state
                        board[jumpRow][jumpCol] = savedJumpSquare;
                        board[overRow][overCol] = savedOverSquare;
                        board[currentRow][currentCol] = savedCurrentSquare;

                        // If no further jumps were found, add this jump sequence
                        if (newRows.size() > rows.size()) {
                            CheckersMove move = new CheckersMove();
                            for (int i = 0; i < newRows.size(); i++) {
                                move.addMove(newRows.get(i), newCols.get(i));
                            }
                            jumps.add(move);
                        }
                    }
                }
            }
        }
    }
    // helper method to check if a position is valid
    private boolean isValidPosition(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // helper method to check if a piece is an opponent's piece
    private boolean isOpponentPiece(int player, int piece) {
        return (player == RED && (piece == BLACK || piece == BLACK_KING)) ||
                (player == BLACK && (piece == RED || piece == RED_KING));
    }

    //helper method to get piece owner
    private int getPieceOwner(int piece) {
        if (piece == RED || piece == RED_KING) return RED;
        if (piece == BLACK || piece == BLACK_KING) return BLACK;
        return EMPTY;
    }

    // method to check if game is over
    public boolean isGameOver() {
        return getLegalMoves(RED) == null && getLegalMoves(BLACK) == null;
    }

    // method to get winner
    public int getWinner() {
        if (!isGameOver()) return EMPTY;

        int redPieces = 0;
        int blackPieces = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board[row][col] == RED || board[row][col] == RED_KING)
                    redPieces++;
                else if (board[row][col] == BLACK || board[row][col] == BLACK_KING)
                    blackPieces++;
            }
        }

        if (redPieces > blackPieces) return RED;
        if (blackPieces > redPieces) return BLACK;
        return EMPTY; // Draw
    }

    //method to clone the board
    public CheckersData clone() {
        CheckersData copy = new CheckersData();
        for (int row = 0; row < 8; row++) {
            System.arraycopy(board[row], 0, copy.board[row], 0, 8);
        }
        return copy;
    }

    //method to evaluate board position (heuristic)
    public int evaluatePosition() {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                switch (board[row][col]) {
                    case RED:
                        score -= 100;
                        // Bonus for advancement
                        score -= (7 - row) * 10;
                        break;
                    case RED_KING:
                        score -= 200;
                        break;
                    case BLACK:
                        score += 100;
                        // Bonus for advancement
                        score += row * 10;
                        break;
                    case BLACK_KING:
                        score += 200;
                        break;
                }

                // Bonus for center control (columns 3,4 and rows 3,4)
                if (board[row][col] != EMPTY && row >= 2 && row <= 5 && col >= 2 && col <= 5) {
                    if (board[row][col] == BLACK || board[row][col] == BLACK_KING)
                        score += 10;
                    else
                        score -= 10;
                }

                // Bonus for back row pieces (harder to king)
                if (row == 0 && (board[row][col] == BLACK || board[row][col] == BLACK_KING))
                    score += 20;
                if (row == 7 && (board[row][col] == RED || board[row][col] == RED_KING))
                    score -= 20;
            }
        }

        return score;
    }

}
