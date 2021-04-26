package bingo.dto;

import lombok.Data;

import java.util.Set;

@Data
public class Column implements Comparable<Column>
{

    private int index;

    private Set<Integer> values;

    public Column(int index, Set<Integer> values)
    {
        this.index = index;
        this.values = values;
    }

    @Override
    public int compareTo(Column o)
    {
        if (getValues() == null || getValues().isEmpty() || o.getValues() == null || o.getValues().isEmpty())
        {
            return 0;
        }
        if (this.getValues().size() < o.getValues().size())
        {
            return -1;
        }
        if (this.getValues().size() > o.getValues().size())
        {
            return 1;
        }
        return 0;
    }
}