package org.zhihanli.hw3;

import org.zhihanli.hw3.Graphics;
import org.zhihanli.hw3.Presenter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class ChessEntryPoint implements EntryPoint {
	@Override
	public void onModuleLoad() {
		final Graphics graphics = new Graphics();
	    Presenter presenter = new Presenter(graphics);
	    presenter.setView(graphics);
	    RootPanel.get().add(graphics);	    
	}
}
