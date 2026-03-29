package com.example.sqlparse;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.schema.Table;

import java.util.*;

public class SqlSubqueryCleaner {

    public static void main(String[] args) throws Exception {
//        String sql = "SELECT u.id, u.name, u.cloumn, (SELECT count(l.hdhd) FROM logs l where l.thanh = 'oioi' ) as total_log " +
//                "FROM users u " +
//                "UNION " +
//                "SELECT e.id, (SELECT max(s.salary) FROM salary_history s) " +
//                "FROM employees e " +
//                "UNION " +
//                "SELECT uu.id, (SELECT count(*) FROM logs_a ll WHERE ll.user_id = u.id) as log_count " +
//                "FROM users_a uu " +
//                "JOIN (SELECT user_id_a, max(order_date) FROM (select user_id_a, order_date  from orders where order_product = 'ORDER_PRODUCT') k " +
//                " GROUP BY k.user_id) o ON uu.id = oo.user_id " +
//                "WHERE EXISTS (SELECT 1 FROM blacklist b WHERE b.user_id = u.id)";


        String sql = "SELECT id, name, cloumn, hdjdj as total_log " +
                "FROM users ";

        parseAndClean(sql);
    }

    public static void parseAndClean(String sql) throws Exception {
        Statement statement = CCJSqlParserUtil.parse(sql);
        Select select = (Select) statement;

        List<String> subQuerys = new ArrayList<>();

        select.getSelectBody().accept(new SelectVisitorAdapter() {
            @Override
            public void visit(SetOperationList setOpList) {
                for (SelectBody selectBody : setOpList.getSelects()) {
                    selectBody.accept(this);
                }
            }

            @Override
            public void visit(PlainSelect plainSelect) {

                // 1. Duyệt qua các SelectItems để tìm Subquery
                for (SelectItem item : plainSelect.getSelectItems()) {
                    if (item instanceof SelectExpressionItem) {
                        SelectExpressionItem sei = (SelectExpressionItem) item;

                        // Nếu là Subquery trong phần SELECT
                        if (sei.getExpression() instanceof SubSelect) {
                            SubSelect sub = (SubSelect) sei.getExpression();
                            // Lấy nội dung subquery (không lấy Alias của chính nó nếu có)
                            subQuerys.add(sub.getSelectBody().toString());

                        }
                    }
                }

                // 2. Duyệt qua FROM và JOIN để lấy Table (Xóa Alias khi in)
                processFromItemWithoutAlias(plainSelect.getFromItem(), subQuerys);
                if (plainSelect.getJoins() != null) {
                    for (Join join : plainSelect.getJoins()) {
                        processFromItemWithoutAlias(join.getRightItem(), subQuerys);
                    }
                }
            }
        });


        Map<String, Set<String>> mapResult = new HashMap<>();

        // Lay danh sach cho subquery
        for(String query : subQuerys){
            System.out.println("subQuery: " + query);
            Statement statementSub = CCJSqlParserUtil.parse(query);
            TablesNamesFinderCustom finderCustomSub = new TablesNamesFinderCustom();
            finderCustomSub.init();
            statementSub.accept(finderCustomSub);
            Map<String, TableColumnDto> mapSubQuery = finderCustomSub.getStringTableColumnDtoMap();

            if(!mapSubQuery.isEmpty()) {

                for (TableColumnDto tableColumnDto : mapSubQuery.values()) {
                    if(tableColumnDto.getTableName() != null) {
                        if (mapResult.containsKey(tableColumnDto.getTableName())) {
                            if (tableColumnDto.getColumns() != null && !tableColumnDto.getColumns().isEmpty())
                                mapResult.get(tableColumnDto.getTableName()).addAll(tableColumnDto.getColumns());
                        } else {
                            if (tableColumnDto.getColumns() != null && !tableColumnDto.getColumns().isEmpty())
                                mapResult.put(tableColumnDto.getTableName(), tableColumnDto.getColumns());
                        }
                    }
                }
            }

        }

        // Lay danh sach query tong
        Statement statementQ = CCJSqlParserUtil.parse(sql);
        TablesNamesFinderCustom finderCustom = new TablesNamesFinderCustom();
        finderCustom.init();
        statementQ.accept(finderCustom);
        Map<String, TableColumnDto> mapQuery = finderCustom.getStringTableColumnDtoMap();
        if(!mapQuery.isEmpty()) {
            for (TableColumnDto tableColumnDto : mapQuery.values()) {
                if(tableColumnDto.getTableName() != null) {
                    if (mapResult.containsKey(tableColumnDto.getTableName())) {
                        if (tableColumnDto.getColumns() != null)
                            mapResult.get(tableColumnDto.getTableName()).addAll(tableColumnDto.getColumns());
                    } else {
                        if (tableColumnDto.getColumns() != null)
                            mapResult.put(tableColumnDto.getTableName(), tableColumnDto.getColumns());
                    }
                }
            }
        }

        for(String key: mapResult.keySet()){
            System.out.println("Ket qua bang: " + key + " ===> ");
            System.out.println("Truong: " + String.join(",", mapResult.get(key)));
        }

    }

    private static void processFromItemWithoutAlias(FromItem fromItem, List<String> subQuerys) {
        if (fromItem instanceof SubSelect) {
            SubSelect subSelect = (SubSelect) fromItem;
            subQuerys.add(subSelect.getSelectBody().toString());
        }
    }
}