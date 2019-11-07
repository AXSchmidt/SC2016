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

public class TryToBeatMe implements IGameHandler {
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
  private int centrumX1, centrumX2, centrumY1, centrumY2;
  private Move bestMove;

  public TryToBeatMe(Starter client) {
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
    bestMove = Lib.getbestMove(boardRatings);
    // Speichert mal unser Board in ne Txt
    if (true) {
      String additional = "--BOARD RATING--";
      Lib.arrayToTxt(boardRatings, "N_board", additional);
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
    for (int i = 0; i < BOARDSIZE; i++) {
      for (int j = 0; j < BOARDSIZE; j++) {
        boardRatings[i][j] = 0;
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
  }

  // Setze Rating des Boardes
  public void setRating() {
    Field currentField;
    Board currentBoard = gameState.getBoard();
    PlayerColor ownColor = currentPlayer.getPlayerColor();
    for (int i = 0; i < BOARDSIZE; i++) {
      for (int j = 0; j < BOARDSIZE; j++) {
        currentField = currentBoard.getField(i, j);
        // Das Feld gehört einem selbst
        if (currentField.getOwner() == ownColor) {
          rateOwn(i, j);
        }
        // Feindbesetzung!
        if (currentField.getOwner() == ownColor.opponent()) {
          // TODO
        }
      }
    }
    rateLast();
  }

  // Um eigene Steine raten
  private void rateOwn(int left, int top) {
    // Finde Ausrichtung raus und setze Rate
    int rateVertical = 4;
    int rateHorizontal = 6;
    if (currentPlayer.getPlayerColor() == PlayerColor.BLUE) {
      rateVertical = 6;
      rateHorizontal = 4;
    }
    // Anzahl der Verbindungen in dem Punkt
    int countConnections = gameState.getBoard().getConnections(left, top).size();
    // Nur einzelne oder Connectionsendpunkte
    if (countConnections <= 1) {
      int direction = getDirection(left, top, currentPlayer.getPlayerColor());
      setKnight(left, top, rateVertical, rateHorizontal, direction);
    } 
  }
  
  private void rateLast(){
	  if(isMoveInZone(this.gameState.getLastMove())){
		  //Hades
		  if(isLastMoveHades){
			  
		  }
		  //Zeus
		  if(isLastMoveZeus){
			  
		  }
		  //Pan
		  if(isLastMovePan){
			  
		  }
		  //Herkules
		  if(isLastMoveHerkules){
			  
		  }
		  //Achilles
		  if(isLastMoveAchilles){
			  
		  }
	  }
  }


  private void rateLine(int left, int top, int rate, int direction) {
    // An der Posi 2x raten, damit man nicht am Rand setzt...
    setRate(left, top, rate);
    while (Lib.inRange(left, top)) {
      setRate(left, top, rate);
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

  private void setKnight(int left, int top, int vertical, int horizontal, int direction) {
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
        setRate(left, top, rate);     
      } else {
        setRate(left, top, MINVAL);
      }
    }
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


  // Ändere Rating eines Feldes
  // private void setRate(int left, int top, int rate) {
  // setRate(left, top, rate, "");
  // }
  private void setRate(int left, int top, int rate) {
    if (Lib.inRange(left, top)) {
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
  
  private boolean isLastMoveBlockInLine (int distance) {
	  Move lastMove = gameState.getLastMove();
	  int x=lastMove.getX();
	  int y=lastMove.getY();
	  // TODO: Gucken, ob LastMove im Hades zu uns liegt...
	  if(this.currentPlayer.getPlayerColor()==PlayerColor.RED){
		  for(int j=0-distance; j<=distance; j+=(2*distance)){
			  if(Lib.inRange(x, y+j)){
				  if(this.gameState.getBoard().getField(x, y+j).getOwner() == PlayerColor.RED){
					  int direction = getDirection(x, y+j);
					  if(j<0 && ((direction == this.DIR_TOP) || direction == this.DIR_ALL)){
						  return true;
					  }else if(j>0 && ((direction == this.DIR_BOTTOM) || direction == this.DIR_ALL)){
						  return true;
					  }
				  }
			  }
		  }
	  }else{
		  for(int i=0-distance; i<=distance; i+=(2*distance)){
			  if(Lib.inRange(x+i, y)){
				  if(this.gameState.getBoard().getField(x+i, y).getOwner() == PlayerColor.BLUE){
					  int direction = getDirection(x+i, y);
					  if(i<0 && ((direction == this.DIR_LEFT) || direction == this.DIR_ALL)){
						  return true;
					  }else if(i>0 && ((direction == this.DIR_RIGHT) || direction == this.DIR_ALL)){
						  return true;
					  }
				  }
			  }
		  }
	  }
  }
  
  private boolean isLastMoveBlockOneNearToLine(int distance){
	  Move lastMove = gameState.getLastMove();
	  int x=lastMove.getX();
	  int y=lastMove.getY();
	  // TODO: Gucken, ob LastMove im Zeus zu uns liegt...
	  if(this.currentPlayer.getPlayerColor() == PlayerColor.RED){
		  for(int i=-1; i<=1; i+=2){
			  for(int j=-distance; j<=distance; j+=(2*distance)){
				  if(Lib.inRange(x+i, y+j)){
					  if(this.gameState.getBoard().getField(x+i, y+j).getOwner() == PlayerColor.RED){
						  int direction = getDirection(x+i, y+j);
						  if(j<0 && ((direction == this.DIR_TOP) || direction == this.DIR_ALL)){
							  return true;
						  }else if(j>0 && ((direction == this.DIR_BOTTOM) || direction == this.DIR_ALL)){
							  return true;
						  }
					  }
				  }
			  }
		  }
	  }else{
		  for(int i=-distance; i<=distance; i+=(2*distance)){
			  for(int j=-1; j<=1; j+=2){
				  if(Lib.inRange(x+i, y+j)){
					  if(this.gameState.getBoard().getField(x+i, y+j).getOwner() == PlayerColor.RED){
						  int direction = getDirection(x+i, y+j);
						  if(i<0 && ((direction == this.DIR_LEFT) || direction == this.DIR_ALL)){
							  return true;
						  }else if(i>0 && ((direction == this.DIR_RIGHT) || direction == this.DIR_ALL)){
							  return true;
						  }
					  }
				  }
			  }
		  }
	  }
	  return false;
  }
  
  private boolean isLastMoveHades () {
	  return isLastMoveBlockInLine(2);
  }
  
  // TODO: auch für andere 4 Blocker (Zeus, Pan, Herkules, Archilles)
  private boolean isLastMoveZeus(){
	  return isLastMoveBlockOneNearToLine(2);
  }
  
  private boolean isLastMovePan(){
	  return isLastMoveBlockInLine(3);
  }
  
  private boolean isLastMoveHerkules(){
	  return isLastMoveBlockInLine(4);
  }
  
  private boolean isLastMoveAchilles(){
	  return isLastMoveBlockOneNearToLine(4);
  }
  
  private boolean isMoveinZone (Move move) {
	  // TODO: move vergleichen mit centrumX1, centrumX2, centrumY1, centrumY2
	  // bzw if player=blue dann ist centrumX1=0 und centrumX2=BOARDSIZE
	  // player=red dann y1,y2
	  int moveX = move.getX();
	  int moveY = move.getY();
	  if(this.currentPlayer.getPlayerColor() == PlayerColor.RED){
		  this.centrumY1 = 0;
		  this.centrumY2 = this.BOARDSIZE;
	  }else{
		  this.centrumX1 = 0;
		  this.centrumX2 = this.BOARDSIZE;
	  }
	  for(int x=this.centrumX1; x<this.centrumX2; x++){
		  if(moveX = x){
			  for(int y=this.centrumY1; y<this.centrumY2; y++){
				  if(moveY = y){
					  return true;
				  }
			  }
		  }
	  }
	  return false;
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