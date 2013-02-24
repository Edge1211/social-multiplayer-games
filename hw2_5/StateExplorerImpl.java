package org.zhihanli.hw2_5;

import java.util.*;
import org.shared.chess.*;
import org.zhihanli.hw2.StateChangerImpl;
import static org.shared.chess.PieceKind.*;

import com.google.appengine.labs.repackaged.com.google.common.collect.Sets;

public class StateExplorerImpl implements StateExplorer {
	StateChangerImpl stateChanger;

	public StateExplorerImpl() {
		stateChanger = new StateChangerImpl();
	}

	@Override
	public Set<Move> getPossibleMoves(State state) {

		// TODO
		Set<Move> res=Sets.newHashSet();
		Set<Position> pos=getPossibleStartPositions(state);
		Iterator<Position> itr=pos.iterator();
		while(itr.hasNext()){
			res.addAll(getPossibleMovesFromPosition(state,itr.next()));
		}

		return res;
	}

	@Override
	public Set<Move> getPossibleMovesFromPosition(State state, Position start) {
		// TODO
		Set<Move> res = Sets.newHashSet();
		Set<Position> possibleMovePos = stateChanger.getLegalMovePosSet(state,
				start);
		Iterator<Position> itr = possibleMovePos.iterator();

		while (itr.hasNext()) {
			Position pos = itr.next();
			if (state.getPiece(start).getKind() == PAWN
					&& (pos.getRow() == 0 || pos.getRow() == 7)) {
				addPromotionMove(res, start, pos);
			} else {
				res.add(new Move(start, pos, null));
			}
		}

		return res;
	}

	private void addPromotionMove(Set<Move> set, Position start, Position target) {
		if (start != null && target != null) {
			set.add(new Move(start, target, BISHOP));
			set.add(new Move(start, target, QUEEN));
			set.add(new Move(start, target, ROOK));
			set.add(new Move(start, target, KNIGHT));
		}
	}

	@Override
	public Set<Position> getPossibleStartPositions(State state) {
		// TODO
		Set<Position> res = Sets.newHashSet();
		for (int row = 0; row < State.ROWS; row++) {
			for (int col = 0; col < State.COLS; col++) {
				if (state.getPiece(row, col) != null
						&& state.getPiece(row, col).getColor() == state
								.getTurn()) {
					Set<Position> possibleMovePos = stateChanger
							.getLegalMovePosSet(state, new Position(row, col));
					if (possibleMovePos.size() > 0)
						res.add(new Position(row, col));
				}
			}
		}

		return res;
	}
}
