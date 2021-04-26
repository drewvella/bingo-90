package bingo.dto;

import com.google.common.base.Strings;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class Strip
{

    private List<Ticket> ticketList;

    public Strip(List<Ticket> ticketList)
    {
        this.ticketList = ticketList;
    }

    public Strip copy() {
        List<Ticket> newTickets = new ArrayList<>();
        ticketList.forEach(ticket -> newTickets.add(ticket.copy()));
        Collections.shuffle(newTickets);
        return new Strip(newTickets);
    }

    public void print()
    {
        System.out.println("###############BINGO-90#############");
        for (Ticket ticket : ticketList)
        {
            for (Row row : ticket.getRows())
            {
                StringBuilder stringBuilder = new StringBuilder();

                for (int i = 0; i < 9; i++)
                {
                    stringBuilder.append("|");
                    String value = row.getColumnValues().get(i) == null ? "" : Integer.toString(row.getColumnValues().get(i));
                    stringBuilder.append(Strings.padStart(value, 3, ' '));
                }
                System.out.println(stringBuilder);
            }
            System.out.println("___________________________________");
        }
    }
}
