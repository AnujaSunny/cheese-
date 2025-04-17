import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChessGame {
    private static final int SIZE = 8;
    private char[][] board;
    private boolean isWhiteTurn;
    private JButton[][] buttons;
    private int selectedRow = -1;
    private int selectedCol = -1;

    public ChessGame() {
        board = new char[][] {
                { 'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r' }, // Black pieces
                { 'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p' }, // Black pawns
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, // Empty row
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, // Empty row
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, // Empty row
                { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' }, // Empty row
                { 'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P' }, // White pawns
                { 'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R' } // White pieces
        };
        isWhiteTurn = true; // White starts the game
        buttons = new JButton[SIZE][SIZE];
        initializeGUI();
    }

    private void initializeGUI() {
        JFrame frame = new JFrame("Chess Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setLayout(new GridLayout(SIZE, SIZE));

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setPreferredSize(new Dimension(150, 150));
                buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                setPieceIcon(buttons[row][col], board[row][col]);
                frame.add(buttons[row][col]);

                final int currentRow = row;
                final int currentCol = col;
                buttons[row][col].addActionListener(e -> handleButtonClick(currentRow, currentCol));
            }
        }

        frame.setVisible(true);
    }

    private void setPieceIcon(JButton button, char piece) {
        String pieceName = switch (piece) {
            case 'r' -> "rook black";
            case 'n' -> "knight black";
            case 'b' -> "bishop black";
            case 'q' -> "queen black";
            case 'k' -> "king black";
            case 'p' -> "pawn black";
            case 'R' -> "rook white";
            case 'N' -> "knight white";
            case 'B' -> "bishop white";
            case 'Q' -> "queen white";
            case 'K' -> "king white";
            case 'P' -> "pawn white";
            default -> null;
        };

        if (pieceName != null) {
            ImageIcon icon = new ImageIcon("resources/" + pieceName + ".png");
            button.setIcon(icon);
        } else {
            button.setIcon(null);
        }
    }

    private void handleButtonClick(int row, int col) {
        if (isKingInCheck()) {
            // If the king is in check, restrict moves to only the king and blocking pieces

        }

        if (selectedRow == -1 && selectedCol == -1) {
            // Select a piece
            if (board[row][col] != ' ' && isWhiteTurn == Character.isUpperCase(board[row][col])) {
                selectedRow = row;
                selectedCol = col;
                highlightValidMoves(row, col);
                buttons[row][col].setBackground(Color.YELLOW); // Highlight selected square
            }
        } else {
            // Attempt to move the selected piece
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                char movingPiece = board[selectedRow][selectedCol];
                board[row][col] = movingPiece;
                board[selectedRow][selectedCol] = ' ';

                // Handle pawn promotion if a pawn reaches the opponent's last row
                if ((movingPiece == 'P' && row == 0) || (movingPiece == 'p' && row == 7)) {
                    promotePawn(row, col);
                }

                updateBoard();
                isWhiteTurn = !isWhiteTurn; // Switch turns
            }
            resetSelection();
        }

        checkForCheckmate();
    }

    private void promotePawn(int row, int col) {
        String[] choices = { "Queen", "Rook", "Bishop", "Knight" };
        String promotionChoice = (String) JOptionPane.showInputDialog(
                null,
                "Choose a promotion:",
                "Pawn Promotion",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                choices[0]);

        if (promotionChoice != null) {
            switch (promotionChoice) {
                case "Queen" -> board[row][col] = isWhiteTurn ? 'Q' : 'q'; // Promote to Queen
                case "Rook" -> board[row][col] = isWhiteTurn ? 'R' : 'r'; // Promote to Rook
                case "Bishop" -> board[row][col] = isWhiteTurn ? 'B' : 'b'; // Promote to Bishop
                case "Knight" -> board[row][col] = isWhiteTurn ? 'N' : 'n'; // Promote to Knight
            }
        }
        updateBoard(); // Update the board to reflect the promotion
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        int direction = Character.isUpperCase(board[fromRow][fromCol]) ? -1 : 1; // White moves up, Black moves down
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        // Forward move
        if (colDiff == 0) {
            // Move one square forward
            if (rowDiff == direction && board[toRow][toCol] == ' ') {
                return true; // Valid single square move
            }
            // Move two squares forward from the starting position
            if (rowDiff == 2 * direction && fromRow == (isWhiteTurn ? 6 : 1) && board[toRow][toCol] == ' '
                    && board[fromRow + direction][fromCol] == ' ') {
                return true; // Valid double square move
            }
        }
        // Capture move
        if (colDiff == 1 && rowDiff == direction && board[toRow][toCol] != ' '
                && Character.isUpperCase(board[fromRow][fromCol]) != Character.isUpperCase(board[toRow][toCol])) {
            return true; // Valid capture
        }

        return false; // Invalid move
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Check if the destination square is occupied by a piece of the same color
        if (board[toRow][toCol] != ' ' && Character.isUpperCase(piece) == Character.isUpperCase(board[toRow][toCol])) {
            return false; // Cannot move to a square occupied by the same color piece
        }

        switch (Character.toLowerCase(piece)) {
            case 'p': // Pawn
                return isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case 'r': // Rook
                return (rowDiff == 0 || colDiff == 0) && isPathClear(fromRow, fromCol, toRow, toCol);
            case 'n': // Knight
                return rowDiff * colDiff == 2; // L-shape
            case 'b': // Bishop
                return rowDiff == colDiff && isPathClear(fromRow, fromCol, toRow, toCol); // Diagonal
            case 'q': // Queen
                return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0)
                        && isPathClear(fromRow, fromCol, toRow, toCol);
            case 'k': // King
                return rowDiff <= 1 && colDiff <= 1; // One square in any direction
        }
        return false; // Invalid piece
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != ' ') {
                return false; // Path is blocked
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true; // Path is clear
    }

    private void checkForCheckmate() {
        if (isKingInCheck()) {
            // Highlight the king's square in red
            highlightKingInCheck();
    
            boolean hasValidMove = false;
    
            // Check all pieces of the current player
            for (int row = 0; row < board.length; row++) {
                for (int col = 0; col < board[row].length; col++) {
                    if (board[row][col] != ' ' && isWhiteTurn == Character.isUpperCase(board[row][col])) {
                        // Check if the piece has any valid moves
                        for (int targetRow = 0; targetRow < board.length; targetRow++) {
                            for (int targetCol = 0; targetCol < board[targetRow].length; targetCol++) {
                                if (isValidMove(row, col, targetRow, targetCol)) {
                                    // Simulate the move to check its validity
                                    char originalPiece = board[targetRow][targetCol];
                                    board[targetRow][targetCol] = board[row][col];
                                    board[row][col] = ' ';
                                    boolean stillInCheck = isKingInCheck();
                                    // Undo the simulated move
                                    board[row][col] = board[targetRow][targetCol];
                                    board[targetRow][targetCol] = originalPiece;
    
                                    if (!stillInCheck) {
                                        hasValidMove = true;
                                        break;
                                    }
                                }
                            }
                            if (hasValidMove) break;
                        }
                    }
                    if (hasValidMove) break;
                }
                if (hasValidMove) break;
            }
    
            if (!hasValidMove) {
                // Checkmate detected
                String winner = isWhiteTurn ? "Black" : "White";
                JOptionPane.showMessageDialog(
                    null,
                    "Checkmate! " + winner + " wins!",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE
                );
                // Optionally reset the game or end the application
                System.exit(0);
            }
        }
    }
    
    private boolean isKingInCheck() {
        int kingRow = -1, kingCol = -1;
        char king = isWhiteTurn ? 'K' : 'k';

        // Find the position of the king
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == king) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        // Check if the king is in check by any opponent's piece
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                char piece = board[row][col];
                // Check if the piece is an opponent's piece
                if (piece != ' ' && Character.isUpperCase(piece) != isWhiteTurn) {
                    // Check if the opponent's piece can move to the king's position
                    if (isValidMove(row, col, kingRow, kingCol)) {
                        return true; // The king is in check
                    }
                }
            }
        }

        return false; // The king is not in check
    }

    private boolean canAnyMoveSaveKing() {
        for (int fromRow = 0; fromRow < SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < SIZE; fromCol++) {
                // Check if the piece belongs to the current player
                if (board[fromRow][fromCol] != ' ' && isWhiteTurn == Character.isUpperCase(board[fromRow][fromCol])) {
                    for (int toRow = 0; toRow < SIZE; toRow++) {
                        for (int toCol = 0; toCol < SIZE; toCol++) {
                            // Store the current state of the board
                            char tempFrom = board[fromRow][fromCol];
                            char tempTo = board[toRow][toCol];

                            // Check if the move is valid
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                // Make the move
                                board[toRow][toCol] = tempFrom;
                                board[fromRow][fromCol] = ' ';

                                // Check if the king is still in check
                                if (!isKingInCheck()) {
                                    // Undo the move
                                    board[fromRow][fromCol] = tempFrom;
                                    board[toRow][toCol] = tempTo;
                                    return true; // Found a valid move that saves the king
                                }

                                // Undo the move
                                board[fromRow][fromCol] = tempFrom;
                                board[toRow][toCol] = tempTo;
                            }
                        }
                    }
                }
            }
        }
        return false; // No valid moves found that save the king
    }

    private void highlightKingInCheck() {
        int kingRow = -1, kingCol = -1;
        char king = isWhiteTurn ? 'K' : 'k';

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == king) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        if (kingRow != -1 && kingCol != -1) {
            buttons[kingRow][kingCol].setBackground(Color.RED);
        }
    }

    private void resetKingHighlight() {
        int kingRow = -1, kingCol = -1;
        char king = isWhiteTurn ? 'K' : 'k';

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == king) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        if (kingRow != -1 && kingCol != -1) {
            buttons[kingRow][kingCol].setBackground((kingRow + kingCol) % 2 == 0 ? Color.WHITE : Color.GRAY);
        }
    }

    private void resetSelection() {
        if (selectedRow != -1 && selectedCol != -1) {
            buttons[selectedRow][selectedCol]
                    .setBackground((selectedRow + selectedCol) % 2 == 0 ? Color.WHITE : Color.GRAY);
        }
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (buttons[row][col].getBackground() == Color.GREEN ||
                        buttons[row][col].getBackground() == Color.RED) {
                    buttons[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                }
            }
        }
        selectedRow = -1;
        selectedCol = -1;
    }

    private void highlightValidMoves(int fromRow, int fromCol) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (isValidMove(fromRow, fromCol, row, col)) {
                    buttons[row][col].setBackground(Color.GREEN); // Highlight valid moves
                }
            }
        }
    }

    private void updateBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                setPieceIcon(buttons[row][col], board[row][col]);
            }
        }
    }

    public static void main(String[] args) {
        new ChessGame();
    }
}