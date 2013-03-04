package org.zhihanli.hw3;

import org.shared.chess.Color;
import static org.shared.chess.Color.*;

import org.shared.chess.GameResult;
import org.shared.chess.Piece;
import org.shared.chess.PieceKind;
import org.zhihanli.hw3.Presenter.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

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
	private Image[][] board = new Image[8][8];
	private Image[] promotionBoard = new Image[4];

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

		for (int row = 0; row < 4; row++) {
			final Image image = new Image();
			promotionBoard[row] = image;
		}

		promotionGrid.setBorderWidth(0);
		promotionGrid.resize(4, 1);

	}

	@Override
	public HasClickHandlers getCellOnChessBoard(int row, int col) {
		return board[row][col];
	}

	@Override
	public HasClickHandlers getCellOnPromotionBoard(int row) {
		return promotionBoard[row];
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
		// TODO
		Image image = board[row][col];

		if (row % 2 == 0 && col % 2 == 1 || row % 2 == 1 && col % 2 == 0) {
			image.setResource(gameImages.blackTile());
		} else {
			image.setResource(gameImages.whiteTile());
		}
		if (piece == null)
			return;

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
		gameStatus.setText(color.toString());
	}

	@Override
	public void setGameResult(GameResult gameResult) {
		// TODO
		if (gameResult != null)
			gameStatus.setText(gameResult.toString());
	}
}
