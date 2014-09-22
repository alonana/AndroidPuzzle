package com.alon.android.puzzle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.net.Uri;
import android.os.ParcelFileDescriptor;

import com.alon.android.puzzle.fragments.FragmentBase;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class GoogleDriveHandler {

	private GoogleApiClient m_api;
	private DriveFile m_googleFile;
	private Utils m_utils;
	private String m_filePath;

	public GoogleDriveHandler(FragmentBase fragment) {
		m_api = fragment.getApiClient();
		m_utils = fragment.getUtils();
	}

	private boolean handleError(Status status) {
		if (status.isSuccess()) {
			return false;
		}

		String message = "error from google drive API "
				+ status.getStatusCode() + ":" + status.getStatusMessage();
		m_utils.handleError(new PuzzleException("dummy for stack trace"),
				message);
		return true;

	}

	public void createFile(Uri fileUri) {
		m_filePath = fileUri.getPath();
		Drive.DriveApi.newContents(m_api).setResultCallback(contentsCallback);
	}

	final private ResultCallback<ContentsResult> contentsCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
			if (handleError(result.getStatus())) {
				return;
			}

			String name = new File(m_filePath).getName();
			Contents contents = result.getContents();
			MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
					.setTitle(name).setMimeType("image/jpeg").setStarred(true)
					.build();

			// create a file on root folder
			Drive.DriveApi.getRootFolder(m_api)
					.createFile(m_api, changeSet, contents)
					.setResultCallback(fileCallback);
		}

	};

	final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {
		@Override
		public void onResult(DriveFileResult result) {
			if (handleError(result.getStatus())) {
				return;
			}
			m_googleFile = result.getDriveFile();

			m_googleFile.openContents(m_api, DriveFile.MODE_WRITE_ONLY, null)
					.setResultCallback(updateCallback);
		}
	};

	final private ResultCallback<ContentsResult> updateCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
			if (handleError(result.getStatus())) {
				return;
			}
			try {
				updateWorker(result);
			} catch (Exception e) {
				m_utils.handleError(e);
			}
		}

		private void updateWorker(ContentsResult result) throws Exception {
			Contents contents = result.getContents();
			ParcelFileDescriptor parcelFileDescriptor = contents
					.getParcelFileDescriptor();
			@SuppressWarnings("resource")
			FileOutputStream out = new FileOutputStream(
					parcelFileDescriptor.getFileDescriptor());

			FileInputStream in = new FileInputStream(m_filePath);
			byte buffer[] = new byte[1024];
			while (true) {
				int read = in.read(buffer);
				if (read == -1) {
					in.close();
					break;
				}
				out.write(buffer, 0, read);
			}

			m_googleFile.commitAndCloseContents(m_api, contents)
					.setResultCallback(commiterCallback);
		}
	};

	final private ResultCallback<Status> commiterCallback = new ResultCallback<Status>() {
		@Override
		public void onResult(Status result) {
			if (handleError(result.getStatus())) {
				return;
			}
			m_utils.message("done");
		}
	};

}
