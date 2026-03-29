package com.example.sqlparse;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.List;

public class SqlSubqueryCleaner {

    public static void main(String[] args) throws Exception {
        String sql = "SELECT u.id, (SELECT count(*) FROM logs l) as total_log " +
                "FROM users u " +
                "UNION " +
                "SELECT e.id, (SELECT max(salary) FROM salary_history) " +
                "FROM employees e " +
                "UNION " +
                "SELECT u.id, (SELECT count(*) FROM logs l WHERE l.user_id = u.id) as log_count " +
                "FROM users u " +
                "JOIN (SELECT user_id, max(order_date) FROM (select * from orders where order_product = 'ORDER_PRODUCT') k " +
                " GROUP BY user_id) o ON u.id = o.user_id " +
                "WHERE EXISTS (SELECT 1 FROM blacklist b WHERE b.user_id = u.id)";

        parseAndClean(sql);
    }

    public static void parseAndClean(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;

        List<String> test = new ArrayList<>();

        select.getSelectBody().accept(new SelectVisitorAdapter() {
            @Override
            public void visit(SetOperationList setOpList) {
                for (SelectBody selectBody : setOpList.getSelects()) {
                    selectBody.accept(this);
                }
            }

            @Override
            public void visit(PlainSelect plainSelect) {
                System.out.println("\n--- Đang xử lý một nhánh SELECT ---");

                // 1. Duyệt qua các SelectItems để tìm Subquery
                for (SelectItem item : plainSelect.getSelectItems()) {
                    if (item instanceof SelectExpressionItem) {
                        SelectExpressionItem sei = (SelectExpressionItem) item;

                        // Nếu là Subquery trong phần SELECT
                        if (sei.getExpression() instanceof SubSelect) {
                            SubSelect sub = (SubSelect) sei.getExpression();
                            // Lấy nội dung subquery (không lấy Alias của chính nó nếu có)
                            test.add(sub.getSelectBody().toString());

                        } else {
                            // Đây là cột bình thường, in ra nội dung biểu thức (u.id, ...)
                            System.out.println("Trường dữ liệu: " + sei.getExpression().toString());
                        }
                    }
                }

                // 2. Duyệt qua FROM và JOIN để lấy Table (Xóa Alias khi in)
                processFromItemWithoutAlias(plainSelect.getFromItem(), test);
                if (plainSelect.getJoins() != null) {
                    for (Join join : plainSelect.getJoins()) {
                        processFromItemWithoutAlias(join.getRightItem(), test);
                    }
                }
            }
        });


        test.forEach(t -> {
            System.out.println("Ket Qua: " + t);
        });

    }

    private static void processFromItemWithoutAlias(FromItem fromItem, List<String> test) {
        if (fromItem instanceof Table) {
            Table table = (Table) fromItem;
            // Chỉ lấy tên bảng, bỏ qua table.getAlias()
            System.out.println("Tên bảng (đã bỏ alias): " + table.getName());
        } else if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            test.add(subSelect.getSelectBody().toString());
        }
    }
}