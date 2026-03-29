package com.example.sqlparse;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.HashSet;
import java.util.Set;

public class SqlAnalyzer {
    public static void main(String[] args) {
        String sql = "SELECT u.id, (SELECT count(*) FROM logs l WHERE l.user_id = u.id) as log_count " +
                "FROM users u " +
                "JOIN (SELECT user_id, max(order_date) FROM (select * from orders where order_product = 'ORDER_PRODUCT') k " +
                " GROUP BY user_id) o ON u.id = o.user_id " +
                "WHERE EXISTS (SELECT 1 FROM blacklist b WHERE b.user_id = u.id)";

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            TablesNamesFinderCustom finderCustom = new TablesNamesFinderCustom();

            finderCustom.init();
            statement.accept(finderCustom);
            // Bắt đầu quét toàn bộ câu lệnh
//            finderCustom.getTableList(statement);

            System.out.println("=== TẤT CẢ CÁC BẢNG ===");
            finderCustom.getTables().forEach(t -> System.out.println("Table: " + t));

            System.out.println("\n=== TẤT CẢ CÁC TRƯỜNG (BAO GỒM TRONG SUBQUERY) ===");
            finderCustom.getColumns().forEach(c -> System.out.println("Field: " + c));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}