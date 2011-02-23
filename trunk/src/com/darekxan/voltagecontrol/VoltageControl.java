package com.darekxan.voltagecontrol;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	
	private ExpandableListAdapter frequencyAdapter;
	String max_freq;
	protected int max_freq_id;
	private String max_frequency = " ";
	private ArrayList<ProcessorFrequency> mFqList = new ArrayList<ProcessorFrequency>();
	@SuppressWarnings("unused")
	private Menu mMenu;
	private int sched_active = 0;
	private String[] sched_table;
	Map<String, String> stock_voltages = new HashMap<String, String>();
	private String time_in_state;
	private String uv_values;

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
				// slideHandleButton.setBackgroundResource(R.drawable.openarrow);
			}
		});

		slidingDrawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				//if (getStates())
					
				// slideHandleButton.setBackgroundResource(R.drawable.closearrow);
			}
		});

		frequencyAdapter = new FrequencyListAdapter(
				this, this.getApplicationContext());

		final Handler uIRefreshHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case REFRESH:
					/* Refresh UI */
					
					
					RelativeLayout rl = (RelativeLayout) findViewById(R.id.contentLayout);
					// getExpandableListView().getExpandableListAdapter().;
					for (int i = 0; i < mFqList.size(); i++) {
						RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
								240,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						mFqList.get(i).getCheckbox()
								.setChecked(mFqList.get(i).isenabled());
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

						View view = mFqList.get(i).getCheckbox();
						view.setId(i);
						rl.addView(view, i, rllp);

					}
					if (!getStates())
						findViewById(R.id.SlidingDrawer).setVisibility(
								View.GONE);

					OnItemSelectedListener schedSpinnerOnSelect = new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View arg1, int arg2, long arg3) {
							sched_active = (int) parent.getSelectedItemId();

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					};
					OnItemSelectedListener freqSpinnerOnSelect = new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> parent,
								View arg1, int arg2, long arg3) {
							max_frequency = parent.getSelectedItem().toString();
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
						}
					};
					freqSpinner.setOnItemSelectedListener(freqSpinnerOnSelect);
					freqSpinner.setAdapter(adapterForFreqSpinner);
					schedSpinner.setAdapter(adapterForSchedSpinner);
					schedSpinner.setSelection(sched_active);
					schedSpinner
							.setOnItemSelectedListener(schedSpinnerOnSelect);
					((FrequencyListAdapter) frequencyAdapter)
							.setFrequencies(mFqList);
					setListAdapter(frequencyAdapter);
					int i = 0;
					while (freqSpinner.getCount() > i
							&& !freqSpinner.getItemAtPosition(i++).toString()
									.equals(max_freq + " Mhz")) {
					
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
				"Initializing",
				"I'm busy doing poorly written system checks now", true);
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
						getfrequency_voltage_table();
						String[] freq_table = gettime_in_state();
						getfreq_table(tester, freq_table);
						getStates();
						try {
	                        OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
	                                "sched.sh", 0));
	                        out.write("#!/system/bin/sh\n# Set \"$1\" scheduler for stl, bml and mmc\nfor i in `ls /sys/block/stl*` /sys/block/bml* /sys/block/mmcblk*\ndo\necho \"$1\" > $i/queue/scheduler\ndone");
	                        out.close();
	                    } catch (java.io.IOException e) {
	                    }
	                    ShellInterface.runCommand("chmod 777 /data/data/com.darekxan.voltagecontrol/files/sched.sh");

						
						adapterForFreqSpinner
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

						for (int i = 0; i < mFqList.size(); i++) {
							adapterForFreqSpinner.add(String.valueOf(mFqList
									.get(i).getValue()) + " Mhz");
						}

						max_freq = ShellInterface
								.getProcessOutput(C_SCALING_MAX_FREQ);
						if (max_freq == null) {
							max_freq = new String("");
						}
						if (max_freq.equals("")) {
							max_freq.concat("0 0");
						}
						max_freq = max_freq.substring(0, max_freq.length() - 4);
						adapterForSchedSpinner
								.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						String schedulers = ShellInterface
								.getProcessOutput("cat /sys/block/mmc"
										+ "blk0/queue/scheduler");
						sched_table = schedulers.split(" ");

						for (int i2 = 0; i2 < sched_table.length; i2++) {
							if (sched_table[i2].contains("[")) {
								sched_active = i2;
								sched_table[i2] = sched_table[i2].substring(1,
										sched_table[i2].length() - 1);
							}
							;
							adapterForSchedSpinner.add(sched_table[i2]);
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
				uv_values = tester;
				if (uv_values == null) {
					uv_values = new String("");
				}
				if (uv_values.equals("")) {
					for (int i = 0; i < freq_table.length; i += 2) {
						mFqList.add(new ProcessorFrequency(Integer
								.parseInt(freq_table[i]) / 1000, 0,
								new CheckBox(getBaseContext())));
					}
				} else {
					uv_table = uv_values.split(" ");
					for (int i = 0; i < freq_table.length; i += 2) {
						mFqList.add(new ProcessorFrequency(Integer
								.parseInt(freq_table[i]) / 1000,
								Integer.parseInt(uv_table[i / 2]),
								new CheckBox(getBaseContext())));
					}

				}
			}

			private String[] gettime_in_state() {
				time_in_state = ShellInterface
						.getProcessOutput(C_TIME_IN_STATE);
				// not needed anymore?
				if (time_in_state == null) {
					time_in_state = new String("");
				}
				if (time_in_state.equals("")) {
					time_in_state.concat("0 0");
				}
				String[] freq_table = time_in_state.split(" ");
				return freq_table;
			}

			private void getfrequency_voltage_table() {
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
		}).start();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Hold on to this
		mMenu = menu;
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
			for (int i = 0; i < mFqList.size(); i++) {
				if (states_enabled_table[i].equals("1"))
					mFqList.get(i).isenabled(Boolean.TRUE);
				else
					mFqList.get(i).isenabled(Boolean.FALSE);
	
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
						+ sched_table[sched_active]);
		
		ShellInterface
				.runCommand("echo 1 > /sys/devices/system/cpu/cpu0/cpufreq/update_states");
		ShellInterface
				.runCommand("echo \""
						+ max_frequency.split(" ")[0]
						+ "000\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq");
		setStates();
	}

	private StringBuilder build_states_enabled_command() {
		StringBuilder command = new StringBuilder();
		command.append("echo \"");
	
		for (int i = 0; i < mFqList.size(); i++) {
			if (mFqList.get(i).isenabled())
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
		for (int i = 0; i < mFqList.size(); i++) {
			command.append(mFqList.get(i).getUv() + " ");
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

	private Object getCommandOutput(String command) {
		try {
			return ShellInterface.getProcessOutput(command);
		} catch (Exception e) {
			return Boolean.FALSE;
		}
	}

	private void saveBootSettings() {
		try {
			OutputStreamWriter out = new OutputStreamWriter(openFileOutput(
					"S_volt_scheduler", 0));
			String tmp = "#!/system/bin/sh\n\nLOG_FILE=/data/volt_scheduler.log\nrm -Rf $LOG_FILE\n\necho \"Starting Insanity Volt Scheduler $( date +\"%m-%d-%Y %H:%M:%S\" )\" | tee -a $LOG_FILE;\n\necho \"Set UV\" | tee -a $LOG_FILE; \n"
					+ build_uv_command()
					+ "\necho \"\"\necho \"---------------\"\n\necho \"Set MAX Scaling Frequency\" | tee -a $LOG_FILE; \necho \""
					+ max_frequency.split(" ")[0]
					+ "000\" > /sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq\necho \"\"\necho \"---------------\"\n\necho \"Select Enabled States\" | tee -a $LOG_FILE; \n"
					+ build_states_enabled_command()
					+ "\necho \"\"\necho \"---------------\"\n\necho \"Set Scheduler for stl, bml and mmc\" | tee -a $LOG_FILE; \n    \nfor i in `ls /sys/block/stl*` /sys/block/bml* /sys/block/mmcblk* ; do\n\techo \""
					+ sched_table[sched_active]
					+ "\" > $i/queue/scheduler;\n\techo \"$i/queue/scheduler\";\n\techo \"---------------\";\ndone;\n\necho \"Insanity Volt Scheduler finished at $( date +\"%m-%d-%Y %H:%M:%S\" )\" | tee -a $LOG_FILE;\n";
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
		/*builder.setPositiveButton("Do it for me!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(getBaseContext(), "Not implemented yet :(", Toast.LENGTH_SHORT)
						.show();
						finish();
						
					}
				});*/
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
						String url = "http://forum.xda-developers.com/showthread.php?t=965103";
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivity(i);
						finish();
					}
				});
		/*builder.setPositiveButton("Do it for me!",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int id) {
						Toast.makeText(getBaseContext(), "Not implemented yet :(", Toast.LENGTH_SHORT)
						.show();
						finish();
					}
				});*/
		builder.setTitle("Unsupported kernel detected");
		builder.setCancelable(false);
		AlertDialog alert = builder.create();

		alert.show();
	}
}
