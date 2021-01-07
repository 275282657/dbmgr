package com.hzmc.dbmgr.util;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
public class ReadExcelFile {
	
	private static DecimalFormat integerFormat = new DecimalFormat("#");
	
	public static List readExcel(String excelFileName){
		//创建一个list 用来存储读取的内容
		List list = new ArrayList();
		Workbook rwb = null;
		Cell cell = null;
		  
		//创建输入流
		InputStream stream = null;
		try {
			stream = new FileInputStream(excelFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		  
	    //获取Excel文件对象
		try {
			rwb = Workbook.getWorkbook(stream);
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    //获取文件的指定工作表 默认的第一个
	    Sheet sheet = rwb.getSheet(0);  
	    //行数(表头的目录不需要，从1开始)
	    for(int i=1; i<sheet.getRows(); i++){
		    //创建一个数组 用来存储每一列的值
		    String[] str = new String[sheet.getColumns()];
		    //列数
		    for(int j=0; j<sheet.getColumns(); j++){
			    //获取第i行，第j列的值
			    cell = sheet.getCell(j,i);   
			    str[j] = cell.getContents();
			    list.add(str[j]);
		   }
		   //把刚获取的列存入list
		   //list.add(str);
	  }
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//返回值集合
	  return list;
	 }
	
	public static List readXLSFile(InputStream inputStream) throws Exception {
		//to do
		List list = new ArrayList();
		Workbook workbook = Workbook.getWorkbook(inputStream);
		Sheet sheet = workbook.getSheet(0);
		Cell[] columns = sheet.getColumn(0);
		for (int i=0; i<columns.length; i++) {
			Cell cell = columns[i];
			String content = cell.getContents();
			if (!content.equals(""))
				list.add(content);
		}
		
		return list;
	}
	
	public static List readTXTFile(InputStream inputStream) throws Exception {
		//to do
		List list = new ArrayList();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer buffer = new StringBuffer();
		
		String line = "";
		while((line=br.readLine()) != null) {
			buffer.append(line.trim());
		}
		String[] apps = buffer.toString().split(",");
		for (int i=0; i<apps.length; i++) {
			if (!apps[i].equals(""))
			list.add(apps[i]);
		}
		return list;
	}
	
    /*
     * 获得excel表格指定单元格的字符值
     */
    private static String getCellStringValue(Row row, int cellIndex) {
    	org.apache.poi.ss.usermodel.Cell cell = row.getCell(cellIndex);
        String cellStringValue = "";
        if (cell != null) {
            int cellType = cell.getCellType();
            switch (cellType) {
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
                cellStringValue = integerFormat.format(cell.getNumericCellValue());
                break;
            case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
                cellStringValue = cell.getStringCellValue();
                break;
            }
        }
        return cellStringValue;
    }
	
	/*
     * 读取office2003的excel文件(.xls)，以一行为一条记录 返回list
     */
    public static List<String[]> readXls(InputStream is) throws Exception {
        List<String[]> list = new ArrayList<String[]>();
        POIFSFileSystem fs = new POIFSFileSystem(is);
        HSSFWorkbook workbook = new HSSFWorkbook(fs);
        HSSFSheet sheet = workbook.getSheetAt(0);

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();
        int colCount = sheet.getRow(0).getLastCellNum();
        for (int rowIx = firstRowNum; rowIx <= lastRowNum; rowIx++) {
            HSSFRow row = sheet.getRow(rowIx);
            if (row == null)
                continue;
            String[] aRow = new String[colCount+1];//加一列保存行号
            for (int colIx = 0; colIx < colCount; colIx++) {
                aRow[colIx] = getCellStringValue(row, colIx);
            }
            aRow[colCount] = "" + (row.getRowNum()+1);
            list.add(aRow);
        }

        return list;
    }

    /*
     * 读取office2007的excel文件(.xlsx)，以一行为一条记录 返回list
     */
    public static List<String[]> readXlsx(InputStream is) throws Exception {
        List<String[]> list = new ArrayList<String[]>();
        XSSFWorkbook workbook = new XSSFWorkbook(is);
        XSSFSheet sheet = workbook.getSheetAt(0);

        int rowLength = sheet.getPhysicalNumberOfRows();
        int headerLen = sheet.getRow(0).getPhysicalNumberOfCells();
        for (int i = 0; i < rowLength; i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null)
                continue;
            String[] aRow = new String[headerLen];
            for (int j = 0; j < headerLen; j++) {
                aRow[j] = getCellStringValue(row, j);
            }
            list.add(aRow);
        }

        return list;
    }
    
    /**
     * 读取json内容
     * @param inputStream
     * @return
     * @throws Exception
     */
	public static String readJsonFile(InputStream inputStream) throws Exception {
		List list = new ArrayList();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuffer buffer = new StringBuffer();
		
		String line = "";
		while((line=br.readLine()) != null) {
			buffer.append(line.trim());
		}
		inputStream.close();   
		return buffer.toString();
	}
	
	public static void main(String[] args){
		List list = ReadExcelFile.readExcel("F:\\Project_Object_Import_Example.xls");
		System.out.println();
	}
}
