package com.apress.movieplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/**
 * AVI movie player activity.
 * 
 * @author Onur Cinar
 */
public class AviPlayerActivity extends Activity implements Callback {
	/** AVI player. */
	private AviPlayer aviPlayer;
	
	/**
	 * On create.
	 * 
	 * @see Activity#onCreate(Bundle)
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.avi_player);
		
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface_view);
		surfaceView.getHolder().addCallback(this);
		
		aviPlayer = new AviPlayer();		
		aviPlayer.setMovieFile(getIntent().getData().getPath());
	}

	/**
	 * Surface changed.
	 * 
	 * @see Callback#surfaceChanged(SurfaceHolder, int, int, int)
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	/**
	 * Surface created.
	 * 
	 * @see Callback#surfaceCreated(SurfaceHolder)
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		aviPlayer.setSurfaceHolder(holder);
		aviPlayer.play();
	}

	/**
	 * Surface destroyed.
	 * 
	 * @see Callback#surfaceDestroyed(SurfaceHolder)
	 */
	public void surfaceDestroyed(SurfaceHolder holder) {
		aviPlayer.stop();
	}
}
