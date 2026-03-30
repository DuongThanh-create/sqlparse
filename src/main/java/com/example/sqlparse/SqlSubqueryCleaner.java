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


        String sql = "SELECT b.action_audit_id, b.thue_bao, b.nhan_vien, b.shop_code, b.action_code, b.product_code, b.product_code_daunoi, b.sub_id, b.contract_id, b.issue_datetime, b.shop_province, b.staff_id, b.reason_id, b.status, b.act_status, b.dev_staff_id nhanvien_moigioi, b.manhanvien_moigioi, b.telecom_service_id, b.promotion_code, b.group_id, b.main FROM ( SELECT s.isdn thue_bao, s.sub_id sub_id, a.action_code, a.issue_datetime issue_datetime, a.user_name nhan_vien, sh.shop_code shop_code, sh.province shop_province, st.staff_id, st.shop_id, a.reason_id, s.status, s.contract_id, s.dev_staff_id, st1.staff_code manhanvien_moigioi, s.act_status, s.product_code Product_code, s.org_product_code product_code_daunoi, s.telecom_service_id, a.action_audit_id, s.promotion_code, gm.main, gm.group_id, row_number() over (partition by a.action_audit_id,a.issue_datetime ORDER BY a.issue_datetime DESC ) rank FROM f_action_audit a inner join subscriber s on (a.pk_id = s.sub_id and s.telecom_service_id in ( '73') and s.isdn not like 'msip%') left join f_bccs_sale_groups_member_pyc_29756 gm on (s.sub_id=gm.sub_id and gm.status=1 and gm.partition = '${YYYYMMDD:DD-1}') left join d_staff st on (st.staff_code = a.user_name and st.status = 1) left join d_shop sh on (st.shop_id = sh.shop_id and sh.status = 1) left join d_staff st1 on (st1.staff_id = s.dev_staff_id and st1.status = 1) WHERE 1=1 and a.action_code IN ('00') and a.partition >= '${YYYYMM01:MM-1}' and a.partition < '${YYYYMM01}' and substr(a.issue_datetime,1,8) >= '${YYYYMM01:MM-1}' and substr(a.issue_datetime,1,8) < '${YYYYMM01}' ) b WHERE rank = 1";

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