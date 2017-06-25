package com.tcs.unixwrapper.model;

public class InputDataModel {

	public String batch = "";
    public String mode = "";
    public String file = "";
    public String port = "";
    public String sudo = "";
    public String script = "";
    public String location = "";
    
	public String getBatch() {
		return batch;
	}
	public void setBatch(String batch) {
		this.batch = batch;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getSudo() {
		return sudo;
	}
	public void setSudo(String sudo) {
		this.sudo = sudo;
	}
	public String getScript() {
		return script;
	}
	public void setScript(String script) {
		this.script = script;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
    

    
}
