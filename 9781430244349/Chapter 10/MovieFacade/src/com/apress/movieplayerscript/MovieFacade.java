package com.apress.movieplayerscript;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.util.Log;

import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;

/**
 * Movie facade.
 * 
 * @author Onur Cinar
 */
public class MovieFacade extends RpcReceiver {
	/** Log tag. */
	private static final String LOG_TAG = "MovieFacade";

	/** Service instance. */
	private final Service service;

	/**
	 * Constructor.
	 * 
	 * @param manager
	 *            facade manager.
	 */
	public MovieFacade(FacadeManager manager) {
		super(manager);

		// Save the server instance for using it as a context
		service = manager.getService();
	}

	@Override
	public void shutdown() {

	}

	/**
	 * Gets a list of all movies.
	 * 
	 * @return movie list.
	 * @throws JSONException
	 */
	@Rpc(description = "Returns a list of all movies.", returns = "a List of movies as Maps")
	public List<JSONObject> moviesGet() throws JSONException {
		List<JSONObject> movies = new LinkedList<JSONObject>();

		// Media columns to query
		String[] mediaColumns = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION,
				MediaStore.Video.Media.DATA, MediaStore.Video.Media.MIME_TYPE };

		// Thumbnail columns to query
		String[] thumbnailColumns = { MediaStore.Video.Thumbnails.DATA };

		// Content resolver
		ContentResolver contentResolver = service.getContentResolver();

		Cursor c = contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Video.Thumbnails.VIDEO_ID }, null, null, null);
		
		Log.i(LOG_TAG, "BEFORE " + c.getCount());
		if (c.moveToFirst()) {
			do {
				Log.d(LOG_TAG, c.getString(0));
			} while (c.moveToNext());
		}
		c.close();
		Log.i(LOG_TAG, "AFTER");
		
		
		// Query external movie content for selected media columns
		Cursor mediaCursor = contentResolver.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns,
				null, null, null);

		// Loop through media results
		if (mediaCursor.moveToFirst()) {
			do {
				// Get the video id
				int id = mediaCursor.getInt(mediaCursor
						.getColumnIndex(MediaStore.Video.Media._ID));

				// Get the thumbnail associated with the video
				Cursor thumbnailCursor = contentResolver.query(
						MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
						thumbnailColumns, MediaStore.Video.Thumbnails.VIDEO_ID
								+ "=" + id, null, null);

				// New movie object from the data
				JSONObject movie = new JSONObject();

				movie.put("title", mediaCursor.getString(mediaCursor
						.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
				movie.put("moviePath", "file://" + mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Video.Media.DATA)));
				movie.put("mimeType", mediaCursor.getString(mediaCursor
						.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)));

				long duration = mediaCursor.getLong(mediaCursor
						.getColumnIndex(MediaStore.Video.Media.DURATION));
				movie.put("duration", getDurationAsString(duration));
				
				if (thumbnailCursor.moveToFirst()) {
					movie.put(
							"thumbnailPath",
							"file://" + thumbnailCursor.getString(thumbnailCursor
									.getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
				} else {
					movie.put("thumbnailPath", "");
				}

				Log.d(LOG_TAG, movie.toString());

				// Close cursor
				thumbnailCursor.close();

				// Add to movie list
				movies.add(movie);

			} while (mediaCursor.moveToNext());

			// Close cursor
			mediaCursor.close();
		}

		return movies;
	}

	/**
	 * Gets the given duration as string.
	 * 
	 * @param duration
	 *            duration value.
	 * @return duration string.
	 */
	private static String getDurationAsString(long duration) {
		// Calculate milliseconds
		long milliseconds = duration % 1000;
		long seconds = duration / 1000;

		// Calculate seconds
		long minutes = seconds / 60;
		seconds %= 60;

		// Calculate hours and minutes
		long hours = minutes / 60;
		minutes %= 60;

		// Build the duration string
		String durationString = String.format("%1$02d:%2$02d:%3$02d.%4$03d",
				hours, minutes, seconds, milliseconds);

		return durationString;
	}
}
