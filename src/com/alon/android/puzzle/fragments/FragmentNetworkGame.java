package com.alon.android.puzzle.fragments;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.alon.android.puzzle.InterfacePostDownload;
import com.alon.android.puzzle.R;
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

	private View m_topView;
	private Room m_room;
	private PuzzleView m_puzzleView;
	private LinkedList<String> m_participants;
	private ProgressDialog m_progress;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// ensure activity is set
		getMainActivity();

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_new_network_game,
				container, false);

		m_topView.findViewById(R.id.btnQuickGame).setOnClickListener(this);
		return m_topView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnQuickGame:
			getUtils().playSound(R.raw.click);
			showProgress();
			initQuickGame();
			break;
		}
	}

	private void showProgress() {
		m_progress = ProgressDialog.show(getMainActivity(), "Network Puzzle",
				"Game setup in progress", true);
	}

	public void initQuickGame() {
		Bundle autoMatch = RoomConfig.createAutoMatchCriteria(1, 1, 0);

		RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
		roomConfigBuilder.setAutoMatchCriteria(autoMatch);
		roomConfigBuilder.setRoomStatusUpdateListener(this);
		roomConfigBuilder.setMessageReceivedListener(this);
		RoomConfig roomConfig = roomConfigBuilder.build();

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
		if (request == RC_WAITING_ROOM) {
			handleRoomWaitingResult(response);
			return;
		}
		super.onActivityResult(request, response, data);
	}

	private void handleRoomWaitingResult(int response) {
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

	private void startGame() {
		m_progress.dismiss();
		m_progress = null;
		getGameSettings().setPieces(2);
		ListItemData data = new ListItemData(
				"Crow",
				"http://3.bp.blogspot.com/-p8wqKwScJ8c/U-ijgHNKLDI/AAAAAAAAAwU/6aQRacux_Vo/s1600/crow_small.jpg",
				"http://2.bp.blogspot.com/-Dkml9vahI9s/U-ijisHmDYI/AAAAAAAAAwc/bsJPV2ZEYXU/s1600/crow.jpg");
		getUtils().download(data, this);
	}

	@Override
	public void postDownload() {
		m_participants = new LinkedList<String>();
		String participantId = Games.Players
				.getCurrentPlayerId(getMainActivity().getApiClient());
		for (Participant participant : m_room.getParticipants()) {

			if (participant.getParticipantId().equals(participantId)) {
				continue;
			}
			m_participants.add(participant.getParticipantId());
		}

		getMainActivity().setFragmentPuzzle(this);
	}

	public void setView(PuzzleView puzzleView) {
		m_puzzleView = puzzleView;
	}

	public void sendPartStatus(PartStatus status, boolean reliable)
			throws Exception {

		byte[] message = status.toBytes();

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
		if (m_puzzleView == null) {
			return;
		}
		byte[] data = message.getMessageData();
		PartStatus status = PartStatus.fromBytes(data);
		m_puzzleView.updateFromNetwork(status, message.isReliable());
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
}
