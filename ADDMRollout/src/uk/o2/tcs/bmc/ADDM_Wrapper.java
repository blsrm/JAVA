/**
 * 
 */
package uk.o2.tcs.bmc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import uk.o2.tcs.bmc.model.ADDMDataModel;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * @author Murugavel Ramachandran
 *
 */
public class ADDM_Wrapper {
	
	
	ArrayList<String> excel_input_arr = new ArrayList<String>();
	ArrayList<ADDMDataModel> addm_model_arr = new ArrayList<ADDMDataModel>();
	
    public static String batch = "";
    public static String mode = "";
    public static String file = "";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	    // ADDM_Wrapper.java [-batch T4] -mode scan/update -file excelfilename 
	    for (int i=0; i < args.length; i++) {
	         switch (args[i].charAt(0)) {
	         case '-':
	             if (args[i].charAt(1) == '-') {
	                 int len = 0;
	                 String argstring = args[i].toString();
	                 len = argstring.length();
	                 if(argstring.substring(2, len).toString().equalsIgnoreCase("batch")){ batch = args[i+1]; } 
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("mode")){ mode = args[i+1]; }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("file")){ file = args[i+1]; }
	                 i= i+1;
	             } else {
	            	 int len = 0;
	                 String argstring = args[i].toString();
	                 len = argstring.length();
	                 if(argstring.substring(1, len).toString().equalsIgnoreCase("batch")){ batch = args[i+1]; } 
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("mode")){ mode = args[i+1]; }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("file")){ file = args[i+1]; }
	                 i= i+1;
	             }           
	             break;         
	         default:            
	         	break;         
	         }     
		}
		
	    if(mode.equalsIgnoreCase("") || file.equalsIgnoreCase("") || args.length < 4){
	    	System.out.println("ADDM Rollout Wrapper Tool to connect Linux, Solaris and HP-UX");
	    	System.out.println("Usage: ADDM_Wrapper.java [-batch T4] -mode [scan/update] -file excelfilename");
	    } else {
			ADDM_Wrapper addm = new ADDM_Wrapper();
			// Collect host, password and other details
			addm.collectADDMHosts(file, batch);
			addm.processingADDMData();	    	
	    }
	}
	
	public void processingADDMData() {
		
		Iterator<String> itr = excel_input_arr.iterator();
		int count = 1;
		
		while(itr.hasNext()){
			
			//Assign Variables
			String hostname = "";
			String username = "";
			String password = "";
			
			String line = itr.next();
			String data_arr[] = line.split("\\|");
			
			hostname = data_arr[0];
			
			System.out.println("Processing ADDM Rollout task for hostname '"+hostname+"' - "+count+" of "+excel_input_arr.size());
			
			//Checking User and password authentication 
			String userPassList = data_arr[2];
			
			if(! userPassList.equalsIgnoreCase("")){
				String userPassArr[] = userPassList.split(",");
				for(int i=0; i<userPassArr.length; i++){
					String userPair[] = userPassArr[i].toString().split(":");
					boolean userFlag = SSHRemoteManager.checkUserConnection(userPair[0], data_arr[0], userPair[1]);
					username = userPair[0];
					password = userPair[1];
					System.out.println(userFlag);
					if(userFlag){
						break;
					}
				}
			}else {
				System.out.println("Update password entry in excel for hostname="+hostname);
			}
			
			scanOrUpdateHostForADDM(hostname, username, password);

			count++;
		}
		
	}
	
	public void scanOrUpdateHostForADDM(String hostname, String username, String password){
		
		ADDMDataModel addmModelObj = new ADDMDataModel();
		SSHRemoteManager sshObj = new SSHRemoteManager(hostname, username, password);
		
		String osName = clean(sshObj.executeCommand("uname -s"));
		String sudoPath = clean(sshObj.executeCommand("which sudo"));
		
		System.out.println("osName="+osName+"=");
		
		String scriptFile = "";
		String scriptLog = "";
		String nslookupPath = "";

		if(osName.equalsIgnoreCase("HP-UX")){
			sudoPath = "/usr/local/bin/sudo";
			nslookupPath = "nslookup";
			scriptFile = "addm_hp-ux.sh";
		}else if(osName.equalsIgnoreCase("Linux")){
			scriptFile = "addm_linux.sh";
			nslookupPath = "nslookup";
		} else if(osName.equalsIgnoreCase("SunOS")){
			scriptFile = "addm_sunos.sh";
			nslookupPath = "/usr/sbin/nslookup";
		}
		
		//Based on OS, select script to execute in corresponding OS
		if(mode.equalsIgnoreCase("update")){
			// Script file copy to server
			boolean copyFlag = sshObj.remoteCopy(scriptFile);
			System.out.println("copyFlag="+copyFlag+"=");
			
			// Script to be executed
			if(username.equalsIgnoreCase("root")){
				scriptLog = clean(sshObj.executeCommand("sh -x /tmp/"+scriptFile));
			} else {
				scriptLog = clean(sshObj.executeSudoCommand(sudoPath, password, "sh -x /tmp/"+scriptFile));
			}
			System.out.println("scriptLog="+scriptLog+"=");
		}

		//
		if(username.equalsIgnoreCase("root")){
			
		} else {
			String ipcheck = clean(sshObj.executeSudoCommand(sudoPath, password, nslookupPath+" "+hostname));
			System.out.println("nslookup="+ipcheck+"=");
			String out = sshObj.executeSudoCommand(sudoPath, password, "pwd");
			System.out.println(out);
		}
		
		sshObj.close();	
		
		//Object Model
		addmModelObj.setOsname(osName);

		
		addm_model_arr.add(addmModelObj);
		
		
	}
	
	public void collectADDMHosts(String fileName, String batch){
		
		try {

			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("UTF-8");
			
			//Create a workbook object from the file at specified location. 
			//Change the path of the file as per the location on your computer. 
			Workbook wrk1 =  Workbook.getWorkbook(new File(fileName), ws);
			
			//Obtain the reference to the first sheet in the workbook
			Sheet sheet1 = wrk1.getSheet(0);
			
            int columns = sheet1.getColumns();
            int rows = sheet1.getRows();
            String data;

            for (int row = 0; row < rows; row++) {
            	String line = "";
                for (int col = 0; col < columns; col++) {
                	data = sheet1.getCell(col, row).getContents();
                    line = line + data + "|";
                }
                line = line + "END"; // Just for dummy
                
                String line_temp = line;
                line_temp = line_temp.replaceAll("\\|", "");
                line_temp = line_temp.replaceAll("END", "");
                
                //System.out.println("CHECK:"+line_temp+":OK");

                if( ! line.contains("Host Name") && 
               		! line.contains("IP Endpint") && 
               		! line_temp.equalsIgnoreCase("")
                ){
                	if( batch.equalsIgnoreCase("") ){
                		excel_input_arr.add(line);
                		//System.out.println("MURU"+line);
                		//breakLine();
                	} else {
                		if(line.contains("|"+batch+"|")) {
                			excel_input_arr.add(line);
                		}
                	}
                }
                

            }
            
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
		    ce.put("test.txt","/tmp/test.txt");
		    ce.disconnect();
		    s.disconnect();    
	}
	 
	 
	 void breakLine(){
		 try{
			 BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			 String s = bufferRead.readLine();
			 System.out.println(s);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}		
	}
	 
	public String clean(String str){
		str = str.trim();
		return str;
	}
	
}
