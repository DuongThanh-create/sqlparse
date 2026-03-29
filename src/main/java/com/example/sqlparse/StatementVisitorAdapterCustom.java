package com.example.sqlparse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class StatementVisitorAdapterCustom extends StatementVisitorAdapter {

    private List<SubQueryDto> subQueryDtos = new ArrayList<>();

    @Override
    public void visit(Select select) {
        SelectVisitorAdapterCustom selectVisitorAdapterCustom = new SelectVisitorAdapterCustom();
        selectVisitorAdapterCustom.setSubQueryDtos(subQueryDtos);
        select.getSelectBody().accept(selectVisitorAdapterCustom);
    }

}
