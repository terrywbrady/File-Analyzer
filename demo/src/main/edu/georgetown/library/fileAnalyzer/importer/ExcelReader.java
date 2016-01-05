package edu.georgetown.library.fileAnalyzer.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelReader {
	static Pattern pInt = Pattern.compile("^(\\d+)\\.0$");
	public static Vector<Vector<String>> readExcel(File f) throws InvalidFormatException, IOException {		
		return readExcelWorksheet(f, 0, 1);
	}

	public static Vector<Vector<String>> readExcelWorksheet(File f, int sheetIndex) throws InvalidFormatException, IOException {
		return readExcelWorksheet(f, sheetIndex, 0);
	}
    public static Vector<Vector<String>> readExcelWorksheet(File f, int sheetIndex, int maxSheets) throws InvalidFormatException, IOException {     
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        FileInputStream fs = new FileInputStream(f);
        try {
            Workbook w = WorkbookFactory.create(fs);
            readExcelWorksheet(data, w, sheetIndex, maxSheets);
        } catch (InvalidFormatException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            fs.close();
        }
        return data;
    }

    public static Vector<Vector<String>> readExcelWorksheetAllTabs(File f) throws InvalidFormatException, IOException {     
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        FileInputStream fs = new FileInputStream(f);
        try {
            Workbook w = WorkbookFactory.create(fs);
            int numsheets = w.getNumberOfSheets(); 
            for(int i=0; i<numsheets; i++) {
                readExcelWorksheet(data, w, i, numsheets);                
            }
        } catch (InvalidFormatException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            fs.close();
        }
        return data;
    }
    public static Vector<Vector<String>> readExcelWorksheet(Workbook w, int sheetIndex, int maxSheets) throws InvalidFormatException, IOException {        
        Vector<Vector<String>> data = new Vector<Vector<String>>();
        return readExcelWorksheet(data, w, sheetIndex, maxSheets);
    }
	public static Vector<Vector<String>> readExcelWorksheet(Vector<Vector<String>> data, Workbook w, int sheetIndex, int maxSheets) throws InvalidFormatException, IOException {		
        if (w.getNumberOfSheets() <= sheetIndex) throw new InvalidFormatException("Cannot find worksheet "+ sheetIndex);
        if (maxSheets != 0) {
            if (w.getNumberOfSheets() > maxSheets) throw new InvalidFormatException("Workbook has more than " + maxSheets + " worksheet(s)");               
        }
        Sheet sheet = w.getSheetAt(sheetIndex);
        for(int r=0; r<sheet.getPhysicalNumberOfRows(); r++) {
            Vector<String> row = new Vector<String>();
            data.add(row);
            Row srow = sheet.getRow(r);
            if (srow == null) continue;
            for(int c=0; c<srow.getLastCellNum(); c++) {
                Cell cell = srow.getCell(c); 
                if (cell == null) {
                    row.add(null);
                } else {
                    String val = "";
                    try {
                        if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                            val = ""+cell.getNumericCellValue();
                            Matcher m = pInt.matcher(val);
                            if (m.matches()) {
                                val = m.group(1);
                            }
                        } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                            val = cell.toString();
                        } else if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                            val = "";
                        } else {
                            val = cell.toString();
                        }
                    } catch (Exception e) {
                        val = "N/A "+e.getMessage();
                    }
                    row.add(val);
                }
                
            }
        }
		return data;
	}

}
