package org.zhihanli.hw2;

import java.util.Iterator;
import java.util.Set;

import static org.shared.chess.Color.WHITE;
import static org.shared.chess.Color.BLACK;
import org.shared.chess.Color;
import org.shared.chess.GameResult;
import org.shared.chess.GameResultReason;
import org.shared.chess.IllegalMove;
import org.shared.chess.Move;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.Position;
import static org.shared.chess.State.COLS;
import static org.shared.chess.State.ROWS;
import org.shared.chess.State;
import org.shared.chess.StateChanger;

import com.google.common.collect.Sets;

public class StateChangerImpl implements StateChanger {

	public void makeMove(State state, Move move) throws IllegalMove {
		if (state.getGameResult() != null) {
			// Game already ended!
			throw new IllegalMove();
		}

		State stateCopy = state.copy();

		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = stateCopy.getPiece(from);
		Piece pieceTo = stateCopy.getPiece(to);
		if (piece == null) {
			// Nothing to move!
			throw new IllegalMove();
		}
		Color color = piece.getColor();
		if (color != state.getTurn()) {
			// Wrong player moves!
			throw new IllegalMove();
		}

		// check if go to square out of board
		if (!onBoard(from.getRow(), from.getCol())
				|| !onBoard(to.getRow(), to.getCol())) {
			throw new IllegalMove();
		}

		// check whether go to square with same color piece
		if (state.getPiece(to) != null
				&& state.getPiece(to).getColor() == state.getPiece(from)
						.getColor()) {
			throw new IllegalMove();
		}

		// check illegal promotion to pieces other than pawn
		if (move.getPromoteToPiece() != null
				&& piece.getKind() != PieceKind.PAWN) {
			throw new IllegalMove();
		}

		// attempt move, only modify state copy
		move(stateCopy, move, piece.getKind());

		/*
		 * if before move king is under check and after move the king is still
		 * under check then it is a illegal move
		 */

		if (isKingUnderCheck(stateCopy, stateCopy.getTurn())) {
			throw new IllegalMove();
		}

		Position prevEnpassantPos = state.getEnpassantPosition();
		boolean captureHappened = state.getPiece(to) != null;
		// make actual move, modify state
		move(state, move, piece.getKind());

		/* take care of enpassant position */
		if (prevEnpassantPos != null
				&& prevEnpassantPos.equals(state.getEnpassantPosition())) {
			state.setEnpassantPosition(null);
		}

		// take care of number of moves without capture nor pawn movement
		if (captureHappened || piece.getKind() == PieceKind.PAWN) {
			state.setNumberOfMovesWithoutCaptureNorPawnMoved(0);

		} else {
			state.setNumberOfMovesWithoutCaptureNorPawnMoved(state
					.getNumberOfMovesWithoutCaptureNorPawnMoved() + 1);
		}

		// switch turn
		if (state.getTurn() == BLACK) {
			state.setTurn(WHITE);
		} else {
			state.setTurn(BLACK);
		}

		// take care of isCanCastle
		// setIsCanCastleBasedOnPieceExistence(state);
		if (captureHappened) {
			setIsCanCastle(state, pieceTo.getKind(), to);
		}

		checkEndOfGame(state, state.getTurn());
	}

	//general move 
	private void move(State state, Move move, PieceKind kind) {
		switch (kind) {
		case KING:
			kingMove(state, move);
			break;
		case KNIGHT:
			knightMove(state, move);
			break;
		case BISHOP:
			bishopMove(state, move);
			break;
		case PAWN:
			pawnMove(state, move);
			break;
		case QUEEN:
			queenMove(state, move);
			break;
		case ROOK:
			rookMove(state, move);
			break;
		default:
			break;
		}
	}

	// take care of isCanCastle 
	private void setIsCanCastle(State state, PieceKind pieceCaptured,
			Position piecePos) {
		Color turn = state.getTurn();
		switch (pieceCaptured) {
		case ROOK:
			if (piecePos.getCol() == 7)
				state.setCanCastleKingSide(turn, false);
			if (piecePos.getCol() == 0)
				state.setCanCastleQueenSide(turn, false);
			break;
		case KING:
			state.setCanCastleKingSide(turn, false);
			state.setCanCastleQueenSide(turn, false);
			break;
		default:
			break;
		}
	}

	public void kingMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();

		Piece piece = state.getPiece(from);

		// check whether castling
		if (isCanCastle(state, move)) {
			boolean isKingSide = from.getCol() < to.getCol();

			state.setPiece(from, null);
			state.setPiece(to, piece);
			if (isKingSide) {
				state.setPiece(from.getRow(), 7, null);
				state.setPiece(from.getRow(), 5, new Piece(state.getTurn(),
						PieceKind.ROOK));
			} else {
				state.setPiece(from.getRow(), 0, null);
				state.setPiece(from.getRow(), 3, new Piece(state.getTurn(),
						PieceKind.ROOK));
			}

		} else if (Math.abs(to.getCol() - from.getCol()) <= 1
				&& Math.abs(to.getRow() - from.getRow()) <= 1) {
			// usual move

			// check whether go to place under attack
			// if (isKingUnderCheck(state, state.getTurn())) {
			// throw new IllegalMove();
			// }

			state.setPiece(from, null);
			state.setPiece(to, piece);

		} else {
			throw new IllegalMove();
		}

		// King moved modify isCanCastle
		state.setCanCastleKingSide(state.getTurn(), false);
		state.setCanCastleQueenSide(state.getTurn(), false);

	}

	private boolean isCanCastle(State state, Move move) {

		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);

		// check there is a valid castle attempt
		if (piece.getKind() != PieceKind.KING) {
			return false;
		}

		int offset = to.getCol() - from.getCol();
		if (Math.abs(offset) != 2) {
			return false;
		}
		if (to.getRow() != from.getRow()) {
			return false;
		}

		/*
		 * check neither of the pieces involved in castling may have been
		 * previously moved during the game
		 */
			boolean isCanCastleKingSide = (offset == 2 && state
				.isCanCastleKingSide(piece.getColor()));
		boolean isCanCastleQueenSide = (offset == -2 && state
				.isCanCastleQueenSide(piece.getColor()));

		if (!isCanCastleKingSide && !isCanCastleQueenSide) {
			if (!(isCanCastleKingSide)) {
				return false;
			}

			if (!(isCanCastleQueenSide)) {
				return false;
			}
		}

		// check There must be no pieces between the king and the rook
		if (offset == 2) {
			if (state.getPiece(from.getRow(), from.getCol() + 1) != null
					|| state.getPiece(from.getRow(), from.getCol() + 2) != null) {
				return false;
			}
		}

		if (offset == -2) {
			if (state.getPiece(from.getRow(), from.getCol() - 1) != null
					|| state.getPiece(from.getRow(), from.getCol() - 2) != null
					|| state.getPiece(from.getRow(), from.getCol() - 3) != null) {
								return false;
			}
		}

		/*
		 * check :The king may not be in check, nor may the king pass through
		 * squares that are under attack by enemy pieces, nor move to a square
		 * where it is in check
		 */
		State tempState = state.copy();
		tempState.setPiece(from, null);
		tempState.setPiece(to, piece);
		if (isKingUnderCheck(tempState, state.getTurn())) {
			return false;
		}
		tempState = state.copy();
		tempState.setPiece(from, null);
		tempState.setPiece(from.getRow(), (from.getCol() + to.getCol()) / 2,
				piece);
		if (isKingUnderCheck(tempState, state.getTurn())) {
			return false;
		}
		tempState = state.copy();
		if (isKingUnderCheck(tempState, tempState.getTurn())) {
			
			return false;
		}

		return true;

	}

	public boolean isKingUnderCheck(State state, Color turn) {
		return isKingUnderCheckAndGetThreatenPos(state, turn) != null;
	}

	public Position isKingUnderCheckAndGetThreatenPos(State state, Color turn) {
		Position kingPos = searchForKing(state, turn);
		for (int row = 0; row < ROWS; row++) {
			for (int col = 0; col < COLS; col++) {
				if (canCapture(new Position(row, col), kingPos, state)) {
					return new Position(row, col);
				}
			}
		}
		return null;
	}

	private boolean canCapture(Position capturer, Position capturee, State state) {
		if (state.getPiece(capturer) == null) {
			return false;
		}
		if (state.getPiece(capturer).getColor() == state.getPiece(capturee)
				.getColor()) {
			return false;
		}

		if (capturer.getCol() == capturee.getCol()
				&& capturer.getRow() == capturee.getRow()) {
			return false;
		}

		Set<Position> moveList = getMovePosSet(state, capturer);
		Iterator<Position> itr = moveList.iterator();
		while (itr.hasNext()) {
			if (itr.next().equals(capturee))
				return true;
		}
		return false;
	}

	private boolean onBoard(int row, int col) {
		return row >= 0 && row < 8 & col >= 0 && col < 8;
	}

	public void knightMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();

		Piece piece = state.getPiece(from);

		int colOffset = to.getCol() - from.getCol();
		int rowOffset = to.getRow() - from.getRow();

		if (!(Math.abs(colOffset) == 1 && Math.abs(rowOffset) == 2)
				&& !(Math.abs(colOffset) == 2 && Math.abs(rowOffset) == 1)) {
			throw new IllegalMove();
		} else {
			state.setPiece(from, null);
			state.setPiece(to, piece);
		}

	}

	public void bishopMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();

		int rowOffset = Math.abs(from.getRow() - to.getRow());
		int colOffset = Math.abs(from.getCol() - to.getCol());

		if (rowOffset != colOffset) {
			throw new IllegalMove();
		}

		int moveRow = to.getRow() > from.getRow() ? 1 : -1;
		int moveCol = to.getCol() > from.getCol() ? 1 : -1;

		int row = from.getRow() + moveRow;
		int col = from.getCol() + moveCol;

		while (!(row == to.getRow() && col == to.getCol())) {
			if (state.getPiece(row, col) != null) {
				throw new IllegalMove();
			}
			row += moveRow;
			col += moveCol;
		}

		Piece piece = state.getPiece(from);
		state.setPiece(from, null);
		state.setPiece(to, piece);

	}

	public void pawnMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		int rowOffset = to.getRow() - from.getRow();
		int colOffset = to.getCol() - from.getCol();

		// illegal promotion
		if (move.getPromoteToPiece() != null) {
			if (to.getRow() != 0 && to.getRow() != 7) {
				throw new IllegalMove();
			}
			switch (move.getPromoteToPiece()) {
			case QUEEN:
				break;
			case ROOK:
				break;
			case BISHOP:
				break;
			case KNIGHT:
				break;
			default:
				throw new IllegalMove();
			}
		}

		if (colOffset == 0 && Math.abs(rowOffset) == 1) {
			// usual move, further detect illegal move
			pawnUsualMove(state, move);
		} else if (Math.abs(colOffset) == 1 && Math.abs(rowOffset) == 1) {
			// capture, further detect illegal move
			pawnCapture(state, move);

		} else if ((Math.abs(rowOffset) == 1 || Math.abs(rowOffset) == 2)
				&& (from.getRow() == 1 || from.getRow() == 6) && colOffset == 0) {
			// initial move, further detect illegal move
			pawnInitMove(state, move);

		} else {
			throw new IllegalMove();
		}

	}

	private void pawnUsualMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);

		PieceKind promotionTo = move.getPromoteToPiece();

		int rowOffset = to.getRow() - from.getRow();
		Color color = state.getTurn();

		if (state.getPiece(to) != null) {
			throw new IllegalMove();
		}
		// usual advance
		if (rowOffset == -1 && color == Color.BLACK) {
			// Black pawn legal move
			if (to.getRow() == 0) {
				// Promotion
				state.setPiece(from, null);
				state.setPiece(to, new Piece(Color.BLACK, promotionTo));
			} else {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			}
		} else if (rowOffset == 1 && color == Color.WHITE) {
			// White pawn legal move
			if (to.getRow() == 7) {
				state.setPiece(from, null);
				state.setPiece(to, new Piece(Color.WHITE, promotionTo));
			} else {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			}
		} else {
			throw new IllegalMove();
		}
	}

	private void pawnCapture(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);
		Color color = state.getTurn();
		PieceKind promotionTo = move.getPromoteToPiece();

		int rowOffset = to.getRow() - from.getRow();
		if (rowOffset == 1 && color == Color.WHITE) {
			// white capture

			// check whether enpassant
			if (state.getPiece(to) == null) {
				if (isCanEnpassant(state, move, to)) {
					state.setPiece(from, null);
					state.setPiece(to, piece);
					state.setPiece(state.getEnpassantPosition(), null);
				} else {
					throw new IllegalMove();
				}
			}

			// usual capture
			state.setPiece(from, null);
			state.setPiece(to, piece);
			if (to.getRow() == 7) {
				state.setPiece(to, new Piece(Color.WHITE, promotionTo));
			}
		} else if (rowOffset == -1 && color == Color.BLACK) {
			// black capture

			// check whether enpassant
			if (state.getPiece(to) == null) {
				if (isCanEnpassant(state, move, to)) {
					state.setPiece(from, null);
					state.setPiece(to, piece);
					state.setPiece(state.getEnpassantPosition(), null);
				} else {
					throw new IllegalMove();
				}
			}

			state.setPiece(from, null);
			state.setPiece(to, piece);
			if (to.getRow() == 0) {
				state.setPiece(to, new Piece(Color.BLACK, promotionTo));
			}

		} else {
			throw new IllegalMove();
		}
	}

	private boolean isCanEnpassant(State state, Move move, Position pawnCapPos) {
		if (state.getEnpassantPosition() == null) {
			return false;
		}

		Position enpassantPos = state.getEnpassantPosition();
		Color colorEnp = state.getPiece(enpassantPos).getColor();
		if (colorEnp == Color.BLACK) {
			return pawnCapPos.getRow() == (enpassantPos.getRow() + 1)
					&& pawnCapPos.getCol() == enpassantPos.getCol();
		} else if (colorEnp == Color.WHITE) {
			return pawnCapPos.getRow() == (enpassantPos.getRow() - 1)
					&& pawnCapPos.getCol() == enpassantPos.getCol();
		}
		return false;

	}

	private void pawnInitMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);
		Color color = state.getTurn();

		int rowOffset = to.getRow() - from.getRow();

		if (state.getPiece(to) != null) {
			throw new IllegalMove();
		}

		if (color == Color.BLACK) {
			if (rowOffset == -1) {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			} else if (rowOffset == -2) {
				if (state.getPiece(from.getRow() - 1, from.getCol()) != null) {
					throw new IllegalMove();
				}
				state.setPiece(from, null);
				state.setPiece(to, piece);
				state.setEnpassantPosition(to);
			} else {
				throw new IllegalMove();
			}

		}

		if (color == Color.WHITE) {
			if (rowOffset == 1) {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			} else if (rowOffset == 2) {
				if (state.getPiece(from.getRow() + 1, from.getCol()) != null) {
					throw new IllegalMove();
				}
				state.setPiece(from, null);
				state.setPiece(to, piece);
				state.setEnpassantPosition(to);
			} else {
				throw new IllegalMove();
			}

		}

	}

	public void rookMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);

		if (from.getRow() == to.getRow() || from.getCol() == to.getCol()) {
			if (isPathClear(state, from, to)) {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			} else {
				throw new IllegalMove();
			}
		} else {
			throw new IllegalMove();
		}
		if (state.isCanCastleKingSide(piece.getColor()) && from.getCol() == 7) {
			state.setCanCastleKingSide(piece.getColor(), false);
		}
		if (state.isCanCastleQueenSide(piece.getColor()) && from.getCol() == 0) {
			state.setCanCastleQueenSide(piece.getColor(), false);
		}

	}

	// check whether there is a path between two positions
	private boolean isPathClear(State state, Position from, Position to) {
		int row = from.getRow();
		int col = from.getCol();
		int moveRow;
		int moveCol;
		if (from.getRow() == to.getRow() && from.getCol() == to.getCol()) {
			return false;
		}

		boolean inDiag = (Math.abs(from.getRow() - to.getRow()) == Math
				.abs(from.getCol() - to.getCol()));
		boolean inSameRow = from.getRow() == to.getRow();
		boolean inSameCol = from.getCol() == to.getCol();
		if (inDiag) {
			moveRow = to.getRow() > from.getRow() ? 1 : -1;
			moveCol = to.getCol() > from.getCol() ? 1 : -1;
		} else if (inSameRow) {
			moveRow = 0;
			moveCol = to.getCol() > from.getCol() ? 1 : -1;
		} else if (inSameCol) {
			moveRow = to.getRow() > from.getRow() ? 1 : -1;
			moveCol = 0;
		} else {
			return false;
		}

		row += moveRow;
		col += moveCol;
		while (!(row == to.getRow() && col == to.getCol())) {
			if (state.getPiece(row, col) != null) {
				return false;
			}
			row += moveRow;
			col += moveCol;
		}
		return true;

	}

	private void queenMove(State state, Move move) throws IllegalMove {
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(from);

		if (from.getRow() == to.getRow()
				|| from.getCol() == to.getCol()
				|| Math.abs(from.getCol() - to.getCol()) == Math.abs(from
						.getRow() - to.getRow())) {
			if (isPathClear(state, from, to)) {
				state.setPiece(from, null);
				state.setPiece(to, piece);
			} else {
				throw new IllegalMove();
			}

		} else {
			throw new IllegalMove();
		}

	}

	private Position searchForKing(State state, Color side) {
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				if (state.getPiece(row, col) != null
						&& state.getPiece(row, col).getKind() == PieceKind.KING
						&& state.getPiece(row, col).getColor() == side) {
					return new Position(row, col);
				}
			}
		}
		return new Position(-1, -1);
	}

	private void checkEndOfGame(State state, Color turn) {
		// TODO: checkmate
		if (isCheckMate(state)) {
			Color win = turn == Color.BLACK ? Color.WHITE : Color.BLACK;
			state.setGameResult(new GameResult(win, GameResultReason.CHECKMATE));
		}

		// TODO: stalemate
		if (isStaleMate(state, turn)) {
			state.setGameResult(new GameResult(null, GameResultReason.STALEMATE));
		}

		// TODO: fifty move rule
		if (state.getNumberOfMovesWithoutCaptureNorPawnMoved() == 100) {
			state.setGameResult(new GameResult(null,
					GameResultReason.FIFTY_MOVE_RULE));
		}

	}

	private boolean isCheckMate(State state) {
		Color turn = state.getTurn();
		Position kingPos = searchForKing(state, turn);
		boolean canEscCheck = false;
		Position threatPos = isKingUnderCheckAndGetThreatenPos(state,
				state.getTurn());

		if (threatPos != null) {
			for (int colOffset = -1; colOffset < 2; colOffset++) {
				for (int rowOffset = 1; rowOffset > -2; rowOffset--) {
					Position to = new Position(kingPos.getRow() + rowOffset,
							kingPos.getCol() + colOffset);

					if (to.equals(kingPos)
							|| !onBoard(to.getRow(), to.getCol())
							|| (state.getPiece(to) != null && state
									.getPiece(to).getColor() == turn))
						continue;
					State temp = state.copy();
					temp.setPiece(kingPos, null);
					temp.setPiece(to, new Piece(turn, PieceKind.KING));
					if (!isKingUnderCheck(temp, temp.getTurn())) {
						canEscCheck = true;
						break;
					}
				}
			}

			// if can not get out of check, check whether can capture
			// threatening piece
			if (!canEscCheck) {
				for (int row = 0; row < State.ROWS; row++) {
					for (int col = 0; col < State.COLS; col++) {
						Piece piece = state.getPiece(row, col);
						if (piece != null
								&& piece.getColor() == turn
								&& canCapture(new Position(row, col),
										threatPos, state)) {
							State tempState = state.copy();
							tempState.setPiece(row, col, null);
							tempState.setPiece(threatPos, piece);
							canEscCheck = !isKingUnderCheck(tempState, turn);

						}
					}
				}
			}

			// last check whether check route can be blocked
			if (!canEscCheck) {
				for (int i = 0; i < State.ROWS; i++) {
					for (int j = 0; j < State.COLS; j++) {
						Piece piece = state.getPiece(i, j);
						if (piece != null && piece.getColor() == turn
								&& piece.getKind() != PieceKind.KING) {
							if (canBlock(state, kingPos, threatPos,
									new Position(i, j))) {
								canEscCheck = true;
								break;
							}
						}
					}
				}
			}
		} else {
			return false;
		}
		
		return !canEscCheck;
	}

	/**
	 * 
	 * @param state
	 * @param piece1
	 * @param piece2
	 * @param blocker
	 * @return if blocker can go to the route between piece 1 and 2, return true
	 */
	private boolean canBlock(State state, Position piece1, Position piece2,
			Position blocker) {

		int row = piece1.getRow();
		int col = piece1.getCol();
		int moveRow = piece2.getRow() > piece1.getRow() ? 1 : -1;
		int moveCol = piece2.getCol() > piece1.getCol() ? 1 : -1;

		if (piece2.getRow() == piece1.getRow())
			moveRow = 0;
		if (piece2.getCol() == piece1.getCol())
			moveCol = 0;

		while (!(row == piece2.getRow() && col == piece2.getCol())) {
			State temp = state.copy();
			try {
				Move tempMove = new Move(blocker, new Position(row, col), null);
				makeMove(temp, tempMove);
			} catch (Exception e) {
				row += moveRow;
				col += moveCol;
				continue;
			}
			return true;

		}
		return false;

	}

	/**
	 * 
	 * @param state
	 * @param turn
	 * @return if there is legal move for this turn, return false
	 */

	private boolean isStaleMate(State state, Color turn) {
		if (isKingUnderCheck(state, turn)) {
			return false;
		}

		// iterate every piece of this turn
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				Piece piece = state.getPiece(row, col);
				if (piece != null && piece.getColor() == turn) {
					if (tryAllMove(state, new Position(row, col)))
						return false;
				}
			}
		}
		return true;

	}

	/**
	 * 
	 * @param state
	 * @param curPos
	 * @return if there is legal move return true
	 */

	private boolean tryAllMove(State state, Position curPos) {
		Set<Position> posSet = getLegalMovePosSet(state, curPos);
		return posSet.size() != 0;
	}

	//remove the illegal move from curPos in posSet
	public void removeIllegalMove(State state, Position curPos,
			Set<Position> posSet) {
		if (posSet == null)
			return;

		Iterator<Position> itr = posSet.iterator();
		Piece piece = state.getPiece(curPos);

		boolean kingSideCastle = false;
		Position kingSidePos = null, kingSideCastlePos = null, queenSidePos = null, queenSideCastlePos = null;
		boolean queenSideCastle = false;
		while (itr.hasNext()) {
			State tempState = state.copy();
			Position tempPos = itr.next();
			if (tempPos.equals(new Position(-1, -1))) {
				queenSideCastle = true;
				itr.remove();
				continue;
			}
			if (tempPos.equals(new Position(8, 8))) {
				kingSideCastle = true;
				itr.remove();
				continue;
			}
			tempState.setPiece(curPos, null);
			tempState.setPiece(tempPos, piece);
			if (isKingUnderCheck(tempState, tempState.getTurn())) {
				itr.remove();
			}
		}
		itr = posSet.iterator();
		if (kingSideCastle || queenSideCastle) {

			while (itr.hasNext()) {
				Position temp = itr.next();
				if (temp.equals(new Position(curPos.getRow(), 3)))
					queenSidePos = temp;
				if (temp.equals(new Position(curPos.getRow(), 5)))
					kingSidePos = temp;
				if (temp.equals(new Position(curPos.getRow(), 2)))
					queenSideCastlePos = temp;
				if (temp.equals(new Position(curPos.getRow(), 6)))
					kingSideCastlePos = temp;
			}
			if (isKingUnderCheck(state, state.getTurn())) {
				posSet.remove(queenSideCastlePos);
				posSet.remove(kingSideCastlePos);
				return;
			}

			if (queenSideCastle) {
				if (queenSidePos == null)
					posSet.remove(queenSideCastlePos);
			}
			if (kingSideCastle) {
				if (kingSidePos == null)
					posSet.remove(kingSideCastlePos);
			}
		}
	}

	//get the legal move from curPos
	public Set<Position> getLegalMovePosSet(State state, Position curPos) {
		Set<Position> posSet = getMovePosSet(state, curPos);
		removeIllegalMove(state, curPos, posSet);
		return posSet;
	}

	//get all the possbible move from curPos
	public Set<Position> getMovePosSet(State state, Position curPos) {
		if (state.getPiece(curPos) == null)
			return Sets.newHashSet();
		switch (state.getPiece(curPos).getKind()) {
		case QUEEN:
			return getQueenMovePosSet(state, curPos);
		case ROOK:
			return getRookMovePosSet(state, curPos);
		case KING:
			return getKingMovePosSet(state, curPos);
		case PAWN:
			return getPawnMovePosSet(state, curPos);
		case BISHOP:
			return getBishopMovePosSet(state, curPos);
		case KNIGHT:
			return getKnightMovePosSet(state, curPos);
		}

		return null;
	}

	/**
	 * 
	 * @param state
	 *            Current state
	 * @param curPos
	 *            Current queen position
	 * @return a array list of positions a queen can currently move to
	 */
	private Set<Position> getQueenMovePosSet(State state, Position curPos) {
		if (curPos == null) {
			return null;
		}

		if (state.getPiece(curPos).getKind() != PieceKind.QUEEN) {
			return null;
		}

		Piece queen = state.getPiece(curPos);
		Set<Position> res = Sets.newHashSet();

		int row = curPos.getRow();
		int col = curPos.getCol();

		int moveRow, moveCol;
		// add positions in same row and col
		for (moveRow = -1; moveRow <= 1; moveRow++) {
			for (moveCol = -1; moveCol <= 1; moveCol++) {
				if ((moveRow != 0 && moveCol != 0) || (moveRow == moveCol)) {
					continue;
				}
				int i = row + moveRow;
				int j = col + moveCol;
				while (onBoard(i, j)) {
					Piece piece = state.getPiece(i, j);
					if (piece != null) {
						if (piece.getColor() == queen.getColor()) {
							break;
						} else {
							res.add(new Position(i, j));
							break;
						}
					} else {
						res.add(new Position(i, j));
					}
					j += moveCol;
					i += moveRow;
				}
			}
		}

		// add positions in diag four direction
		for (moveRow = -1; moveRow <= 1; moveRow += 2) {
			for (moveCol = -1; moveCol <= 1; moveCol += 2) {
				int i = row + moveRow;
				int j = col + moveCol;
				while (onBoard(i, j)) {
					Piece piece = state.getPiece(i, j);
					if (piece != null) {
						if (piece.getColor() == queen.getColor()) {
							break;
						} else {
							res.add(new Position(i, j));
							break;
						}
					} else {
						res.add(new Position(i, j));
					}
					i += moveRow;
					j += moveCol;
				}
			}
		}

		return res;
	}

	/**
	 * 
	 * @param state
	 * @param curPos
	 * @return get possible move to positions of all kinds of pieces
	 */
	private Set<Position> getRookMovePosSet(State state, Position curPos) {
		Set<Position> res = Sets.newHashSet();
		int row = curPos.getRow();
		int col = curPos.getCol();

		Piece rook = state.getPiece(curPos);
		int moveRow, moveCol;
		// add positions in same row and col
		for (moveRow = -1; moveRow <= 1; moveRow++) {
			for (moveCol = -1; moveCol <= 1; moveCol++) {
				if ((moveRow != 0 && moveCol != 0) || (moveRow == moveCol)) {
					continue;
				}
				int i = row + moveRow;
				int j = col + moveCol;
				while (onBoard(i, j)) {
					Piece piece = state.getPiece(i, j);
					if (piece != null) {
						if (piece.getColor() == rook.getColor()) {
							break;
						} else {
							res.add(new Position(i, j));
							break;
						}
					} else {
						res.add(new Position(i, j));
					}
					j += moveCol;
					i += moveRow;
				}
			}
		}
		return res;
	}

	private Set<Position> getKingMovePosSet(State state, Position curPos) {
		Set<Position> res = Sets.newHashSet();
		int row = curPos.getRow();
		int col = curPos.getCol();

		for (int moveRow = -1; moveRow <= 1; moveRow++) {
			for (int moveCol = -1; moveCol <= 1; moveCol++) {
				if (!onBoard(row + moveRow, col + moveCol)) {
					continue;
				}
				if (moveRow == 0 && moveCol == 0) {
					continue;
				}
				Piece piece = state.getPiece(row + moveRow, col + moveCol);
				if (piece != null
						&& piece.getColor() == state.getPiece(curPos)
								.getColor()) {
					continue;
				}
				res.add(new Position(row + moveRow, col + moveCol));
			}
		}

		// add castling pos
		Piece piece = state.getPiece(curPos);
		Position queenSide = new Position(curPos.getRow(), 2);
		Position kingSide = new Position(curPos.getRow(), 6);
		Position kingSideRook = new Position(curPos.getRow(), 7);
		Position queenSideRook = new Position(curPos.getRow(), 0);

		if (state.isCanCastleKingSide(piece.getColor())
				&& isPathClear(state, curPos, kingSideRook)) {
			if (!(state.getPiece(kingSide) != null && state.getPiece(kingSide)
					.getColor() == piece.getColor())) {
				res.add(kingSide);
				res.add(new Position(8, 8));
			}
		}

		if (state.isCanCastleQueenSide(piece.getColor())
				&& isPathClear(state, curPos, queenSideRook)) {
			if (!(state.getPiece(queenSide) != null && state
					.getPiece(queenSide).getColor() == piece.getColor())) {
				res.add(queenSide);
				res.add(new Position(-1, -1));
			}
		}

		return res;
	}

	private Set<Position> getBishopMovePosSet(State state, Position curPos) {
		Set<Position> res = Sets.newHashSet();
		int row = curPos.getRow();
		int col = curPos.getCol();
		int moveRow, moveCol;
		Piece bishop = state.getPiece(curPos);
		// add positions in diag four direction
		for (moveRow = -1; moveRow <= 1; moveRow += 2) {
			for (moveCol = -1; moveCol <= 1; moveCol += 2) {
				int i = row + moveRow;
				int j = col + moveCol;
				while (onBoard(i, j)) {
					Piece piece = state.getPiece(i, j);
					if (piece != null) {
						if (piece.getColor() == bishop.getColor()) {
							break;
						} else {
							res.add(new Position(i, j));
							break;
						}
					} else {
						res.add(new Position(i, j));
					}
					i += moveRow;
					j += moveCol;
				}
			}
		}
		return res;
	}

	private Set<Position> getPawnMovePosSet(State state, Position curPos) {
		Set<Position> res = Sets.newHashSet();
		Color color = state.getPiece(curPos).getColor();
		int rowOffset = color == Color.BLACK ? -1 : 1;
		for (int colOffset = -1; colOffset <= 1; colOffset++) {
			if (!onBoard(curPos.getRow() + rowOffset, curPos.getCol()
					+ colOffset)) {
				continue;
			}
			Piece piece = state.getPiece(curPos.getRow() + rowOffset,
					curPos.getCol() + colOffset);
			if (colOffset == 1 || colOffset == -1) {
				if (piece != null && piece.getColor() != color) {
					res.add(new Position(curPos.getRow() + rowOffset, curPos
							.getCol() + colOffset));
				}

				Position enpassantPos = state.getEnpassantPosition();
				if (piece == null && enpassantPos != null) {
					if (enpassantPos.getRow() == curPos.getRow()
							&& (enpassantPos.getCol() - curPos.getCol()) == colOffset) {
						res.add(new Position(curPos.getRow() + rowOffset,
								curPos.getCol() + colOffset));
					}
				}

			}
			if (colOffset == 0) {
				if (piece == null) {
					res.add(new Position(curPos.getRow() + rowOffset, curPos
							.getCol() + colOffset));
				}
			}
		}

		// add initial move to pos
		int initRow = color == Color.WHITE ? 1 : 6;
		int initOffset = (color == Color.WHITE) ? 2 : -2;
		if (curPos.getRow() == initRow) {
			Position initMove = new Position(curPos.getRow() + initOffset,
					curPos.getCol());
			Position initMoveBefore = new Position(curPos.getRow() + initOffset
					/ 2, curPos.getCol());
			if (state.getPiece(initMoveBefore) == null
					&& state.getPiece(initMove) == null) {
				res.add(initMove);
			}
		}

		return res;
	}

	private Set<Position> getKnightMovePosSet(State state, Position curPos) {
		Set<Position> res = Sets.newHashSet();
		Color color = state.getPiece(curPos).getColor();
		int row = curPos.getRow();
		int col = curPos.getCol();
		int[] moveRow = { -1, 1, -2, 2 };
		int[] moveCol = { -1, 1, -2, 2 };

		for (int i : moveRow) {
			for (int j : moveCol) {
				if (Math.abs(i) == Math.abs(j)) {
					continue;
				}

				if (onBoard(row + i, col + j)) {
					Piece piece = state.getPiece(row + i, col + j);
					if (piece != null && piece.getColor() != color) {
						res.add(new Position(row + i, col + j));
					}

					if (piece == null)
						res.add(new Position(row + i, col + j));
				}
			}
		}

		return res;
	}
}
