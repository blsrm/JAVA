package uk.o2.tcs.bmc;

import com.jcraft.jsch.*;

import java.awt.*;
import javax.swing.*;
import java.io.*;
import java.util.Properties;

public class Test2{
  public static void main(String[] arg){
    try{
      JSch jsch=new JSch();

		String remoteHostUserName = "testuser";
		String remoteHostpassword = "tele2017";
		String remoteHostName = "172.17.207.93";
		
		 //remoteHostUserName = "yhardik1";
		 //remoteHostpassword = "abcd1234";
		 //remoteHostName = "cnmlw-ivm-11";
		
	    Session session = jsch.getSession(remoteHostUserName, remoteHostName, 22);
	    session.setPassword(remoteHostpassword);
	    Properties config = new Properties();
	    config.put("StrictHostKeyChecking", "no");
	    session.setConfig(config);
	    session.connect(60000);

     //String command="mkdir /data";
      String command="which sudo";
      String sudo_pass=null;

      sudo_pass=remoteHostpassword;


      Channel channel=session.openChannel("exec");

      // man sudo
      // -S The -S (stdin) option causes sudo to read the password from the
      // standard input instead of the terminal device.
      // -p The -p (prompt) option allows you to override the default
      // password prompt and use a custom one.
      //((ChannelExec)channel).setCommand("/usr/local/bin/sudo -S -p '' "+command);
      
      ((ChannelExec)channel).setCommand("sudo -S -p '' "+command);


      InputStream in=channel.getInputStream();
      OutputStream out=channel.getOutputStream();
      ((ChannelExec)channel).setErrStream(System.err);

      channel.connect();

      out.write((sudo_pass+"\n").getBytes());
      out.flush();

      byte[] tmp=new byte[1024];
      while(true){
        while(in.available()>0){
          int i=in.read(tmp, 0, 1024);
          if(i<0)break;
          System.out.print(new String(tmp, 0, i));
        }
        if(channel.isClosed()){
          System.out.println("exit-status: "+channel.getExitStatus());
          break;
        }
        try{Thread.sleep(1000);}catch(Exception ee){}
      }
      channel.disconnect();
      session.disconnect();
    }
    catch(Exception e){
      System.out.println(e);
    }
  }
}