/**
 * 
 * This is OpenTraining, an Android application for planning your your fitness training.
 * Copyright (C) 2012 Christian Skubich
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package de.skubware.opentraining.activity;

import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import de.skubware.opentraining.R;

/**
 * A dialog for selecting a muscle.
 * 
 */
public class SelectMuscleDialog extends AlertDialog implements OnTouchListener {
	private final String TAG = "SelectMuscleDialog";

	/** Reference to original activity. */
	private Activity mActivity;
	
	/** The ImageView with the displayed image */
	private ImageView mImageView;
	
	/** The ImageView (hidden) with the hotspot map */
	private ImageView mHotSpotImageview;

	
	public final static HashMap<Integer, Integer> sColorMap = new HashMap<Integer, Integer>();
	static {
		//sColorMap.put(Color.rgb(0x00, 0xff, 0xff), R.drawable.muscle_lower_leg);
		//sColorMap.put(0xff6600, R.drawable.muscle_tigh);
		sColorMap.put(0Xff00ff, R.drawable.muscle_triceps);
		sColorMap.put(0Xff0000, R.drawable.muscle_shoulder);
		sColorMap.put(0X808000, R.drawable.muscle_biceps);
		sColorMap.put(0X0000ff, R.drawable.muscle_abdominal);
	}
	
	/** A map for caching the drawables. */
	public HashMap<Integer, Drawable> mColorMap = new HashMap<Integer, Drawable>();
	

	
	
	/**
	 * Constructor.
	 */
	public SelectMuscleDialog(final Activity activity) {
		super(activity);
		this.mActivity = activity;

		// set title and message
		this.setTitle("Choose a muscle");

		// CheckBox
		LayoutInflater inflater = this.getLayoutInflater();

		View v = inflater.inflate(R.layout.dialog_choose_muscle, null);
		setView(v);

		
		// image caching
		for (int color : sColorMap.keySet()) {
			mColorMap.put(color, mActivity.getResources().getDrawable(sColorMap.get(color)));
		}
		
		
		/*// positive button
		this.setButton(BUTTON_POSITIVE, activity.getString(R.string.accept), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});

		// negative button
		this.setButton(BUTTON_NEGATIVE, activity.getString(R.string.not_accept), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				activity.finish();
			}
		});
		 */

	}
	
	@Override
	public void onStart(){
		mHotSpotImageview = (ImageView) findViewById(R.id.imageview_hotspotmap);
		mImageView = (ImageView) findViewById(R.id.image_muscle);
		mImageView.setOnTouchListener(this);
	}
	
	

	/**
	 * Respond to the user touching the screen. Change images to make things
	 * appear and disappear from the screen.
	 */
	public boolean onTouch(View v, MotionEvent ev) {
		
		Log.v(TAG, "onTouch(), MotionEvent: " + ev.toString());	

		final int evX = (int) ev.getX();
		final int evY = (int) ev.getY();


		if(ev.getAction() == MotionEvent.ACTION_DOWN){
			
			// determine the touched color (on the hidden imageview)
			int touchColor = getHotspotColor(R.id.imageview_hotspotmap, evX, evY);

			
			for (int color : mColorMap.keySet()) {
				if (colorMatch(color, touchColor)){
					mImageView.setImageDrawable(mColorMap.get(color));
					Log.v(TAG, "Image set!");
					return true;
				}
				Log.v(TAG, "No color match: " + color + ", " + touchColor );
			}

			return false;

		}else{
			return false;
		}

	}

	/**
	 * Gets the color from the hotspot image at point x-y.
	 */
	public int getHotspotColor(int hotspotId, int x, int y) {
		mHotSpotImageview.setDrawingCacheEnabled(true);
		Bitmap hotspots = Bitmap.createBitmap(mHotSpotImageview.getDrawingCache());
		mHotSpotImageview.setDrawingCacheEnabled(false);
		return hotspots.getPixel(x, y);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			mActivity.finish();
			return true;
		}

		return false;
	}

	/**
	 * Checks if to colors match close enough.
	 * 
	 * @param color1
	 *            The first color to compare
	 * 
	 * @param color2
	 *            The second color to compare
	 * 
	 * @return boolean true if both colors match
	 */
	public boolean colorMatch(int color1, int color2) {
		int tolerance = 25;

		
		if ((int) Math.abs(Color.red(color1) - Color.red(color2)) > tolerance)
			return false;
		if ((int) Math.abs(Color.green(color1) - Color.green(color2)) > tolerance)
			return false;
		if ((int) Math.abs(Color.blue(color1) - Color.blue(color2)) > tolerance)
			return false;
		
		Log.v(TAG, "Colors match");
		return true;
	} // end match

}