package com.alon.android.puzzle.fragments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.alon.android.puzzle.GameInit;
import com.alon.android.puzzle.GameSettings;
import com.alon.android.puzzle.InterfacePostDownload;
import com.alon.android.puzzle.PuzzleException;
import com.alon.android.puzzle.R;
import com.alon.android.puzzle.Utils;
import com.alon.android.puzzle.lazylist.ListItemData;
import com.alon.android.puzzle.play.PartStatus;
import com.alon.android.puzzle.play.PuzzleView;
import com.alon.android.puzzle.play.ScoreEvent;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
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

	private final static int RC_SELECT_PLAYERS = 10000;
	private final static int RC_INVITATION_INBOX = 10001;
	private final static int RC_WAITING_ROOM = 10002;

	private Room m_room;
	private PuzzleView m_puzzleView;
	private LinkedList<String> m_participants;
	private ProgressDialog m_progress;
	private volatile GameInit m_gameInitSelf;
	private GameInit m_gameInitJoined;

	private boolean m_invited;
	private boolean m_seedSent;
	private boolean m_seedReceived;
	private View m_topView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		m_gameInitSelf = new GameInit(getUtils(), getGameSettings().getPieces());
		m_seedReceived = false;
		m_seedSent = false;

		// ensure activity is set
		getMainActivity();

		getUtils().loadSound(R.raw.click);

		m_topView = inflater.inflate(R.layout.fragment_network_game, container,
				false);

		getUtils().setPiecesButtonText(m_topView, R.id.btnPiecesNetwork);
		m_topView.findViewById(R.id.btnQuickGame).setOnClickListener(this);
		m_topView.findViewById(R.id.btnPiecesNetwork).setOnClickListener(this);
		m_topView.findViewById(R.id.btnInviteFriend).setOnClickListener(this);
		m_topView.findViewById(R.id.btnInvitationInbox)
				.setOnClickListener(this);
		updateInboxText();
		return m_topView;
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.btnQuickGame:
			m_invited = false;
			getUtils().playSound(R.raw.click);
			handleQuickGame();
			break;
		case R.id.btnInviteFriend:
			m_invited = false;
			getUtils().playSound(R.raw.click);
			inviteFriend();
			break;
		case R.id.btnInvitationInbox:
			m_invited = true;
			getUtils().playSound(R.raw.click);
			invitationInbox();
			break;
		case R.id.btnPiecesNetwork:
			getUtils().playSound(R.raw.click);
			getMainActivity().setFragmentPieces(true);
			break;
		}
	}

	private void invitationInbox() {
		progressCreate("Invitation inbox in progress");

		Intent intent = Games.Invitations
				.getInvitationInboxIntent(getMainActivity().getApiClient());
		startActivityForResult(intent, RC_INVITATION_INBOX);
	}

	private void inviteFriend() {
		progressCreate("Inviting game in progress");

		Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(
				getMainActivity().getApiClient(), 1, 1);
		startActivityForResult(intent, RC_SELECT_PLAYERS);
	}

	public void handleQuickGame() {
		progressCreate("Network setup in progress");

		Bundle autoMatch = RoomConfig.createAutoMatchCriteria(1, 1, 0);

		RoomConfig.Builder builder = createRoomConfigBuilder();
		builder.setVariant(getGameSettings().getPieces());
		builder.setAutoMatchCriteria(autoMatch);
		RoomConfig roomConfig = builder.build();

		Games.RealTimeMultiplayer.create(getMainActivity().getApiClient(),
				roomConfig);
	}

	private RoomConfig.Builder createRoomConfigBuilder() {
		RoomConfig.Builder builder = RoomConfig.builder(this);
		builder.setRoomStatusUpdateListener(this);
		builder.setMessageReceivedListener(this);
		return builder;
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
		if (m_seedSent) {
			return;
		}

		startGame(true, false);
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
			leaveRoom(true);
		}
	}

	private void onActivityResultWorker(int request, int response, Intent data)
			throws Exception {
		if (request == RC_WAITING_ROOM) {
			handleRoomWaitingResult(response);
			return;
		}
		if (request == RC_SELECT_PLAYERS) {
			handleInviteResult(response, data);
			return;
		}
		if (request == RC_INVITATION_INBOX) {
			handleInvitationInboxResult(response, data);
			return;
		}
		super.onActivityResult(request, response, data);
	}

	private void handleInvitationInboxResult(int response, Intent data)
			throws Exception {
		if (response != Activity.RESULT_OK) {
			progressDismiss();
			return;
		}

		Bundle extras = data.getExtras();
		Invitation invitation = extras
				.getParcelable(Multiplayer.EXTRA_INVITATION);

		RoomConfig.Builder builder = createRoomConfigBuilder();
		builder.setVariant(getGameSettings().getPieces());
		RoomConfig roomConfig = builder.setInvitationIdToAccept(
				invitation.getInvitationId()).build();
		Games.RealTimeMultiplayer.join(getMainActivity().getApiClient(),
				roomConfig);
	}

	private void handleInviteResult(int response, Intent data) {
		if (response != Activity.RESULT_OK) {
			progressDismiss();
			return;
		}

		final ArrayList<String> invitees = data
				.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

		Bundle autoMatchCriteria = null;
		int minAutoMatchPlayers = data.getIntExtra(
				Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
		int maxAutoMatchPlayers = data.getIntExtra(
				Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

		if (minAutoMatchPlayers > 0) {
			autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
					minAutoMatchPlayers, maxAutoMatchPlayers, 0);
		} else {
			autoMatchCriteria = null;
		}

		// create the room and specify a variant if appropriate
		RoomConfig.Builder builder = createRoomConfigBuilder();
		builder.setVariant(getGameSettings().getPieces());
		builder.addPlayersToInvite(invitees);
		if (autoMatchCriteria != null) {
			builder.setAutoMatchCriteria(autoMatchCriteria);
		}
		RoomConfig roomConfig = builder.build();

		Games.RealTimeMultiplayer.create(getMainActivity().getApiClient(),
				roomConfig);
	}

	private void handleRoomWaitingResult(int response) throws Exception {
		if (response != Activity.RESULT_OK) {
			progressDismiss();
			leaveRoom(true);
			return;
		}

		startCommunication();
	}

	public void leaveRoom(boolean backToMainInMidGame) {
		if (m_room == null) {
			return;
		}

		if (getMainActivity().getApiClient().isConnected()
				&& getMainActivity().isSignedIn()) {
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

	private void startCommunication() throws Exception {

		// run in background to avoid lock of GMS
		AsyncTask<Void, Integer, Void> task = new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				try {
					startCommunicationWorker();
				} catch (Exception e) {
					getUtils().handleError(e);
					leaveRoom(true);
				}
				return null;
			}
		};
		task.execute((Void) null);

	}

	private void startCommunicationWorker() throws Exception {

		loadParticipants();
		waitForPuzzleSizeForInvited();
		sendGameSeed();

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				seedWaitTimeout();
			}
		};
		timer.schedule(task, 60000);
	}

	private void waitForPuzzleSizeForInvited() throws Exception {
		if (!m_invited) {
			return;
		}
		progressUpdate("Waiting for puzzle size");
		long startTime = System.currentTimeMillis();
		while (!m_gameInitSelf.isJoined()) {
			if (System.currentTimeMillis() - startTime > 60000) {
				throw new Exception("timeout waiting for puzzle size");
			}
			Thread.sleep(300);
		}
	}

	private void sendGameSeed() throws Exception {
		progressUpdate("Creating puzzle seed");
		byte[] message = Utils.serializeObject(m_gameInitSelf);
		sendMessage(true, message);
		getUtils().debug("seed sent");
	}

	protected void seedWaitTimeout() {
		if (m_progress == null) {
			return;
		}
		getMainActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				progressDismiss();
				getUtils().message("Timeout initiating game seed, quitting");
				getMainActivity().setFragmentMain();
			}
		});
	}

	private void progressCreate(String message) {
		m_progress = ProgressDialog.show(getMainActivity(), "Network Puzzle",
				message, true);
	}

	private void progressUpdate(final String message) {
		if (m_progress != null) {
			getMainActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					m_progress.setMessage(message);
				}
			});
		}
	}

	private void progressDismiss() {
		if (m_progress == null) {
			return;
		}
		m_progress.dismiss();
		m_progress = null;
	}

	@Override
	public void postDownload() {
		progressDismiss();
		getMainActivity().setFragmentPuzzle(this);
	}

	private void loadParticipants() {
		progressUpdate("Loading participants list");

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

	public void sendMessage(boolean reliable, Serializable object)
			throws Exception {

		byte[] message = Utils.serializeObject(object);

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

		if (object.getClass().equals(ScoreEvent.class)) {

			if (m_puzzleView == null) {
				return;
			}
			ScoreEvent event = (ScoreEvent) object;
			m_puzzleView.updateScoreFromNetwork(event);
			return;
		}

		if (object.getClass().equals(GameInit.class)) {
			getUtils().debug("game init message arrived");
			GameInit otherInit = (GameInit) object;
			handleSeedReceived(otherInit);
			return;
		}

		throw new Exception("invalid message " + object.getClass().getName());
	}

	private void handleSeedReceived(GameInit otherInit) {
		if (m_invited) {
			GameSettings settings = getGameSettings();
			settings.setPieces(otherInit.getPieces());
			settings.save();
			m_gameInitSelf = new GameInit(getUtils(), otherInit.getPieces());
		}
		m_gameInitJoined = m_gameInitSelf.join(otherInit);
		progressUpdate("Remote seed received");
		startGame(false, true);
	}

	/*
	 * we need to start the game only after our seed was sent AND other seed was
	 * received. This might be called in parallel from other threads.
	 */
	synchronized private void startGame(boolean seedSent, boolean seedReceived) {

		if (seedSent) {
			m_seedSent = true;
		}
		if (seedReceived) {
			m_seedReceived = true;
		}
		if (!m_seedSent) {
			return;
		}
		if (!m_seedReceived) {
			return;
		}

		progressUpdate("Loading image");
		ArrayList<ListItemData> images = FragmentDownload.getImages();
		ListItemData data = images.get(m_gameInitJoined.getImageIndex());
		getUtils().download(data, this);
	}

	@Override
	public void cleanup() {
		progressDismiss();

		/*
		 * Notice: Do not leaveRoom() on cleanup, as we need to keep it for the
		 * FragmentPuzzle
		 */
	}

	public GameInit getGameInit() {
		return m_gameInitJoined;
	}

	@Override
	public void updateInvitations() throws Exception {
		updateInboxText();
	}

	private void updateInboxText() {
		Button button = (Button) m_topView
				.findViewById(R.id.btnInvitationInbox);
		String text = getString(R.string.invitationInbox);

		int amount = getGameSettings().getInvitations().size();
		if (amount == 0) {
			button.setText(text + " (empty)");
			// button.setEnabled(false);
		} else {
			button.setText(text + " (" + amount + ")");
			// button.setEnabled(true);
		}
	}

}
