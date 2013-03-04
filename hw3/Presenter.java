package org.zhihanli.hw3;

import org.shared.chess.Color;
import static org.shared.chess.Color.*;
import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.State;
import org.shared.chess.Position;
import org.shared.chess.Move;
import static org.shared.chess.PieceKind.*;

import java.util.ArrayList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;

import org.zhihanli.hw2.StateChangerImpl;

import com.google.gwt.user.client.Window;

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
		
		HasClickHandlers getCellOnChessBoard(int row,int col);
		HasClickHandlers getCellOnPromotionBoard(int row);
		
		void showPromotionGrid();
		void hidePromotionGrid();
		
	}

	private View view;
	private StateChangerImpl stateChanger = new StateChangerImpl();
	private Position selectPos = null;
	private Position prevPos = null;
	private State state = new State();
	private PieceKind promoteTo = null;
	private int moveCount = 0;
	private ArrayList<State> stateRecord = new ArrayList<State>();

	public Presenter(View view) {
		setView(view);
		setState(state);
		bind();
		stateRecord.add(state.copy());
		History.newItem("move" + moveCount);
		moveCount++;
	}

	public void setView(View view) {
		this.view = view;
	}

	public void setState(State state) {
		view.setWhoseTurn(state.getTurn());
		view.setGameResult(state.getGameResult());
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				view.setPiece(r, c, state.getPiece(r, c));
			}
		}
	}
	
	public void bind(){
		for(int row=0;row<State.ROWS;row++){
			for(int col=0;col<State.COLS;col++){
				final int r=row;
				final int c=col;
				view.getCellOnChessBoard(row, col).addClickHandler(new ClickHandler(){
					public void onClick(ClickEvent event){
						clickBoard(r,c);
					}
				});
			}
		}
		
		for(int row=0;row<4;row++){
			final int r=row;
			view.getCellOnPromotionBoard(r).addClickHandler(new ClickHandler(){
				public void onClick(ClickEvent event){
					clickPromotionBoard(r);
				}
			});
		}
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				String historyToken = event.getValue();

				// Parse the history token
				try {
					int len=historyToken.length();
					if (historyToken.substring(0, 4).equals("move")) {
						String moveIndexToken = historyToken.substring(4, len);
						int moveIndex = Integer.parseInt(moveIndexToken);
						setState(stateRecord.get(moveIndex));
					} else {
						Window.alert("else");
						setState(stateRecord.get(moveCount));
					}
				} catch (IndexOutOfBoundsException e) {
					setState(stateRecord.get(0));
				}
			}
		});

	}

	public void clickBoard(int row, int col) {
		Position newPos = new Position(row, col);

		if (selectPos == null) {

		} else {
			// TODO: promotion
			Move move = getMove(selectPos, newPos);
			stateChange(move);

			// unHighLight
			view.setHighlighted(true, selectPos.getRow(), selectPos.getCol(),
					false);
			prevPos = new Position(selectPos.getRow(), selectPos.getCol());
		}
		selectPos = new Position(row, col);
		view.setHighlighted(true, selectPos.getRow(), selectPos.getCol(), true);
	}

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
		Window.alert(promoteTo.toString());
		view.setHighlighted(false, row, 0, true);
		stateChange(new Move(prevPos, selectPos, promoteTo));
		view.hidePromotionGrid();
	}

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

	public void stateChange(Move move) {
		try {
			stateChanger.makeMove(state, move);
		} catch (Exception e) {
			// Window.alert(e.getMessage());
			return;
		}
		setState(state);
		stateRecord.add(state.copy());
		History.newItem("move" + moveCount);
		moveCount++;
	}

}
