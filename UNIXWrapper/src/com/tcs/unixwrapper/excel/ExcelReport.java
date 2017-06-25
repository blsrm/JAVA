package com.tcs.unixwrapper.excel;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;

import com.tcs.unixwrapper.model.OutputDataModel;

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

public class ExcelReport {

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
	SimpleDateFormat sdf = new SimpleDateFormat("dd_MMM_yyyy_HH_mm_ss");
	Date date = new Date();

	
	public void processExcelReport(ArrayList<OutputDataModel> excel_model_arr, String batch, Hashtable<String, ArrayList<String>> excel_header_hash){

		try{

			File file = new File("WRAPPER_REPORT_"+batch+"_"+sdf.format(new Date())+".xls");
		    WorkbookSettings wbSettings = new WorkbookSettings();

		    wbSettings.setLocale(new Locale("en", "EN"));

		    WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);

		    workbook.createSheet("Wrapper Execution List", 0);
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
			timesRed.setAlignment(jxl.format.Alignment.LEFT);
			timesRed.setVerticalAlignment(jxl.format.VerticalAlignment.TOP);
			timesRed.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesRed.setWrap(true);

			timesOrange = new WritableCellFormat(cellFont);
			timesOrange.setBackground(Colour.ORANGE);
			timesOrange.setBorder(Border.ALL, BorderLineStyle.THIN);
			timesOrange.setWrap(true);
			
		    CellView cv = new CellView();
		    cv.setFormat(timesHead);
		    cv.setAutosize(true);
		    
		    ArrayList<String> header_arr = excel_header_hash.get("header");
		    

		    // Write a few headers
		    int head_count = 0;
			addCaption(ss, head_count, 0, "Sl No #" ,7); head_count++;
			addCaption(ss, head_count, 0, "Hostname" ,20); head_count++;
			addCaption(ss, head_count, 0, "OS Type" ,10); head_count++;
			addCaption(ss, head_count, 0, "Username" ,10); head_count++;
			addCaption(ss, head_count, 0, "Batch" ,20); head_count++;
			addCaption(ss, head_count, 0, "Mode" ,20); head_count++;
			
			if(header_arr != null){
				Iterator<String> itr_head = header_arr.iterator();
				while(itr_head.hasNext()){
					String custom_head = itr_head.next();
					addCaption(ss, head_count, 0, custom_head ,20); head_count++;
				}				
			}
			addCaption(ss, head_count, 0, "Script Output" ,80); head_count++;
			
			Iterator<OutputDataModel> itr = excel_model_arr.iterator();

			int i = 0;
			int excel_count = 1;
			while(itr.hasNext()){ 
				
				OutputDataModel obj = itr.next();
				
				i++;
				
				head_count = 0; // Reset head count for header
				int heightInPoints = 2000;
			    ss.setRowView(excel_count, heightInPoints);
		        addNumber(ss, head_count, excel_count, i, times); head_count++;
		        addLabel(ss, head_count, excel_count, obj.getHostname(), times); head_count++;
		        
		        addLabel(ss, head_count, excel_count, obj.getOsname(), times); head_count++;
		        addLabel(ss, head_count, excel_count, obj.getUsername(), times); head_count++;
		        addLabel(ss, head_count, excel_count, obj.getBatch(), times); head_count++;
		        addLabel(ss, head_count, excel_count, obj.getMode(), times); head_count++;

				if(header_arr != null){
					Iterator<String> itr_head = header_arr.iterator();
					while(itr_head.hasNext()){
						String custom_head = itr_head.next();
						addLabel(ss, head_count, excel_count, obj.getExcel_body_hash().get(custom_head), times); head_count++;
					}					
				}
		        
		        if(obj.getScriptlog() != null && (
		        		obj.getScriptlog().contains("No such file or directory") ||
		        		obj.getScriptlog().contains("command not found") ||
		        		obj.getScriptlog().contains("Authentication issues for provided users") ||
		        		obj.getScriptlog().contains("Unable to upload script") ||
		        		obj.getScriptlog().contains("timeout")
		        		)
		        	){
		        	addLabel(ss, head_count, excel_count, obj.getScriptlog(), timesRed); head_count++;
		        }else{
		        	addLabel(ss, head_count, excel_count, obj.getScriptlog(), times); head_count++;
		        }
		        
		        
		        

		        excel_count++;
	        }

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
	addCaption(sheet, 0, 0, "Sl No #" ,10);
	addCaption(sheet, 1, 0, "Hostname" ,20);
	addCaption(sheet, 2, 0, "IP Address" ,20);
	addCaption(sheet, 3, 0, "Nslookup IP" ,15);
	addCaption(sheet, 4, 0, "OS Type" ,10);
	addCaption(sheet, 5, 0, "Username" ,20);
	addCaption(sheet, 6, 0, "UID Status" ,15);
	addCaption(sheet, 7, 0, "GID Status" ,15);
	addCaption(sheet, 8, 0, "Sudo Status" ,40);
	addCaption(sheet, 9, 0, "SSH Status" ,40);
	addCaption(sheet, 10, 0, "Homedir Status" ,40);
	addCaption(sheet, 11, 0, "Script Output" ,80);
	addCaption(sheet, 12, 0, "Remarks" ,20);
   }
	
	  private void addCaption(WritableSheet sheet, int column, int row, String s, int width) throws RowsExceededException, WriteException {
		    Label label;
		    label = new Label(column, row, s, timesHead);
		    sheet.setColumnView(column, width);
		    sheet.addCell(label);
		  }

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
