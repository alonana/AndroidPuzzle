package com.alon.android.puzzle.fragments;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.os.ParcelFileDescriptor;

import com.alon.android.puzzle.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class DriveHandler {

	private GoogleApiClient m_apiClient;
	private Utils m_utils;
	private DriveFile m_file;

	public DriveHandler(FragmentNewGame fragment) {
		m_apiClient = fragment.getApiClient();
		m_utils = fragment.getUtils();
	}

	public void createImage() {
		Drive.DriveApi.newContents(m_apiClient).setResultCallback(
				driverContentsCallback);
	}

	final private ResultCallback<ContentsResult> driverContentsCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
			if (!checkStatus(result.getStatus(), "open drive content")) {
				return;
			}

			try {
				DriveFolder appDataFolder = Drive.DriveApi
						.getAppFolder(m_apiClient);
				Contents contents = result.getContents();

				MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
						.setTitle("Newfile2").setMimeType("puzzle/internal")
						.build();

				FileOutputStream output = new FileOutputStream(contents
						.getParcelFileDescriptor().getFileDescriptor());
				output.write("Hello World!".getBytes());
				output.close();

				appDataFolder.createFile(m_apiClient, changeSet, contents)
						.setResultCallback(fileCallback);
			} catch (Exception e) {
				m_utils.handleError(e);
			}
		}
	};

	final private ResultCallback<DriveFileResult> fileCallback = new ResultCallback<DriveFileResult>() {

		@Override
		public void onResult(DriveFileResult result) {
			if (!checkStatus(result.getStatus(), "creating new file")) {
				return;
			}

			m_file = result.getDriveFile();
			m_file.openContents(m_apiClient, DriveFile.MODE_WRITE_ONLY, null)
					.setResultCallback(fileConentCallback);
		}
	};

	final private ResultCallback<ContentsResult> fileConentCallback = new ResultCallback<ContentsResult>() {
		@Override
		public void onResult(ContentsResult result) {
			if (!checkStatus(result.getStatus(), "creating file content")) {
				return;
			}

			try {
				Contents contents = result.getContents();

				ParcelFileDescriptor parcelFileDescriptor = contents
						.getParcelFileDescriptor();

				FileOutputStream fileOutputStream = new FileOutputStream(
						parcelFileDescriptor.getFileDescriptor());
				Writer writer = new OutputStreamWriter(fileOutputStream);
				writer.write("hello world");
				writer.close();
				m_file.commitAndCloseContents(m_apiClient, contents)
						.setResultCallback(fileCommitCallback);
				m_utils.message("commit file success");
			} catch (Exception e) {
				m_utils.handleError(e);
			}
		}
	};

	final private ResultCallback<Status> fileCommitCallback = new ResultCallback<Status>() {
		@Override
		public void onResult(Status result) {

			if (!checkStatus(result.getStatus(), "committing file")) {
				return;
			}
		}
	};

	private boolean checkStatus(Status status, String description) {
		if (status.isSuccess()) {
			return true;
		}
		String message = "Error " + description + status.getStatusCode() + ":"
				+ status.getStatusMessage();
		m_utils.handleError(null, message);
		return false;
	}
}
