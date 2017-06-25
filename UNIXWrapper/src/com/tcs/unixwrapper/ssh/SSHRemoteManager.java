/**
 * 
 */
package com.tcs.unixwrapper.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * @author Murugavel Ramachandran
 *
 */

// https://gist.github.com/kdelfour/5f1fde64c3d23daea704
public class SSHRemoteManager {

	private String username;
	private String password;
	private String hostname;
	private int port;
	
	private Session session;
	
	public SSHRemoteManager(String hostname, String username, String password, String port) {
		this.username = username;
		this.password = password;
		this.hostname = hostname;
		if(port.equalsIgnoreCase("")) port = "22";
		this.port = Integer.parseInt(port);
	}
	
	private Session getSession(){
		if(session == null || !session.isConnected()){
	        session = connect(hostname,username,password,port);
	    }
	    return session;
	}

	private Session connect(String hostname, String username, String password, int port){

	    JSch jSch = new JSch();
	    try {
	    	session = jSch.getSession(username, hostname, port);
	        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
	        session.setPassword(password);
	        Properties config = new Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect(60000);
	        //System.out.println("Connected!");
	    }catch(Exception e){
	        System.out.println("An error occurred while connecting to "+hostname+": "+e);
	    }
	    return session;
	}

	public String executeCommand(String command)
	  {
	     StringBuilder outputBuffer = new StringBuilder();
	     //System.out.println(command);
	     try
	     {
	        Channel channel = getSession().openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        InputStream commandOutput = channel.getInputStream();
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        channel.connect();
	        
	        int count = 1;
	        int readByte = commandOutput.read();
	        while(readByte != 0xffffffff && commandOutput.available()>0)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
	           if(count>1000){
	        	   break;
		       }
	           count++;
	        }

	        channel.disconnect();
	     }
	     catch(IOException ioX)
	     {
	        return ioX.getMessage();
	     }
	     catch(JSchException jschX)
	     {
	        return jschX.getMessage();
	     }

	     return outputBuffer.toString();
	  }
	
	public String executeShellCommand(String command)
	  {
	     StringBuilder outputBuffer = new StringBuilder();
	     //System.out.println(command);
	     try
	     {
	        Channel channel = getSession().openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        ((ChannelExec) channel).setPty(true);
	        
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        channel.connect();
	        InputStream commandOutput = channel.getInputStream();
	        int count = 1;
	        int readByte = commandOutput.read();
	        while(readByte != 0xffffffff && commandOutput.available()>0)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
	           if(count>1000){
	        	   break;
		       }
	           count++;
	        }
	        try { Thread.sleep(500); } catch(Exception ex){}
	        channel.disconnect();
	     }
	     catch(IOException ioX)
	     {
	        return ioX.getMessage();
	     }
	     catch(JSchException jschX)
	     {
	        return jschX.getMessage();
	     }

	     return outputBuffer.toString();
	  }
	
	public String executeShellCommandForScript(String command)
	  {
	     StringBuilder outputBuffer = new StringBuilder();
	     //System.out.println(command);
	     try
	     {
	        Channel channel = getSession().openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        ((ChannelExec) channel).setPty(true);
	        
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        channel.connect();
	        InputStream commandOutput = channel.getInputStream();
	        int count = 1;
	        int readByte = commandOutput.read();
	        while(readByte != 0xffffffff && commandOutput.available()>0)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
	           if(count>1000){
	        	   break;
		       }
	           count++;
	        }
	        try { Thread.sleep(5000); } catch(Exception ex){}
	        channel.disconnect();
	     }
	     catch(IOException ioX)
	     {
	        return ioX.getMessage();
	     }
	     catch(JSchException jschX)
	     {
	        return jschX.getMessage();
	     }

	     return outputBuffer.toString();
	  }
	
	
	public void close(){
	    session.disconnect();
	    //System.out.println("\nDisconnected session");
	}
	
	
	public static boolean checkUserConnection(String remoteHostUserName, String remoteHostName, String remoteHostpassword, String port) {
		 
	 	if(port.equalsIgnoreCase("")) port = "22";
	 	
	    boolean flag;
	    try{
	    	JSch js = new JSch();
	    	Session s = js.getSession(remoteHostUserName, remoteHostName, Integer.parseInt(port));
	    	s.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
		    s.setPassword(remoteHostpassword);
		    Properties config = new Properties();
		    config.put("StrictHostKeyChecking", "no");
		    s.setConfig(config);
		    s.connect();
		    flag = true;
		    s.disconnect();  		    	
	    }catch(Exception e){
	    	flag = false;
	    }
	    return flag;
	}

	 public boolean remoteCopy(String scriptFile, String destinationFile) {
		 try
	     {
			 Channel c = getSession().openChannel("sftp");
			 ChannelSftp ce = (ChannelSftp) c;
			 ce.connect();
			 ce.put(scriptFile,destinationFile);
			 ce.disconnect();
			 return true;
	     }catch(Exception ex){
	    	 ex.printStackTrace();
	    	 return false;
	     }
	}
	
	public String removePassEntry(String str, String pass)
	{
		str = str.replaceAll(pass, "");
		return str;
	}
	
}
