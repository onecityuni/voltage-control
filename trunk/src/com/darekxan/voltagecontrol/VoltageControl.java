package com.darekxan.voltagecontrol;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import android.app.AlertDialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.Spinner;
import android.widget.Toast;

public class VoltageControl extends ExpandableListActivity {
	

	public static class VoltageControlData {
		private ExpandableListAdapter frequencyAdapter;
		private String max_freq;
		private int max_freq_id;
		private String max_frequency;
		private ArrayList<ProcessorFrequency> mFqList;
		private Menu mMenu;
		private int sched_active;
		private String[] sched_table;
		private String time_in_state;
		private String uv_values;

		public VoltageControlData(String max_frequency,
				ArrayList<ProcessorFrequency> mFqList, int sched_active) {
			this.max_frequency = max_frequency;
			this.mFqList = mFqList;
			this.sched_active = sched_active;
		}

		public ExpandableListAdapter getFrequencyAdapter() {
			return frequencyAdapter;
		}

		public void setFrequencyAdapter(ExpandableListAdapter frequencyAdapter) {
			this.frequencyAdapter = frequencyAdapter;
		}

		public String getMax_freq() {
			return max_freq;
		}

		public void setMax_freq(String max_freq) {
			this.max_freq = max_freq;
		}

		public int getMax_freq_id() {
			return max_freq_id;
		}

		public void setMax_freq_id(int max_freq_id) {
			this.max_freq_id = max_freq_id;
		}

		public String getMax_frequency() {
			return max_frequency;
		}

		public void setMax_frequency(String max_frequency) {
			this.max_frequency = max_frequency;
		}

		public ArrayList<ProcessorFrequency> getmFqList() {
			return mFqList;
		}

		public void setmFqList(ArrayList<ProcessorFrequency> mFqList) {
			this.mFqList = mFqList;
		}

		public Menu getmMenu() {
			return mMenu;
		}

		public void setmMenu(Menu mMenu) {
			this.mMenu = mMenu;
		}

		public int getSched_active() {
			return sched_active;
		}

		public void setSched_active(int sched_active) {
			this.sched_active = sched_active;
		}

		public String[] getSched_table() {
			return sched_table;
		}

		public void setSched_table(String[] sched_table) {
			this.sched_table = sched_table;
		}

		public String getTime_in_state() {
			return time_in_state;
		}

		public void setTime_in_state(String time_in_state) {
			this.time_in_state = time_in_state;
		}

		public String getUv_values() {
			return uv_values;
		}

		public void setUv_values(String uv_values) {
			this.uv_values = uv_values;
		}
	}

	// COMMAND STRINGS
	protected static final String 	C_FREQUENCY_VOLTAGE_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/frequency_voltage_table";
	protected static final String 	C_STATES_ENABLED = "cat /sys/devices/system/cpu/cpu0/cpufreq/states_enabled_table";
	protected static final String 	C_TIME_IN_STATE = "cat /sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state";
	protected static final String 	C_UV_MV_TABLE = "cat /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
	protected static final String	C_SCALING_MAX_FREQ = "cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	// KERNEL SUPPORT FLAGS
	protected static final int 		K_FREQUENCY_VOLTAGE_TABLE_CAP = 5;
	protected static final int 		K_NO_CAP = -1;
	protected static final int 		K_ROOT_CAP = 3;
	protected static final int 		K_STATES_ENABLED_CAP = 6;
	protected static final int 		K_UV_CAP = 4;
	protected static final int 		NOROOT = 1;
	// HANDLER MESSAGES
	protected static final int 		REFRESH = 0;
	protected static final int 		WRONGKERNEL = 2;
	
	protected VoltageControlData data = new VoltageControlData(" ",
			new ArrayList<ProcessorFrequency>(), 0);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		final ArrayAdapter<String> adapterForSchedSpinner = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item);
		final ArrayAdapter<String> adapterForFreqSpinner = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item);
		
		final Spinner schedSpinner = (Spinner) findViewById(R.id.Spinner01);
		final Spinner freqSpinner = (Spinner) findViewById(R.id.Spinner02);
		final Button slideHandleButton;
		Button shedHelpButton = (Button) findViewById(R.id.shedHelpButton);
		shedHelpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSchedHelp();
				
			}
		});
		Button freqHelpButton = (Button) findViewById(R.id.freqHelpButton);
		freqHelpButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showFreqLimitHelp();
				
			}
		});
		SlidingDrawer slidingDrawer;
		slideHandleButton = (Button) findViewById(R.id.slideHandleButton);
		slidingDrawer = (SlidingDrawer) findViewById(R.id.SlidingDrawer);
		slidingDrawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			
			@Override
			public void onDrawerOpened() {
				slideHandleButton.setBackgroundResource(R.drawable.tray_handle_tab);
				
				//slideHandleButton.
			}
		});

		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				//if (getStates())
					
				 slideHandleButton.setBackgroundResource(R.drawable.tray_handle_tab);
			}
		});

		data.setFrequencyAdapter(new FrequencyListAdapter(
				this.getApplicationContext()));

		final Handler uIRefreshHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH:
					/* Refresh UI */
					
					
					RelativeLayout rl = (RelativeLayout) findViewById(R.id.contentLayout);
				
					// getExpandableListView().getExpandableListAdapter().;
					for (int i = 0; i < data.getmFqList().size(); i++) {
						RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
								240,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						data.getmFqList().get(i).getCheckbox()
								.setChecked(data.getmFqList().get(i).isenabled());
						
						
						if ((i % 2) != 0) {
							rllp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
							if (i > 1)
								rllp.addRule(RelativeLayout.ALIGN_LEFT, 0);

						}
						if (i > 0) {
							if (i == 2)
								rllp.addRule(RelativeLayout.BELOW, 1);
							else
								rllp.addRule(RelativeLayout.BELOW, i - 2);

						}
						
						View view = data.getmFqList().get(i).getCheckbox();
						view.setId(i);
						
						rl.addView(view, i, rllp);
						
					}
					View helptext = findViewById(R.id.sliderHelpText);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					helptext.setLayoutParams(lp);
					if (!getStates())
						findViewById(R.id.SlidingDrawer).setVisibility(
								View.GONE);

					OnItemSelectedListener schedSpinnerOnSelect = new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View arg1, int arg2, long arg3) {
							data.setSched_active((int) parent.getSelectedItemId());

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					};
					OnItemSelectedListener freqSpinnerOnSelect = new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View arg1, int arg2, long arg3) {
							data.setMax_frequency(parent.getSelectedItem().toString());
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					};
					freqSpinner.setOnItemSelectedListener(freqSpinnerOnSelect);
					freqSpinner.setAdapter(adapterForFreqSpinner);
					schedSpinner.setAdapter(adapterForSchedSpinner);
					schedSpinner.setSelection(data.getSched_active());
					schedSpinner
							.setOnItemSelectedListener(schedSpinnerOnSelect);
					((FrequencyListAdapter) data.getFrequencyAdapter())
							.setFrequencies(data.getmFqList());
					setListAdapter(data.getFrequencyAdapter());
					int i = 0;
					while (freqSpinner.getCount() > i
							&& !freqSpinner.getItemAtPosition(i).toString()
									.contains(data.getMax_freq())) {
						i++;
					}
					// Toast.makeText(getBaseContext(), Integer.toString(i),
					// Toast.LENGTH_LONG).show();
					// Toast.makeText(getBaseContext(), max_freq,
					// Toast.LENGTH_LONG).show();
					freqSpinner.setSelection(i);
					// Toast.makeText(getBaseContext(),
					// adapterForSpinner.getItem(5).substring(0,
					// adapterForSpinner.getItem(5).length()-4 ),
					// Toast.LENGTH_LONG).show();
					break;

				case NOROOT:
					showNoRootAlert();
					break;
				case WRONGKERNEL:
					showWrongKernelAlert();
					break;

				}
				;

			}
		};

		final ProgressDialog spinnerDialog = ProgressDialog.show(this,
				"Initializing...",
				"Shh! I'm busy doing poorly written system checks now...", true);
		new Thread(new Runnable() {

			public void run() {

				String tester = null;
				if (ShellInterface.isSuAvailable()) {
					tester = ShellInterface
							.getProcessOutput(C_UV_MV_TABLE);
				}
				// Toast.makeText(this, tester, Toast.LENGTH_LONG).show();
				if (tester != null) {
					if (tester == "") {
						uIRefreshHandler.sendEmptyMessage(WRONGKERNEL);
					} else {
						FrequencyListAdapter.getfrequency_voltage_table();
						String[] freq_table = gettime_in_state();
						getfreq_table(tester, freq_table);
						getStates();
							
						 try {
		                        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
		                                "sched.sh", 0));
		                        out.write("# Set \"$1\" scheduler for stl, bml and mmc\nfor i in `ls /sys/block/stl*` /sys/block/bml* /sys/block/mmcblk*\ndo\necho \"$1\" > $i/queue/scheduler\ndone");
		                        out.close();
		                    } catch (java.io.IOException e) {
		                    }
		                    ShellInterface.runCommand("chmod 777 /data/data/com.darekxan.voltagecontrol/files/sched.sh");
						
						adapterForFreqSpinner
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

						for (int i = 0; i < data.getmFqList().size(); i++) {
							adapterForFreqSpinner.add(String.valueOf(data.getmFqList()
									.get(i).getValue()) + " Mhz");
						}

						data.setMax_freq(ShellInterface
								.getProcessOutput(C_SCALING_MAX_FREQ));
						if (data.getMax_freq() == null) {
							data.setMax_freq(new String(""));
						}
						if (data.getMax_freq().equals("")) {
							data.getMax_freq().concat("0 0");
						}
						data.setMax_freq(data.getMax_freq().substring(0, data.getMax_freq().length() - 4));
						adapterForSchedSpinner
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						String schedulers = ShellInterface
								.getProcessOutput("cat /sys/block/mmc"
										+ "blk0/queue/scheduler");
						data.setSched_table(schedulers.split(" "));

						for (int i2 = 0; i2 < data.getSched_table().length; i2++) {
							if (data.getSched_table()[i2].contains("[")) {
								data.setSched_active(i2);
								data.getSched_table()[i2] = data.getSched_table()[i2].substring(1,
										data.getSched_table()[i2].length() - 1);
							}
							;
							adapterForSchedSpinner.add(data.getSched_table()[i2]);
						}
						uIRefreshHandler.sendEmptyMessage(REFRESH);
					}
				} else {
					uIRefreshHandler.sendEmptyMessage(NOROOT);
				}
				spinnerDialog.dismiss();
				return;
			}

			private void getfreq_table(String tester, String[] freq_table) {
				String[] uv_table;
				data.setUv_values(tester);
				if (data.getUv_values() == null) {
					data.setUv_values(new String(""));
				}
				if (data.getUv_values().equals("")) {
					for (int i = 0; i < freq_table.length; i += 2) {
						data.getmFqList().add(new ProcessorFrequency(Integer
								.parseInt(freq_table[i]) / 1000, 0,
								new CheckBox(getBaseContext())));
					}
				} else {
					uv_table = data.getUv_values().split(" ");
					for (int i = 0; i < freq_table.length; i += 2) {
						data.getmFqList().add(new ProcessorFrequency(Integer
								.parseInt(freq_table[i]) / 1000,
								Integer.parseInt(uv_table[i / 2]),
								new CheckBox(getBaseContext())));
					}

				}
			}

			private String[] gettime_in_state() {
				data.setTime_in_state(ShellInterface
						.getProcessOutput(C_TIME_IN_STATE));
				// not needed anymore?
				if (data.getTime_in_state() == null) {
					data.setTime_in_state(new String(""));
				}
				if (data.getTime_in_state().equals("")) {
					data.getTime_in_state().concat("0 0");
				}
				String[] freq_table = data.getTime_in_state().split(" ");
				return freq_table;
			}

		
		}).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		data.setmMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (R.id.exit): {
			this.finish();
			return true;
		}
		case (R.id.apply): {
			applySettings();
			return true;
		}
		case (R.id.reset): {
			Toast.makeText(this, "Not implemented yet :(", Toast.LENGTH_SHORT)
					.show();
			return true;
		}
		case (R.id.boot): {
			saveBootSettings();
			return true;
		}
		case (R.id.noboot): {
			deleteBootSettings();
			return true;
		}
		case (R.id.about): {
			showAboutScreen();
			return true;
		}
		}
		return true;

	}

	boolean getStates() {
		String states_enabled_table_tmp = ShellInterface
				.getProcessOutput(C_STATES_ENABLED);
		try {
			String[] states_enabled_table = states_enabled_table_tmp.split(" ");
			for (int i = 0; i < data.getmFqList().size(); i++) {
				if (states_enabled_table[i].equals("1"))
					data.getmFqList().get(i).isenabled(Boolean.TRUE);
				else
					data.getmFqList().get(i).isenabled(Boolean.FALSE);
	
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private void applySettings() {
		ShellInterface.runCommand(build_uv_command());
		
		ShellInterface
				.runCommand("/data/data/com.darekxan.voltagecontrol/files/sched.sh "
						+ data.getSched_table()[data.getSched_active()]);
		
		ShellInterface
				.runCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/update_states");
		ShellInterface
				.runCommand("echo "
						+ data.getMax_frequency().split(" ")[0]
						+ "000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
		setStates();
	}

	private StringBuilder build_states_enabled_command() {
		StringBuilder command = new StringBuilder();
		command.append("echo \"");
	
		for (int i = 0; i < data.getmFqList().size(); i++) {
			if (data.getmFqList().get(i).isenabled())
				command.append("1 ");
			else
				command.append("0 ");
		}
		command.append("\" > /sys/devices/system/cpu/cpu0/cpufreq/states_enabled_table");
		return command;
	}

	private String build_uv_command() {
	
		StringBuilder command = new StringBuilder();
		command.append("echo \"");
		for (int i = 0; i < data.getmFqList().size(); i++) {
			command.append(data.getmFqList().get(i).getUv() + " ");
		}
		command.append("\" > /sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table");//
	
		return command.toString();
	
	}

	private void deleteBootSettings() {
		if (!ShellInterface.getProcessOutput("ls /etc/init.d/").contains(
				"S_volt_scheduler")) {
			Toast.makeText(this, "No settings file present!",
					Toast.LENGTH_SHORT).show();
		} else {
			ShellInterface.runCommand("busybox mount -o remount,rw  /system");
			ShellInterface.runCommand("rm /etc/init.d/S_volt_scheduler");
			ShellInterface.runCommand("busybox mount -o remount,ro  /system");
			Toast.makeText(this, "Settings deleted!", Toast.LENGTH_SHORT)
					.show();
		}
		;
	}



	private void saveBootSettings() {
		try {
			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
					"S_volt_scheduler", 0));
			String tmp = "#!/system/bin/sh\n#set UV\n"
					+ build_uv_command()
					+ "\necho "
					+ data.getMax_frequency().split(" ")[0]
					+ "000 > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq"
					+ "\n#select enabled states\n"
					+ build_states_enabled_command()
					+ "\n#set scheduler for stl, bml and mmc\nfor i in `ls /sys/block/stl*` /sys/block/bml* /sys/block/mmcblk*\ndo\necho \""
					+ data.getSched_table()[data.getSched_active()]
					+ "\" > $i/queue/scheduler\ndone\n";
			out.write(tmp);
			out.close();
		} catch (java.io.IOException e) {
			Toast.makeText(this, "ERROR: file not saved!", Toast.LENGTH_LONG)
					.show();
		}

		ShellInterface
				.runCommand("chmod 777 /data/data/com.darekxan.voltagecontrol/files/S_volt_scheduler");
		ShellInterface.runCommand("busybox mount -o remount,rw  /system");
		ShellInterface.runCommand("mkdir /etc/init.d");
		ShellInterface
				.runCommand("busybox cp /data/data/com.darekxan.voltagecontrol/files/S_volt_scheduler /etc/init.d/S_volt_scheduler");
		ShellInterface.runCommand("busybox mount -o remount,ro  /system");
		Toast.makeText(this,
				"Settings saved in file \"/etc/init.d/S_volt_scheduler\"",
				Toast.LENGTH_LONG).show();
	}

	private void setStates() {
		try {
		StringBuilder command = build_states_enabled_command();
		// Log.d("VC", command.toString());

		ShellInterface.runCommand(command.toString());
		}
		catch (Exception e) {}
	}

	private void showAboutScreen() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.app_name);
		builder.setMessage(R.string.about_text);
		builder.setTitle("About Voltage Control "
				+ getResources().getText(R.string.version));
		builder.setNegativeButton("Back",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		builder.setNeutralButton("Visit us on XDA",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						String url = "http://forum.xda-developers.com/showthread.php?t=829731";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});
		builder.setPositiveButton("Donate to author",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						String url = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=687NLE4LWTW5E";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
					}
				});

		AlertDialog alert = builder.create();

		alert.show();
	}

	private void showNoRootAlert() {
		setContentView(R.layout.blank);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.nosu_text);
		builder.setNegativeButton("Exit",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
		builder.setNeutralButton("More info",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						String url = "http://forum.xda-developers.com/showthread.php?t=829731";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
						finish();
					}
				});
		builder.setPositiveButton("Do it for me!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(getBaseContext(), "Not implemented yet :(", Toast.LENGTH_SHORT)
						.show();
						finish();
						
					}
				});
		builder.setTitle("No root available");
		builder.setCancelable(false);
		AlertDialog alert = builder.create();

		alert.show();
	}

	private void showSchedHelp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.sched_help_text);
				builder.setTitle(R.string.sched_help_header);
				builder.setNeutralButton("Ok", null);
		builder.setCancelable(true);
		AlertDialog alert = builder.create();
		alert.show();
	}
	private void showHowTo() { 
		//TODO
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		FrameLayout fl = (FrameLayout) findViewById(android.R.id.custom);
		View myView = null;
		 fl.addView(myView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		builder.setMessage(R.string.sched_help_text);
				builder.setTitle(R.string.sched_help_header);
				builder.setNeutralButton("Ok", null);
		builder.setCancelable(true);
		AlertDialog alert = builder.create();
		alert.show();
	}
	private void showFreqLimitHelp() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.freq_help_text);
				builder.setTitle(R.string.freq_help_header);
				builder.setNeutralButton("Ok", null);
		builder.setCancelable(true);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void showWrongKernelAlert() {
		setContentView(R.layout.blank);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.bkernel_text);
		builder.setNegativeButton("Exit",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
		builder.setNeutralButton("Get kernel from XDA",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						String url = "http://forum.xda-developers.com/showthread.php?t=829731";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
						finish();
					}
				});
		builder.setPositiveButton("Do it for me!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(getBaseContext(), "Not implemented yet :(", Toast.LENGTH_SHORT)
						.show();
						finish();
					}
				});
		builder.setTitle("Unsupported kernel detected");
		builder.setCancelable(false);
		AlertDialog alert = builder.create();

		alert.show();
	}
}
