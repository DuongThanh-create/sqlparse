package com.example.sqlparse;

import lombok.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class TablesNamesFinderCustom extends TablesNamesFinder {

    private Set<String> columns = new HashSet<>();
    private Set<String> tables = new HashSet<>();

    @Override
    public void visit(Column tableColumn) {
        // Lấy tên trường (có thể kèm alias table nếu có)
//        columns.add(tableColumn.getFullyQualifiedName() );
        columns.add(tableColumn.getColumnName() + " :::  " + tableColumn.getTable());
        super.visit(tableColumn);
    }

    @Override
    public void visit(Table table) {
        tables.add(table.getFullyQualifiedName() + "=====" +  table.getAlias());
        super.visit(table);
    }

    public void init() {
        this.init(false);
    }

}
