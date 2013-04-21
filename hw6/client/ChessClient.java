package org.zhihanli.hw6.client;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.shared.chess.Color;
import org.shared.chess.Move;
import org.shared.chess.State;
import org.zhihanli.hw3.Presenter;
import org.zhihanli.hw6.server.StateSerializer;
import org.zhihanli.hw8.GameMessages;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactoryImpl;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ChessClient {

	private String email = null;
	private String token = null;
	private String name = null;
	private ChessServiceAsync chessSvc = GWT.create(ChessService.class);
	private Presenter presenter;
	private Color myColor;
	private Socket socket;
	private Long currentMatch;
	private int rank = -1;
	private int RD = 0;
	GameMessages messages = (GameMessages) GWT.create(GameMessages.class);

	public ChessClient(Presenter presenter) {
		this.presenter = presenter;

	}

	public void setColor(Color color) {
		myColor = color;
	}

	/**
	 * Send move to server through RPC
	 * 
	 * @param move
	 */
	public void sendMoveToServer(Move move, final String time,
			final boolean isTrying) {
		// Initialize the service proxy.
		if (chessSvc == null) {
			chessSvc = GWT.create(ChessService.class);
		}

		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				// Window.alert("Unable to connect to server");
				if (!isTrying)
					Window.alert(messages.connectFailure() + " "
							+ messages.saveMoveOnLocalStorage());

			}

			public void onSuccess(Boolean result) {
				// Window.alert(result.toString());
				presenter.deleteMove(time);
				requestMatchList();
			}
		};

		chessSvc.sendMove(MoveSerializer.moveToString(move) + "#"
				+ currentMatch.toString(), email, callback);

	}

	/**
	 * login to server, get token for channel service
	 */
	public void login() {
		presenter.setButtons(false);
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				Window.alert(messages.connectFailure());
			}

			public void onSuccess(String result) {
				name = result.split(" ")[0];
				email = result.split(" ")[1];
				token = result.split(" ")[2];
				presenter.setCurrentPlayer(messages.setCurrentPlayerInfo(email,
						name));
				presenter.setButtons(true);

				String r = result.split(" ")[3];
				rank = Integer.valueOf(r);
				String rd = result.split(" ")[4];
				RD = Integer.valueOf(rd);
				presenter.setRank(messages.ranking() + " [" + (rank - 2 * RD)
						+ ", " + (rank + 2 * RD) + "]");
				// presenter.setRank(rank);
				initChannel(token);
			}
		};
		presenter.setWaitingStatus();

		chessSvc.login(callback);
	}

	public void closeSocket() {
		if (socket != null)
			socket.close();
	}

	private void requestMatchList() {
		AsyncCallback<List<String>> callback = new AsyncCallback<List<String>>() {
			public void onFailure(Throwable caught) {

				// Window.alert("Unable to connect to server- request match list: "
				// + caught.getMessage());
				Window.alert(messages.connectFailure());

			}

			public void onSuccess(List<String> result) {
				// Window.alert(" match list: " + result);
				presenter.updateMatchList(processDate(result));
			}
		};
		chessSvc.requestMatchList(email, callback);
	}

	public void sendAutoMatchRequest() {

		AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {

				// Window.alert("Unable to connect to server-auto match request "
				// + caught.getMessage());
				Window.alert(messages.connectFailure());

			}

			public void onSuccess(Boolean result) {
				// getWaitingList();
				if (result == false)
					// Window.alert("Sorry,no other players available at this time.");

					Window.alert(messages.setAutoMatchFailReply());
			}
		};
		chessSvc.autoMatch(email, callback);
	}

	private void initChannel(String token) {
		Channel channel = new ChannelFactoryImpl().createChannel(token);

		socket = channel.open(new SocketListener() {
			@Override
			public void onOpen() {
				// Window.alert("Channel opened!");
				requestMatchList();
			}

			@Override
			public void onMessage(String message) {
				// Window.alert("Received: " + message);
				parseMsg(message);
			}

			@Override
			public void onError(ChannelError error) {
				// Window.alert("Channel error: " + error.getCode() + " : "
				// + error.getDescription());
				Window.alert(messages.channelError());
			}

			@Override
			public void onClose() {
				presenter.setPlayersInfo("disconnected");
			}
		});
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
				Window.alert(messages.startToPlay());
				String color;
				myColor = msg.charAt(1) == 'W' ? Color.WHITE : Color.BLACK;
				if (myColor == Color.WHITE) {
					presenter.setMyTurn(true);
					color = messages.colorWhite();
				} else {
					presenter.setMyTurn(false);
					color = messages.colorBlack();
				}
				Long id = new Long(msg.split(" ")[2]);
				// presenter.setState(new State());
				loadStateWithMatchId(id, false);
				String opp = msg.split("")[1];
				presenter.setPlayersInfo(messages.versus() + " " + name + " "
						+ color + " vs " + opp);
			} else if (msg.charAt(0) == 'N' && msg.charAt(1) == 'M') {
				requestMatchList();
				// presenter.addMatch(msg.substring(2));
				// currentMatch = new Long(msg.split(" ")[1]);
				// loadStateWithMatchId(currentMatch);
			} else if (msg.split("#")[0].equals("rank")) {
				String rankAndRD = msg.split("#")[1];
				int rank = Integer.valueOf(rankAndRD.split(" ")[0]);
				int RD = Integer.valueOf(rankAndRD.split(" ")[1]);
				presenter.setRank(messages.ranking() + " [" + (rank - 2 * RD)
						+ ", " + (rank + 2 * RD) + "]");
			}
		}
	}

	public void sendNewMatch(String p1Email, String p2Email) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {

				// Window.alert("Unable to connect to server-start new match "
				// + caught.getMessage());
				Window.alert(messages.connectFailure());
			}

			public void onSuccess(Void result) {
				// requestMatchList();
				// Window.alert(result + " match request");
			}
		};
		if (p1Email.equals(p2Email)) {
			Window.alert(messages.cannotPlayWithSelf());
		} else {
			chessSvc.sendNewMatch(p1Email, p2Email, callback);
		}
	}

	public String getEmail() {
		return email;
	}

	public void refreshCurrentState() {
		if (currentMatch != null)
			loadStateWithMatchId(currentMatch, true);
	}

	public void loadStateWithMatchId(final Long id, final boolean isTrying) {
		AsyncCallback<String> callback = new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {

				// Window.alert("Unable to connect to server-start new match "
				// + caught.getMessage());
				if (!isTrying)
					Window.alert(messages.connectFailure());
			}

			public void onSuccess(String result) {
				// Window.alert(result);

				if (result != null) {
					String[] resultSplit = result.split("##");

					// presenter.setState(StateSerializer
					// .deserialize(resultSplit[0]));
					// Window.alert(resultSplit[0]);
					State state = StateSerializer.deserialize(resultSplit[0]);
					// if (state.getGameResult() != null)
					// Window.alert(state.getGameResult().getWinner()
					// .toString()
					// + " on load");

					presenter.setState(state);
					if (resultSplit[1].equals(getEmail())) {
						presenter.setMyTurn(true);
					} else {
						presenter.setMyTurn(false);
					}
					currentMatch = id;
					String opp = resultSplit[2];
					String me = resultSplit[3];
					presenter.setPlayersInfo(me + " " + messages.colorWhite()
							+ " vs " + opp);
				}
			}
		};
		chessSvc.getStateAndTurnAndPlayerInfoWithMatchId(id, callback);

	}

	public void deleteMatchFromPlayer(String email, Long matchId) {
		AsyncCallback<Void> callback = new AsyncCallback<Void>() {
			public void onFailure(Throwable caught) {

				// Window.alert("Unable to connect to server-delete match "
				// + caught.getMessage());
				Window.alert(messages.connectFailure());

			}

			public void onSuccess(Void result) {
				requestMatchList();
				// Window.alert(result + " match request");
			}
		};
		chessSvc.deleteMatchFromPlayer(email, matchId, callback);
	}

	@SuppressWarnings("deprecation")
	public List<String> processDate(List<String> matchList) {
		List<String> processedMatchList = new LinkedList<String>();
		for (String match : matchList) {
			String[] matchSplit = match.split("#");
			String dateString = matchSplit[5];

			Date today = new Date(dateString);
			String formatedDate = DateTimeFormat.getLongDateFormat()
					.format(today).toString();

			String processedMatch = "Match ID: " + matchSplit[0] + " Player1: "
					+ matchSplit[1] + " Player2: " + matchSplit[2] + " Turn: "
					+ matchSplit[3] + " Result: " + matchSplit[4]
					+ " Start date: " + formatedDate;

			processedMatchList.add(processedMatch);
		}
		return processedMatchList;
	}

}