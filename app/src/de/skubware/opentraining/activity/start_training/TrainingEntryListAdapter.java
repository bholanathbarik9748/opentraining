/**
 * 
 * This is OpenTraining, an Android application for planning your your fitness training.
 * Copyright (C) 2012-2013 Christian Skubich
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

package de.skubware.opentraining.activity.start_training;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import de.skubware.opentraining.R;
import de.skubware.opentraining.activity.manage_workouts.WorkoutListActivity;
import de.skubware.opentraining.basic.FSet;
import de.skubware.opentraining.basic.FSet.SetParameter;
import de.skubware.opentraining.basic.FitnessExercise;
import de.skubware.opentraining.basic.TrainingEntry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class TrainingEntryListAdapter extends BaseAdapter {

	private SherlockFragmentActivity mActivity;
	private static LayoutInflater mInflater = null;

	private FitnessExercise mFEx;
	private TrainingEntry mTrainingEntry;


	public TrainingEntryListAdapter(SherlockFragmentActivity activity, FitnessExercise fEx, TrainingEntry trainingEntry) {
		mActivity = activity;
		mInflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);


		mTrainingEntry = trainingEntry;
		mFEx = fEx;
	}

	public int getCount() {
		return mTrainingEntry.getFSetList().size() + 1;
	}

	public Object getItem(int position) {
		if (position > mTrainingEntry.getFSetList().size() - 1)
			return null;

		return mTrainingEntry.getFSetList().get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;

		// last element is an empty row
		if (position == getCount() - 1) {
			vi = mInflater.inflate(R.layout.list_row_empty, null);

			// add listener
			vi.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					showDialog(null);
				}
			});

			return vi;
		}

		vi = mInflater.inflate(R.layout.list_row, null);
		final FSet set = (FSet) getItem(position);

		// set values
		TextView textview_weight = (TextView) vi.findViewById(R.id.textview_weight);
		TextView textview_rep = (TextView) vi.findViewById(R.id.textview_rep);
		TextView textview_duration = (TextView) vi.findViewById(R.id.textview_duration);

		for (SetParameter para : set.getSetParameters()) {
			if (para instanceof SetParameter.Weight) {
				textview_weight.setText(para.toString());
			}
			if (para instanceof SetParameter.Duration) {
				textview_duration.setText(para.toString());
			}
			if (para instanceof SetParameter.Repetition) {
				textview_rep.setText(para.toString());
			}
		}
		
		// set button icons
		final ImageButton imagebutton_check = (ImageButton) vi.findViewById(R.id.imagebutton_check);
		final ImageButton imagebutton_notcheck = (ImageButton) vi.findViewById(R.id.imagebutton_check);
		if(mTrainingEntry.hasBeenDone(set)){
			imagebutton_check.setImageResource(R.drawable.icon_check_green);
		}else{
			imagebutton_check.setImageResource(R.drawable.icon_check_white);
		}
		

		// add button listener

		imagebutton_check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				imagebutton_check.setImageResource(R.drawable.icon_check_green);
				imagebutton_notcheck.setImageResource(R.drawable.icon_cross_white);
				mTrainingEntry.setHasBeenDone(set, true);
				trainingEntryEdited();
				
				if(mFEx.isTrainingEntryFinished(mTrainingEntry)){
					showExerciseFinishedDialog();
				}
			}
		});

		imagebutton_notcheck.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				imagebutton_check.setImageResource(R.drawable.icon_check_white);
				imagebutton_notcheck.setImageResource(R.drawable.icon_cross_red);
				deleteSet(set);
				
				if(mFEx.isTrainingEntryFinished(mTrainingEntry)){
					showExerciseFinishedDialog();
				}
			}
		});
		

		// add lister for changing values
		final View wrapper_duration = (View) vi.findViewById(R.id.wrapper_duration);
		final View wrapper_rep = (View) vi.findViewById(R.id.wrapper_rep);
		final View wrapper_weight = (View) vi.findViewById(R.id.wrapper_weight);

		OnClickListener changeSetValuesListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				showDialog(set);
			}
		};
		wrapper_duration.setOnClickListener(changeSetValuesListener);
		wrapper_rep.setOnClickListener(changeSetValuesListener);
		wrapper_weight.setOnClickListener(changeSetValuesListener);

		return vi;
	}

	private void deleteSet(FSet set) {
		mTrainingEntry.getFSetList().remove(set);
		trainingEntryEdited();
	}
	
	private void trainingEntryEdited(){
		FExDetailFragment fragment = (FExDetailFragment) mActivity.getSupportFragmentManager().findFragmentById(R.id.exercise_detail_container);
		fragment.onEntryEdited(this.mFEx);
	}

	/**
	 * Shows DialogFragmentAddEntry with the given {@link FSet}.
	 * 
	 * @param set
	 *            The FSet to edit. If it is null a new FSet will be added to
	 *            the TrainingEntry.
	 * 
	 * @see DialogFragmentAddEntry#newInstance(FitnessExercise, FSet)
	 */
	private void showDialog(FSet set) {
		FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
		Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag("dialog");
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		DialogFragment newFragment = DialogFragmentAddEntry.newInstance(mFEx, set, mTrainingEntry);
		newFragment.show(ft, "dialog");
	}
	
	private void showExerciseFinishedDialog(){
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
		builder.setTitle(mActivity.getString(R.string.exercise_finished));
		builder.setPositiveButton(mActivity.getString(android.R.string.ok), new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mActivity instanceof FExDetailActivity){
					mActivity.finish();
				}else{
					//TODO switch to next exercise?
					//((FExListActivity)mActivity).showNextExercise();
				}
			}
		});

		
		builder.create().show();
	}	

}