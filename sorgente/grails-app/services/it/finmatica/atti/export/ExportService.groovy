package it.finmatica.atti.export

import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFRichTextString
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.zkoss.zul.Filedownload

class ExportService {

	void downloadExcel (def exportOptions, def listaDocumenti) {
		byte[] fileExcel = createExcel(exportOptions, listaDocumenti)
		
		InputStream is = new ByteArrayInputStream(fileExcel);
		Filedownload.save(is, "application/vnd.ms-excel", "export_"+new Date().format("yyyyMMddHHmmss")+".xls")
		is.close()
	}
	
	byte[] createExcel (def exportOptions, def listaDocumenti) {

		Workbook wb = new HSSFWorkbook()
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("Export");
		
		Row testata = sheet.createRow((short)0);
		
		Font font= wb.createFont();
	    font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		CellStyle style = wb.createCellStyle()
		style.setFont(font);
		style.setWrapText(true);
		
		Set set = exportOptions.entrySet();
		Iterator iterator = set.iterator();

		int columns = 0
		while(iterator.hasNext()) {
			Map.Entry me = (Map.Entry)iterator.next();
			def map = me.getValue()
			if (map?.index > -1) {
				columns++
				Cell cell = testata.createCell(map.index)
				cell.setCellValue(map.label)
				cell.setCellStyle(style)
			}
		}

		for(int num_row = 0; num_row < listaDocumenti.size(); num_row++ ) {
			def rows = listaDocumenti.get(num_row)
			Set columnSet = rows.entrySet();

			Row row = sheet.createRow((short)num_row+1);
			for(Iterator columnIterator = columnSet.iterator();columnIterator.hasNext();) {
				Map.Entry column = (Map.Entry)columnIterator.next();
				if (exportOptions[column.getKey()]?.index > -1){
					Cell cell = row.createCell(exportOptions[column.getKey()].index);
					String key = column.getKey()
					String value = column.getValue()
					// elimino gli spazi bianchi dall'inzio e fine riga perché in alcuni casi
					// ci sono degli "a capo" che fanno sembrare la riga vuota (ad es. nel campo oggetto): https://erp.finmatica.it/finsapweb/chiamataServizio.aspx?EDIT=259581
					value = value?.trim()
					if (value != null && value.length() > 0){
						switch (exportOptions[key].columnType) {
							case 'DATE':
								try {
									// #34171: per problemi di visualizzazione con excel 2013 le date vengono ora formattate come stringa
									/*
									HSSFCellStyle cellStyle = wb.createCellStyle();
									cellStyle.setDataFormat(wb.createDataFormat().getFormat(exportOptions[key].formato));
									cell.setCellStyle(cellStyle);
									*/
									cell.setCellValue(new HSSFRichTextString(new Date(column.getValue().getTime()).format(exportOptions[key].formato)));
									
								} catch (Exception e) {
									cell.setCellValue(new HSSFRichTextString(value));
								}
								break;
							case 'NUMBER':
								try {
									cell.setCellType(Cell.CELL_TYPE_NUMERIC)
									cell.setCellValue(column.getValue());
								} catch (Exception e) {
									cell.setCellValue(new HSSFRichTextString(value));
								}
								break;
							case 'BOOLEAN':
								try {
									//cell.setCellType(Cell.CELL_TYPE_BOOLEAN)
									//cell.setCellValue(column.getValue());
									if (column.getValue()){
										cell.setCellValue(new HSSFRichTextString("Si"));
									}
									else {
										cell.setCellValue(new HSSFRichTextString("No"));
									}
									
								} catch (Exception e) {
									cell.setCellValue(new HSSFRichTextString(value));
								}
								break;
							case 'TEXT':
							default:
								cell.setCellValue(new HSSFRichTextString(value));
								break;
						}
						HSSFCellStyle cellStyle = cell.getCellStyle();
						if (cellStyle == null) {
							cellStyle = wb.createCellStyle();
							cell.setCellStyle(cellStyle);
						}
						cellStyle.setWrapText(true);
					}
				}
			}
		}

		for(int i = 0; i< columns; i++){
			sheet.autoSizeColumn(i)
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		wb.write(bos);
		bos.close();
		return bos.toByteArray()
	}
}
