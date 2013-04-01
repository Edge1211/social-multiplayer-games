package org.zhihanli.hw3;

import org.shared.chess.State;
import org.zhihanli.hw3.Graphics;
import org.zhihanli.hw3.Presenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;

public class ChessEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		final Graphics graphics = new Graphics();

		Presenter presenter = new Presenter();
		presenter.init(graphics);

		if (!History.getToken().isEmpty()) {
			presenter.setState(presenter.deserialize(History.getToken()));
		} else {
//			presenter.setState(new State());
		}

		final Presenter p = presenter;
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				String historyToken = event.getValue();

				// Parse the history token
				if (historyToken != null) {
					String stateToken = historyToken;
					p.reset();
					p.setState(p.deserialize(stateToken));
				} else {
					p.reset();
					p.setState(new State());
				}
			}
		});

		RootPanel.get().add(graphics);
	}
}
