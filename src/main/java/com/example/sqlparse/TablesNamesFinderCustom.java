package com.example.sqlparse;

import lombok.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.*;

@Setter
@Getter
@NoArgsConstructor
public class TablesNamesFinderCustom extends TablesNamesFinder {

//    private Set<String> columns = new HashSet<>();
//    private Set<String> tables = new HashSet<>();
    private Map<String, TableColumnDto> stringTableColumnDtoMap = new HashMap<>();

    @Override
    public void visit(Column tableColumn) {
        // Lấy tên trường (có thể kèm alias table nếu có)
        // columns.add(tableColumn.getFullyQualifiedName() );
        if(tableColumn.getTable() != null){
            if(stringTableColumnDtoMap.containsKey(tableColumn.getTable().getName())){
                TableColumnDto tableColumnDto = stringTableColumnDtoMap.get(tableColumn.getTable().getName());
                tableColumnDto.getColumns().add(tableColumn.getColumnName());
            } else {
                TableColumnDto tableColumnDto = new TableColumnDto();
                tableColumnDto.getColumns().add(tableColumn.getColumnName());
                stringTableColumnDtoMap.put(tableColumn.getTable().getName(), tableColumnDto);
            }
        } else{
            if(stringTableColumnDtoMap.containsKey("NO_ALIAS")){
                TableColumnDto tableColumnDto = stringTableColumnDtoMap.get("NO_ALIAS");
                tableColumnDto.getColumns().add(tableColumn.getColumnName());
            } else {
                TableColumnDto tableColumnDto = new TableColumnDto();
                tableColumnDto.getColumns().add(tableColumn.getColumnName());
                stringTableColumnDtoMap.put("NO_ALIAS", tableColumnDto);
            }
        }
        super.visit(tableColumn);
    }

    @Override
    public void visit(Table table) {
        if(table.getAlias() != null) {
            if (stringTableColumnDtoMap.containsKey(table.getAlias().getName())) {
                TableColumnDto tableColumnDto = stringTableColumnDtoMap.get(table.getAlias().getName());
                if (tableColumnDto.getTableName() == null) tableColumnDto.setTableName(table.getName());
            } else {
                stringTableColumnDtoMap.put(table.getAlias().getName(), TableColumnDto.builder()
                        .tableName(table.getName())
                        .columns(new HashSet<>())
                        .build());
            }
        } else{

            if (!stringTableColumnDtoMap.containsKey("NO_ALIAS")) {
                TableColumnDto tableColumnDto = new TableColumnDto();
                tableColumnDto.setTableName(table.getName());
                stringTableColumnDtoMap.put("NO_ALIAS", tableColumnDto);
            } else {
                TableColumnDto tableColumnDto = stringTableColumnDtoMap.get("NO_ALIAS");
                tableColumnDto.setTableName(table.getName());
            }
        }
        super.visit(table);
    }

    public void init() {
        this.init(false);
    }

}
