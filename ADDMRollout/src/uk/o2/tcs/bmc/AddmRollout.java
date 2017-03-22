package uk.o2.tcs.bmc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


public class AddmRollout {
	
	//mailhost.uk.pri.o2.com 
	//172.17.4.41
	//

	public static void main(String[] args) throws JSchException, IOException, SftpException {
		String remoteHostUserName = "testuser";
		String remoteHostpassword = "tele2017";
		String remoteHostName = "172.17.207.93";
		
		//String remoteHostUserName = "root";
		//String remoteHostpassword = "letmein";
		//String remoteHostName = "snbas-rhvm-009";
		
		int port = 22;
		

			remoteCopy(remoteHostUserName, remoteHostName, remoteHostpassword);
			remoteShell(remoteHostUserName, remoteHostName, remoteHostpassword);


	}
	
	 public static void remoteCopy(String remoteHostUserName, String remoteHostName, String remoteHostpassword) throws JSchException, IOException, SftpException {
		    JSch js = new JSch();
		    Session s = js.getSession(remoteHostUserName, remoteHostName, 22);
		    s.setPassword(remoteHostpassword);
		    Properties config = new Properties();
		    config.put("StrictHostKeyChecking", "no");
		    s.setConfig(config);
		    s.connect();

		    Channel c = s.openChannel("sftp");
		    ChannelSftp ce = (ChannelSftp) c;

		    ce.connect();

		    ce.put("test.txt","test.txt");


		    ce.disconnect();
		    s.disconnect();    
	}
	
	 public static void remoteShell(String remoteHostUserName, String remoteHostName, String remoteHostpassword) throws JSchException, IOException {
		    JSch js = new JSch();
		    Session s = js.getSession(remoteHostUserName, remoteHostName, 22);
		    s.setPassword(remoteHostpassword);
		    Properties config = new Properties();
		    config.put("StrictHostKeyChecking", "no");
		    s.setConfig(config);
		    s.connect();

		    Channel c = s.openChannel("exec");
		    ChannelExec ce = (ChannelExec) c;
		    //ce.setPty(true);
		    //http://stackoverflow.com/questions/18534922/idea-regarding-sudo-su-user-in-jsch
		    

		    ce.setCommand("sudo -S -p '' pwd");
		    //ce.getSession().setPassword(remoteHostpassword);
		    







		    
		    ce.setErrStream(System.err);

		    ce.connect();
		    
		    InputStream in=c.getInputStream();
	          OutputStream out=c.getOutputStream();		    
	          out.write((remoteHostpassword+"\n").getBytes());
	          out.flush();

	          byte[] tmp=new byte[1024];
	          boolean isForceStop = false;
	          while( isForceStop == false ){              
	              while(in.available()>0 ){
	                  int i=in.read(tmp, 0, 1024);
	                  if(i<0)break;
	              }
	              if(ce.isClosed()){
	                  break;
	              }


	              try{Thread.sleep(100);}catch(Exception ee){}
	          }   
	          
		    BufferedReader reader = new BufferedReader(new InputStreamReader(ce.getInputStream()));
		    String line;
		    while ((line = reader.readLine()) != null) {
		      System.out.println(line);
		    }

		    ce.disconnect();
		    s.disconnect();

		    System.out.println("Exit code: " + ce.getExitStatus());

	}

}
