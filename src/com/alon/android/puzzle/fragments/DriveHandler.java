package com.alon.android.puzzle.fragments;

import java.io.OutputStream;

import com.alon.android.puzzle.Utils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveFolder.DriveFolderResult;
import com.google.android.gms.drive.MetadataChangeSet;

public class DriveHandler {

	private GoogleApiClient m_apiClient;
	private Utils m_utils;

	public DriveHandler(FragmentNewGame fragment) {
		m_apiClient = fragment.getApiClient();
		m_utils = fragment.getUtils();
	}

	public void createImage() {
		driveCreateFolder();
	}

	private void driveCreateFolder() {
		MetadataChangeSet changeSet = new MetadataChangeSet.Builder().setTitle(
				"New folder").build();

		DriveFolder root = Drive.DriveApi.getRootFolder(m_apiClient);
		PendingResult<DriveFolder.DriveFolderResult> result = root
				.createFolder(m_apiClient, changeSet);
		result.setResultCallback(new ResultCallback<DriveFolder.DriveFolderResult>() {

			@Override
			public void onResult(DriveFolderResult result) {
				if (!result.getStatus().isSuccess()) {
					String message = "Error creating folder "
							+ result.getStatus().getStatusCode() + ":"
							+ result.getStatus().getStatusMessage();
					m_utils.handleError(null, message);
					return;
				}

				driveCreateContent(result.getDriveFolder());
			}
		});
	}

	protected void driveCreateContent(final DriveFolder driveFolder) {
		PendingResult<ContentsResult> result = Drive.DriveApi
				.newContents(m_apiClient);
		result.setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {

			@Override
			public void onResult(ContentsResult result) {
				if (!result.getStatus().isSuccess()) {
					String message = "Error creating contents "
							+ result.getStatus().getStatusCode() + ":"
							+ result.getStatus().getStatusMessage();
					m_utils.handleError(null, message);
					return;
				}

				driveCreateFile(driveFolder, result.getContents());
			}
		});
	}

	private void driveCreateFile(DriveFolder driveFolder, Contents contents) {

		MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
				.setTitle("New file").setMimeType("text/plain").build();

		PendingResult<DriveFileResult> result = driveFolder.createFile(
				m_apiClient, changeSet, contents);
		result.setResultCallback(new ResultCallback<DriveFolder.DriveFileResult>() {

			@Override
			public void onResult(DriveFileResult result) {
				if (!result.getStatus().isSuccess()) {
					String message = "Error creating file "
							+ result.getStatus().getStatusCode() + ":"
							+ result.getStatus().getStatusMessage();
					m_utils.handleError(null, message);
					return;
				}

				driveOpenFile(result.getDriveFile());
			}
		});
	}

	private void driveOpenFile(final DriveFile driveFile) {
		PendingResult<ContentsResult> result = driveFile.openContents(
				m_apiClient, DriveFile.MODE_WRITE_ONLY, null);
		result.setResultCallback(new ResultCallback<DriveApi.ContentsResult>() {

			@Override
			public void onResult(ContentsResult result) {
				if (!result.getStatus().isSuccess()) {
					String message = "Error openning file "
							+ result.getStatus().getStatusCode() + ":"
							+ result.getStatus().getStatusMessage();
					m_utils.handleError(null, message);
					return;
				}

				driveSetFileContent(driveFile, result.getContents());
			}
		});
	}

	private void driveSetFileContent(DriveFile driveFile, Contents contents) {
		try {
			OutputStream out = contents.getOutputStream();
			out.write("aaa".getBytes());
			out.close();
			contents.close();
			driveFile.commitAndCloseContents(m_apiClient, contents);
			m_utils.message("done");
		} catch (Exception e) {
			m_utils.handleError(e);
		}
	}

}
