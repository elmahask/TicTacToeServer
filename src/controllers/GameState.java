package controllers;

public class GameState {

    private int counter;
    private char[][] boardCell;

    public GameState() {
        counter = 0;
        boardCell = new char[3][3];
    }

    boolean isDraw() {
        return (counter  % 9 == 0 && !(isWin('x') || isWin('o')));
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public char[][] getBoard() {
        return boardCell;
    }

    public void setCellBoard(char[][] board) {
        this.boardCell = board;
    }

    public void setCellInBoard(int row, int column, char symbol) {
       
        this.boardCell[row][column] = symbol;
        counter++;

//        System.out.println("row :" +  row  + "column : " + column + "Symbol: " + symbol );
    }

    // symbol is X or O
    boolean isWin(char symbol) {
     

        for (int i = 0; i < 3; i++) {
            if (boardCell[i][0] == symbol
                    && boardCell[i][1] == symbol
                    && boardCell[i][2] == symbol) {
                return true;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (boardCell[0][j] == symbol
                    && boardCell[1][j] == symbol
                    && boardCell[2][j] == symbol) {
                return true;
            }
        }

        if (boardCell[0][0] == symbol
                && boardCell[1][1] == symbol
                && boardCell[2][2] == symbol) {
            return true;
        }

        if (boardCell[0][2] == symbol
                && boardCell[1][1] == symbol
                && boardCell[2][0] == symbol) {
            return true;
        }

        return false;
    }

}
