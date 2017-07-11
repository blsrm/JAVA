/**
 * 
 */
package com.tdc.common;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Murugavel Ramachandran (m41569)
 *
 */

public class Common {

	public String formatStr2Sql(String str){
		//System.out.println(str+" Inside of Common");
		if(!(str instanceof String)) str = "";
		if(str.equals("")) return "";
		String str_arr[] = str.split(",");
		String str_format = "";
		int i;
		for(i=0; i<str_arr.length; i++){
			if(i==0) str_format += "'"+str_arr[i]+"'";
			if(i>0) str_format += ",'"+str_arr[i]+"'";
		}
		return str_format;
	}

	public String formatEscapeChar(String str){
		if(str.equals("")) return "";
		
		//System.out.println("orug="+str);
		//str = str.replaceAll("\\","\\\\\\");
		str = str.replaceAll("'","&#039;");
		//str = str.replaceAll("'","\\'");
		//System.out.println("Change="+str);
		return str;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector splitRecordByCount(Vector v){
		Vector output = new Vector();
		Enumeration e = v.elements();
		int i=0;
		String str = "";
		while(e.hasMoreElements()){
			String id = (String)e.nextElement();
			if(i<1000){
				if(i==0) str += id;
				if(i>0) str += ","+id;
			}else{
				output.addElement(str);
				i=0;
				str = "";
				str += id;
			}
			i++;
		}
		output.addElement(str);
		return output;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector splitRecordByCount_Clob(Vector v){
		Vector output = new Vector();
		Enumeration e = v.elements();
		int i=0;
		String str = "";
		while(e.hasMoreElements()){
			String id = (String)e.nextElement();
			if(i<1){
				if(i==0) str += id;
				if(i>0) str += ","+id;
			}else{
				output.addElement(str);
				i=0;
				str = "";
				str += id;
			}
			i++;
		}
		output.addElement(str);
		return output;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector joinRecordByVector(String str){
		Vector output = new Vector();
		String str_arr[] = str.split(",");
		int i;
		for(i=0; i<str_arr.length; i++){
			output.addElement(str_arr[i]);
		}
		return output;
	}

	public String DateConversion(String str_date, String from, String to){
		// from "dd/MM/yyyy", to "dd-MMM-yy"
		String str_out = "";
		try{
			DateFormat formatter ; 
	     	Date date ;
			formatter = new SimpleDateFormat(from);
	        date = (Date)formatter.parse(str_date);
	        SimpleDateFormat sdf=new SimpleDateFormat(to);
	        str_out = sdf.format(date);
	 	}catch(Exception e){
		 		e.printStackTrace();
	 	}
        //System.out.println("Date After Con : "+ str_out);
	 	return str_out;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Vector sortArray2vector(String str){
		String[] str_arr = str.split(",");
		java.util.Arrays.sort(str_arr);
		Hashtable h = new Hashtable();
		Vector v = new Vector();
		for(int i=0; i<str_arr.length; i++){
			if(!h.containsKey(str_arr[i])) { 
				h.put(str_arr[i],str_arr[i]);
				//System.out.println("TD : "+str_arr[i]);
				v.addElement(str_arr[i]);
			}
		}
		return v;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Hashtable splitYearByPCF(Vector v, String cYear){
		Hashtable h = new Hashtable();
		String pYear_out = ""; //past year
		String cYear_out = ""; //current year
		String fYear_out = ""; //future year
		String mYear_out = ""; //miscellaneous year
		Enumeration e = v.elements();
		int cYear_int = Integer.parseInt(cYear);
		while(e.hasMoreElements()){
			String year = e.nextElement().toString();
			if(year.startsWith("0")){
				int index = Integer.parseInt(year.substring(0,2));
				if(index < cYear_int){
					pYear_out += ","+year;
				}else if(index > cYear_int){
					fYear_out += ","+year;
				}else{
					cYear_out += ","+year;
				}
			}else{
				mYear_out += ","+year;
			}
		}
		
		pYear_out = pYear_out.replaceFirst(",","");
		cYear_out = cYear_out.replaceFirst(",","");
		fYear_out = fYear_out.replaceFirst(",","");
		mYear_out = mYear_out.replaceFirst(",","");
		
		h.put("PAST_YEAR",pYear_out);
		h.put("CURRENT_YEAR",cYear_out);
		h.put("FUTURE_YEAR",fYear_out);
		h.put("MISC_YEAR",mYear_out);
		return h;
	}

	public String[] splitString(String s, String del) {
	    if (s==null) return new String[0];
	    
	    StringTokenizer st= new StringTokenizer(s, del);
	    int n= st.countTokens(); 
	    String[] ss= new String[n];
	    
	    for (int i=0; i<n; i++) 
	        ss[i]= st.nextToken();
	    return ss;
	}

	public String splitToken(String s, String del, int position) {
	    if (s==null) return "";
	    
	    //System.out.println(position + " = " +s);
	    
	    StringTokenizer st= new StringTokenizer(s, del);
	    int n= st.countTokens();
	    String ss = "";
	    for (int i=0; i<n; i++)
	    {
	    	String temp = st.nextToken();
	    	//System.out.println(i + " - " +position + " -- " + temp);
	    	if(i == position) {
	    		ss = temp;
	    		//System.out.println(i + " - " +position + " = " +ss);
	    	}
	    }
	    return ss;
	}
	
	public String checkElement(String data){
		if(!(data instanceof String)) data = "";
		
		if(data.toString().equalsIgnoreCase(null) || data.toString() == null || data.length() == 0){
		   data = "";	
		}
		return data;
	}
}
