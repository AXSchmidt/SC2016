package sc.player2016.logic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import sc.plugin2016.Board;
import sc.plugin2016.Field;
import sc.plugin2016.FieldType;
import sc.plugin2016.Move;
import sc.plugin2016.PlayerColor;

public class Lib {
	private static final int BOARDSIZE = 24;
	private static final int MINVAL = -9;

	public static class Punkt {
		int x;
		int y;

		Punkt(int x_koord, int y_koord) {
			this.x = x_koord;
			this.y = y_koord;
		}
		
		boolean isEqual(Punkt p2) {
			return (x == p2.x && y == p2.y);
		}
	}
	
	public static class Punkte {
		List<Punkt> points;
		Punkte() {
			points = new ArrayList<Punkt>();
		}
		boolean hasPoint(Punkt p) {
			for (int i = 0; i < points.size(); i++) {
				if (points.get(0).isEqual(p)) {
					return true;
				}
			}
			return false;
		}		
		void insert(Punkt p) {
			points.add(p);
		}
	}

 	public static Punkt getCentrum(int x1, int x2, int y1, int y2) {		
		int x, y;
		// Setze Mitte
		if ((x2 - x1) % 2 == 0) {
			x = (x2 + x1) / 2;
		} else {
			x = (x2 + x1 + 1) / 2;
			if (x > (BOARDSIZE / 2)) {
				x--;
			}
		}
		if ((y2 - y1) % 2 == 0) {
			y = (y2 + y1) / 2;
		} else {
			y = (y2 + y1 + 1) / 2;
			if (y > (BOARDSIZE / 2)) {
				y--;
			}
		}	
		return new Punkt(x, y);
	}
	
	// Schreib Array in ne Textdatei
	public static void arrayToTxt(int[][] arr, String name, String additional) {
		try {
			PrintWriter pr = new PrintWriter(System.getProperty("user.home") + "/Desktop/"+name+".txt");
			String line = "RATING:";
			String bestM = "";
			String conn  = "";
			int rate = MINVAL - 1;
			for (int i = 0; i < BOARDSIZE; i++) {
				pr.println(line);
				line = "";
				for (int j = 0; j < BOARDSIZE; j++) {
					line += String.format("%3s", arr[j][i]);
					if (arr[j][i] > rate) {
						rate = arr[j][i];
						Integer newInt = new Integer(rate);
						bestM = "Best Move (" + j + "/" + i + ") - Rating: " + newInt.toString();
					}
				}
			}
			pr.println(line);
			pr.println(bestM);
			pr.println(additional);
			pr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No such file exists.");
		}
	}
	
	public static void arrayStrToTxt(String[][] arr, String name) {
		try {
			PrintWriter pr = new PrintWriter(System.getProperty("user.home") + "/Desktop/"+name+".txt");
			String line = "RATING:";
			String conn  = "";
			for (int i = 0; i < BOARDSIZE; i++) {
				pr.println(line);
				line = "";
				for (int j = 0; j < BOARDSIZE; j++) {
					line += " " + ("______" + arr[j][i]).substring(arr[j][i].length());
				}
			}
			pr.println(line);
			pr.println("");
			pr.println("M Mitte (Runde 0)");
			pr.println("c Zentrum blau");
			pr.println("C Zentrum rot");
			pr.println("b Rand blau Spieler blau");
			pr.println("r Rand blau Spieler rot");
			pr.println("R Rand rot Spieler rot");
			pr.println("b Rand rot Spieler blau");
			pr.println("p Ran Verteidigung blau");
			pr.println("h Hades Verteidigung blau");
			pr.println("P Ran Verteidigung rot");
			pr.println("H Hades Verteidigung rot");
			pr.println("L Rate Line doppel");
			pr.println("l Rate Line");
			pr.println("K Rate Knight faraway");
			pr.println("k Rate Knight");
			pr.println("N Kein Pferdehüpfer möglich");
			pr.println("E Extended Pferd");
			pr.println("q BlockBlock blau");
			pr.println("Q BlockBlock rot");
			pr.println("d Blocker");
			pr.println("o Rate Surround oben unten");
			pr.println("u Rate Surround links rechts");
			pr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No such file exists.");
		}
	}
	
	// Schreibt Board in TXT
	private void putBoard(Board printBoard, String name, String headLine) {
		try {
			PrintWriter pr = new PrintWriter(System.getProperty("user.home") + "/Desktop/"+name+".txt");
			String line = "BOARD: " + headLine;
			for (int i = 0; i < BOARDSIZE; i++) {
				pr.println(line);
				line = "";
				for (int j = 0; j < BOARDSIZE; j++) {
					Field newField = printBoard.getField(j, i);
					if (newField.getOwner() == PlayerColor.BLUE) {
						line += "B";						
					} else if (newField.getOwner() == PlayerColor.RED) {
						line += "R";						
					} else if (newField.getType() == FieldType.BLUE) {
						line += "b";
					} else if (newField.getType() == FieldType.RED) {
						line += "r";
					} else if (newField.getType() == FieldType.SWAMP) {
						line += "S";
					} else {
						line += " ";
					}
				}
			}
			pr.println(line);
			pr.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("No such file exists.");
		}
	}
	
	// Liegt das Feld i, j im Bereich
	public static boolean inRange(int left, int top) {
		return ((left >= 0) & (left < BOARDSIZE) & (top >= 0) & (top < BOARDSIZE));
	}
	
	// Suche Zug mit bestmöglichem Rating
	public static Move getbestMove(int[][] arr) {
		int rate = MINVAL - 1;
		Move move = new Move(0, 0);
		for (int i = 0; i < BOARDSIZE; i++) {
			for (int j = 0; j < BOARDSIZE; j++) {
				if (arr[i][j] > rate) {
					rate = arr[i][j];
					move = new Move(i, j);
				}
			}
		}
		return move;
	}
	
}