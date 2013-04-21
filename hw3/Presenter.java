package org.zhihanli.hw3;

import org.zhihanli.hw5.Situation;
import static org.zhihanli.hw5.Situation.*;

import org.zhihanli.hw6.client.ChessClient;
import org.zhihanli.hw6.client.MoveSerializer;
import org.shared.chess.Color;
import static org.shared.chess.Color.*;

import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.State;
import org.shared.chess.Position;
import org.shared.chess.Move;
import static org.shared.chess.GameResultReason.*;
import static org.shared.chess.PieceKind.*;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DragEvent;
import com.google.gwt.event.dom.client.DragHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDragHandlers;
import com.google.gwt.event.dom.client.HasDragOverHandlers;
import com.google.gwt.event.dom.client.HasDropHandlers;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

import org.zhihanli.hw2.StateChangerImpl;
import org.zhihanli.hw2_5.StateExplorerImpl;

public class Presenter {
	public interface View {
		/**
		 * Renders the piece at this position. If piece is null then the
		 * position is empty.
		 */
		void setPiece(int row, int col, Piece piece);

		/**
		 * Turns the highlighting on or off at this cell. Cells that can be
		 * clicked should be highlighted.
		 */
		void setHighlighted(boolean isChessBoard, int row, int col,
				boolean highlighted);

		/**
		 * Indicate whose turn it is.
		 */
		void setWhoseTurn(Color color);

		/**
		 * Indicate whether the game is in progress or over.
		 */
		void setGameResult(GameResult gameResult);

		void setPromotionGrid(Color turn);

		HasClickHandlers getCellOnChessBoard(int row, int col);

		HasDragHandlers getDragCellOnChessBoard(int row, int col);

		HasDragOverHandlers getDragOverCellOnChessBoard(int row, int col);

		HasDropHandlers getDropCellOnChessBoard(int row, int col);

		HasClickHandlers getCellOnPromotionBoard(int row);

		HasClickHandlers getRestartButton();

		// HasClickHandlers getSaveButton();

		HasClickHandlers getLoadButton();

		// HasChangeHandlers getSaveList();

		HasChangeHandlers getMatchList();

		HasClickHandlers getClearAllButton();

		HasClickHandlers getDeleteButton();

		HasClickHandlers getAutoMatchButton();

		HasClickHandlers getDisconnectButton();

		HasClickHandlers getNewMatchButton();

		HasClickHandlers getDeleteMatchButton();

//		String getSelection();

		String getSelectionFromMatchList();

		String getEmailInput();

		void showPromotionGrid();

		void hidePromotionGrid();

		void cellAppearAndDisappear(int row, int col, boolean isAppear,
				int duration);

		void setOpacity(int row, int col, double value);

		void setDraggable(int row, int col, boolean dragable);

		// void setSaveList(String save);

		void setMatchList(String match);

		void setPlayersInfo(String info);

		// void clearSaveList();

		void clearMatchList();

		void addHistoryItem(String record);

		void playSound(Situation situation);

		void setTimer(Timer timer);

		Timer getTimer();

		void setButtons(boolean visible);

		void setCurrentPlayer(String player);

		void setRank(String rankRange);
	}

	private View view;
	private StateChangerImpl stateChanger = new StateChangerImpl();
	private StateExplorerImpl stateExplorer = new StateExplorerImpl();

	// current select cell
	private Position selectPos = null;
	// previous select cell
	private Position prevPos = null;
	// maintain game state
	private State state;
	// promotion kind
	private PieceKind promoteTo = null;
	// store the current move
	private Move move = null;
	// store the drag from position
	private Position dragFrom = null;
	// store the drag to position(drop) position
	private Position dragTo = null;
	// indicate whether this is a drag action(distinguish with click)
	private boolean inDrag = false;
	// store the record to load
	private String load = null;
	// indicate whether capture happened(play according sound)
	private boolean captureHappened = false;

	private boolean playing = false;

	private ChessClient chessClient = new ChessClient(this);

	private boolean isMyTurn;

	/**
	 * Initialize function
	 * 
	 * @param view
	 */
	public void init(View view) {
		setView(view);
		bind();
		// updateSaveList();

		chessClient.login();
		checkConnection();
	}

	public void setWaitingStatus() {
		view.setWhoseTurn(null);
	}

	public void setPlayersInfo(String info) {
		view.setPlayersInfo(info);

	}

	/**
	 * update the saved games list, due to storage change
	 */
	// public void updateSaveList() {
	// view.clearSaveList();
	// Storage storage = Storage.getLocalStorageIfSupported();
	// if (storage != null) {
	// for (int i = 0; i < storage.getLength(); i++) {
	// view.setSaveList(storage.key(i));
	// }
	// }

	// }

	public void setButtons(boolean visible) {
		view.setButtons(visible);
	}

	public void updateMatchList(List<String> matchList) {
		if (matchList != null) {
			view.clearMatchList();
			for (String s : matchList) {
				view.setMatchList(s);
			}
		}
	}

	public void addMatch(String match) {
		view.setMatchList(match);
	}

	public void setView(View view) {
		this.view = view;
	}

	public void setState(State state) {
		playing = true;
		this.state = state;
		setAllNonTransparent();
		view.setWhoseTurn(state.getTurn());
		view.setGameResult(state.getGameResult());

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setPiece(r, c, state.getPiece(r, c));
				view.setHighlighted(true, r, c, false);
			}
		}
		// view.addHistoryItem(serialize(state));
	}

	public void clearBoard() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setPiece(r, c, null);
				view.setHighlighted(true, r, c, false);
			}
		}
	}

	/**
	 * set all cells to non-transparent
	 */
	private void setAllNonTransparent() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setOpacity(r, c, 1.0);
			}
		}
	}

	/**
	 * function to do animations for chess move
	 */
	private void moveAnimation(Move move) {
		if (move == null)
			return;
		Position from = move.getFrom();
		Position to = move.getTo();
		Piece piece = state.getPiece(to);
		// Animation to disappear the from piece
		view.cellAppearAndDisappear(from.getRow(), from.getCol(), false, 1000);

		int row = from.getRow();
		int col = from.getCol();
		if (piece.getKind() != KNIGHT) {
			boolean inDiag = (Math.abs(from.getRow() - to.getRow()) == Math
					.abs(from.getCol() - to.getCol()));
			boolean inSameRow = from.getRow() == to.getRow();
			boolean inSameCol = from.getCol() == to.getCol();
			int moveRow = 0;
			int moveCol = 0;
			if (inDiag) {
				moveRow = to.getRow() > from.getRow() ? 1 : -1;
				moveCol = to.getCol() > from.getCol() ? 1 : -1;
			} else if (inSameRow) {
				moveRow = 0;
				moveCol = to.getCol() > from.getCol() ? 1 : -1;
			} else if (inSameCol) {
				moveRow = to.getRow() > from.getRow() ? 1 : -1;
				moveCol = 0;
			}

			row += moveRow;
			col += moveCol;
			while (!(row == to.getRow() && col == to.getCol())) {
				// appear then disappear

				view.setPiece(row, col, piece);
				view.cellAppearAndDisappear(row, col, true, 1000);
				view.cellAppearAndDisappear(row, col, false, 1000);

				row += moveRow;
				col += moveCol;
			}
		}
		view.cellAppearAndDisappear(to.getRow(), to.getCol(), false, 1000);
		view.cellAppearAndDisappear(to.getRow(), to.getCol(), true, 1000);
		view.setPiece(to.getRow(), to.getCol(), piece);
	}

	/**
	 * bind the handlers to cells on game board and promotion board
	 */
	public void bind() {
		// bind handlers to cells on game board
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				final int r = row;
				final int c = col;

				view.getCellOnChessBoard(row, col).addClickHandler(
						new ClickHandler() {
							public void onClick(ClickEvent event) {
								clickBoard(r, c);
							}
						});

				view.getDragCellOnChessBoard(row, col).addDragHandler(
						new DragHandler() {
							@Override
							public void onDrag(DragEvent event) {
								if (!playing)
									return;
								dragFrom = new Position(r, c);
								inDrag = true;
								if (selectPos != null)
									view.setHighlighted(true,
											selectPos.getRow(),
											selectPos.getCol(), false);
								highLightPossibleMove(state, dragFrom, true);
							}
						});

				view.getDragOverCellOnChessBoard(r, c).addDragOverHandler(
						new DragOverHandler() {
							@Override
							public void onDragOver(DragOverEvent event) {

							}
						});

				view.getDropCellOnChessBoard(row, col).addDropHandler(
						new DropHandler() {
							@Override
							public void onDrop(DropEvent event) {
								if (!playing || !isMyTurn)
									return;
								drop(r, c);
							}
						});

				// view.getClearAllButton().addClickHandler(new ClickHandler() {
				// public void onClick(ClickEvent event) {
				// Storage storage = Storage.getLocalStorageIfSupported();
				// if (storage != null) {
				// storage.clear();
				// view.clearSaveList();
				// }
				// }
				// });

				// view.getDeleteButton().addClickHandler(new ClickHandler() {
				// public void onClick(ClickEvent event) {
				// if (load != null) {
				// Storage storage = Storage
				// .getLocalStorageIfSupported();
				// if (storage != null) {
				// storage.removeItem(load);
				// updateSaveList();
				// }

				// }
				// }
				// });

			}
		}

		// bind handlers to cells on promotion board
		for (int row = 0; row < 4; row++) {
			final int r = row;
			view.getCellOnPromotionBoard(r).addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent event) {
					clickPromotionBoard(r);
				}
			});
		}

		// bind handler to restart button
		view.getRestartButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				reset();
				setState(new State());
			}
		});

		// bind handler to save button
		// view.getSaveButton().addClickHandler(new ClickHandler() {
		// public void onClick(ClickEvent event) {
		// save();
		// }
		// });

		// bind handler to load button
		view.getLoadButton().addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				load();
			}
		});

		// bind handler to save button
		// view.getSaveList().addChangeHandler(new ChangeHandler() {
		// public void onChange(ChangeEvent event) {
		// load = view.getSelection();
		// }
		// });

		view.getMatchList().addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				// TODO: HANDLER
				String match = view.getSelectionFromMatchList();
				String id = match.split(" ")[2];
				// send id to server
				chessClient.loadStateWithMatchId(new Long(id), false);

			}
		});

		view.getNewMatchButton().addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				if (view.getEmailInput() != null) {
					chessClient.sendNewMatch(view.getEmailInput(),
							chessClient.getEmail());
				}
			}
		});

		view.setTimer(new Timer() {
			public void run() {
				setState(state);
			}
		});

		view.getAutoMatchButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				setWaitingStatus();
				clearBoard();

				chessClient.sendAutoMatchRequest();
			}
		});

		view.getDisconnectButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				chessClient.closeSocket();
			}
		});

		view.getDeleteMatchButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String match = view.getSelectionFromMatchList();
				String id = match.split(" ")[2];
				clearBoard();
				setWaitingStatus();
				// delete selected match from current player's list
				chessClient.deleteMatchFromPlayer(chessClient.getEmail(),
						new Long(id));
			}
		});

	}

	/**
	 * To reset flags, when a saved game record(state) is loaded
	 */
	public void reset() {
		selectPos = null;
		prevPos = null;
		promoteTo = null;
		move = null;
		dragFrom = null;
		dragTo = null;
		inDrag = false;
		load = null;
		captureHappened = false;
	}

	/**
	 * Handler for drop action to cell(row,col)
	 * 
	 * @param row
	 *            row
	 * @param col
	 *            column
	 */
	public void drop(int row, int col) {
		view.setHighlighted(true, row, col, true);
		dragTo = new Position(row, col);
		move = getMove(dragFrom, dragTo);
		stateChange(move, true);
	}

	public void setMyTurn(boolean isMyTurn) {
		this.isMyTurn = isMyTurn;
	}

	/**
	 * Handler for click action to cell(row,col)
	 * 
	 * @param row
	 * @param col
	 */
	public void clickBoard(int row, int col) {
		if (!playing || !isMyTurn)
			return;
		Position newPos = new Position(row, col);

		// play select sound
		view.playSound(SELECT);

		if (selectPos != null) {
			// generate a move
			move = getMove(selectPos, newPos);
			// try to change the state
			stateChange(move, true);

			// unHighLight
			clearHighLight();
			view.setHighlighted(true, selectPos.getRow(), selectPos.getCol(),
					false);

			prevPos = new Position(selectPos.getRow(), selectPos.getCol());
		}

		selectPos = new Position(row, col);
		highLightPossibleMove(state, selectPos, true);
		view.setHighlighted(true, selectPos.getRow(), selectPos.getCol(), true);
	}

	/**
	 * To set the every cell on chess board to unhighlighted
	 */
	private void clearHighLight() {
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setHighlighted(true, r, c, false);
			}
		}

	}

	/**
	 * To highlight possible moves
	 * 
	 * @param state
	 *            current state
	 * @param pos
	 *            piece position
	 * @param highlighted
	 *            To highlight to unhighlight
	 */
	public void highLightPossibleMove(State state, Position pos,
			boolean highlighted) {
		if (pos == null || state.getPiece(pos) == null)
			return;
		setAllNonTransparent();
		clearHighLight();

		if ((state.getPiece(pos).getColor() != state.getTurn()))
			return;

		Set<Move> posSet = stateExplorer.getPossibleMovesFromPosition(state,
				pos);

		for (Move m : posSet) {
			view.setHighlighted(true, m.getTo().getRow(), m.getTo().getCol(),
					highlighted);
		}
	}

	/**
	 * handler of click on promotion board
	 * 
	 * @param row
	 */
	public void clickPromotionBoard(int row) {
		switch (row) {
		case 0:
			promoteTo = QUEEN;
			break;
		case 1:
			promoteTo = ROOK;
			break;
		case 2:
			promoteTo = BISHOP;
			break;
		case 3:
			promoteTo = KNIGHT;
			break;
		}
		stateChange(new Move(prevPos, selectPos, promoteTo), true);
		view.hidePromotionGrid();
	}

	/**
	 * given two position clicked return a Move
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public Move getMove(Position from, Position to) {
		Color turn = state.getTurn();
		int lastRow = turn == WHITE ? 7 : 0;
		if (to.getRow() == lastRow) {
			// promotion
			try {
				State temp = state.copy();
				stateChanger.makeMove(temp, new Move(from, to, ROOK));
			} catch (Exception e) {
				return new Move(from, to, null);
			}
			view.setPromotionGrid(turn);
			view.showPromotionGrid();
			return new Move(from, to, null);
		} else {
			view.hidePromotionGrid();
			return new Move(from, to, null);
		}
	}

	/**
	 * given a move, try to change the state
	 * 
	 * @param move
	 * @return return true if legal move
	 */
	public boolean stateChange(Move move, boolean isMyMove) {
		if (state == null)
			state = new State();
		State stateCopy = state.copy();
		try {
			stateChanger.makeMove(state, move);
		} catch (Exception e) {
			inDrag = false;
			return false;
		}

		captureHappened = stateCopy.getPiece(move.getTo()) != null;
		// patch for enpassant catch
		Position enpPos = stateCopy.getEnpassantPosition();
		int rowOffset = stateCopy.getTurn() == WHITE ? 1 : -1;
		if (stateCopy.getPiece(move.getFrom()).getKind() == PAWN
				&& enpPos != null
				&& move.getTo().equals(
						new Position(enpPos.getRow() + rowOffset, enpPos
								.getCol())))
			captureHappened = true;

		if (captureHappened)
			view.playSound(EAT);
		if (state.getGameResult() != null)
			view.playSound(END_OF_GAME);

		if (!inDrag) {
			// Execute the timer to expire 0.9 seconds in the future
			if (view.getTimer() != null) {
				view.getTimer().schedule(900);
			} else {
				setState(state);
			}

			moveAnimation(move);
		} else {
			setState(state);
		}

		if (isMyMove) {
			String time = saveMove(move);
			chessClient.sendMoveToServer(move, time, false);
			isMyTurn = false;
			// chessClient.login();
			// if (token != null)
			// login(token);
		} else {
			isMyTurn = true;
		}
		inDrag = false;
		return true;
	}

	/**
	 * To perform save action: save a new record to local storage,key is the
	 * current time
	 */
	public void save() {
		Storage storage = null;
		String save = null;
		storage = Storage.getLocalStorageIfSupported();
		if (storage == null) {
			Window.alert("Sorry, your browser doesn't support this feature");
			return;
		} else {
			Date date = new Date();
			DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMddHHmmss");
			save = dtf.format(date, TimeZone.createTimeZone(0));
			storage.setItem(save, serialize(state));
		}
//		view.setSaveList(save);
	}

	/**
	 * To load a game from user selection
	 */
	public void load() {
		Storage storage = null;
		storage = Storage.getLocalStorageIfSupported();
		if (storage == null) {
			Window.alert("Sorry, your browser doesn't support this feature");
			return;
		} else {
			if (load != null) {
				String stateString = storage.getItem(load);
				if (stateString != null)
					setState(deserialize(stateString));
			}
		}
	}

	/**
	 * serialize function to store state
	 * 
	 * @param state
	 * @return serialized string of state
	 */
	public String serialize(State state) {
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

	public State deserialize(String string) {
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

		Color winner = buf[70].split(" ")[0].equals("WHITE") ? WHITE : BLACK;
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
	private Piece stringToPiece(String s) {
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

	public void setCurrentPlayer(String player) {
		view.setCurrentPlayer(player);
	}

	public void setRank(String rankRange) {
		view.setRank(rankRange);
	}

	private String saveMove(Move move) {

		Storage storage = Storage.getLocalStorageIfSupported();
		if (storage == null) {
			Window.alert("Sorry, your browser doesn't support local storage");
			return null;
		} else {
			Date date = new Date();
			DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMddHHmmss");
			String time = dtf.format(date, TimeZone.createTimeZone(0));
			storage.setItem(time, MoveSerializer.moveToString(move));
			return time;
		}
	}

	public void deleteMove(String time) {
		Storage storage = Storage.getLocalStorageIfSupported();
		if (storage != null) {
			storage.removeItem(time);
		}
	}

	public void checkConnection() {
		Timer timer = new Timer() {
			public void run() {
				/**
				 * check if there is any move in local storage
				 */
				Storage storage = Storage.getLocalStorageIfSupported();
				if (storage != null) {
					if (storage.getLength() > 0) {
						String moveString = storage.getItem(storage.key(0));
						chessClient.sendMoveToServer(
								MoveSerializer.stringToMove(moveString),
								storage.key(0), true);
						chessClient.refreshCurrentState();

					}

				}

			}
		};

		timer.scheduleRepeating(10000);

	}

}