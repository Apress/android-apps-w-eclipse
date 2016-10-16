package com.apress.movieplayer;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * AVI player.
 * 
 * @author Onur Cinar
 */
class AviPlayer implements Runnable {
	/** Log tag. */
	private static final String LOG_TAG = "AviPlayer";

	/** Surface holder. */
	private SurfaceHolder surfaceHolder;

	/** Movie file. */
	private String movieFile;

	/** Playing flag. */
	private boolean isPlaying;

	/** Thread instance. */
	private Thread thread;

	/**
	 * Sets the surface holder.
	 * 
	 * @param surfaceHolder
	 *            surface holder.
	 */
	public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
		this.surfaceHolder = surfaceHolder;
	}

	/**
	 * Sets the movie file.
	 * 
	 * @param movieFile
	 *            movie file.
	 */
	public void setMovieFile(String movieFile) {
		this.movieFile = movieFile;
	}

	/**
	 * Start playing.
	 */
	public synchronized void play() {
		if (thread == null) {
			isPlaying = true;

			thread = new Thread(this);
			thread.start();
		}
	}

	/**
	 * Stop playing.
	 */
	public synchronized void stop() {
		isPlaying = false;
	}

	/**
	 * Runs in its thread.
	 */
	public void run() {
		try {
			render(surfaceHolder, movieFile);
		} catch (Exception e) {
			Log.e(LOG_TAG, "render", e);
		}

		thread = null;
	}

	/**
	 * New bitmap using given width and height.
	 * 
	 * @param width
	 *            bitmap width.
	 * @param height
	 *            bitmap height.
	 * @return bitmap instance.
	 */
	private static Bitmap newBitmap(int width, int height) {
		return Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
	}

	/**
	 * Renders the frames from the AVI file.
	 * 
	 * @param surfaceHolder surface holder.
	 * @param movieFile movie file.
	 * @throws Exception
	 */
	private native void render(SurfaceHolder surfaceHolder, String movieFile)
			throws Exception;

	/** Loads the native library. */
	static {
		System.loadLibrary("MoviePlayer");
	}
}
