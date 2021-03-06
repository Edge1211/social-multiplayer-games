package org.zhihanli.hw3;

import org.shared.chess.Color;
import static org.shared.chess.Color.*;
import static org.zhihanli.hw5.Situation.EAT;
import static org.zhihanli.hw5.Situation.END_OF_GAME;
import static org.zhihanli.hw5.Situation.SELECT;

import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.zhihanli.hw3.Presenter.View;
import org.zhihanli.hw5.AudioControl;
import org.zhihanli.hw5.CellWithAnimation;
import org.zhihanli.hw5.Situation;
import org.zhihanli.hw8.GameMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDragHandlers;
import com.google.gwt.event.dom.client.HasDragOverHandlers;
import com.google.gwt.event.dom.client.HasDropHandlers;
import com.google.gwt.media.client.Audio;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.History;

public class Graphics extends Composite implements View {
	private static GameImages gameImages = GWT.create(GameImages.class);
	private static GraphicsUiBinder uiBinder = GWT
			.create(GraphicsUiBinder.class);

	interface GraphicsUiBinder extends UiBinder<Widget, Graphics> {
	}

	@UiField
	Button save;
	@UiField
	Button load;
	@UiField
	Button match;

	@UiField
	Button restart;
	@UiField
	Button clearAll;
	@UiField
	Button delete;
	@UiField
	GameCss css;
	@UiField
	Label gameStatus;
	@UiField
	Label playersInfo;
	@UiField
	Grid gameGrid;
	@UiField
	Grid promotionGrid;
//	@UiField
//	ListBox saveList;
	@UiField
	Button disconnect;
	@UiField
	ListBox matchList;
	@UiField
	TextBox emailInput;
	@UiField
	Button newMatch;
	@UiField
	Button deleteMatch;
	@UiField
	Label currentPlayer;
	@UiField
	Label rank;

	public @UiFactory
	ListBox makeMultipleListBox() {
		return new ListBox(true);
	}

	private Image[][] board = new Image[8][8];
	private Image[] promotionBoard = new Image[4];

	private Timer timer = null;
	GameMessages messages = (GameMessages) GWT.create(GameMessages.class);

	public Graphics() {
		initWidget(uiBinder.createAndBindUi(this));
		gameGrid.resize(8, 8);
		gameGrid.setCellPadding(0);
		gameGrid.setCellSpacing(0);
		gameGrid.setBorderWidth(0);
		gameGrid.setWidth("50px");

		restart.setText("Start a new game");
		save.setText("Save game");
		load.setText("Load game");
		clearAll.setText("Delete all saved games");

		// delete.setText(messages.setDeleteMatchLabel());
		match.setText(messages.setAutoMatchLabel());
		disconnect.setText("close connection");
		disconnect.setVisible(false);

		

		emailInput.setWidth("150px");
		matchList.setVisibleItemCount(1);
		newMatch.setText(messages.setStartGameLabel());
		deleteMatch.setText(messages.setDeleteMatchLabel());
		// matchList.setWidth("1000px");

		restart.setVisible(false);
		load.setVisible(false);
//		saveList.setVisible(false);
		save.setVisible(false);
		clearAll.setVisible(false);
		delete.setVisible(false);

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				final Image image = new Image();
				board[row][col] = image;
				image.setWidth("100%");
				image.setResource(gameImages.empty());
				image.getElement().setDraggable(Element.DRAGGABLE_TRUE);
				gameGrid.setWidget(row, col, image);
			}
		}

		for (int row = 0; row < 4; row++) {
			final Image image = new Image();
			promotionBoard[row] = image;
		}

		promotionGrid.setBorderWidth(0);
		promotionGrid.resize(4, 1);
	}

	@Override
	public HasClickHandlers getAutoMatchButton() {

		return match;
	}

	@Override
	public HasClickHandlers getDisconnectButton() {

		return disconnect;
	}

	@Override
	public HasClickHandlers getNewMatchButton() {

		return newMatch;
	}

	@Override
	public HasClickHandlers getCellOnChessBoard(int row, int col) {
		return board[7 - row][col];
	}

	@Override
	public HasDragHandlers getDragCellOnChessBoard(int row, int col) {
		return getCell(row, col);
	}

	@Override
	public HasDropHandlers getDropCellOnChessBoard(int row, int col) {
		return getCell(row, col);
	}

	@Override
	public HasDragOverHandlers getDragOverCellOnChessBoard(int row, int col) {
		return getCell(row, col);
	}

//	@Override
//	public HasChangeHandlers getSaveList() {
//		return saveList;
//	}

	@Override
	public HasChangeHandlers getMatchList() {
		return matchList;
	}

	@Override
	public HasClickHandlers getDeleteMatchButton() {
		return deleteMatch;
	}

//	@Override
//	public void setSaveList(String save) {
//		saveList.addItem(save);

//	}

	@Override
	public void setMatchList(String match) {
		matchList.addItem(match);
		matchList.setVisibleItemCount(1);
	}

	@Override
	public void clearMatchList() {
		matchList.clear();
	}

//	@Override
//	public String getSelection() {
//		int idx = saveList.getSelectedIndex();
//		if (idx == -1)
//			return null;
//		return saveList.getItemText(idx);
//	}

	@Override
	public String getSelectionFromMatchList() {
		int idx = matchList.getSelectedIndex();
		if (idx == -1)
			return null;
		return matchList.getItemText(idx);
	}

	public Image getCell(int row, int col) {
		return board[7 - row][col];
	}

	@Override
	public void cellAppearAndDisappear(int row, int col, boolean isAppear,
			int duration) {
		CellWithAnimation cellAnimation = new CellWithAnimation(getCell(row,
				col), isAppear);

		cellAnimation.run(duration);
	}

	@Override
	public void setPlayersInfo(String info) {
		playersInfo.setText(info);
	}

	@Override
	public HasClickHandlers getCellOnPromotionBoard(int row) {
		return promotionBoard[row];
	}

	@Override
	public HasClickHandlers getRestartButton() {
		return restart;
	}

//	@Override
//	public HasClickHandlers getSaveButton() {
//		return save;
//	}

	@Override
	public HasClickHandlers getLoadButton() {
		return load;
	}

	@Override
	public HasClickHandlers getClearAllButton() {
		return clearAll;
	}

	@Override
	public HasClickHandlers getDeleteButton() {
		return delete;
	}

//	@Override
//	public void clearSaveList() {
//		saveList.clear();
//	}

	@Override
	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	@Override
	public Timer getTimer() {
		return timer;
	}

	@Override
	public void hidePromotionGrid() {
		DOM.setStyleAttribute(promotionGrid.getElement(), "visibility",
				"hidden");
	}

	@Override
	public void showPromotionGrid() {
		DOM.setStyleAttribute(promotionGrid.getElement(), "visibility", "");
	}

	@Override
	public void setPromotionGrid(Color turn) {
		if (turn == WHITE) {
			promotionBoard[0].setResource(gameImages.whiteQueen());
		} else {
			promotionBoard[0].setResource(gameImages.blackQueen());
		}
		if (turn == WHITE) {
			promotionBoard[1].setResource(gameImages.whiteRook());
		} else {
			promotionBoard[1].setResource(gameImages.blackRook());
		}
		if (turn == WHITE) {
			promotionBoard[2].setResource(gameImages.whiteBishop());
		} else {
			promotionBoard[2].setResource(gameImages.blackBishop());
		}
		if (turn == WHITE) {
			promotionBoard[3].setResource(gameImages.whiteKnight());

		} else {
			promotionBoard[3].setResource(gameImages.blackKnight());
		}
		for (int i = 0; i < 4; i++) {
			promotionGrid.setWidget(i, 0, promotionBoard[i]);
		}
	}

	@Override
	public void setPiece(int row, int col, Piece piece) {

		Image image = board[7 - row][col];

		if (piece == null) {
			image.setResource(gameImages.empty());
			// setDraggable(row,col,false);
			return;
		}

		// setDraggable(row,col,true);
		PieceKind kind = piece.getKind();
		Color color = piece.getColor();
		switch (kind) {
		case KING:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackKing());
			} else {
				image.setResource(gameImages.whiteKing());
			}
			break;
		case BISHOP:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackBishop());
			} else {
				image.setResource(gameImages.whiteBishop());
			}
			break;
		case KNIGHT:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackKnight());
			} else {
				image.setResource(gameImages.whiteKnight());
			}
			break;
		case PAWN:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackPawn());
			} else {
				image.setResource(gameImages.whitePawn());
			}
			break;
		case QUEEN:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackQueen());
			} else {
				image.setResource(gameImages.whiteQueen());
			}
			break;
		case ROOK:
			if (color == Color.BLACK) {
				image.setResource(gameImages.blackRook());
			} else {
				image.setResource(gameImages.whiteRook());
			}
			break;
		}
	}

	@Override
	public void setHighlighted(boolean isChessBoard, int row, int col,
			boolean highlighted) {
		Element element;
		if (isChessBoard) {
			element = board[7 - row][col].getElement();
		} else {
			element = promotionBoard[row].getElement();
		}
		if (highlighted) {
			element.setClassName(css.highlighted());
		} else {
			element.removeClassName(css.highlighted());
		}
	}

	@Override
	public void setWhoseTurn(Color color) {

		if (color == null) {
			gameStatus.setText(messages.setWaitingStatus());
		} else {
			String turn = color == WHITE ? messages.colorWhite() : messages
					.colorBlack();
			gameStatus.setText(turn + messages.toPlay());
		}
	}

	@Override
	public void setGameResult(GameResult gameResult) {

		if (gameResult != null) {

			String winner = gameResult.getWinner() == WHITE ? messages
					.colorWhite() : messages.colorBlack();
			// Window.alert(gameResult.getWinner().toString() + " " +
			// winner+" in set");
			String reason = null;
			switch (gameResult.getGameResultReason()) {
			case CHECKMATE:
				reason = messages.checkMate();
				break;

			case FIFTY_MOVE_RULE:
				reason = messages.fiftyMoveRule();
				break;
			case THREEFOLD_REPETITION_RULE:
				reason = messages.threeFoldRepetitionRule();
				break;
			case STALEMATE:
				reason = messages.stalemate();
				break;

			}
			if (gameResult.getWinner() == null)
				gameStatus.setText(messages.gameEnd() + " " + reason);
			else
				gameStatus.setText(messages.gameEnd() + " " + winner + " "
						+ messages.win() + " " + reason);
		}
	}

	@Override
	public void setOpacity(int row, int col, double value) {
		getCell(row, col).getElement().getStyle().setOpacity(value);
	}

	@Override
	public void setDraggable(int row, int col, boolean dragable) {
		if (dragable) {
			getCell(row, col).getElement().setDraggable(
					"Element.DRAGGABLE_TRUE");
		} else {
			getCell(row, col).getElement().setDraggable(
					"Element.DRAGGABLE_FALSE");

		}
	}

	@Override
	public void addHistoryItem(String record) {
		History.newItem(record);
	}

	public void playSound(Situation situation) {
		Audio audio = null;
		switch (situation) {
		case EAT:
			audio = AudioControl.createAudio(EAT);
			break;
		case END_OF_GAME:
			audio = AudioControl.createAudio(END_OF_GAME);
			break;
		case SELECT:
			audio = AudioControl.createAudio(SELECT);
			break;
		}

		if (audio != null)
			audio.play();
	}

	@Override
	public String getEmailInput() {
		return emailInput.getText();
	}

	@Override
	public void setButtons(boolean visible) {
		match.setVisible(visible);
		// disconnect.setVisible(visible);
		matchList.setVisible(visible);

		emailInput.setVisible(visible);
		newMatch.setVisible(visible);

		deleteMatch.setVisible(visible);

	}

	@Override
	public void setCurrentPlayer(String player) {
		currentPlayer.setText(player);
	}

	@Override
	public void setRank(String rankRange) {
		rank.setText(rankRange);
		// rank.setText("sldakf");
	}
}
