package com.example.sqlparse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.IOException;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BatchReadXml {

    public static void main(String[] args) {
        // Đường dẫn đến thư mục chứa file XML
        String folderPath = "D:\\testjobsql";


        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile) // Chỉ lấy file, bỏ qua thư mục
                    .forEach(path -> {
                        System.out.println("--- Đang đọc file: " + path.getFileName() + " ---");
                        readSqlTags(path.toFile());
                    });
        } catch (IOException e) {
            System.err.println("Lỗi khi truy cập thư mục: " + e.getMessage());
        }
    }

    // Hàm xử lý logic XML (tách riêng để dễ quản lý)
    public static void readSqlTags(java.io.File xmlFile) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("sql");

            for (int i = 0; i < nList.getLength(); i++) {
                String sqlContent = nList.item(i).getTextContent().trim();
                if (!sqlContent.isEmpty()) {
                    processSqlContent(sqlContent);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi đọc file " + xmlFile.getName() + ": " + e.getMessage());
        }
    }


    private static void processSqlContent(String rawSql) {
        // 1. Xóa các khoảng trắng thừa, xuống dòng để dễ xử lý
        String cleanSql = rawSql.replaceAll("\\s+", " ").trim();

        // 2. Regex: Bắt đầu bằng select, dừng khi gặp ; hoặc cluster hoặc cuối chuỗi
        // (?i) giúp không phân biệt hoa thường
        String regex = "(?i)select.*?(?=;|cluster|$)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(cleanSql);

        while (matcher.find()) {
            String result = matcher.group().trim();
            if (!result.isEmpty()) {
                System.out.println("-> SQL tìm thấy: " + result);
            }
        }
    }


}