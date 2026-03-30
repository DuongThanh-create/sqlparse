package com.example.sqlparse;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelWriter {
    public static void main(String[] args) {
        // 1. Tạo một Workbook mới (.xlsx)
        Workbook workbook = new XSSFWorkbook();

        // 2. Tạo một Sheet
        Sheet sheet = workbook.createSheet("Danh sách người dùng");

        // 3. Tạo dữ liệu mẫu
        Object[][] data = {
                {"Họ tên", "Email"},
                {"Nguyễn Văn A", "ana@example.com"},
                {"Trần Thị B", "bt@example.com"}
        };

        // 4. Duyệt qua mảng dữ liệu và đổ vào Sheet
        int rowNum = 0;
        for (Object[] datatype : data) {
            Row row = sheet.createRow(rowNum++);
            int colNum = 0;
            for (Object field : datatype) {
                Cell cell = row.createCell(colNum++);
                if (field instanceof String) {
                    cell.setCellValue((String) field);
                } else if (field instanceof Integer) {
                    cell.setCellValue((Integer) field);
                }
            }
        }

        // 5. Ghi file ra ổ đĩa
        try (FileOutputStream outputStream = new FileOutputStream("D:\\testjobsql\\UsersData.xlsx")) {
            workbook.write(outputStream);
            System.out.println("Ghi file Excel thành công!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
