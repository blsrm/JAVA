package com.tdc.common;import java.io.BufferedReader;import java.io.File;import java.io.FileNotFoundException;import java.io.FileReader;import java.io.FileWriter;import java.io.IOException;import java.io.PrintWriter;import java.util.Vector;
/** * @author Murugavel Ramachandran * */
public class FileProcess {
	@SuppressWarnings({ "rawtypes", "unchecked" })	public Vector getContentsOfFile(File raFile) {
		Vector v = new Vector();		BufferedReader br = null;
		try{		  br = new BufferedReader(new FileReader(raFile));			  String line = null;		  while((line = br.readLine())!= null){			  line = line.trim();			  if(!line.equals("")) v.addElement(line);		  }		}catch(FileNotFoundException ex){			ex.printStackTrace();		}catch(IOException ioe){			ioe.printStackTrace();		}		return v;	}
	public void setContentsOfFile(String content, String filename) {		try{		PrintWriter pw = new PrintWriter(new FileWriter(filename));		pw.print(content);		pw.close();		}catch(Exception e){			e.printStackTrace();		}		return ;	}
}
