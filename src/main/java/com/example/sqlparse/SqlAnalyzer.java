package com.example.sqlparse;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlAnalyzer {
    public static void main(String[] args) {
//        String sql = "SELECT u.id, (SELECT count(*) FROM logs l WHERE l.user_id = u.id) as log_count " +
//                "FROM users u " +
//                "JOIN (SELECT user_id, max(order_date) FROM (select * from orders where order_product = 'ORDER_PRODUCT') k " +
//                " GROUP BY user_id) o ON u.id = o.user_id " +
//                "WHERE EXISTS (SELECT 1 FROM blacklist b WHERE b.user_id = u.id)";

        String sql = "SELECT user_id_a, max(order_date) FROM (SELECT user_id_a, order_date FROM " +
                "orders WHERE order_product = 'ORDER_PRODUCT') k GROUP BY k.user_id";

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);

            TablesNamesFinderCustom finderCustom = new TablesNamesFinderCustom();

            finderCustom.init();
            statement.accept(finderCustom);
            Map<String, TableColumnDto> map = finderCustom.getStringTableColumnDtoMap();

            for(String key : map.keySet()){

                System.out.println("===== Alias ===== " + key);
                System.out.println("===== Table ===== " + map.get(key).getTableName());
                System.out.println("===== Columns ===== " + String.join(",", map.get(key).getColumns()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}