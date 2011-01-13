package com.darekxan.voltagecontrol;

import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ProcessorFrequency {
	private boolean state_enabled;
	private int value;
	private int uv;
	private int stock_voltage;
	CheckBox checkbox;
		
	public CheckBox getCheckbox() {
		return checkbox;
	}
	public void setCheckbox(CheckBox checkbox) {
		this.checkbox = checkbox;
	}
	public int stock_voltage(){
		return this.stock_voltage;
	}
	public void stock_voltage(int voltage)	{
		this.stock_voltage=voltage;
	}
	public boolean isenabled()
	{
	return state_enabled;
	}
	public void isenabled(boolean is)
	{
		this.state_enabled = is;
	}
	public int getUv() {
		return uv;
	}

	public void setUv(int uv) {
		this.uv = uv;
	}

	public ProcessorFrequency(int _value, int _uv, CheckBox checkbox) {
		this.value = _value;
		this.uv = _uv;
		this.checkbox = checkbox;
		OnCheckedChangeListener listener = new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO Auto-generated method stub
				isenabled(isChecked);
			}};
			this.checkbox.setOnCheckedChangeListener(listener);
			this.checkbox.setChecked(this.state_enabled);
			this.checkbox.setText(String.valueOf(this.value)+" Mhz");
		
	}

	public int getValue() {
		return value;
	}

	public void setValue(int _value) {
		this.value = _value;

	}
}