package uk.o2.tcs.bmc;

import java.util.Properties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JschRemote {
	
	 public boolean checkUserConnection(String remoteHostUserName, String remoteHostName, String remoteHostpassword) {
		 
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
		    	//throws JSchException, IOException, SftpException {
		    }
		    return flag;
	}

}
