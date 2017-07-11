package com.tdc.common;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;


public class Mail {

	public void sendMail(String from, String to[], String cc[], String bcc[], String subject, String body, String host) {
		
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		
		Session session = Session.getInstance(props, null);
				
		try {
		    // create a message
		    Message msg = new MimeMessage(session);
		    msg.setFrom(new InternetAddress(from));

		    InternetAddress[] address_to = new InternetAddress[to.length];
		    for (int i=0; i<to.length; i++){
		    	address_to[i] = new InternetAddress(to[i]);
		    }
		    msg.setRecipients(Message.RecipientType.TO, address_to);
		    
		    if(cc.length > 0){
			    InternetAddress[] address_cc = new InternetAddress[cc.length];
			    for (int i=0; i<cc.length; i++){
			    	address_cc[i] = new InternetAddress(cc[i]);
			    }
			    msg.setRecipients(Message.RecipientType.CC, address_cc);
		    }
		    if(bcc.length > 0){
			    InternetAddress[] address_bcc = new InternetAddress[bcc.length];
			    for (int i=0; i<bcc.length; i++){
			    	address_bcc[i] = new InternetAddress(bcc[i]);
			    }
			    msg.setRecipients(Message.RecipientType.BCC, address_bcc);
		    }
		    msg.setSubject(subject);
		    msg.setContent(body, "text/html");
		    msg.setSentDate(new Date());
		    
		    Transport.send(msg);
		    System.out.println("Successfully Sent.....");
		} catch (MessagingException mex) {
		    System.out.println("\n--Exception handling in Mail.java");
		    mex.printStackTrace();
		}
	}

	public void sendMail(String from, String to[], String cc[], String bcc[], String subject, String body, String host, String f1, String fileName) {
		
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		
		Session session = Session.getInstance(props, null);
				
		try {
		    // create a message
		    Message msg = new MimeMessage(session);
		    msg.setFrom(new InternetAddress(from));

		    InternetAddress[] address_to = new InternetAddress[to.length];
		    for (int i=0; i<to.length; i++){
		    	address_to[i] = new InternetAddress(to[i]);
		    }
		    msg.setRecipients(Message.RecipientType.TO, address_to);
		    
		    if(cc.length > 0){
			    InternetAddress[] address_cc = new InternetAddress[cc.length];
			    for (int i=0; i<cc.length; i++){
			    	address_cc[i] = new InternetAddress(cc[i]);
			    }
			    msg.setRecipients(Message.RecipientType.CC, address_cc);
		    }
		    if(bcc.length > 0){
			    InternetAddress[] address_bcc = new InternetAddress[bcc.length];
			    for (int i=0; i<bcc.length; i++){
			    	address_bcc[i] = new InternetAddress(bcc[i]);
			    }
			    msg.setRecipients(Message.RecipientType.BCC, address_bcc);
		    }
		    msg.setSubject(subject);
		    //msg.setContent(body, "text/html");
		    msg.setSentDate(new Date());

	        // create and fill the first message part
	        MimeBodyPart mbp = new MimeBodyPart();
	        //mbp.setText(body);
	        mbp.setContent(body, "text/html");

	        // create and fill the second message part
	        MimeBodyPart mbp1 = new MimeBodyPart();
	        DataSource source = new FileDataSource(f1);
	        mbp1.setDataHandler(new DataHandler(source));
	        mbp1.setFileName(fileName);

	        // create the Multipart and its parts to it
	        Multipart mp = new MimeMultipart();
	        mp.addBodyPart(mbp);
	        mp.addBodyPart(mbp1);
	   
	        // add the Multipart to the message
	        msg.setContent(mp);
		    
		    Transport.send(msg);
		    System.out.println("Successfully Sent.....");
		} catch (MessagingException mex) {
		    System.out.println("\n--Exception handling in MailSend.java");
		    mex.printStackTrace();
		}
	}
	
    public static void main (String args[]) 
    {
    	Mail m = new Mail();
    	
    	String to_arr [] = {"mura@tdc.dk"};
    	String cc_arr [] = {"mura@tdc.dk"};
    	String bcc_arr [] = {};
    	
    	
    	String body = "";
    	
    	body = "<html>" +
    			"<body style=\"text-align: left; background-color: #FFFFFF; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12px;\">" +
    			"<table border=0 style=\"text-align: left; background-color: #FFFFFF; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12px;\" width=\"80%\">" +
    			"<tr><td>" +
    			"Hello Murugavel Ramachandran<br/><br/>" +
    			"You have recently been in contact with TDC Service Desk.<br/><br/>" +
    			"Your request is registered in our HP Service Manager system with case number below<br/><br/>" +
    			"<table border=0 style=\"border: 1px solid black;text-align: left; background-color: #FFFFFF; font-family: Verdana, Arial, Helvetica, sans-serif; font-size: 12px;\" width=\"80%\">" +
    			"<tr bgcolor=\"#66FF99\"><td width=\"12%\">Request #</td><td width=\"40%\">Descriptions</td><td width=\"15%\">open date</td><td width=\"15%\">close date</td><td width=\"20%\">Remarks</td></tr>" +
    			"<tr bgcolor=\"#E0F8F7\"><td>SD79365</td><td>TEST</td><td>01-OCT-2012</td><td>02-OCT-2012</td><td>Resolved</td></tr>" +
    			"</table>" +
    			"<br/>" +
    			"We welcome your feedback on how your request has been handled.<br/><br/>" +
    			"Please activate enclosed 'link' that will direct you to our customer survey. Answer the questions, and add to any of your own comments.<br/><br/>" +
    			"User Satisfaction Survey is not expected to take more than 3 minutes of your time.<br/><br/>" +
    			"<a href=\"http://localhost:8080/survey/pages/Login.action?ref=123456789\" style=\"\">Click here to start the survey</a><br/><br/>" +
    			"Your feedback is vital to our quality and success.<br/><br/>" +
    			"Please remember to comment on any negative responses<br/><br/>" +
    			"Thank you in advance for your help.<br/><br/>" +
    			"</td></tr>" +
    			"</body>" +
    			"</html>";
    	
    	
    	
    	m.sendMail("hptools@tdc.dk", to_arr, cc_arr, bcc_arr, "Survey Sample Mail", body, "10.106.95.16");
    }
}
