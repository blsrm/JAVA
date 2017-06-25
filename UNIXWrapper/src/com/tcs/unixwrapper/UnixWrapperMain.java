/**
 * 
 */
package com.tcs.unixwrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import com.tcs.unixwrapper.excel.ExcelReport;
import com.tcs.unixwrapper.model.InputDataModel;
import com.tcs.unixwrapper.model.OutputDataModel;
import com.tcs.unixwrapper.ssh.SSHRemoteManager;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * @author Murugavel Ramachandran
 *
 */
public class UnixWrapperMain {
	
	ArrayList<String> excel_input_arr = new ArrayList<String>();
	ArrayList<OutputDataModel> output_model_arr = new ArrayList<OutputDataModel>();
	Hashtable<String, ArrayList<String>> excel_header_hash  = new Hashtable<String, ArrayList<String>>();
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		InputDataModel inObj = new InputDataModel();

	    for (int i=0; i < args.length; i++) {
	         switch (args[i].charAt(0)) {
	         case '-':
	             if (args[i].charAt(1) == '-') {
	                 int len = 0;
	                 String argstring = args[i].toString();
	                 len = argstring.length();
	                 if(argstring.substring(2, len).toString().equalsIgnoreCase("batch")){ inObj.setBatch(args[i+1]); } 
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("mode")){ inObj.setMode(args[i+1]); }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("file")){ inObj.setFile(args[i+1]); }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("port")){ inObj.setPort(args[i+1]); }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("sudo")){ inObj.setSudo(args[i+1]); }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("script")){ inObj.setScript(args[i+1]); }
	                 else if(argstring.substring(2, len).toString().equalsIgnoreCase("location")){ inObj.setLocation(args[i+1]); }
	                 i= i+1;
	             } else {
	            	 int len = 0;
	                 String argstring = args[i].toString();
	                 len = argstring.length();
	                 if(argstring.substring(1, len).toString().equalsIgnoreCase("batch")){ inObj.setBatch(args[i+1]); } 
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("mode")){ inObj.setMode(args[i+1]); }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("file")){ inObj.setFile(args[i+1]); }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("port")){ inObj.setPort(args[i+1]); }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("sudo")){ inObj.setSudo(args[i+1]); }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("script")){ inObj.setScript(args[i+1]); }
	                 else if(argstring.substring(1, len).toString().equalsIgnoreCase("location")){ inObj.setLocation(args[i+1]); }
	                 i= i+1;
	             }           
	             break;         
	         default:            
	         	break;         
	         }     
		}
		
	    if(inObj.getMode().equalsIgnoreCase("") || inObj.getFile().equalsIgnoreCase("") || args.length < 4){
	    	System.out.println("\nUNIX Generic Wrapper Tool V 1.0 to connect Linux, Solaris and HP-UX");
	    	System.out.println("\n\tContact if any issues - Murugavel Ramachandran (349137)");
	    	System.out.println("\n\tUsage: UnixWrapperMain [-batch T4] -mode [scan/update/upload] -file excelfilename [-port 22] [-sudo enable/disable] [-script custom.sh/file to upload] [-location /tmp]");
	    } else {
	    	UnixWrapperMain uwm = new UnixWrapperMain();
			uwm.collectHostDetails(inObj.getFile(), inObj.getBatch());
			uwm.processingHostData(inObj);
	    }		

	}
	
	public void collectHostDetails(String fileName, String batch){
		
		try {
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("UTF-8");

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
                
                if( ! line.contains("Host Name") && 
               		! line.contains("IP Endpint") && 
               		! line_temp.equalsIgnoreCase("")
                ){
                	if( batch.equalsIgnoreCase("") ){
                		excel_input_arr.add(line);
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

	
	public void processingHostData(InputDataModel inObj) {
		
		Iterator<String> itr = excel_input_arr.iterator();
		int count = 1;
		
		while(itr.hasNext()){
			
			//Assign Variables
			String hostname = "";
			String username = "";
			String password = "";
			
			String usernameFailedList = "";
			
			boolean userFlag = false;
			
			String line = itr.next();
			String excel_data_arr[] = line.split("\\|");
			
			hostname = excel_data_arr[0];
			
			System.out.println("\nProcessing task for hostname '"+hostname+"' - "+inObj.getBatch()+"' - "+count+" of "+excel_input_arr.size()+"\n");
			
			//Checking User and password authentication 
			String userPassList = excel_data_arr[2];
			
			if(! userPassList.equalsIgnoreCase("")){
				String userPassArr[] = userPassList.split(",");
				for(int i=0; i<userPassArr.length; i++){
					String userPair[] = userPassArr[i].toString().split(":");
					userFlag = SSHRemoteManager.checkUserConnection(userPair[0], excel_data_arr[0], userPair[1], inObj.getPort());
					username = userPair[0];
					password = userPair[1];
					usernameFailedList += username + "\n";
					writeOutput("tab", "Trying to connect "+username+":*********@"+hostname+" .... "+ userFlag);
					if(userFlag){
						break;
					}
				}
			}else {
				System.out.println("\tUpdate password entry in excel for hostname="+hostname);
			}
			
			if(userFlag){
				scanOrUpdateHostForTask(hostname, username, password, excel_data_arr, inObj);
			}else {
				OutputDataModel outObj = new OutputDataModel();
				outObj.setHostname(hostname);
				outObj.setUsername(usernameFailedList);
				outObj.setBatch(inObj.getBatch());
				outObj.setMode(inObj.getMode());
				outObj.setScriptlog("Authentication issues for provided users to access server.");
				output_model_arr.add(outObj);
			}

			count++;
		}
		
		//Excel Report Generation
		writeOutput("", "\nGenerating Excel Report ...");
		ExcelReport excel = new ExcelReport();
		excel.processExcelReport(output_model_arr, inObj.getBatch(), excel_header_hash);
		
	}

	public void scanOrUpdateHostForTask(String hostname, String username, String password, String [] excel_data_arr, InputDataModel inObj){
		
		OutputDataModel outObj = new OutputDataModel();
		SSHRemoteManager sshObj = new SSHRemoteManager(hostname, username, password, inObj.getPort());
		
		String osName = clean(sshObj.executeShellCommand("uname -s"));
		String sudoSystemPath = clean(sshObj.executeShellCommand("which sudo"));
		
		String scriptFile = "";
		String scriptLog = "";
		String excelLog = "";
		String sudoPath = "";
		String destLocationPath = "";
		boolean sudoModeDisable = false;
		
		if(osName.equalsIgnoreCase("HP-UX")){
			sudoPath = "/usr/local/bin/sudo";
			scriptFile = "hp-ux.sh";
		}else if(osName.equalsIgnoreCase("Linux")){
			sudoPath = "/usr/bin/sudo";
			scriptFile = "linux.sh";
		} else if(osName.equalsIgnoreCase("SunOS")){
			sudoPath = "/usr/local/bin/sudo";
			scriptFile = "sunos.sh";
		}
		
		// Override custom script
		if(!inObj.getScript().equalsIgnoreCase("")){
			scriptFile = inObj.getScript();
		}
		
		// Override Sudo path
		if(!sudoSystemPath.equalsIgnoreCase("") && !sudoSystemPath.contains("no sudo in") ){
			sudoPath = sudoSystemPath;
		}
		
		// Override destination location path
		if(!inObj.getLocation().equalsIgnoreCase("")){
			destLocationPath = inObj.getLocation();
		} else {
			destLocationPath = "/tmp";
		}

		// Sudo enable yes or no for normal user?? for root, this is not applicable
		if(inObj.getSudo().equalsIgnoreCase("disable")){
			sudoModeDisable = true;
		}
		
		writeOutput("tab", "Mode="+inObj.getMode());
		writeOutput("tab", "osName="+osName);
		writeOutput("tab", "sudoPath="+sudoPath);
		writeOutput("tab", "destLocationPath="+destLocationPath);
		writeOutput("tab", "scriptFile="+scriptFile);
		writeOutput("tab", "sudoModeDisable="+sudoModeDisable);

		// Script file copy to server
		String destinationFile = "";
		if(destLocationPath.substring(destLocationPath.length()-1, destLocationPath.length()).equalsIgnoreCase("/") ){
			destinationFile = destLocationPath+scriptFile;
		} else {
			destinationFile = destLocationPath+"/"+scriptFile;
		}
		
		boolean copyFlag = sshObj.remoteCopy(scriptFile, destinationFile);
		writeOutput("tab", "File uploaded to Server="+copyFlag);
		
		if(copyFlag && ( inObj.getMode().equalsIgnoreCase("scan") || inObj.getMode().equalsIgnoreCase("update") )  ){

			writeOutput("tab", "Script copied to location="+destinationFile);
			
			// Script file execution based on UPDATE / SCAN defined inside script
			if(username.equalsIgnoreCase("root")){
				scriptLog = clean(sshObj.executeShellCommand("sh -x "+destinationFile + " "+inObj.getMode()+" > /tmp/wrapper.log"));
			} else if(sudoModeDisable) {
				scriptLog = clean(sshObj.executeShellCommand("sh -x "+destinationFile + " "+inObj.getMode()+" > /tmp/wrapper.log"));
			} else {
				scriptLog = clean(sshObj.executeShellCommandForScript("echo \""+password+"\" | "+sudoPath+" -S sh -x "+destinationFile + " "+inObj.getMode()+" > /tmp/wrapper.log"));
				scriptLog = clean(sshObj.executeShellCommand("cat /tmp/wrapper.log"));
			}

			// Read script log file to generate excel output data		
			excelLog = clean(sshObj.executeShellCommand("cat /tmp/wrapper.log | grep -i '\\[REPORT\\]'"));
			writeOutput("tab", "scriptLog="+scriptLog);
			writeOutput("tab", "excelLog="+excelLog);
			
		} else if( copyFlag && inObj.getMode().equalsIgnoreCase("upload") ) {
			scriptLog = "File uploaded to Server="+copyFlag + "\n" + "File uploaded to location="+destinationFile;
			
			writeOutput("tab", "File uploaded to location="+destinationFile);
		} else {
			scriptLog = "Unable to upload script/file to Server in the location : "+ destinationFile + ". It may be permission issues.";
		}

		
		ArrayList<String> excel_header_arr = new ArrayList<String>();
		Hashtable<String, String> excel_body_hash  = new Hashtable<String, String>();
		
		if(! excelLog.equalsIgnoreCase("")){
			
			String[] ss=excelLog.split("[\n\r]+");
			for(int i=0;i<ss.length;i++)
			{
				String line = ss[i];
				line = clean(line.replaceAll("\\[REPORT\\]", ""));
				
				String[] item_arr=line.split("=");
				
				if(!excel_body_hash.containsKey(item_arr[0])){
					excel_body_hash.put(clean(item_arr[0]), clean(item_arr[1]));
					excel_header_arr.add(clean(item_arr[0]));
					//System.out.println("MURU key = "+item_arr[0]);
					//System.out.println("MURU value = "+item_arr[1]);
					//System.out.println("MURU = "+line);
				}
			}
			
			//Header details pushed one time to use in Excel report module
			if(!excel_header_hash.containsKey("header")){
				excel_header_hash.put("header", excel_header_arr);
			}
	
		}
		outObj.setExcel_header_arr(excel_header_arr);
		outObj.setExcel_body_hash(excel_body_hash);	
		
		outObj.setHostname(hostname);
		outObj.setUsername(username);
		outObj.setBatch(inObj.getBatch());
		outObj.setMode(inObj.getMode());
		outObj.setOsname(osName);
		outObj.setScriptlog(scriptLog);
		
		output_model_arr.add(outObj);
		sshObj.close();
		
		
	}
	
	
	public void writeOutput(String tab, String str){
		if(tab.equalsIgnoreCase("")) {
			System.out.println(str);
		} else {
			System.out.println("\t"+str);
		}
		
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
