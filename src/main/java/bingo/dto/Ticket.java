package bingo.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Ticket
{

    private int ticketNo;

    private List<Row> rows;

    private static final int TICKET_LENGTH = 3;

    public Ticket(int ticketNo)
    {
        this.ticketNo = ticketNo;
        this.rows = new ArrayList<>();
        while (rows.size() < TICKET_LENGTH)
        {
            rows.add(new Row(rows.size()));
        }
    }
    public Ticket(int ticketNo, List<Row> rows)
    {
        this.ticketNo = ticketNo;
        this.rows = rows;
    }

    public Ticket copy()
    {
        List<Row> rowList = new ArrayList<>();
        getRows().forEach(row -> rowList.add(row.clone()));
        return new Ticket(this.ticketNo,rowList);
    }

    public void sortColumns()
    {
        for (int i = 0; i < 9; i++)
        {
            List<Integer> columnValues = new ArrayList<>();
            int key = i;
            // Populate the columnValues list.
            rows.stream().filter(rowSummary -> rowSummary.getColumnValues().containsKey(key))
                .forEach(rowSummary -> columnValues.add(rowSummary.getColumnValues().get(key)));
            // Sort the list
            Collections.sort(columnValues);
            // Set the values in the rows.
            for (int j = 0; j < rows.size(); j++)
            {
                Row row = rows.get(j);
                row.setRowNo(j);
                if (row.getColumnValues().containsKey(key))
                {
                    row.getColumnValues().put(key, columnValues.get(0));
                    columnValues.remove(0);
                }
            }
        }
    }

}
