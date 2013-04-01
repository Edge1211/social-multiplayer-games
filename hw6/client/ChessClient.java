package org.zhihanli.hw6.client;

import java.util.Date;

import org.shared.chess.Color;
import org.shared.chess.Move;
import org.shared.chess.State;
import org.zhihanli.hw3.Presenter;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.shared.GWT;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.TimeZone;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChessClient {

	private String userid = null;
	private String token = null;
	private String name = null;
	private ChessServiceAsync chessSvc = GWT.create(ChessService.class);
	private Presenter presenter;
	private Color myColor;

	public ChessClient(Presenter presenter) {
		Date date = new Date();
		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyyMMddHHmmss");
		this.presenter = presenter;
		userid = dtf.format(date, TimeZone.createTimeZone(0));

	}

	public void setColor(Color color) {
		myColor = color;
	}

	private void askForGoogleUserid() {

		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors. server down?
				Window.alert("Unable to connect to server--get google user info");
			}

			public void onSuccess(String result) {
				if (result != null) {
					name = result;
					presenter.setPlayersInfo(result);
					// login();
				}
				// Window.alert(result.toString());
			}
		};

		chessSvc.askForGoogleUserid(userid, callback);
	}

	/**
	 * Send move to server through RPC
	 * 
	 * @param move
	 */

	public void sendMoveToServer(Move move) {
		// Initialize the service proxy.
		if (chessSvc == null) {
			chessSvc = GWT.create(ChessService.class);
		}

		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors. server down?
				Window.alert("Unable to connect to server");
			}

			public void onSuccess(Boolean result) {
				// Window.alert(result.toString());
			}
		};

		chessSvc.sendMove(MoveSerializer.moveToString(move), userid, callback);

	}

	/**
	 * login to server, get token for channel service
	 */

	public void login() {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors. server down?
				Window.alert("Unable to connect to server- log in");
			}

			public void onSuccess(String result) {
				token = result;
				initChannel(token);
				// Window.alert(result);
				askForGoogleUserid();
			}
		};
		presenter.setWaitingStatus();
		chessSvc.login(userid, callback);
	}

	public void sendAutoMatchRequest() {

		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors. server down?
				Window.alert("Unable to connect to server-auto match request");
			}

			public void onSuccess(Boolean result) {

		//		Window.alert(result + " match request");
			}
		};
		chessSvc.autoMatch(callback);
	}

	private void initChannel(String token) {
		Channel channel = new ChannelFactoryImpl().createChannel(token);
		Socket socket = channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				Window.alert("Channel opened!");
				sendConnectedSignal();
			}

			@Override
			public void onMessage(String message) {
				// Window.alert("Received: " + message);
				parseMsg(message);
			}

			@Override
			public void onError(ChannelError error) {
				Window.alert("Channel error: " + error.getCode() + " : "
						+ error.getDescription());
			}

			@Override
			public void onClose() {
				Window.alert("Channel closed!");
			}
		});
	}

	private void sendConnectedSignal() {
		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// TODO: Do something with errors. server down?
				Window.alert("Unable to connect to server-channel init");
			}

			public void onSuccess(Boolean result) {

				Window.alert(result
						+ " connected, if false please refresh");
			}
		};
		chessSvc.sendMove("C", userid, callback);
	}

	private void parseMsg(String msg) {
		if (msg.charAt(0) == 'M') {
			// Window.alert("got move");
			// presenter.setState(new State());
			presenter.stateChange(
					MoveSerializer.stringToMove(msg.substring(1)), false);
		} else {
			if (msg.charAt(0) == 'R') {
				// ready to play
				Window.alert("Start to play.");
				myColor = msg.charAt(1) == 'W' ? Color.WHITE : Color.BLACK;
				if (myColor == Color.WHITE) {
					presenter.setMyTurn(true);
				} else {
					presenter.setMyTurn(false);
				}

				presenter.setState(new State());
				presenter.setPlayersInfo(name + " " + myColor + " vs "
						+ msg.substring(2));
			}
		}
	}
}