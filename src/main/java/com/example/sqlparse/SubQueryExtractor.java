package com.example.sqlparse;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;

import java.util.ArrayList;
import java.util.List;

public class SubQueryExtractor {
    public static void main(String[] args) throws Exception {
        String sql = "SELECT u.id, (SELECT count(*) FROM logs l WHERE l.user_id = u.id) as log_count " +
                "FROM users u " +
                "JOIN (SELECT user_id, max(order_date) FROM (select * from orders where order_product = 'ORDER_PRODUCT') " +
                " GROUP BY user_id) o ON u.id = o.user_id " +
                "WHERE EXISTS (SELECT 1 FROM blacklist b WHERE b.user_id = u.id)";

        Statement statement = CCJSqlParserUtil.parse(sql);

        StatementVisitorAdapterCustom statementVisitorAdapterCustom = new StatementVisitorAdapterCustom();

        statement.accept(statementVisitorAdapterCustom);

        List<SubQueryDto> subQueries = statementVisitorAdapterCustom.getSubQueryDtos();

        subQueries.forEach(s -> {
            System.out.println("SQL: " + s.getSubQuery() + " ; Alias: " + s.getAlisa());
        });


    }
}
