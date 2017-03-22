/**
 * 
 */
package uk.o2.tcs.bmc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Murugavel Ramachandran
 *
 */
public class SSHRemoteManager {

	private String username;
	private String password;
	private String hostname;
	
	private Session session;
	private ChannelExec channel;
	
	public SSHRemoteManager(String hostname, String username, String password) {
		// TODO Auto-generated constructor stub
		this.username = username;
		this.password = password;
		this.hostname = hostname;
	}
	
	private Session getSession(){
		if(session == null || !session.isConnected()){
	        session = connect(hostname,username,password);
	    }
	    return session;
	}

	private Channel getChannel(){
	    if(channel == null || !channel.isConnected()){
	        try{
	            channel = (ChannelExec)getSession().openChannel("exec");
	            channel.connect();
	        }catch(Exception e){
	            System.out.println("Error while opening channel: "+ e);
	        }
	    }
	    return channel;
	}
	
	private Session connect(String hostname, String username, String password){

	    JSch jSch = new JSch();

	    try {
	    	session = jSch.getSession(username, hostname, 22);
	        session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
	        session.setPassword(password);
	        Properties config = new Properties(); 
	        config.put("StrictHostKeyChecking", "no");
	        session.setConfig(config);
	        session.connect(60000);
	        System.out.println("Connected!");
	    }catch(Exception e){
	        System.out.println("An error occurred while connecting to "+hostname+": "+e);
	    }

	    return session;

	}

	public String executeCommand(String command)
	  {
	     StringBuilder outputBuffer = new StringBuilder();

	     try
	     {
	        Channel channel = getSession().openChannel("exec");
	        ((ChannelExec)channel).setCommand(command);
	        InputStream commandOutput = channel.getInputStream();
	        channel.connect();
	        int readByte = commandOutput.read();

	        while(readByte != 0xffffffff)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
	        }

	        channel.disconnect();
	     }
	     catch(IOException ioX)
	     {
	        return null;
	     }
	     catch(JSchException jschX)
	     {
	        return null;
	     }

	     return outputBuffer.toString();
	  }

	public String executeSudoCommand(String sudoPath, String password, String command)
	{
	     StringBuilder outputBuffer = new StringBuilder();
	     System.out.println("sudoPath="+sudoPath);
	     try
	     {
	        Channel channel = getSession().openChannel("exec");
	        
	        System.out.println(sudoPath+" -S -p '' "+command);
	        
	        ((ChannelExec)channel).setCommand(sudoPath+" -S -p '' "+command);
	        
	        InputStream commandOutput = channel.getInputStream();
	        OutputStream out=channel.getOutputStream();
	        ((ChannelExec)channel).setErrStream(System.err);
	        
	        channel.connect();
	        
	        out.write((password+"\n").getBytes());
	        out.flush();
	        
	        int readByte = commandOutput.read();

	        while(readByte != 0xffffffff)
	        {
	           outputBuffer.append((char)readByte);
	           readByte = commandOutput.read();
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
	
	public void close(){
	    session.disconnect();
	    System.out.println("Disconnected session");
	}
	
	
	public static boolean checkUserConnection(String remoteHostUserName, String remoteHostName, String remoteHostpassword) {
		 
	 	System.out.println(remoteHostName);
	 	System.out.println(remoteHostUserName);
	 	System.out.println(remoteHostpassword);
	    
	    boolean flag;
	    try{
	    	JSch js = new JSch();
	    	Session s = js.getSession(remoteHostUserName, remoteHostName, 22);
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

	 public boolean remoteCopy(String scriptFile) {
		 try
	     {
			 Channel c = getSession().openChannel("sftp");
			 ChannelSftp ce = (ChannelSftp) c;
			 ce.connect();
			 ce.put(scriptFile,"/tmp/"+scriptFile);
			 ce.disconnect();
			 return true;
	     }catch(Exception ex){
	    	 ex.printStackTrace();
	    	 return false;
	     }
	}
	
}
