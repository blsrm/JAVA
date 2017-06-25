package com.tcs.unixwrapper.model;

import java.util.ArrayList;
import java.util.Hashtable;

public class OutputDataModel {
	
	public String hostname;
	public String username;
	public String password;
	public String osname;
	public String batch;
	public String ipaddress;
	public String mode;
	public ArrayList<String> excel_header_arr;
	public Hashtable<String, String> excel_body_hash;
	
	public String scriptlog;
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getOsname() {
		return osname;
	}
	public void setOsname(String osname) {
		this.osname = osname;
	}
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}
	public String getIpaddress() {
		return ipaddress;
	}
	public void setIpaddress(String ipaddress) {
		this.ipaddress = ipaddress;
	}
	public String getScriptlog() {
		return scriptlog;
	}
	public void setScriptlog(String scriptlog) {
		this.scriptlog = scriptlog;
	}
	public ArrayList<String> getExcel_header_arr() {
		return excel_header_arr;
	}
	public void setExcel_header_arr(ArrayList<String> excel_header_arr) {
		this.excel_header_arr = excel_header_arr;
	}
	public Hashtable<String, String> getExcel_body_hash() {
		return excel_body_hash;
	}
	public void setExcel_body_hash(Hashtable<String, String> excel_body_hash) {
		this.excel_body_hash = excel_body_hash;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}

}
