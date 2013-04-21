package org.zhihanli.hw6.server;

import org.shared.chess.*;
import static org.shared.chess.Color.BLACK;
import static org.shared.chess.Color.WHITE;
import static org.shared.chess.GameResultReason.CHECKMATE;
import static org.shared.chess.GameResultReason.FIFTY_MOVE_RULE;
import static org.shared.chess.GameResultReason.STALEMATE;
import static org.shared.chess.GameResultReason.THREEFOLD_REPETITION_RULE;
import static org.shared.chess.PieceKind.BISHOP;
import static org.shared.chess.PieceKind.KING;
import static org.shared.chess.PieceKind.KNIGHT;
import static org.shared.chess.PieceKind.PAWN;
import static org.shared.chess.PieceKind.QUEEN;
import static org.shared.chess.PieceKind.ROOK;


public class StateSerializer {
	/**
	 * serialize function to store state
	 * 
	 * @param state
	 * @return serialized string of state
	 */
	public static String serialize(State state) {
		StringBuffer res = new StringBuffer();
		res.append(state.getTurn() + ";");

		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				Piece p = state.getPiece(new Position(row, col));
				if (p == null) {
					res.append(new String("null;"));
				} else {
					res.append(new String(p.getColor().toString() + " "
							+ p.getKind().toString() + ";"));
				}
			}
		}

		res.append(state.isCanCastleKingSide(BLACK) + ";");
		res.append(state.isCanCastleKingSide(WHITE) + ";");
		res.append(state.isCanCastleQueenSide(BLACK) + ";");
		res.append(state.isCanCastleQueenSide(WHITE) + ";");
		if (state.getEnpassantPosition() != null) {
			res.append(state.getEnpassantPosition().getRow() + " "
					+ state.getEnpassantPosition().getCol() + ";");
		} else {
			res.append(-1 + " " + -1 + ";");
		}

		if (state.getGameResult() != null) {
			res.append(state.getGameResult().getWinner() + " "
					+ state.getGameResult().getGameResultReason() + ";");
		} else {
			res.append("null null;");
		}
		res.append(state.getNumberOfMovesWithoutCaptureNorPawnMoved());
		return res.toString();
	}

	/**
	 * deserialize function restore state from string
	 * 
	 * @param string
	 * @return restored state
	 */

	public static State deserialize(String string) {
		String[] buf = string.split(";");
		Piece[][] board = new Piece[State.ROWS][State.COLS];
		State state = new State(BLACK, board, new boolean[2], new boolean[2],
				null, 0, null);

		Color turn = buf[0].equals("W") ? WHITE : BLACK;
		state.setTurn(turn);

		boolean canCastleKingSideBlack = buf[65].equals("true") ? true : false;
		boolean canCastleKingSideWhite = buf[66].equals("true") ? true : false;
		boolean canCastleQueenSideBlack = buf[67].equals("true") ? true : false;
		boolean canCastleQueenSideWhite = buf[68].equals("true") ? true : false;

		state.setCanCastleKingSide(BLACK, canCastleKingSideBlack);
		state.setCanCastleKingSide(WHITE, canCastleKingSideWhite);
		state.setCanCastleQueenSide(BLACK, canCastleQueenSideBlack);
		state.setCanCastleQueenSide(WHITE, canCastleQueenSideWhite);

		if (buf[69].length() == 5) {
			state.setEnpassantPosition(null);
		} else {
			int row = Integer.valueOf(buf[69].split(" ")[0]);
			int col = Integer.valueOf(buf[69].split(" ")[1]);
			state.setEnpassantPosition(new Position(row, col));
		}

		Color winner = buf[70].split(" ")[0].equals("W") ? WHITE : BLACK;
		if (buf[70].split(" ")[0].equals("null")) {
			winner = null;
		}
		String resultReason = buf[70].split(" ")[1];
		if (resultReason.equals("CHECKMATE")) {
			state.setGameResult(new GameResult(winner, CHECKMATE));
		} else if (resultReason.equals("FIFTY_MOVE_RULE")) {
			state.setGameResult(new GameResult(winner, FIFTY_MOVE_RULE));
		} else if (resultReason.equals("THREEFOLD_REPETITION_RULE")) {
			state.setGameResult(new GameResult(winner,
					THREEFOLD_REPETITION_RULE));
		} else if (resultReason.equals("STALEMATE")) {
			state.setGameResult(new GameResult(winner, STALEMATE));
		}

		state.setNumberOfMovesWithoutCaptureNorPawnMoved(Integer
				.valueOf(buf[71]));
		int i = 1;
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				state.setPiece(row, col, stringToPiece(buf[i++]));
			}

		}

		return state;
	}

	/**
	 * helper function for deserialize funciont
	 * 
	 * @param s
	 *            parsed string
	 * @return piece
	 */
	private static Piece stringToPiece(String s) {
		if (s.equals("null"))
			return null;
		Color color = s.split(" ")[0].equals("W") ? WHITE : BLACK;
		String kind = s.split(" ")[1];
		if (kind.equals("PAWN")) {
			return new Piece(color, PAWN);
		}

		if (kind.equals("ROOK")) {
			return new Piece(color, ROOK);
		}

		if (kind.equals("KNIGHT")) {
			return new Piece(color, KNIGHT);
		}

		if (kind.equals("BISHOP")) {
			return new Piece(color, BISHOP);
		}

		if (kind.equals("KING")) {
			return new Piece(color, KING);
		}

		if (kind.equals("QUEEN")) {
			return new Piece(color, QUEEN);
		}
		return null;

	}

}
