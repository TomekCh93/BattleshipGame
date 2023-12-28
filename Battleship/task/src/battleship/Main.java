package battleship;

import java.io.IOException;
import java.util.*;

public class Main {
    static String[][] table = new String[11][11];
    static List<Ship> ships = new ArrayList<>();
    static String[][] tablePlayer2 = new String[11][11];
    static List<Ship> shipsPlayer2 = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        prepareGame(table, ships, 1);
        prepareGame(tablePlayer2, shipsPlayer2, 2);
        boolean player1turn = true;
        play(player1turn);

    }

    private static void play(boolean player1turn) throws IOException {
        while (!ships.isEmpty() || !shipsPlayer2.isEmpty()) {
            playerMove(player1turn);
            player1turn = !player1turn;
        }
    }

    private static void playerMove(boolean player1turn) throws IOException {
        Scanner scanner = new Scanner(System.in);
        printDoubleTable(player1turn);
        logInfo(player1turn);
        while (scanner.hasNext()) {
            if (takeAShot(scanner.next(), player1turn) || !ships.isEmpty()) {
                System.out.println("Press Enter and pass the move to another player");
                System.in.read();
                System.out.println("...");
                break;
            }

        }
    }

    private static void logInfo(boolean player1turn) {
        String playerName = player1turn ? "Player 1" : "Player 2";
        System.out.println(playerName + ", it's your turn:");
    }


    private static void printDoubleTable(boolean player1turn) {
        String[][] playerTable = player1turn ? table : tablePlayer2;
        String[][] enemyTable = player1turn ? tablePlayer2 : table;
        printTable(enemyTable, true);
        System.out.println("---------------------");
        printTable(playerTable, false);

    }

    private static void printTable(String[][] playerTable, boolean gameInProgress) {
        for (int i = 0; i < playerTable.length; i++) {
            for (int j = 0; j < playerTable[i].length; j++) {
                if (!gameInProgress) {
                    System.out.print(playerTable[i][j] + " ");
                } else {
                    if (playerTable[i][j] == "O") {
                        System.out.print("~" + " ");
                    } else {
                        System.out.print(playerTable[i][j] + " ");
                    }

                }

            }
            System.out.println();
        }
    }

    private static void prepareGame(String[][] playerTable, List<Ship> playerShips, int i)
            throws IOException {
        System.out.println("Player " + i + ", place your ships on the game field");
        System.out.println();
        fillTable(playerTable);
        printTable(playerTable, false);
        try (Scanner scanner = new Scanner(System.in)) {
            for (Ships ship : Ships.values()) {
                System.out.println(
                        "Enter the coordinates of the " + ship.getDisplayName() + "(" + ship.getLength() + " cells):");
                if (scanner.hasNext()) {
                    String inputx = scanner.next();
                    String inputy = scanner.next();
                    Coordinates coords = getCoords(inputx + "  " + inputy);
                    while (!isSuccessfulLaunch(playerTable, coords, ship) && scanner.hasNext()) {
                        inputx = scanner.next();
                        inputy = scanner.next();
                        coords = getCoords(inputx + "  " + inputy);
                    }
                    playerShips.add(new Ship(ship, coords));
                    printTable(playerTable, false);
                }
            }
        }
        System.out.println("Press Enter and pass the move to another player");
        System.in.read();
        System.out.println("...");
    }

    private static boolean takeAShot(String next, boolean player1turn) {
        if (!isValidCoordinates(next)) {
            System.out.println("Error! You entered the wrong coordinates! Try again:");
            return false;
        }

        Coordinates coords = getCoords(next);
        List<Ship> currPlayerShips = player1turn ? shipsPlayer2 : ships;
        String[][] currPlayerTable = player1turn ? tablePlayer2 : table;

        processShotResult(coords, currPlayerShips, currPlayerTable);

        return true;
    }

    private static void processShotResult(Coordinates coords, List<Ship> currPlayerShips, String[][] currPlayerTable) {
        String cellValue = currPlayerTable[coords.getxRow()][coords.getxCol()];

        switch (cellValue) {
            case "X" -> System.out.println("You hit a ship!");
            case "M" -> System.out.println("You missed!");
            case "O" -> handleSunkShip(currPlayerShips, currPlayerTable, coords);
            default -> {
                System.out.println("You missed!");
                currPlayerTable[coords.getxRow()][coords.getxCol()] = "M";
            }
        }
    }

    private static void handleSunkShip(List<Ship> currPlayerShips, String[][] currPlayerTable, Coordinates coords) {
        for (Iterator<Ship> iterator = currPlayerShips.iterator(); iterator.hasNext(); ) {
            Ship ship = iterator.next();
            ship.checkCoords(coords);

            handleShipStatus(ship, iterator, currPlayerShips, currPlayerTable);
        }

        currPlayerTable[coords.getxRow()][coords.getxCol()] = "X";
    }

    private static void handleShipStatus(Ship ship, Iterator<Ship> iterator, List<Ship> currPlayerShips, String[][] currPlayerTable) {
        if (ship.getHealth() == 0) {
            iterator.remove();
            handleSankShip(currPlayerShips);
        } else {
            handleHitShip();
        }
    }

    private static void handleSankShip(List<Ship> currPlayerShips) {
        if (currPlayerShips.isEmpty()) {
            System.out.println("You sank the last ship. You won. Congratulations!");
        } else {
            System.out.println("You sank a ship! Specify a new target:");
        }
    }

    private static void handleHitShip() {
        System.out.println("You hit a ship!");
    }

    private static boolean isValidCoordinates(String input) {
        return checkCooords(input);
    }

    private static boolean checkCooords(String next) {
        if (next.charAt(0) > 'J') {
            return false;
        }
        if (next.length() > 2) {
            return next.charAt(2) == '0';
        }

        return true;
    }

    private static void fillTable(String[][] table) {
        for (String[] row : table) {
            Arrays.fill(row, "~");
        }
        table[0][0] = " ";
        int aNumericAscii = 65;
        int oneNumericAscii = 49;
        for (int i = 0; i < table.length; i++) {
            for (int j = 0; j < table[i].length; j++) {
                if (j == 0 && i != 0) {
                    table[i][j] = String.valueOf((char) aNumericAscii);
                    aNumericAscii++;

                }
                if (i == 0 && j != 0) {
                    if (oneNumericAscii < 58) {
                        table[i][j] = String.valueOf((char) oneNumericAscii);
                        oneNumericAscii++;
                    } else {
                        table[i][j] = "10";
                    }

                }
            }
        }

    }

    private static boolean isSuccessfulLaunch(String[][] playerTable, Coordinates coords, Ships ship) {
        if (coords.getxRow() == 0 && coords.getyRow() == 0) {
            System.out.println("\nError! Wrong ship location! Try again:\n");
            return false;
        }

        if (!checkLength(ship.getLength(), coords)) {
            System.out.println("\nError! Wrong length of the " + ship.getDisplayName() + "! Try again:\n");
            return false;
        }

        if (!checkIfNotTooClose(playerTable, coords)) {
            System.out.println("\nError! You placed it too close to another one. Try again:\n");
            return false;
        }

        return !checkBusy(playerTable, coords);
    }

    private static boolean checkBusy(String[][] playerTable, Coordinates coords) {
        if (coords.isHorizontal()) {
            for (int i = coords.getxCol(); i <= coords.getyCol(); i++) {
                if (playerTable[coords.getxRow()][i].equals("0"))
                    return true;
                else {
                    playerTable[coords.getxRow()][i] = "O";

                }
            }
        } else {
            for (int i = coords.getxRow(); i <= coords.getyRow(); i++) {
                if (playerTable[i][coords.getxCol()] == "O")
                    return true;
                else {
                    playerTable[i][coords.getxCol()] = "O";

                }
            }
        }
        return false;
    }

    private static boolean checkIfNotTooClose(String[][] playerTable, Coordinates coords) {
        return coords.isHorizontal() ||
                coords.getxCol() <= 1 ||
                coords.getyCol() <= 1 ||
                coords.getxRow() >= 10 ||
                coords.getyRow() >= 10 ||
                !"O".equals(playerTable[coords.getyRow() + 1][coords.getyCol()]);
    }

    private static boolean checkLength(int length, Coordinates coords) {
        int currLen = coords.isHorizontal() ? (coords.getyCol() - coords.getxCol() + 1) : (coords.getyRow() - coords.getxRow() + 1);
        return currLen == length;
    }

    private static Coordinates getCoords(String next) {
        try {
            return new Coordinates(next.split(" "));
        } catch (Exception e) {
            return null;
        }
    }
}

enum Ships {

    AIRCRAFT_CARRIER(5, "Aircraft Carrier"),
    BATTLESHIP(4, "Battleship"),
    SUBMARINE(3, "Submarine"),
    CRUISER(3, "Cruiser"),
    DESTROYER(2, "Destroyer");;
    private final int length;
    private final String displayName;

    Ships(int length, String displayName) {
        this.length = length;
        this.displayName = displayName;
    }

    public int getLength() {
        return length;
    }

    public String getDisplayName() {
        return displayName;
    }
}

class Coordinates {
    private boolean isHorizontal;
    private int xRow;
    private int xCol;
    private int yRow;
    private int yCol;

    public Coordinates() {

    }

    public Coordinates(String[] coords) {
        if (coords.length == 1) {
            processxCoord(coords[0]);
            return;
        }
        processxCoord(coords[0]);
        processyCoord(coords[2]);
        if (xRow == yRow) {
            this.isHorizontal = true;
            if (this.xCol > this.yCol) {
                int tmp = xCol;
                xCol = yCol;
                yCol = tmp;
            }
        } else if (xCol == yCol) {
            if (this.xRow > this.yRow) {
                int tmp = xRow;
                xRow = yRow;
                yRow = tmp;
            }
        } else {
            xRow = 0;
            yRow = 0;
        }
    }

    private void processxCoord(String coord) {

        Character rowChar = coord.charAt(0);
        this.xRow = convertCharToRowNum(rowChar);
        if (coord.length() > 2) {
            this.xCol = 10;
            return;
        }
        Character colChar = coord.charAt(1);
        this.xCol = convertCharToColNum(colChar);
    }

    private void processyCoord(String coord) {
        Character rowChar = coord.charAt(0);
        this.yRow = convertCharToRowNum(rowChar);
        if (coord.length() > 2) {
            this.yCol = 10;
            return;
        }
        Character colChar = coord.charAt(1);
        this.yCol = convertCharToColNum(colChar);
    }

    private int convertCharToRowNum(Character c) {
        return c - 'A' + 1;

    }

    private int convertCharToColNum(Character c) {
        return c - '1' + 1;

    }

    public boolean isHorizontal() {
        return isHorizontal;
    }

    public int getxRow() {
        return xRow;
    }

    public int getxCol() {
        return xCol;
    }

    public int getyRow() {
        return yRow;
    }

    public int getyCol() {
        return yCol;
    }

    public void setxRow(int xRow) {
        this.xRow = xRow;
    }

    public void setxCol(int xCol) {
        this.xCol = xCol;
    }

    public void setyRow(int yRow) {
        this.yRow = yRow;
    }

    public void setyCol(int yCol) {
        this.yCol = yCol;
    }

}

class Ship {
    private Ships type;
    private List<Coordinates> shipCoords;
    private int health;

    Ship(Ships type, Coordinates coords) {
        this.type = type;
        this.shipCoords = calculateAllShipCoords(coords);
        this.health = this.shipCoords.size();
    }

    public void checkCoords(Coordinates coords) {
        if (newHit(coords)) {
            this.health--;
        }
    }

    private boolean newHit(Coordinates coords) {
        for (int i = 0; i < shipCoords.size(); i++) {
            if (coords.getxRow() == shipCoords.get(i)
                    .getxRow() && coords.getxCol() == shipCoords.get(i)
                    .getxCol()) {
                shipCoords.remove(i);
                return true;

            }

        }
        return false;
    }

    private List<Coordinates> calculateAllShipCoords(Coordinates coords) {
        List<Coordinates> result = new ArrayList<>();
        if (coords.isHorizontal()) {
            int restRow = coords.getxRow();
            int startCol = coords.getxCol() - 1;
            int endCol = coords.getyCol() - 1;
            while (startCol <= endCol) {
                Coordinates newCoords = new Coordinates();
                newCoords.setxRow(restRow);
                newCoords.setxCol(++startCol);
                result.add(newCoords);
            }

        } else {
            int restCol = coords.getxCol();
            int startRow = coords.getxRow() - 1;
            int endRow = coords.getyRow() - 1;
            while (startRow <= endRow) {
                Coordinates newCoords = new Coordinates();
                newCoords.setxCol(restCol);
                newCoords.setxRow(++startRow);
                result.add(newCoords);
            }
        }
        return result;
    }

    public int getHealth() {
        return health;
    }
}

