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
        board = new char[][]{
                {'r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'}, // Black pieces
                {'p', 'p', 'p', 'p', 'p', 'p', 'p', 'p'}, // Black pawns
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, // Empty row
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, // Empty row
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, // Empty row
                {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '}, // Empty row
                {'P', 'P', 'P', 'P', 'P', 'P', 'P', 'P'}, // White pawns
                {'R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'}  // White pieces
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
                buttons[row][col].setPreferredSize(new Dimension(100, 100));
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

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        char piece = board[fromRow][fromCol];
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        if (board[toRow][toCol] != ' ' && Character.isUpperCase(piece) == Character.isUpperCase(board[toRow][toCol])) {
            return false; // Cannot move to a square occupied by the same color piece
        }

        switch (Character.toLowerCase(piece)) {
            case 'p': // Pawn
                return isValidPawnMove(fromRow, fromCol, toRow, toCol);
            case 'r': // Rook
                return isPathClear(fromRow, fromCol, toRow, toCol) && (rowDiff == 0 || colDiff == 0);
            case 'n': // Knight
                return rowDiff * colDiff == 2; // L-shape
            case 'b': // Bishop
                return isPathClear(fromRow, fromCol, toRow, toCol) && rowDiff == colDiff; // Diagonal
            case 'q': // Queen
                return isPathClear(fromRow, fromCol, toRow, toCol)
                        && (rowDiff == colDiff || rowDiff == 0 || colDiff == 0);
            case 'k': // King
                return rowDiff <= 1 && colDiff <= 1;
        }
        return false;
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);

        int currentRow = fromRow + rowStep;
        int currentCol = fromCol + colStep;

        while (currentRow != toRow || currentCol != toCol) {
            if (board[currentRow][currentCol] != ' ') {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }

    private boolean isValidPawnMove(int fromRow, int fromCol, int toRow, int toCol) {
        int direction = Character.isUpperCase(board[fromRow][fromCol]) ? -1 : 1; // White moves up, Black moves down
        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        if (colDiff == 0 && board[toRow][toCol] == ' ') { // Forward move
            return rowDiff == direction || (rowDiff == 2 * direction && (fromRow == 1 || fromRow == 6));
        } else if (colDiff == 1 && board[toRow][toCol] != ' '
                && Character.isUpperCase(board[fromRow][fromCol]) != Character.isUpperCase(board[toRow][toCol])) {
            return rowDiff == direction; // Capture
        }
        return false;
    }

    private void promotePawn(int row, int col) {
        String[] choices = {"Queen", "Rook", "Bishop", "Knight"};
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
                case "Queen" -> board[row][col] = isWhiteTurn ? 'Q' : 'q';
                case "Rook" -> board[row][col] = isWhiteTurn ? 'R' : 'r';
                case "Bishop" -> board[row][col] = isWhiteTurn ? 'B' : 'b';
                case "Knight" -> board[row][col] = isWhiteTurn ? 'N' : 'n';
            }
        }
    }

    private void checkForCheckmate() {
        if (isKingInCheck()) {
            highlightKingInCheck(); // Highlight the king's square
            if (!canAnyMoveSaveKing()) {
                String winner = isWhiteTurn ? "Black" : "White";
                JOptionPane.showMessageDialog(null, winner + " wins by checkmate!");
                System.exit(0); // End the game
            }
        }
    }

    private boolean isKingInCheck() {
        int kingRow = -1, kingCol = -1;
        char king = isWhiteTurn ? 'K' : 'k';

        // Find the king's position
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == king) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        // Check if the king is in check
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (isValidMove(row, col, kingRow, kingCol)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canAnyMoveSaveKing() {
        for (int fromRow = 0; fromRow < SIZE; fromRow++) {
            for (int fromCol = 0; fromCol < SIZE; fromCol++) {
                if (board[fromRow][fromCol] != ' ' && isWhiteTurn == Character.isUpperCase(board[fromRow][fromCol])) {
                    for (int toRow = 0; toRow < SIZE; toRow++) {
                        for (int toCol = 0; toCol < SIZE; toCol++) {
                            char tempFrom = board[fromRow][fromCol];
                            char tempTo = board[toRow][toCol];
                            if (isValidMove(fromRow, fromCol, toRow, toCol)) {
                                board[toRow][toCol] = tempFrom;
                                board[fromRow][fromCol] = ' ';
                                if (!isKingInCheck()) {
                                    board[fromRow][fromCol] = tempFrom;
                                    board[toRow][toCol] = tempTo;
                                    return true;
                                }
                                board[fromRow][fromCol] = tempFrom;
                                board[toRow][toCol] = tempTo;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void highlightKingInCheck() {
        int kingRow = -1, kingCol = -1;
        char king = isWhiteTurn ? 'K' : 'k';

        // Find the king's position
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == king) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        // Highlight the square where the king is in check
        if (kingRow != -1 && kingCol != -1) {
            buttons[kingRow][kingCol].setBackground(Color.RED);
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
