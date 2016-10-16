package com.apress.movieplayer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Movie player.
 * 
 * @author Onur Cinar
 */
public class MoviePlayerActivity extends Activity implements
		OnItemClickListener {
	/** Log tag. */
	private static final String LOG_TAG = "MoviePlayer";

	/**
	 * On create lifecycle method.
	 * 
	 * @param savedInstanceState
	 *            saved state.
	 * @see Activity#onCreate(Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		scanAviFiles();

		ArrayList<Movie> movieList = new ArrayList<Movie>();

		// Media columns to query
		String[] mediaColumns = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DURATION,
				MediaStore.Video.Media.DATA, MediaStore.Video.Media.MIME_TYPE };

		// Thumbnail columns to query
		String[] thumbnailColumns = { MediaStore.Video.Thumbnails.DATA };

		// Query external movie content for selected media columns
		Cursor mediaCursor = managedQuery(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, mediaColumns,
				null, null, null);

		// Loop through media results
		if ((mediaCursor != null) && mediaCursor.moveToFirst()) {
			do {
				// Get the video id
				int id = mediaCursor.getInt(mediaCursor
						.getColumnIndex(MediaStore.Video.Media._ID));

				// Get the thumbnail associated with the video
				Cursor thumbnailCursor = managedQuery(
						MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
						thumbnailColumns, MediaStore.Video.Thumbnails.VIDEO_ID
								+ "=" + id, null, null);

				// New movie object from the data
				Movie movie = new Movie(mediaCursor, thumbnailCursor);
				Log.d(LOG_TAG, movie.toString());

				// Add to movie list
				movieList.add(movie);

			} while (mediaCursor.moveToNext());
		}

		// Define movie list adapter
		MovieListAdapter movieListAdapter = new MovieListAdapter(this,
				movieList);

		// Set list view adapter to movie list adapter
		ListView movieListView = (ListView) findViewById(R.id.movieListView);
		movieListView.setAdapter(movieListAdapter);

		// Set item click listener
		movieListView.setOnItemClickListener(this);
	}

	/**
	 * On item click listener.
	 */
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// Gets the selected movie
		Movie movie = (Movie) parent.getAdapter().getItem(position);

		// Plays the selected movie
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse(movie.getMoviePath()),
				movie.getMimeType());
		startActivity(intent);
	}

	/**
	 * Goes through the external movies director and scans the AVI files into
	 * the movies.
	 */
	private void scanAviFiles() {
		LinkedList<File> queue = new LinkedList<File>();
		queue.add(Environment.getExternalStorageDirectory());

		while (!queue.isEmpty()) {
			File dir = queue.poll();
			Log.i(LOG_TAG, "Scanning " + dir.getPath());

			File[] files = dir.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isDirectory()) {
						queue.add(file);
					} else if (file.getName().endsWith(".avi")) {
						scanAviFile(file);
					}
				}
			}
		}
	}

	/**
	 * Scans the given AVI files into movies.
	 * 
	 * @param file
	 *            AVI file.
	 */
	private void scanAviFile(File file) {
		ContentValues contentValues = new ContentValues();

		String data = file.getPath();
		Log.i(LOG_TAG, "scanAviFile " + data);

		contentValues.put(MediaStore.Video.Media.TITLE, file.getName());
		contentValues.put(MediaStore.Video.Media.DATA, data);
		contentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/avi");

		ContentResolver contentResolver = getContentResolver();

		if (0 >= contentResolver.update(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues,
				MediaStore.Video.Media.DATA + "=?", new String[] { data })) {

			contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
					contentValues);
		}
	}
}
