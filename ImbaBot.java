package sc.player2016.logic;

import java.util.List;

import sc.player2016.Starter;
import sc.plugin2016.Board;
import sc.plugin2016.Connection;
import sc.plugin2016.Field;
import sc.plugin2016.FieldType;
import sc.plugin2016.GameState;
import sc.plugin2016.IGameHandler;
import sc.plugin2016.Move;
import sc.plugin2016.Player;
import sc.plugin2016.PlayerColor;
import sc.shared.GameResult;

public class ImbaBot implements IGameHandler {
  private static final int BOARDSIZE = 24;
  private static final int MINVAL = -9;
  private static final int DIR_NONE = -1;
  private static final int DIR_ALL = 0;
  private static final int DIR_TOP = 1;
  private static final int DIR_BOTTOM = 2;
  private static final int DIR_LEFT = 3;
  private static final int DIR_RIGHT = 4;
  private Starter client;
  private GameState gameState;
  private Player currentPlayer;
  private int[][] boardRatings;
  private int[][] testBoardInt;
  private String[][] testBoardStr;
  private int centrumX1, centrumX2, centrumY1, centrumY2;

  public ImbaBot(Starter client) {
    this.client = client;
  }

  @Override
  public void gameEnded(GameResult data, PlayerColor color,
      String errorMessage) {
    System.out.println("*** Das Spiel ist beendet");
  }

  @Override
  public void onRequestAction() {
    // initialize
    defaultSettings();
    // Setze Rating des Boardes
    setRating();
    // Waehle besten Zug aus
    Move bestMove = Lib.getbestMove(boardRatings);
    // Speichert mal unser Board in ne Txt
    if (true) {
      String additional = "--BOARD RATING--";
      Lib.arrayToTxt(boardRatings, "board", additional);
      Lib.arrayToTxt(testBoardInt, "tBoardInt", "");
      Lib.arrayStrToTxt(testBoardStr, "tBoardStr");
    }
    // Gib Zug zurück
    sendAction(bestMove);
  }

  /*
   * Findet u.a. das Zentrum und setzt das Rating auf 0 Zentrum: Das Zentrum
   * ist die groesstmoegliche Flaeche auf dem Board OHNE Sumpf centrumX1/Y1
   * ist der obere linke Punkt und centrumX2/Y2 der untere rechte Punkt
   */
  private void defaultSettings() {
    Board currentBoard = gameState.getBoard();
    Field currentField;
    FieldType currentFieldType;
    PlayerColor currentPlayerColor;
    int lengthCols = 0, lengthRows = 0;
    int lengthColBest = 0, lengthRowBest = 0;
    boolean cleanRow = true, cleanCol = true;
    boardRatings = new int[BOARDSIZE][BOARDSIZE];
    testBoardInt = new int[BOARDSIZE][BOARDSIZE];
    testBoardStr = new String[BOARDSIZE][BOARDSIZE];
    for (int i = 0; i < BOARDSIZE; i++) {
      for (int j = 0; j < BOARDSIZE; j++) {
        boardRatings[i][j] = 0;
        testBoardInt[i][j] = 0;
        testBoardStr[i][j] = "";
        currentField = currentBoard.getField(i, j);
        currentFieldType = currentField.getType();
        currentPlayerColor = currentPlayer.getPlayerColor();
        if ( // Das Feld liegt am linken / rechten Rand
        (currentFieldType == FieldType.BLUE && currentPlayerColor == PlayerColor.RED)
            ||
            // Das Feld liegt am oberen / unteren Rand
            (currentFieldType == FieldType.RED && currentPlayerColor == PlayerColor.BLUE)
            ||
            // Das Feld ist ein Sumpffeld
            (currentFieldType == FieldType.SWAMP) ||
            // Das Feld gehört einem selbst
            (currentField.getOwner() == currentPlayerColor) ||
            // Feindbesetzung!
            (currentField.getOwner() == currentPlayerColor
                .opponent())) {
          boardRatings[i][j] = MINVAL;
        }
        // Ueberpruefen ob eine Zeile sumpffrei ist
        if (currentBoard.getField(j, i).getType() == FieldType.SWAMP) {
          cleanRow = false;
        }
        // Ueberpruefen ob eine Spalte sumpffrei ist
        if (currentBoard.getField(i, j).getType() == FieldType.SWAMP) {
          cleanCol = false;
        }
      }
      if (cleanCol) {
        lengthCols++;
      } else {
        cleanCol = true;
        if (lengthCols > lengthColBest) {
          lengthColBest = lengthCols;
          centrumX1 = i - lengthCols;
          centrumX2 = i - 1;
        }
        lengthCols = 0;
      }
      if (cleanRow) {
        lengthRows++;
      } else {
        cleanRow = true;
        if (lengthRows > lengthRowBest) {
          lengthRowBest = lengthRows;
          centrumY1 = i - lengthRows;
          centrumY2 = i - 1;
        }
        lengthRows = 0;
      }
    }
    rateCentrum();
  }

  /*
   * Berechnet das Zentrum, bewertet es und die umliegenden Felder... Das
   * Zentrum ist die Fläche mit den breitesten sumpffreien Spalten und Zeilen
   */
  public void rateCentrum() {
    // Mitte des Zentrums
    Lib.Punkt centrum = Lib.getCentrum(centrumX1, centrumX2, centrumY1,
        centrumY2);
    // Ganze Spalte, Zeile raten:
    int round = gameState.getRound();
    // Setze Mitte
    if (round == 0) {
      setRate(centrum.x, centrum.y, 1, "M");
    }
    int p1;
    int p2;
    if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
      for (int i = 0; i < BOARDSIZE; i++) {
        p1 = centrumY1 + round;
        p2 = centrumY2 - round;
        for (int j = p1; j <= p2; j++) {
          int rate = j - p1;
          if (rate > (p2 - p1) / 2) {
            rate = p2 - j;
          }
          setRate(i, j, rate, "c");
        }
      }
    }
    if (currentPlayer.getPlayerColor() == PlayerColor.RED) {
      p1 = centrumX1 + round;
      p2 = centrumX2 - round;
      for (int i = p1; i <= p2; i++) {
        for (int j = 0; j < BOARDSIZE; j++) {
          int rate = i - p1;
          if (rate > (p2 - p1) / 2) {
            rate = p2 - i;
          }
          setRate(i, j, rate, "C");
        }
      }
    }
  }

  // Setze Rating des Boardes
  public void setRating() {
    Boolean lineRated;
    Field currentField;
    Board currentBoard = gameState.getBoard();
    PlayerColor ownColor = currentPlayer.getPlayerColor();
    for (int i = 0; i < BOARDSIZE; i++) {
      lineRated = false;
      for (int j = 0; j < BOARDSIZE; j++) {
        currentField = currentBoard.getField(i, j);
        // Das Feld liegt am linken / rechten Rand:
        if (currentField.getType() == FieldType.BLUE) {
          if (ownColor == PlayerColor.BLUE) {
            setRate(i, j, -1, "b");
          } else {
            setRate(i - 1, j, -1, "r");
            setRate(i + 1, j, -1, "r");
          }
        }
        // Das Feld liegt am oberen / unteren Rand:
        if (currentField.getType() == FieldType.RED) {
          if (ownColor == PlayerColor.RED) {
            setRate(i, j, -1, "R");
          } else {
            setRate(i, j - 1, -1, "B");
            setRate(i, j + 1, -1, "B");
          }
        }
        // Das Feld ist ein Sumpffeld:
        if (currentField.getType() == FieldType.SWAMP) {
          rateSurround(i, j, 1, -1);
        }
        // Das Feld gehört einem selbst
        if (currentField.getOwner() == ownColor) {
          rateOwn(i, j);
        }
        // Feindbesetzung!
        if (currentField.getOwner() == ownColor.opponent()) {
          rateOpponent(i, j);
        }
        // Gucken ob ne Spalte oder Zeile einen Gegner hat, wenn ja
        // Zeile/Spalte raten
        if (!lineRated) {
          if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
            if (currentBoard.getField(i, j).getOwner() == PlayerColor.RED) {
              rateLine(i, 0, 1, DIR_BOTTOM);
              lineRated = true;
            }
          } else {
            if (currentBoard.getField(j, i).getOwner() == PlayerColor.RED) {
              rateLine(0, j, 1, DIR_RIGHT);
              lineRated = true;
            }
          }
        }
      }
    }
  }

  // Um eigene Steine raten
  private void rateOwn(int left, int top) {
    // Direkte umliegende Felder schlecht bewerten
    rateSurround(left, top, 1, -2);
    // Finde Ausrichtung raus und setze Rate
    int rateVertical = 4;
    int rateHorizontal = 6;
    int rateVerticalExt = 1;
    int rateHorizontalExt = 3;
    if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
      rateVertical = 6;
      rateHorizontal = 4;
      rateVerticalExt = 3;
      rateHorizontalExt = 1;
    }
    // Anzahl der Verbindungen in dem Punkt
    int countConnections = gameState.getBoard().getConnections(left, top)
        .size();
    // Nur einzelne oder Connectionsendpunkte
    if (countConnections <= 1) {
      int direction = getDirection(left, top,
          currentPlayer.getPlayerColor());
      // Punkt schlechter raten wenn Gegner im weg ist
      if (countConnections == 1) {
        int oppInLine = blockedOpponents(left, top, direction);
        rateVertical -= oppInLine;
        rateHorizontal -= oppInLine;
      }
      // Gegner blocken is toll
      if (nearOpponents(left, top, 1)) {
        rateVertical += 2;
        rateHorizontal += 2;
      }
      if (nearOpponents(left, top, 2)) {
        rateVertical += 1;
        rateHorizontal += 1;
      }
      setKnight(left, top, rateVertical, rateHorizontal, direction);
      setKnightExt(left, top, rateVerticalExt, rateHorizontalExt);
      // Wenn der Gegner einen Blocker hat
      setBlockBlock(left, top, currentPlayer.getPlayerColor());
    } else { // Punkt in mitten einer Connection
      // Kein Pferd in mitten einer Connection erlauben
      noKnight(left, top);
      if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
        rateLine(left, 0, MINVAL, DIR_BOTTOM);
        rateLine(left - 1, 0, -2, DIR_BOTTOM);
        rateLine(left + 1, 0, -2, DIR_BOTTOM);
      } else {
        rateLine(0, top, MINVAL, DIR_RIGHT);
        rateLine(0, top - 1, -2, DIR_RIGHT);
        rateLine(0, top + 1, -2, DIR_RIGHT);
      }
    }
    // Blocke Steine die direkt neben Dir liegen
    setBlocker(left, top);
  }

  // Um Gegner Steine raten
  private void rateOpponent(int left, int top) {
    int rateDef = 3;
    Move lastMove = gameState.getLastMove();
    int lastConn = gameState.getBoard().getConnections(lastMove.getX(), lastMove.getY()).size();
    if ((left == lastMove.getX()) && (top == lastMove.getY())) {
    	if (lastConn == 0) {
    		rateDef += 6;
    	}
    	if (lastConn == 1) {
    		rateDef += 3;
    	}      
    }
    // Anzahl der Verbindungen in dem Punkt
    List<Connection> connections = gameState.getBoard().getConnections(
        left, top);
    int countConnections = connections.size();
    switch (countConnections) {
    case 0: // Punkt hat KEINE Verbindung
      if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
        if (top > BOARDSIZE / 2) { // Stein befindet sich in unterer
                      // Haelfte
          if (existsHades(left, top, currentPlayer.getPlayerColor())) {
            setRate(left + 1, top, rateDef, "H");
            setRate(left - 1, top, rateDef, "H");
          } else {
            setRate(left, top - 2, rateDef, "h"); // Hades
                                // Verteidigung
          }
          setRate(left, top + 3, rateDef, "p"); // Pan Verteidigung
        } else { // Stein befindet sich in oberer Haelfte
          setRate(left, top - 3, rateDef, "p"); // Pan Verteidigung
          if (existsHades(left, top, currentPlayer.getPlayerColor())) {
            setRate(left + 1, top, rateDef, "H");
            setRate(left - 1, top, rateDef, "H");
          } else {
            setRate(left, top + 2, rateDef, "h"); // Hades
                                // Verteidigung
          }
        }
        // Spieler Rot
      } else {
        if (left > BOARDSIZE / 2) { // Stein befindet sich in rechter
          // Haelfte
          if (existsHades(left, top, currentPlayer.getPlayerColor())) {
            setRate(left, top + 1, rateDef, "H");
            setRate(left, top - 1, rateDef, "H");
          } else {
            setRate(left - 2, top, rateDef, "h"); // Hades
                                // Verteidigung
          }
          setRate(left + 3, top, rateDef, "p"); // Pan Verteidigung
        } else { // Stein befindet sich in linker Haelfte
          setRate(left - 3, top, rateDef, "p"); // Pan Verteidigung
          if (existsHades(left, top, currentPlayer.getPlayerColor())) {
            setRate(left, top + 1, rateDef, "H");
            setRate(left, top - 1, rateDef, "H");
          } else {
            setRate(left + 2, top, rateDef, "h"); // Hades
                                // Verteidigung
          }
        }
      }
      break;
    case 1: // Punkt ist ENDPUNKT einer Verbindung / Connection
      if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
        int y2 = top;
        if (y2 == connections.get(0).y1) {
          y2 = connections.get(0).y2;
        }
        if (top < y2) {

          if (top > BOARDSIZE / 2) { // Stein befindet sich in unterer
            // Haelfte
            setRate(left, top - 2, rateDef, "h"); // Hades
                                // Verteidigung
          } else { // Stein befindet sich in oberer Haelfte
            setRate(left, top - 3, rateDef, "p"); // Pan
                                // Verteidigung
          }
        } else {
          if (left > BOARDSIZE / 2) { // Stein befindet sich in
            // rechter Haelfte
            setRate(left + 3, top, rateDef, "p"); // Pan
                                // Verteidigung
          } else { // Stein befindet sich in linker Haelfte
            setRate(left + 2, top, rateDef, "h"); // Hades
                                // Verteidigung
          }
        }
      } else {
        int x2 = left;
        if (x2 == connections.get(0).x1) {
          x2 = connections.get(0).x2;
        }
        if (left < x2) {
          if (left > BOARDSIZE / 2) { // Stein befindet sich in
            // rechter Haelfte
            setRate(left - 2, top, rateDef, "H"); // Hades
                                // Verteidigung
          } else { // Stein befindet sich in linker Haelfte
            setRate(left - 3, top, rateDef, "P"); // Pan
                                // Verteidigung
          }
        } else {
          if (left > BOARDSIZE / 2) { // Stein befindet sich in
            // rechter Haelfte
            setRate(left + 3, top, rateDef, "P"); // Pan
                                // Verteidigung
          } else { // Stein befindet sich in linker Haelfte
            setRate(left + 2, top, rateDef, "H"); // Hades
                                // Verteidigung
          }
        }
      }
      break;
    default: // Wenn Gegner mitten in einer Connection ist...
      if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
        rateLine(0, top, -3, DIR_RIGHT);
        rateLine(0, top - 1, -1, DIR_RIGHT);
        rateLine(0, top + 1, -1, DIR_RIGHT);
      } else {
        rateLine(left, 0, -3, DIR_BOTTOM);
        rateLine(left - 1, 0, -1, DIR_BOTTOM);
        rateLine(left + 1, 0, -1, DIR_BOTTOM);
      }
    }
  }

  private boolean nearOpponents(int left, int top, int radius) {
    for (int i = left - radius; i <= left + radius; i++) {
      for (int j = top - radius; j <= top + radius; j++) {
        if (Lib.inRange(i, j)) {
          if (gameState.getBoard().getField(left, top).getOwner() == currentPlayer
              .getPlayerColor().opponent()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void rateLine(int left, int top, int rate, int direction) {
    // An der Posi 2x raten, damit man nicht am Rand setzt...
    setRate(left, top, rate, "L");
    while (Lib.inRange(left, top)) {
      setRate(left, top, rate, "l");
      switch (direction) {
      case DIR_LEFT:
        left--;
        break;
      case DIR_RIGHT:
        left++;
        break;
      case DIR_TOP:
        top--;
        break;
      case DIR_BOTTOM:
        top++;
        break;
      }
    }

  }

  // Bewerte die direkten Zugmöglichkeiten (in Schachspringermanier)
  private void noKnight(int left, int top) {
    setKnight(left, top, 0, 0, DIR_ALL);
  }

  private void setKnight(int left, int top, int vertical, int horizontal,
      int direction) {
    if (direction == DIR_TOP || direction == DIR_LEFT
        || direction == DIR_ALL) {
      rateKnight(left - 1, top - 2, left, top, horizontal);
      rateKnight(left - 2, top - 1, left, top, vertical);
    }
    if (direction == DIR_TOP || direction == DIR_RIGHT
        || direction == DIR_ALL) {
      rateKnight(left + 1, top - 2, left, top, horizontal);
      rateKnight(left + 2, top - 1, left, top, vertical);
    }
    if (direction == DIR_BOTTOM || direction == DIR_RIGHT
        || direction == DIR_ALL) {
      rateKnight(left + 1, top + 2, left, top, horizontal);
      rateKnight(left + 2, top + 1, left, top, vertical);
    }
    if (direction == DIR_BOTTOM || direction == DIR_LEFT
        || direction == DIR_ALL) {
      rateKnight(left - 1, top + 2, left, top, horizontal);
      rateKnight(left - 2, top + 1, left, top, vertical);
    }
  }

  private void rateKnight(int left, int top, int fromX, int fromY, int rate) {
    if (Lib.inRange(left, top)) {
      Boolean farAway = false;
      GameState gameStateNew;
      try {
        gameStateNew = (GameState) gameState.clone();
        gameStateNew.getBoard().put(left, top, currentPlayer);
      } catch (CloneNotSupportedException e) {
        gameStateNew = gameState;
      }
      int oldConnections = gameState.getBoard()
          .getConnections(fromX, fromY).size();
      int newConnections = gameStateNew.getBoard()
          .getConnections(fromX, fromY).size();
      // Pferdchensprung ist auch neue Connection!
      if (newConnections > oldConnections) {
        int newPoints = gameStateNew.getPointsForPlayer(currentPlayer
            .getPlayerColor());
        int oldPoints = gameState.getPointsForPlayer(currentPlayer
            .getPlayerColor());
        if (newPoints <= oldPoints) {
          rate -= 1;
        } else {
          rate += Math.max(0, oldPoints - 3);
        }
        if (nearOpponents(left, top, 1)) {
          rate -= 3;
        }
        if (nearOpponents(left, top, 2)) {
          rate -= 2;
        }
        for (int i = left - 1; i <= left + 1; i++) {
          for (int j = top - 1; j <= top + 1; j++) {
            if (Lib.inRange(i, j)) {
              Field field = gameStateNew.getBoard()
                  .getField(i, j);
              if (field.getOwner() == currentPlayer
                  .getPlayerColor().opponent()) {
                farAway = true;
              }
            }
          }
        }
        if (farAway) {
          setRate(left, top, rate, "K");
        } else {
          setRate(left, top, rate - 1, "k");
        }
      } else {
        setRate(left, top, MINVAL, "N");
      }
    }
  }

  private void setKnightExt(int left, int top, int vertical, int horizontal) {
    int neutral = (vertical + horizontal) / 2;
    rateKnightExt(left - 3, top - 3, neutral);
    rateKnightExt(left - 2, top - 4, horizontal);
    rateKnightExt(left, top - 4, horizontal);
    rateKnightExt(left + 2, top - 4, horizontal);
    rateKnightExt(left + 3, top - 3, neutral);
    rateKnightExt(left + 4, top - 2, vertical);
    rateKnightExt(left + 4, top, vertical);
    rateKnightExt(left + 4, top + 2, vertical);
    rateKnightExt(left + 3, top + 3, neutral);
    rateKnightExt(left + 2, top + 4, horizontal);
    rateKnightExt(left, top + 4, horizontal);
    rateKnightExt(left - 2, top + 4, horizontal);
    rateKnightExt(left - 3, top + 3, neutral);
    rateKnightExt(left - 4, top + 2, vertical);
    rateKnightExt(left - 4, top, vertical);
    rateKnightExt(left - 4, top - 2, vertical);
  }

  private void rateKnightExt(int left, int top, int rate) {
    if (Lib.inRange(left, top)) {
      if (!nearOpponents(left, top, 1)) {
        setRate(left, top, rate, "E");
      }
    }
  }

  // Reagiere auf Blocker
  private void setBlockBlock(int left, int top, PlayerColor color) {
    int rate = 4;
    // Antwort auf Hades
    if (color == PlayerColor.BLUE) {
      if (checkOwner(left - 2, top, color.opponent())) {
        setRate(left - 4, top, rate, "q");
      }
      if (checkOwner(left + 2, top, color.opponent())) {
        setRate(left + 4, top, rate, "q");
      }
    } else {
      if (checkOwner(left, top - 2, color.opponent())) {
        setRate(left, top - 4, rate, "Q");
      }
      if (checkOwner(left, top + 2, color.opponent())) {
        setRate(left, top + 4, rate, "Q");
      }
    }
  }

  // Setze Blocker
  private void setBlocker(int left, int top) {
    int rate = 3;
    // Alle direkt umliegenden Felder abgrasen
    for (int i = left - 1; i <= left + 1; i++) {
      for (int j = top - 1; j <= top + 1; j++) {
        // Prüfen ob Feld im Range liegt und Gegner gehört
        if (checkOwner(i, j, currentPlayer.getPlayerColor().opponent())) {
          if (i < left) {
            setRate(left - 2, top - 1, rate, "d");
            setRate(left - 2, top + 1, rate, "d");
          }
          if (i <= left) {
            setRate(left - 1, top - 2, rate, "d");
            setRate(left - 1, top + 2, rate, "d");
          }
          if (i >= left) {
            setRate(left + 1, top - 2, rate, "d");
            setRate(left + 1, top + 2, rate, "d");
          }
          if (i > left) {
            setRate(left + 2, top - 1, rate, "d");
            setRate(left + 2, top + 1, rate, "d");
          }
          if (j < top) {
            setRate(left - 1, top - 2, rate, "d");
            setRate(left + 1, top - 2, rate, "d");
          }
          if (j <= top) {
            setRate(left - 2, top - 1, rate, "d");
            setRate(left + 2, top - 1, rate, "d");
          }
          if (j >= top) {
            setRate(left - 2, top + 1, rate, "d");
            setRate(left + 2, top + 1, rate, "d");
          }
          if (j > top) {
            setRate(left - 1, top + 2, rate, "d");
            setRate(left + 1, top + 2, rate, "d");
          }
        }
      }
    }
  }

  // Zaehl mal die Gegner faecherartig vor dem Stein
  private int blockedOpponents(int left, int top, int direction) {
    int blocked = 0;
    int iMin = 0;
    int iMax = BOARDSIZE;
    int fanDefault = left;
    boolean otherDir = false;
    // Setze die i, j Grenzen richtig
    switch (direction) {
    case DIR_TOP:
      iMax = top;
      break;
    case DIR_BOTTOM:
      iMin = top;
      otherDir = true;
      break;
    case DIR_LEFT:
      iMax = left;
      fanDefault = top;
      break;
    case DIR_RIGHT:
      iMin = left;
      fanDefault = top;
      otherDir = true;
      break;
    }
    int fan = 0;
    Field field;
    for (int i = iMin; i <= iMax; i++) {
      if (otherDir) {
        fan = i - iMin;
      } else {
        fan = iMax - i;
      }
      for (int j = fanDefault - fan; j <= fanDefault + fan; j++) {
        if (Lib.inRange(i, j)) {
          if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
            field = gameState.getBoard().getField(j, i);
          } else {
            field = gameState.getBoard().getField(i, j);
          }
          if (field.getOwner() == currentPlayer.getPlayerColor()
              .opponent()) {
            blocked++;
          }
        }
      }
    }
    return blocked;
  }

  private boolean existsHades(int left, int top, PlayerColor owner) {
    boolean exists = false;
    if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
      exists = checkOwner(left, top + 2, owner)
          || checkOwner(left, top - 2, owner);
    } else {
      exists = checkOwner(left + 2, top, owner)
          || checkOwner(left - 2, top, owner);
    }
    return exists;
  }

  private boolean checkOwner(int left, int top, PlayerColor owner) {
    if (Lib.inRange(left, top)) {
      if (gameState.getBoard().getField(left, top).getOwner() == owner) {
        return true;
      }
    }
    return false;
  }

  private int getDirection(int left, int top, PlayerColor color) {
    int direction = DIR_NONE;
    List<Connection> connections = gameState.getBoard().getConnections(
        left, top);
    int countConnections = connections.size();
    switch (countConnections) {
    case 0: // Punkt hat KEINE Verbindung
      direction = DIR_ALL;
      break;
    case 1: // Punkt ist ENDPUNKT einer Verbindung / Connection
      if (color == PlayerColor.BLUE) {
        int x2 = left;
        if (x2 == connections.get(0).x1) {
          x2 = connections.get(0).x2;
        }
        if (left < x2) {
          direction = DIR_LEFT;
        } else {
          direction = DIR_RIGHT;
        }
      } else {
        int y2 = top;
        if (y2 == connections.get(0).y1) {
          y2 = connections.get(0).y2;
        }
        if (top < y2) {
          direction = DIR_TOP;
        } else {
          direction = DIR_BOTTOM;
        }
      }
      break;
    }
    return direction;
  }

  /*
   * Wertet das Feld (left, top) mit "rate" und die umliegenden Felder (bis
   * "radius") immer -1 als rate
   */
  private void rateSurround(int left, int top, int radius, int rate) {
    int i;
    // Oben / Unten
    for (i = -radius; i <= radius; i++) {
      setRate(left + i, top + radius, rate, "o");
      setRate(left + i, top - radius, rate, "o");
    }
    // Links / Rechts
    for (i = -radius + 1; i < radius; i++) {
      setRate(left - radius, top + i, rate, "u");
      setRate(left + radius, top + i, rate, "u");
    }
  }

  // Ändere Rating eines Feldes
  // private void setRate(int left, int top, int rate) {
  // setRate(left, top, rate, "");
  // }
  private void setRate(int left, int top, int rate, String name) {
    if (Lib.inRange(left, top)) {
      testBoardInt[left][top] = testBoardInt[left][top] + 1;
      testBoardStr[left][top] = testBoardStr[left][top] + name;
      if (rate <= MINVAL) {
        boardRatings[left][top] = MINVAL;
      } else {
        int boardRate = boardRatings[left][top];
        if (boardRate > MINVAL) {
          boardRatings[left][top] = Math
              .max(boardRate + rate, MINVAL);
        }
      }
    }
  }

  @Override
  public void onUpdate(Player player, Player otherPlayer) {
    currentPlayer = player;
    System.out.println("*** Spielerwechsel: " + player.getPlayerColor());
  }

  @Override
  public void onUpdate(GameState gameState) {
    this.gameState = gameState;
    currentPlayer = gameState.getCurrentPlayer();
    System.out.print("*** Das Spiel geht vorran: Zug = "
        + gameState.getTurn());
    System.out.println(", Spieler = " + currentPlayer.getPlayerColor());
  }

  @Override
  public void sendAction(Move move) {
    client.sendMove(move);
  }

}