package bingo.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Row
{

    int rowNo = 0;

    private final Map<Integer, Integer> columnValues = new HashMap<>();

    public Row(int rowNo)
    {
        this.rowNo = rowNo;
    }

    public void addNumber(int columnIndex, int value) {
        this.getColumnValues().put(columnIndex,value);
    }

    public Map<Integer, Integer> getColumnValues()
    {
        return columnValues;
    }


    public Row clone() {
        Row row = new Row(rowNo);
        for (Map.Entry<Integer, Integer> integerIntegerEntry : columnValues.entrySet())
        {
            row.addNumber(integerIntegerEntry.getKey(),integerIntegerEntry.getValue());
        }
        return row;
    }

    @Override
    public String toString()
    {
        return "Row{" +
               "rowNo=" + rowNo +
               ", columnValues=" + columnValues +
               '}';
    }
}