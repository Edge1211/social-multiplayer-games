package org.zhihanli.hw6.client;


import org.shared.chess.Move;
import org.shared.chess.PieceKind;
import org.shared.chess.Position;

public class MoveSerializer {
	public static Move stringToMove(String move) {
		String[] s = move.split(";");
		return new Move(new Position(Integer.valueOf(s[0]),
				Integer.valueOf(s[1])), new Position(Integer.valueOf(s[2]),
				Integer.valueOf(s[3])), stringToPieceKind(s[4]));
	}

	public static String moveToString(Move move) {
		if(move==null)
			return null;
		return move.getFrom().getRow() + ";" + move.getFrom().getCol() + ";"
				+ move.getTo().getRow() + ";" + move.getTo().getCol() + ";"
				+ move.getPromoteToPiece();

	}

	/**
	 * helper function for deserialize funciont
	 * 
	 * @param s
	 *            parsed string
	 * @return piece
	 */
	private static PieceKind stringToPieceKind(String s) {
		if (s.equals("null"))
			return null;
		if (s.equals("PAWN")) {
			return PieceKind.PAWN;
		}

		if (s.equals("ROOK")) {
			return PieceKind.ROOK;
		}

		if (s.equals("KNIGHT")) {
			return PieceKind.KNIGHT;
		}

		if (s.equals("BISHOP")) {
			return PieceKind.BISHOP;
		}

		if (s.equals("KING")) {
			return PieceKind.KING;
		}

		if (s.equals("QUEEN")) {
			return PieceKind.QUEEN;
		}
		return null;

	}

}
