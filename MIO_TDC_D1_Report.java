/**
 * 
 */
package com.tdc.mio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import com.tdc.common.Common;
import com.tdc.common.Mail;
import com.tdc.tools.db.MysqlDB;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

/**
 * @author "Murugavel Ramachandran (M41569), TDC Project"
 *
 */
public class MIO_TDC_D1_Report {

	/**
	 * @param args
	 */
	MysqlDB mysqlCon = new MysqlDB();
	Mail mailObj = new Mail();
	Common com = new Common();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
	Date date = new Date();
	
	Vector<String> output = new Vector<String>();
	ArrayList<String> out_arr = new ArrayList<String>();
	Hashtable<String, String> fieldsMap = new Hashtable<String, String>();
	String fields_arr[];
	Hashtable<String, String> db_hash = new Hashtable<String, String>();
	ArrayList<String> tableUpdate = new ArrayList<String>();
	
	public String message = "";
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MIO_TDC_D1_Report mio = new MIO_TDC_D1_Report();
		
		//mio.fieldsMapping();
		//mio.executeTSReportQuery();
		mio.processExcelData("C:\\Users\\m41569\\Desktop\\MIO TTM\\TDC_Excel_download3.xls");
		//mio.processOutputData();
	}
	
	
	public void executeTSReportQuery(){
		
		try{

			//MYSQL Data preparation
			Connection conn_my = mysqlCon.connect();
			Statement stmt_my = null;
			ResultSet rs_my = null;
			stmt_my = conn_my.createStatement();
			
			String sql = "SELECT * FROM mio_report";
			
			rs_my = stmt_my.executeQuery(sql);
			while(rs_my.next()){ 
				
				String temp = "";
				temp += "request_no="+com.checkElement(rs_my.getString("request_no"));
				temp += "request_status="+com.checkElement(rs_my.getString("request_status"));
				temp += "assigned_to="+com.checkElement(rs_my.getString("assigned_to"));
				temp += "priority="+com.checkElement(rs_my.getString("priority"));
				temp += "created_by="+com.checkElement(rs_my.getString("created_by"));
				temp += "tdc_number="+com.checkElement(rs_my.getString("tdc_number"));
				temp += "created_on="+com.checkElement(rs_my.getString("created_on"));
				temp += "minor_task_name="+com.checkElement(rs_my.getString("minor_task_name"));
				temp += "vendor_dispatcher="+com.checkElement(rs_my.getString("vendor_dispatcher"));
				temp += "agreed_proposal_date="+com.checkElement(rs_my.getString("agreed_proposal_date"));
				temp += "agreed_response_date="+com.checkElement(rs_my.getString("agreed_response_date"));
				temp += "estimated_delivery_date="+com.checkElement(rs_my.getString("estimated_delivery_date"));
				temp += "calculated_proposal_date="+com.checkElement(rs_my.getString("calculated_proposal_date"));
				temp += "estimate_hours="+com.checkElement(rs_my.getString("estimate_hours"));
				temp += "functional_test_approved_by_tdc="+com.checkElement(rs_my.getString("functional_test_approved_by_tdc"));
				temp += "main_Application="+com.checkElement(rs_my.getString("main_Application"));
				temp += "is_delivery_date_commited="+com.checkElement(rs_my.getString("is_delivery_date_commited"));
				temp += "tower_lead="+com.checkElement(rs_my.getString("tower_lead"));
				temp += "vendor_pm="+com.checkElement(rs_my.getString("vendor_pm"));
				temp += "mt_vendor="+com.checkElement(rs_my.getString("mt_vendor"));
				temp += "business_line_request="+com.checkElement(rs_my.getString("business_line_request"));
				temp += "sent_to_vendor="+com.checkElement(rs_my.getString("sent_to_vendor"));
				temp += "sent_to_vendor_first_time="+com.checkElement(rs_my.getString("sent_to_vendor_first_time"));
				temp += "proposal_delivered="+com.checkElement(rs_my.getString("proposal_delivered"));
				temp += "proposal_delivered_first_time="+com.checkElement(rs_my.getString("proposal_delivered_first_time"));
				temp += "released_for_work="+com.checkElement(rs_my.getString("released_for_work"));
				temp += "released_for_work_first_time="+com.checkElement(rs_my.getString("released_for_work_first_time"));
				temp += "functional_test_approval="+com.checkElement(rs_my.getString("functional_test_approval"));
				temp += "functional_test_approval_first_time="+com.checkElement(rs_my.getString("functional_test_approval_first_time"));
				temp += "acceptance_test_approval="+com.checkElement(rs_my.getString("acceptance_test_approval"));
				temp += "acceptance_test_approval_first_time="+com.checkElement(rs_my.getString("acceptance_test_approval_first_time"));
				temp += "return_to_tdc="+com.checkElement(rs_my.getString("return_to_tdc"));
				temp += "return_to_tdc_first_time="+com.checkElement(rs_my.getString("return_to_tdc_first_time"));
				temp += "rel_for_work_small_task="+com.checkElement(rs_my.getString("rel_for_work_small_task"));
				temp += "rel_for_work_small_task_first_time="+com.checkElement(rs_my.getString("rel_for_work_small_task_first_time"));
				temp += "vendor="+com.checkElement(rs_my.getString("vendor"));
				
				db_hash.put(rs_my.getString("request_no"), temp);
			}
	        
			stmt_my.close();
	        conn_my.close();
	        mysqlCon.close();
		}catch(Exception e){
				e.printStackTrace();
		}
	}	
	public void fieldsMapping(){
		
		fieldsMap.put("Request No.:", "request_no");
		fieldsMap.put("Request Status:", "request_status");
		fieldsMap.put("Assigned To:", "assigned_to");
		fieldsMap.put("Priority:", "priority");
		fieldsMap.put("Created By:", "created_by");
		fieldsMap.put("TDC Number:", "tdc_number");
		fieldsMap.put("Created On:", "created_on");
		fieldsMap.put("Minor Task Name:", "minor_task_name");
		fieldsMap.put("Vendor Dispatcher:", "vendor_dispatcher");
		fieldsMap.put("Agreed Proposal Date:", "agreed_proposal_date");
		fieldsMap.put("Agreed Response Date:", "agreed_response_date");
		fieldsMap.put("Estimated Delivery Date:", "estimated_delivery_date");
		fieldsMap.put("Calculated Proposal Date:", "calculated_proposal_date");
		fieldsMap.put("Estimate Hours:", "estimate_hours");
		fieldsMap.put("Functional test approved by TDC:", "functional_test_approved_by_tdc");
		fieldsMap.put("Main Application:", "main_Application");
		fieldsMap.put("Is Delivery date commited?:", "is_delivery_date_commited");
		fieldsMap.put("Tower Lead:", "tower_lead");
		fieldsMap.put("Vendor PM:", "vendor_pm");
		fieldsMap.put("MT Vendor:", "mt_vendor");
		fieldsMap.put("Vendor:", "vendor");
		
		// New Fields
		fieldsMap.put("Estimated Delivery date - first change:", "estimated_delivery_date_timestamp_first");
		fieldsMap.put("Estimated Delivery date - last change:", "estimated_delivery_date_timestamp");
		fieldsMap.put("Release:", "release_name");
		fieldsMap.put("Release - first change:", "release_date_first");
		fieldsMap.put("Release - last change:", "release_date");

		fieldsMap.put("Request_No__:", "request_no");
		fieldsMap.put("Business_Line_Request_:", "business_line_request");
		fieldsMap.put("Request_Status_:", "request_status");
		fieldsMap.put("SentToVendor:", "sent_to_vendor");
		fieldsMap.put("SentToVendorFirstTime:", "sent_to_vendor_first_time");
		fieldsMap.put("ProposalDelivered:", "proposal_delivered");
		fieldsMap.put("ProposalDeliveredFirstTime:", "proposal_delivered_first_time");
		fieldsMap.put("ReleasedForWork:", "released_for_work");
		fieldsMap.put("ReleasedForWorkFirstTime:", "released_for_work_first_time");
		fieldsMap.put("FunctionalTestApproval:", "functional_test_approval");
		fieldsMap.put("FunctionalTestApprovalFirstTime:", "functional_test_approval_first_time");
		fieldsMap.put("AcceptanceTestApproval:", "acceptance_test_approval");
		fieldsMap.put("AcceptanceTestApprovalFirstTime:", "acceptance_test_approval_first_time");
		fieldsMap.put("ReturnToTdc:", "return_to_tdc");
		fieldsMap.put("ReturnToTdcFirstTime:", "return_to_tdc_first_time");
		fieldsMap.put("RelForWorkSmallTask:", "rel_for_work_small_task");
		fieldsMap.put("RelForWorkSmallTaskFirstTime:", "rel_for_work_small_task_first_time");
		fieldsMap.put("Vendor_:", "vendor");
		fieldsMap.put("Main_Application_:", "main_Application");
		fieldsMap.put("Vendor_Dispatcher_:", "vendor_dispatcher");
		
	}
	
	public void processExcelData(String targetPath){
		
		try {
			
			//String fileName = "RequestSearchExcelExport.xls";
			//String fileName = "test.xls";
			String fileName = targetPath;
			
			WorkbookSettings ws = new WorkbookSettings();
			ws.setEncoding("UTF-8");
			
			//Create a workbook object from the file at specified location. 
			//Change the path of the file as per the location on your computer. 
			//Workbook wrk1 =  Workbook.getWorkbook(new File("/home/m41569/"+fileName), ws);
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
                
                System.out.println(row + " CHECK: "+line_temp+":OK");

                if( ! line.contains("Exported on: ") && 
               		! line.contains("Request Search Results") &&
               		! line.matches("(.*)Request Search Results(.*)") &&
               		! line.matches("(.*)Exported on(.*)") && 
               		! line_temp.equalsIgnoreCase("")
                ){
                	System.out.println("MURU"+line);
                   	out_arr.add(line);
                }
                //breakLine();
                //System.out.println(line);
            }
            
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void processOutputData(){
		
		Iterator<String> itr = out_arr.iterator();
		
		String data_arr[];
		
		String line = "";
		@SuppressWarnings("unused")
		String mode = "TCS";
		
		if(itr.hasNext()){
			line = itr.next();
			
			if(line.startsWith("Request Search Results") || 
					line.equalsIgnoreCase("Request Search Results||||||||||||||||||||END") ||
					line.matches("(.*)Request Search Results(.*)")					
			){
				line = itr.next();
			}
			
			
			
			
			if(line.startsWith("Request_No__")){
				mode = "TDCExtract";
			}else{
				mode = "TCSExtract";
			}
			
			fields_arr = line.split("\\|");
			
			//System.out.println(line);
			itr.remove();
		}

		// Customizing to append : char for all headers both TCS and TDC extract
		fields_arr = appendColon(fields_arr);
		//System.out.println(mode);
		
		while(itr.hasNext()){
			line = itr.next();
			
			//System.out.println("Processing line = "+line);
			
			data_arr = line.split("\\|");
			
			String q = "";
			String q_end = "";

			if(		line.startsWith("Request Search Results") || line.matches("(.*)Request Search Results(.*)") || 
					line.equalsIgnoreCase("Request Search Results||||||||||||||||||||END") || line.matches("(.*)Exported on(.*)") || 
					line.startsWith("Exported on")){
				break;
			}
			
			if(db_hash.containsKey(data_arr[0])){
				q = "UPDATE mio_report SET ";
				q_end = "WHERE request_no='"+com.checkElement(data_arr[0].toString())+"' ";
			}else{
				q = "INSERT INTO mio_report SET request_no='"+com.checkElement(data_arr[0].toString())+"', ";
			}
			
			for(int i=1; i<data_arr.length-1; i++){
					
					String key = com.checkElement(com.formatEscapeChar(data_arr[i].toString()));
				
					if(key.equalsIgnoreCase("") || key.equals(null)){
						//System.out.println("EMPTY = "+fields_arr[i]);
					}else{
						//Implemented Trigger for checking fields values and write it in another table called mio_report_log
						//String checkChange = checkDBCompareFieldsValue(data_arr[0], fieldsMap.get(fields_arr[i]), checkFormat(fieldsMap.get(fields_arr[i]),key));
						//System.out.println("fields_arr[i] = "+fields_arr[i]);
						//System.out.println("fields_arr[i] = "+fieldsMap.get(fields_arr[i]));
						//System.out.println("key = "+key);
						
						// Exception Checking
						if(com.checkElement(fieldsMap.get(fields_arr[i])).equals(null) || com.checkElement(fieldsMap.get(fields_arr[i])).equalsIgnoreCase("")){
							System.out.println("One of new Columnn/Field no - "+fields_arr[i]+" is not configured in System to map in request no - " + data_arr[0] + ". Please contact Murugavel Ramachandran (mura@tdc.dk)");
							setMessage("One of the Columnn/Field - \""+fields_arr[i]+"\" is not configured in System to map in request no -" + data_arr[0] + ". Please contact Murugavel Ramachandran (mura@tdc.dk)");
							return;
						}
						
						
						q += fieldsMap.get(fields_arr[i]) + "="+checkFormat(fieldsMap.get(fields_arr[i]),key, data_arr[0])+", ";
					}
					//breakLine();
			}
			
			q += "last_updated=now() ";
			
			if(db_hash.containsKey(data_arr[0])){
				q = q + q_end;
			}
			
			tableUpdate.add(q);
			
			System.out.println(q);

			//breakLine();
		}
		
		updatingTable(tableUpdate);
		
	}

	public String[] appendColon(String[] arrInput){
		String[] arrOutput = new String[arrInput.length];
		int i;
		for(i=0; i<arrInput.length; i++){
			//System.out.println(arrInput[i] + " --- BEFORE");
			arrOutput[i] = arrInput[i].replaceAll(":", "") + ":";
			//System.out.println(arrInput[i].replaceAll(":", "") + ": --- AFTER");
		}
		return arrOutput;
	}
	
	public String checkDBCompareFieldsValue(String request_no, String field, String value){
		
		//System.out.println(field + " = BEGIN COMPARE = " + value);
		if(db_hash.containsKey(request_no)){
			String db_line = db_hash.get(request_no);
			
			StringTokenizer st= new StringTokenizer(db_line, "|");
		    int n= st.countTokens(); 
		    @SuppressWarnings("unused")
			String[] ss= new String[n];
		    
		    for (int i=0; i<n; i++){
		    	String temp = st.nextToken();
		    	String key_db = com.splitToken(temp, "=", 1);
		    	String key_value = com.splitToken(temp, "=", 2);
		    	
		    	if(key_db.equalsIgnoreCase(field)){
		    		System.out.println(value + " = COMPARE = " + key_value);
		    	}
		    }
		}else{
			System.out.println(field + " = NEW RECORD = " + value);
		}
		
		return "";
	}
	
	public void updatingTable(ArrayList<String> one){
		@SuppressWarnings("unused") int i;
		try{
			//MYSQL Data preparation
			Connection conn_my = mysqlCon.connect();
			Statement stmt_my = null;

			stmt_my = conn_my.createStatement();
			
			Iterator<String> e1 = one.iterator();
			while(e1.hasNext()){
				String query = (String)e1.next();
				System.out.println("EXEC: "+query);
				i = stmt_my.executeUpdate(query);
			}
			stmt_my.close();
			//conn_my.commit();
		}catch(Exception e){
			e.printStackTrace();
			setMessage("DB Error = " + e.getMessage());
			System.out.println("DB Error = " + e.getMessage());
		}
	}


	public String checkFormat(String field, String value, String request_no){
		String str="";
		
		//String OS = System.getProperty("os.name", "generic").toLowerCase();
				
		str = "'"+value+"'";

		//System.out.println("Field="+field + "  and value="+value + "  and BEFORE formated Date="+str);
		
		if(field.equalsIgnoreCase("sent_to_vendor") || 
				field.equalsIgnoreCase("sent_to_vendor_first_time") ||
				field.equalsIgnoreCase("proposal_delivered") ||
				field.equalsIgnoreCase("proposal_delivered_first_time") ||
				field.equalsIgnoreCase("released_for_work") ||
				field.equalsIgnoreCase("released_for_work_first_time") ||
				field.equalsIgnoreCase("functional_test_approval") ||
				field.equalsIgnoreCase("functional_test_approval_first_time") ||
				field.equalsIgnoreCase("acceptance_test_approval") ||
				field.equalsIgnoreCase("acceptance_test_approval_first_time") ||
				field.equalsIgnoreCase("return_to_tdc") ||
				field.equalsIgnoreCase("return_to_tdc_first_time") ||
				field.equalsIgnoreCase("created_on") ||
				field.equalsIgnoreCase("agreed_proposal_date") ||
				field.equalsIgnoreCase("agreed_response_date") ||
				field.equalsIgnoreCase("estimated_delivery_date") ||
				field.equalsIgnoreCase("estimated_delivery_date_timestamp_first") ||
				field.equalsIgnoreCase("estimated_delivery_date_timestamp") ||
				field.equalsIgnoreCase("release_date_first") ||
				field.equalsIgnoreCase("release_date") ||
				field.equalsIgnoreCase("calculated_proposal_date")||
				field.equalsIgnoreCase("rel_for_work_small_task")||
				field.equalsIgnoreCase("rel_for_work_small_task_first_time")
				){
			

			if(value.matches("(.*)([0-9]{2})\\/([A-Za-z]{3})\\/([0-9]{2})(.*)")){   // 25/May/14
				str="STR_TO_DATE('"+value+"','%d/%m/%y')";
			}else if(value.matches("(.*)([0-9]{2})-([A-Za-z]{3})-([0-9]{4})(.*)")){ // 25-May-2014
				str="STR_TO_DATE('"+value+"','%d-%b-%Y')";
			}else if (value.matches("(.*)(\\d+)-(\\d+)-([0-9]{4})(.*)")){   // 25-05-2014
				str="STR_TO_DATE('"+value+"','%d-%m-%Y')";
			}else if (value.matches("(.*)(\\d+)-(\\d+)-([0-9]{2})(.*)")){	// 25-05-14
				str="STR_TO_DATE('"+value+"','%d-%m-%y')";
			}else if(value.matches("(.*)(\\d+)\\/(\\d+)\\/([0-9]{2})(.*)")){ // 25/05/14
				str="STR_TO_DATE('"+value+"','%m/%d/%y')";
			}else if(value.matches("(.*)(\\d+)-([A-Za-z]{3})-(\\d+)(.*)")){	// 25-May-14
				str="STR_TO_DATE('"+value+"','%d-%b-%Y')";
			}else if (value.matches("(.*)([A-Za-z]{3}) (\\d+), ([0-9]{4})(.*)")){	// May 05, 2014
				str="STR_TO_DATE('"+value+"','%b %d, %Y')";
			}else if(value.equalsIgnoreCase(null) || value.equalsIgnoreCase("")){
				str="null";
			}
			
			
		}

		//System.out.println("Field="+field + "  and value="+value + "  and formated Date="+str);
		
		return str;
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


	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}


	/**
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}


