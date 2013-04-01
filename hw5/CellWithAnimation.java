package org.zhihanli.hw5;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.ui.Widget;

public class CellWithAnimation extends Animation {
	private Widget widget;
	private boolean isAppear;

	public CellWithAnimation(Widget widget, boolean isAppear) {
		this.widget = widget;
		this.isAppear = isAppear;
	}

	@Override
	protected void onUpdate(double progress) {
		double opacity = isAppear ? progress : 1 - progress;

		widget.getElement().getStyle().setOpacity(opacity);
	}
}