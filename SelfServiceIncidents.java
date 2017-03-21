package com.tdc.selfservice;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import com.tdc.common.Common;
import com.tdc.common.Mail;
import com.tdc.db.MssqlDB;
import com.tdc.db.MysqlDB;
import com.tdc.metrics.CommonQuery;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormats;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class SelfServiceIncidents {

	MssqlDB dbConn = new MssqlDB();
	MysqlDB mysqlCon = new MysqlDB();
	Mail mailObj = new Mail();
	Common com = new Common();
	CommonQuery q = new CommonQuery();
	
	private WritableCellFormat times;
	private WritableCellFormat timesGreen;
	private WritableCellFormat timesPurple;
	private WritableCellFormat timesViolet;
	private WritableCellFormat timesRed;
	private WritableCellFormat timesOrange;
	private WritableCellFormat timesHead;
	private WritableFont cellFont;
	private WritableFont cellHead;
	private WritableCellFormat date_format;

	//Logger logger = Logger.getLogger(this.getClass());
	ResourceBundle rb_sla = ResourceBundle.getBundle("selfservice",Locale.getDefault());
	SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
	Date date = new Date();
	int count_sla = 0;

	
	public void processSelfServiceIncidents(){

		try{

			File file = new File("/home/m41569/POST_RELEASE_INCIDENTS_REPORT_"+sdf.format(new Date())+".xls");
		    WorkbookSettings wbSettings = new WorkbookSettings();

		    wbSettings.setLocale(new Locale("en", "EN"));

		    WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

		    workbook.createSheet("Active Incidents", 0);
		    WritableSheet ss = workbook.getSheet(0);
		    
			cellHead = new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD);
			cellHead.setColour(Colour.WHITE);

			timesHead = new WritableCellFormat(cellHead);
			timesHead.setBackground(Colour.BROWN);
			timesHead.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesHead.setAlignment(Alignment.CENTRE);
			timesHead.setWrap(true);
			  
			// Create cell font and format
			cellFont = new WritableFont(WritableFont.ARIAL, 9);
			cellFont.setColour(Colour.BLACK);
			
			times = new WritableCellFormat(cellFont);
			//times.setBackground(Colour.ORANGE);
			times.setAlignment(jxl.format.Alignment.LEFT);
			times.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);
			times.setBorder(Border.ALL, BorderLineStyle.THIN);
			times.setWrap(true);

			timesGreen = new WritableCellFormat(cellFont);
			timesGreen.setBackground(Colour.LIGHT_GREEN);
			timesGreen.setAlignment(jxl.format.Alignment.LEFT);
			timesGreen.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);
			timesGreen.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesGreen.setWrap(true);

			timesPurple = new WritableCellFormat(cellFont);
			timesPurple.setBackground(Colour.PINK);
			timesPurple.setAlignment(jxl.format.Alignment.LEFT);
			timesPurple.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);
			timesPurple.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesPurple.setWrap(true);

			timesViolet = new WritableCellFormat(cellFont);
			timesViolet.setBackground(Colour.VIOLET);
			timesViolet.setAlignment(jxl.format.Alignment.LEFT);
			timesViolet.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);
			timesViolet.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesViolet.setWrap(true);
			
			timesRed = new WritableCellFormat(cellFont);
			timesRed.setBackground(Colour.RED);
			timesRed.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesRed.setWrap(true);

			timesOrange = new WritableCellFormat(cellFont);
			timesOrange.setBackground(Colour.ORANGE);
			timesOrange.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesOrange.setWrap(true);
			
		    CellView cv = new CellView();
		    cv.setFormat(timesHead);
		    cv.setAutosize(true);

		    // Write a few headers
			addCaption(ss, 0, 0, "Error No #" ,10);
			addCaption(ss, 1, 0, "Priority" ,8);
			addCaption(ss, 2, 0, "Severity" ,8);
			addCaption(ss, 3, 0, "Open Time" ,15);
			addCaption(ss, 4, 0, "Description" ,40);
			addCaption(ss, 5, 0, "Affected Service" ,20);
			addCaption(ss, 6, 0, "Area" ,15);
			addCaption(ss, 7, 0, "Customer Experience" ,15);
			addCaption(ss, 8, 0, "Status\n"+sdf.format(new Date()) ,40);
			addCaption(ss, 9, 0, "Activity Log" ,80);
			
			
			
			//MSSQL Data preparation
			Connection conn = dbConn.connect();
			Statement stmt = null;
			ResultSet rs = null;
			stmt = conn.createStatement();
			
			String sql = "SELECT " +
						 "number, " +
						 "priority_code, " +
						 "severity, " +
						 "problem_status, " +
						 "assignment, " +
						 "open_time, " +
						 "close_time, " +
						 "REPLACE(REPLACE(REPLACE(brief_description, CHAR(10), ''), CHAR(13), ''), CHAR(9), '') brief_description, " +
						 "(select top 1 affected_item from probsummarym2 where number=p.number) affected_item, " +
						 "subcategory, " +
						 "action, " +
						 "update_action, " +
						 "update_time " +
						 "FROM PROBSUMMARYM1 as p "+
						 "WHERE category IN ('incident') "+
						 "AND problem_status NOT IN ('Resolved','Closed') " +
						 "AND open_time between CONVERT( DATETIME, '01 FEB 2015', 106 ) and CONVERT( DATETIME, '15 JUN 2015', 106 ) " +
						 "ORDER BY priority_code, open_time " ;
 
			System.out.println(sql);
			
			String update_action = "";

			rs = stmt.executeQuery(sql);
			int i = 0;
			int excel_count = 1;
			while(rs.next()){ 
				
				update_action = rs.getString("update_action");
				
	        	System.out.println(rs.getString("number"));
	        	
	        	//System.out.println(update_action);
	        	
	        	
	    		if(!(update_action instanceof String)) update_action = "";
	    		if(update_action.equals("")) update_action = "";
	    		
	    		String update_action_arr[] = update_action.split("\n");
	    		
	    		String lastUpdate = "";
	    		String lastUpdateFinal = "";
	    		
	    		lastUpdateFinal += "Status:\n" + rs.getString("problem_status") + "\n\n";
	    		lastUpdateFinal += "Assigned To:\n" + rs.getString("assignment") + "\n\n";
	    		
	    		boolean flag = false;
	    		int flagCount = 0;

	    		for(i=0; i<update_action_arr.length; i++){

	    			//System.out.println("ARRAY ==> "+update_action_arr[i]);
	    			if(flagCount <= 2){
	    			
	    			
	    			if(update_action_arr[i].matches("^(\\d{2}/\\d{2}/\\d{2})(.*)$")){
	    				flag = true;
	    				//System.out.println("PODA ==> "+update_action_arr[i]);
	    				flagCount++;
	    			}else{
	    				if(flag && flagCount <= 2) { lastUpdate += update_action_arr[i] + "\n"; }
	    			}
	    			
	    			}
	    			
	    		}
	        	
	    		//System.out.println("lastUpdate ==> "+lastUpdate);
	        	
	    		//System.out.println(sdf.format(new Date()));

				i++;
				count_sla++;
				
				lastUpdateFinal += "Progress:\n" + lastUpdate + "\n\n";
				lastUpdateFinal += "Circumvention/Work-around:\n\n";

				
				int heightInPoints = 3500;
			    ss.setRowView(excel_count, heightInPoints);
		        addLabel(ss, 0, excel_count, rs.getString("number"), times);
		        addLabel(ss, 1, excel_count, rs.getString("priority_code"), times);
		        addLabel(ss, 2, excel_count, rs.getString("severity"), times);
		        addLabel(ss, 3, excel_count, rs.getString("open_time"), times);
		        addLabel(ss, 4, excel_count, rs.getString("brief_description"), times);
		        addLabel(ss, 5, excel_count, rs.getString("affected_item"), times);
		        addLabel(ss, 6, excel_count, rs.getString("subcategory"), times);
		        addLabel(ss, 7, excel_count, "", times);
		        
		        if(rs.getString("assignment").matches("Accen.*")){
		        	addLabel(ss, 8, excel_count, lastUpdateFinal, timesPurple);
		        }else if(rs.getString("assignment").matches(".*TDC.*")){
		        	addLabel(ss, 8, excel_count, lastUpdateFinal, timesOrange);
		        }else if(rs.getString("assignment").matches(".*TCS.*") || rs.getString("assignment").matches(".*UDV.*")){
		        	addLabel(ss, 8, excel_count, lastUpdateFinal, timesGreen);
		        }else{
		        	addLabel(ss, 8, excel_count, lastUpdateFinal, times);
		        }
		        addLabel(ss, 9, excel_count, update_action, times);
		        
		        excel_count++;
	        }

			stmt.close();
	        conn.close();
	        dbConn.close();
	        
	        
	        workbook.write();
	        workbook.close();
	        
	        
	        
		}catch(Exception e){
				e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unused")
	private void createLabel(WritableSheet sheet)
    throws WriteException {
  
	cellHead = new WritableFont(WritableFont.ARIAL, 9, WritableFont.BOLD);
	cellHead.setColour(Colour.WHITE);

	timesHead = new WritableCellFormat(cellHead);
	timesHead.setBackground(Colour.BROWN);
	timesHead.setBorder(Border.ALL, BorderLineStyle.THIN);
	timesHead.setAlignment(Alignment.CENTRE);
	timesHead.setWrap(true);
	  
	// Create cell font and format
	cellFont = new WritableFont(WritableFont.ARIAL, 9);
	cellFont.setColour(Colour.BLACK);
	
	times = new WritableCellFormat(cellFont);
	//times.setBackground(Colour.ORANGE);
	times.setBorder(Border.ALL, BorderLineStyle.THIN);
	times.setWrap(true);

	timesRed = new WritableCellFormat(cellFont);
	timesRed.setBackground(Colour.RED);
	timesRed.setBorder(Border.ALL, BorderLineStyle.THIN);
	timesRed.setWrap(true);

	timesOrange = new WritableCellFormat(cellFont);
	timesOrange.setBackground(Colour.ORANGE);
	timesOrange.setBorder(Border.ALL, BorderLineStyle.THIN);
	timesOrange.setWrap(true);
	
    CellView cv = new CellView();
    cv.setFormat(timesHead);
    cv.setAutosize(true);

    // Write a few headers
	addCaption(sheet, 0, 0, "IM No #" ,10);
	addCaption(sheet, 1, 0, "Priority" ,8);
	addCaption(sheet, 2, 0, "Severity" ,8);
	addCaption(sheet, 3, 0, "Open Time" ,15);
	addCaption(sheet, 4, 0, "Description" ,40);
	addCaption(sheet, 5, 0, "Affected Service" ,20);
	addCaption(sheet, 6, 0, "Area" ,15);
	addCaption(sheet, 7, 0, "Customer Experience" ,15);
	addCaption(sheet, 8, 0, "Status\n"+sdf.format(new Date()) ,40);
	addCaption(sheet, 9, 0, "Activity Log" ,80);
   }
	
	  private void addCaption(WritableSheet sheet, int column, int row, String s, int width) throws RowsExceededException, WriteException {
		    Label label;
		    label = new Label(column, row, s, timesHead);
		    sheet.setColumnView(column, width);
		    sheet.addCell(label);
		  }

		  @SuppressWarnings("unused")
		private void addNumber(WritableSheet sheet, int column, int row, Integer integer, WritableCellFormat t) throws WriteException, RowsExceededException {
		    Number number;
		    number = new Number(column, row, integer, t);
		    sheet.addCell(number);
		  }

		  @SuppressWarnings("unused")
		private void addDate(WritableSheet sheet, int column, int row, String s) throws WriteException, RowsExceededException {
			  Label label;
			  date_format = new WritableCellFormat(DateFormats.FORMAT9);
			  label = new Label(column, row,s,date_format);
			  sheet.addCell(label);
		  }

		  private void addLabel(WritableSheet sheet, int column, int row, String s, WritableCellFormat t)
		      throws WriteException, RowsExceededException {
		    Label label;
		    label = new Label(column, row, s, t);
		    sheet.addCell(label);
		  }
	

}
