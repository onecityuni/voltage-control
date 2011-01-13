package com.darekxan.voltagecontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class FrequencyListAdapter extends BaseExpandableListAdapter {
	protected static final String 	C_FREQUENCY_VOLTAGE_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/frequency_voltage_table";
	private Context context;
	public static Map<String, String> stock_voltages = new HashMap<String, String>();
	ArrayList<ProcessorFrequency> mFqList = new ArrayList<ProcessorFrequency>();
	
	public FrequencyListAdapter(Context context) {
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
				context).inflate(R.layout.module, parent, false);
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
		stock_voltage = stock_voltages.get(String.valueOf(frequency));
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
	public static void getfrequency_voltage_table() {
		String frequency_voltage_table = ShellInterface
				.getProcessOutput(C_FREQUENCY_VOLTAGE_TABLE);
		if (frequency_voltage_table.equals("")) {
			stock_voltages.put("100", "950");
			stock_voltages.put("200", "950");
			stock_voltages.put("400", "1050");
			stock_voltages.put("800", "1200");
			stock_voltages.put("1000", "1275");
			stock_voltages.put("1120", "1300");
			stock_voltages.put("1200", "1300");
		} else {
			String[] tfrequency_voltage_table = frequency_voltage_table
					.split(" ");
			String[] frequency_table = new String[20];
			String[] voltage_table = new String[20];
			int j = 0;
			// Toast.makeText(getApplicationContext(),
			// tfrequency_voltage_table[0],
			// Toast.LENGTH_LONG).show();
			for (int i = 0; i < tfrequency_voltage_table.length; i += 3) {

				frequency_table[j] = String
						.valueOf(tfrequency_voltage_table[i]);
				voltage_table[j] = String
						.valueOf(tfrequency_voltage_table[i + 1]);
				stock_voltages.put(
						String.valueOf(tfrequency_voltage_table[i]
								.subSequence(
										0,
										tfrequency_voltage_table[i]
												.length() - 3)),
						String.valueOf(tfrequency_voltage_table[i + 1]));
				// Log.d("VC", frequency_table[j]+ ": "+
				// voltage_table[j]);
				j++;
			}
			// stock_voltages.put("100", "950");

		}
	}
}