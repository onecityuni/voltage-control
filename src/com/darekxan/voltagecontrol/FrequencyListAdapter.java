package com.darekxan.voltagecontrol;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FrequencyListAdapter extends BaseExpandableListAdapter {
	/**
	 * 
	 */
	private final VoltageControl voltageControl;

	private Context context;

	ArrayList<ProcessorFrequency> mFqList = new ArrayList<ProcessorFrequency>();

	public FrequencyListAdapter(VoltageControl voltageControl, Context context) {
		this.voltageControl = voltageControl;
		this.context = context;
	}
	
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mFqList.get(groupPosition).getUv();
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return 1;
	}

	@Override
	public View getChildView(final int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		LinearLayout moduleLayout = (LinearLayout) LayoutInflater.from(
				this.voltageControl.getBaseContext()).inflate(R.layout.module, parent, false);
		final SeekBar seekBar;
		final TextView progressText;
		seekBar = (SeekBar) moduleLayout.findViewById(R.id.freqSB);
		progressText = (TextView) moduleLayout
				.findViewById(R.id.freq_voltageTxt);
		progressText.setText(Integer.toString(mFqList.get(groupPosition)
				.getUv()) + " mV");
		seekBar.setProgress((200 - mFqList.get(groupPosition).getUv()) / 25);
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				progressText.setText("-"
						+ Integer.toString(200 - 25 * progress) + "mV");
				mFqList.get(groupPosition).setUv(200 - 25 * progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});

		// TextView textView = getGenericView();
		// textView.setText(getChild(groupPosition,
		// childPosition).toString());

		return moduleLayout;
	}

	@Override
	public Object getGroup(int groupPosition) {
		int frequency = mFqList.get(groupPosition).getValue();
		String frequency_s = String.valueOf(frequency);
		String stock_voltage;
		String actual_voltage;
		stock_voltage = this.voltageControl.stock_voltages.get(String.valueOf(frequency));
		String uv = Integer.toString(mFqList.get(groupPosition).getUv());
		if (stock_voltage != null)
			actual_voltage = String.valueOf((Integer
					.parseInt(stock_voltage) - Integer.parseInt(uv)));
		else {
			stock_voltage = "?";
			actual_voltage = "?";
		}
		return frequency_s + " Mhz: " + stock_voltage + " - " + uv + " = "
				+ actual_voltage + "mV";
	}

	@Override
	public int getGroupCount() {
		return mFqList.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String group = (String) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.group, null);
		}
		TextView tv = (TextView) convertView.findViewById(R.id.tvGroup);
		tv.setText(group);
		// convertView.setVisibility(View.GONE);

		// getExpandableListView().setGroupIndicator(null);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void setFrequencies(ArrayList<ProcessorFrequency> mFqList) {
		this.mFqList = mFqList;
	}
}