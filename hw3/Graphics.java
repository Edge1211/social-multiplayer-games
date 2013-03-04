package org.zhihanli.hw3;

import org.shared.chess.Color;
import static org.shared.chess.Color.*;

import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.shared.chess.State;

import static org.shared.chess.PieceKind.*;
import org.zhihanli.hw3.Presenter.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;

public class Graphics extends Composite implements View {
	private static GameImages gameImages = GWT.create(GameImages.class);
	private static GraphicsUiBinder uiBinder = GWT
			.create(GraphicsUiBinder.class);

	interface GraphicsUiBinder extends UiBinder<Widget, Graphics> {
	}

	@UiField
	GameCss css;
	@UiField
	Label gameStatus;
	@UiField
	Grid gameGrid;
	@UiField
	Grid promotionGrid;
	@UiField
	Label gameTurn;
	private Image[][] board = new Image[8][8];
	private Image[] promotionBoard = new Image[4];
	private Presenter presenter;

	public Graphics() {
		initWidget(uiBinder.createAndBindUi(this));
		gameGrid.resize(8, 8);
		gameGrid.setCellPadding(0);
		gameGrid.setCellSpacing(0);
		gameGrid.setBorderWidth(0);

		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {
				final Image image = new Image();
				board[row][col] = image;
				image.setWidth("100%");
				if (row % 2 == 0 && col % 2 == 1 || row % 2 == 1
						&& col % 2 == 0) {
					image.setResource(gameImages.blackTile());
				} else {
					image.setResource(gameImages.whiteTile());
				}
				gameGrid.setWidget(row, col, image);
			}
		}
		gameStatus.setText("Zhihan Li's chess");
		presenter = new Presenter(this);

		promotionGrid.setBorderWidth(0);
		promotionGrid.resize(4, 1);

	}

	public Presenter getPresenter() {
		return this.presenter;
	}

	@Override
	public void setPromotionGrid(boolean hiding, Color turn) {
		if (hiding) {
			DOM.setStyleAttribute(promotionGrid.getElement(), "visibility",
					"hidden");
			return;
		} else {
			DOM.setStyleAttribute(promotionGrid.getElement(), "visibility", "");

		}

		if (turn == WHITE) {
			final Image i = new Image();
			i.setResource(gameImages.whiteQueen());
			promotionBoard[0] = i;
		} else {
			final Image i = new Image();
			i.setResource(gameImages.blackQueen());
			promotionBoard[0] = i;
		}
		if (turn == WHITE) {
			final Image i = new Image();
			i.setResource(gameImages.whiteRook());
			promotionBoard[1] = i;
		} else {
			final Image i = new Image();
			i.setResource(gameImages.blackRook());
			promotionBoard[1] = i;
		}
		if (turn == WHITE) {
			final Image i = new Image();
			i.setResource(gameImages.whiteBishop());
			promotionBoard[2] = i;
		} else {
			final Image i = new Image();
			i.setResource(gameImages.blackBishop());
			promotionBoard[2] = i;
		}
		if (turn == WHITE) {
			final Image i = new Image();
			i.setResource(gameImages.whiteKnight());
			promotionBoard[3] = i;
		} else {
			final Image i = new Image();
			i.setResource(gameImages.blackKnight());
			promotionBoard[3] = i;
		}
		for (int i = 0; i < 4; i++) {
			promotionGrid.setWidget(i, 0, promotionBoard[i]);
		}
		for (int row = 0; row < 4; row++) {
			final int r = row;
			Image image = promotionBoard[row];
			image.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					// handle the click event
					presenter.clickPromotionBoard(r);
				}
			});
		}

	}

	public void addHandler() {
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				final int r = row;
				final int c = col;
				Image image = board[row][col];
				image.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						// handle the click event
						presenter.clickBoard(r, c);
					}
				});
			}
		}
	}

	@Override
	public void setPiece(int row, int col, Piece piece) {
		// TODO
		Image image = board[row][col];
		if (piece == null) {
			if (row % 2 == 0 && col % 2 == 1 || row % 2 == 1 && col % 2 == 0) {
				image.setResource(gameImages.blackTile());
			} else {
				image.setResource(gameImages.whiteTile());
			}
			return;
		}

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
			element = board[row][col].getElement();
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
		// TODO
		gameTurn.setText(color.toString());
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		// TODO
	}

	// @UiHandler("gameGrid")
	// void handleClick(SelectionChangeEvent<Grid> e) {
	//
	// Window.alert("Hello, UiBinder");
	// }
}
