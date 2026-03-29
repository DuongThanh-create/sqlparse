package com.example.sqlparse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SelectVisitorAdapterCustom extends SelectVisitorAdapter {

    private List<SubQueryDto> subQueryDtos;

    @Override
    public void visit(PlainSelect plainSelect) {
        // 1. Kiểm tra Subquery trong phần SELECT items
        for (SelectItem item : plainSelect.getSelectItems()) {

            findSubSelect(item.toString());
        }

        // 2. Kiểm tra Subquery trong phần FROM/JOIN
        if (plainSelect.getFromItem() instanceof SubSelect) {

            subQueryDtos.add(SubQueryDto.builder()
                            .subQuery(((SubSelect) plainSelect.getFromItem()).getSelectBody().toString())
                            .alisa(plainSelect.getFromItem().getAlias().getName())
                            .build());
        }
        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                if (join.getRightItem() instanceof SubSelect) {
                    subQueryDtos.add(
                            SubQueryDto.builder()
                                    .subQuery(((SubSelect) join.getRightItem()).getSelectBody().toString())
                                    .alisa(join.getRightItem().getAlias().getName())
                                    .build()
                    );
                }
            }
        }

        // 3. Kiểm tra Subquery trong phần WHERE (như EXISTS, IN, so sánh)
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(new ExpressionVisitorAdapter() {
                @Override
                public void visit(SubSelect subSelect) {
                    subQueryDtos.add(
                            SubQueryDto.builder()
                                    .subQuery(subSelect.getSelectBody().toString())
                                    .build()
                    );
                }
                @Override
                public void visit(ExistsExpression exists) {
                    exists.getRightExpression().accept(this);
                }
            });
        }
    }


    // Hàm phụ trợ quét nhanh chuỗi SelectItem
    private void findSubSelect(String text) {
        if (text.toUpperCase().contains("SELECT")) {
            // Logic bóc tách sâu hơn nếu cần, ở đây minh họa lấy text
            subQueryDtos.add(SubQueryDto.builder()
                    .subQuery(text)
                    .build());
        }
    }

}
