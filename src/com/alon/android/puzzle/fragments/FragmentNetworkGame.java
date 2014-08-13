package com.alon.android.puzzle.fragments;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.alon.android.puzzle.GameInit;
import com.alon.android.puzzle.InterfacePostDownload;
import com.alon.android.puzzle.PuzzleException;
import com.alon.android.puzzle.R;
import com.alon.android.puzzle.Utils;
import com.alon.android.puzzle.lazylist.ListItemData;
import com.alon.android.puzzle.play.PartStatus;
import com.alon.android.puzzle.play.PuzzleView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer.ReliableMessageSentCallback;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

public class FragmentNetworkGame extends FragmentBase implements
		OnClickListener, RoomStatusUpdateListener,
		RealTimeMessageReceivedListener, RoomUpdateListener,
		InterfacePostDownload, ReliableMessageSentCallback {

	private final static int RC_WAITING_ROOM = 10002;

	private Room m_room;
	private PuzzleView m_puzzleView;
	private LinkedList<String> m_participants;
	private ProgressDialog m_progress;
	private GameInit m_gameInit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// ensure activity is set
		getMainActivity();

		getUtils().loadSound(R.raw.click);

		View topView = inflater.inflate(R.layout.fragment_network_game,
				container, false);

		getUtils().setPiecesButtonText(topView, R.id.btnPiecesNetwork);
		topView.findViewById(R.id.btnQuickGame).setOnClickListener(this);
		topView.findViewById(R.id.btnPiecesNetwork).setOnClickListener(this);
		return topView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnQuickGame:
			initQuickGame();
			break;
		case R.id.btnPiecesNetwork:
			getMainActivity().setFragmentPieces(true);
			break;
		}
	}

	public void initQuickGame() {
		getUtils().playSound(R.raw.click);

		m_gameInit = new GameInit(getGameSettings().getPieces());

		m_progress = ProgressDialog.show(getMainActivity(), "Network Puzzle",
				"Game setup in progress", true);

		Bundle autoMatch = RoomConfig.createAutoMatchCriteria(1, 1, 0);

		RoomConfig.Builder builder = RoomConfig.builder(this);
		builder.setVariant(getGameSettings().getPieces());

		builder.setAutoMatchCriteria(autoMatch);
		builder.setRoomStatusUpdateListener(this);
		builder.setMessageReceivedListener(this);
		RoomConfig roomConfig = builder.build();

		Games.RealTimeMultiplayer.create(getMainActivity().getApiClient(),
				roomConfig);
	}

	@Override
	public void onRoomConnecting(Room room) {
		m_room = room;
	}

	@Override
	public void onRoomAutoMatching(Room room) {
		m_room = room;
	}

	@Override
	public void onPeerInvitedToRoom(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onPeerDeclined(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onPeerJoined(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onPeerLeft(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onPeersConnected(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onPeersDisconnected(Room room, List<String> list) {
		m_room = room;
	}

	@Override
	public void onP2PConnected(String s) {

	}

	@Override
	public void onP2PDisconnected(String s) {

	}

	@Override
	public void onLeftRoom(int i, String s) {

	}

	@Override
	public void onRealTimeMessageSent(int arg0, int arg1, String arg2) {

	}

	@Override
	public void onConnectedToRoom(Room room) {
		m_room = room;
	}

	@Override
	public void onDisconnectedFromRoom(Room room) {
		if (m_room == null) {
			// no message in case we are initiating the leave of room
			return;
		}

		// silent leave room in case other side completed game
		if (!isPuzzleEnd()) {
			getUtils().message("You've got disconnected, leaving game");
		}

		leaveRoom(true);
	}

	private boolean isPuzzleEnd() {
		if (m_puzzleView == null) {
			return false;
		}
		if (!m_puzzleView.isAllGlued()) {
			return false;
		}
		return true;
	}

	@Override
	public void onJoinedRoom(int statusCode, Room room) {
		if (handleRoomError("onJoinedRoom", statusCode)) {
			return;
		}
		startWaitingIntent(room);
	}

	@Override
	public void onRoomConnected(int statusCode, Room room) {
		if (handleRoomError("onRoomConnected", statusCode)) {
			return;
		}
	}

	@Override
	public void onRoomCreated(int statusCode, Room room) {
		if (handleRoomError("onRoomCreated", statusCode)) {
			return;
		}
		startWaitingIntent(room);
	}

	private boolean handleRoomError(String event, int statusCode) {
		if (statusCode == GamesStatusCodes.STATUS_OK) {
			return false;
		}

		String message = "room creation failed, " + event + " status code: "
				+ statusCode;
		getUtils().handleError(null, message);
		getMainActivity().setFragmentMain();
		return true;
	}

	private void startWaitingIntent(Room room) {
		m_room = room;

		Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(
				getMainActivity().getApiClient(), room, Integer.MAX_VALUE);
		startActivityForResult(intent, RC_WAITING_ROOM);
	}

	@Override
	public void onActivityResult(int request, int response, Intent data) {
		try {
			onActivityResultWorker(request, response, data);
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	private void onActivityResultWorker(int request, int response, Intent data)
			throws Exception {
		if (request == RC_WAITING_ROOM) {
			handleRoomWaitingResult(response);
			return;
		}
		super.onActivityResult(request, response, data);
	}

	private void handleRoomWaitingResult(int response) throws Exception {
		if (response != Activity.RESULT_OK) {
			leaveRoom(true);
			return;
		}

		startGame();
	}

	public void leaveRoom(boolean backToMainInMidGame) {
		if (m_room == null) {
			return;
		}

		if (getMainActivity().getApiClient().isConnected()) {
			Games.RealTimeMultiplayer.leave(getMainActivity().getApiClient(),
					this, m_room.getRoomId());
		}
		m_room = null;

		if (backToMainInMidGame) {
			if (!isPuzzleEnd()) {
				// only back to main in case end dialog is not shown
				getMainActivity().setFragmentMain();
			}
		}
	}

	private void startGame() throws Exception {

		loadParticipants();

		if (m_progress != null) {
			m_progress.setMessage("Creating puzzle seed");
		}
		byte[] message = Utils.serializeObject(m_gameInit);
		sendMessage(true, message);

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				startGameTimeout();
			}
		};
		timer.schedule(task, 60000);
	}

	protected void startGameTimeout() {
		if (m_progress == null) {
			return;
		}
		getMainActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				m_progress.dismiss();
				m_progress = null;
				getUtils().message("Timeout initiating game seed, quitting");
				getMainActivity().setFragmentMain();
			}
		});
	}

	private void postGameInit(GameInit otherInit) {
		m_gameInit.join(otherInit);

		if (m_progress != null) {
			m_progress.setMessage("Loading image");
		}
		ArrayList<ListItemData> images = FragmentDownload.getImages();
		ListItemData data = images.get(m_gameInit.getImageIndex());
		getUtils().download(data, this);
	}

	@Override
	public void postDownload() {

		m_progress.dismiss();
		m_progress = null;

		getMainActivity().setFragmentPuzzle(this);
	}

	private void loadParticipants() {
		if (m_progress != null) {
			m_progress.setMessage("Loading participants list");
		}

		String myPlayerId = Games.Players.getCurrentPlayerId(getMainActivity()
				.getApiClient());
		String myParticipantId = m_room.getParticipantId(myPlayerId);

		m_participants = new LinkedList<String>();
		for (Participant participant : m_room.getParticipants()) {

			if (participant.getParticipantId().equals(myParticipantId)) {
				continue;
			}
			m_participants.add(participant.getParticipantId());
		}

		if (m_participants.size() == 0) {
			throw new PuzzleException("no participants");
		}
		if (m_participants.size() > 1) {
			throw new PuzzleException("too many participants");
		}
	}

	public void setView(PuzzleView puzzleView) {
		m_puzzleView = puzzleView;
	}

	public void sendPartStatus(PartStatus status, boolean reliable)
			throws Exception {

		byte[] message = status.toBytes();

		sendMessage(reliable, message);
	}

	private void sendMessage(boolean reliable, byte[] message) {
		GoogleApiClient api = getMainActivity().getApiClient();
		String roomId = m_room.getRoomId();

		if (reliable) {
			for (String participantId : m_participants) {
				Games.RealTimeMultiplayer.sendReliableMessage(api, this,
						message, roomId, participantId);
			}
		} else {
			Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(api,
					message, roomId);
		}
	}

	@Override
	public void onRealTimeMessageReceived(RealTimeMessage message) {
		try {
			onRealTimeMessageReceivedWorker(message);
		} catch (Exception e) {
			getUtils().handleError(e);
		}
	}

	private void onRealTimeMessageReceivedWorker(RealTimeMessage message)
			throws Exception {

		byte[] data = message.getMessageData();
		Object object = Utils.deserializeObject(data);

		if (object.getClass().equals(PartStatus.class)) {

			if (m_puzzleView == null) {
				return;
			}
			PartStatus status = (PartStatus) object;
			m_puzzleView.updateFromNetwork(status, message.isReliable());
			return;
		}

		if (object.getClass().equals(GameInit.class)) {
			Utils.debug("game init message arrived");
			GameInit otherInit = (GameInit) object;
			postGameInit(otherInit);
			return;
		}

		throw new Exception("invalid message " + object.getClass().getName());
	}

	@Override
	public void cleanup() {
		if (m_progress != null) {
			m_progress.dismiss();
			m_progress = null;
		}

		/*
		 * Notice: Do not leaveRoom() on cleanup, as we need to keep it for the
		 * FragmentPuzzle
		 */
	}

	public GameInit getGameInit() {
		return m_gameInit;
	}
}
