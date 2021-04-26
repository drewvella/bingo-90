package bingo.service;

import bingo.dto.Column;
import bingo.dto.Row;
import bingo.dto.Strip;
import bingo.dto.Ticket;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
public class BingoStripGenerator
{

    private static final int THREAD_POOL_SIZE = 10;

    private static final int STRIP_SIZE = 6;

    private static final int MAX_RANGE = 90;

    /**
     * Generate a list of columns and their full values. Column 0 will contain 1-9, Column 1 = 10-19, Column 2 = 20-29, Column 3 = 30-39,
     * Column 4 = 40-49, Column 5 = 50-59, Column 6 = 60-69, Column 7 = 70-79, Column 8= 80-90.
     * @return List containing {@link Column}
     */
    private List<Column> generateColumnBaseData()
    {
        List<Column> columns = new ArrayList<>();
        Set<Integer> columnData = new HashSet<>();
        for (int i = 1, count = i + 1; i <= MAX_RANGE; i++, count++)
        {
            // Add the number to the column.
            columnData.add(i);
            // if the current count (started from 0 is divisible by 10 (will trigger on 9,19,29 etc) or the current number is the maximum number
            if (count % 10 == 0 || i == MAX_RANGE)
            {
                // If the count is not equal to 90 then keep adding empty strings till the maximum number of 18 is reached and reinitialise the
                // columnData to start a new column. Having the count != max condition here allows the number 90 to be added to the last column.
                if (count != MAX_RANGE)
                {
                    columns.add(new Column(columns.size(), columnData));
                    Collections.shuffle(columns);
                    columnData = new HashSet<>();
                }
            }
        }
        return columns;
    }

    /**
     * Generates a number of strips. For large amounts of strips this uses the base method {@link BingoStripGenerator#generateStrip()} to generate
     * a number of computed strips. These generated strips are then used at random to create further combinations without doing the actual full
     * computation. Refer to {@link Strip#copy()} and {@link BingoStripGenerator#shuffleStripColumns(Strip)} for more information.
     * @param noOfStrips No of strips to generate.
     * @return List of randomized Strips.
     */
    public List<Strip> generateStrips(int noOfStrips)
    {
        List<Strip> results = new ArrayList<>();
        while (results.size() < noOfStrips)
        {
            // If the results are empty or there are less than 5 results then generate a strip using the computed method.
            if (results.isEmpty() || results.size() < 5)
            {
                results.add(generateStrip());
            }
            else
            {
                //If there are some strips already generated use an existing one to generate a different combination.
                int randomIndex = getRandomNumberInIndexRange(0, results.size());
                // Pick one at random from the results.
                Strip templateStrip = results.get(randomIndex);
                // Clone the template
                Strip newStrip = templateStrip.copy();
                // Shuffle the columns which will result in a new combination.
                shuffleStripColumns(newStrip);
                // Add to the result
                results.add(newStrip);
            }
        }
        return results;
    }

    /**
     * Shuffles all the columns in a strip. This is done by iterating through all the rows and putting a random value picked from the column list
     * into the row column.
     * @param strip Strip which needs to be shuffled.
     */
    private void shuffleStripColumns(Strip strip)
    {

        //Get all possible column values and put them in a map for easier lookup.
        Map<Integer, List<Integer>> columnValues = new HashMap<>();
        List<Column> columns = generateColumnBaseData();
        columns.forEach(column -> columnValues.put(column.getIndex(), Lists.newArrayList(column.getValues())));
        // Replace all the values in the rows with random numbers picked from the column. The rows would already have values for that column but in
        // this way they are randomized to generate a new combination without doing the full computation of splitting the columns into rows.
        for (Ticket ticket : strip.getTicketList())
        {
            ticket.getRows().forEach(row ->
                                     {
                                         for (Integer columnNo : row.getColumnValues().keySet())
                                         {
                                             Integer value = columnValues.get(columnNo).get(0);
                                             row.getColumnValues().put(columnNo, value);
                                             columnValues.get(columnNo).remove(0);
                                         }
                                     });
            // Sort the ticket columns
            ticket.sortColumns();
        }
    }

    /**
     * Generates a number of Strips for Bingo 90.
     *
     * @param noOfStrips The number of strips to create.
     *
     * @return List of two dimensional string arrays containing layout of the strip (numbers and empty values)
     */
    public List<Strip> generateStripsThreaded(int noOfStrips) throws Exception
    {
        // Create a thread pool.
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        // Create tasks for each request.
        List<Strip> results = new ArrayList<>(generateStrips(Math.min(noOfStrips, 5)));
        if(results.size() == noOfStrips) {
            return  results;
        } else {
            int remaining = noOfStrips - results.size();
            for (int i = 0; i < remaining; i++)
            {
                Future<Strip> strip = executorService.submit(() ->
                                                             {
                                                                 int randomIndex = getRandomNumberInIndexRange(0, results.size());
                                                                 Strip templateStrip = results.get(randomIndex);
                                                                 Strip newStrip = templateStrip.copy();
                                                                 shuffleStripColumns(newStrip);
                                                                 return newStrip;
                                                             });
                results.add(strip.get());
            }
        }
        executorService.shutdown();
        return results;
    }

    /**
     * Generate a Strip made up of 6 tickets.
     *
     * @return {@link Strip} containing 6 tickets.
     */
    public Strip generateStrip()
    {

        List<Column> columns = generateColumnBaseData();
        List<Ticket> tickets = new ArrayList<>();
        try
        {
            //Create 6 tickets
            for (int ticketNo = 1; ticketNo <= STRIP_SIZE; ticketNo++)
            {
                Ticket ticket = new Ticket(ticketNo);
                tickets.add(ticket);
                // Add one column in every ticket.
                for (Column column : columns)
                {
                    List<Row> eligibleRows = ticket.getRows()
                                                   .stream()
                                                   .filter(row -> row.getColumnValues().keySet().size() < 5)
                                                   .collect(Collectors.toList());
                    // Pick random row in ticket.
                    int rowNumber = getRandomNumberInIndexRange(0, eligibleRows.size());
                    Row row = eligibleRows.get(rowNumber);
                    int value = getRandomValueFromColumn(column);
                    row.addNumber(column.getIndex(), value);
                }
            }
            // Populate remaining columns in row. There is a chance that the columns wont be enough or valid and will therefore trigger a
            // regeneration of the whole strip.
            for (Ticket ticket : tickets)
            {
                for (Row row : ticket.getRows())
                {
                    while (row.getColumnValues().keySet().size() < 5)
                    {
                        Column randomColumn = getRandomColumn(row.getColumnValues().keySet(), columns);
                        int value = getRandomValueFromColumn(randomColumn);
                        row.addNumber(randomColumn.getIndex(), value);
                    }
                }
                ticket.sortColumns();
            }
            Collections.shuffle(tickets);
            return new Strip(tickets);
        }
        catch (IllegalStateException e)
        {
            log.debug("Error when generating strip, triggering regeneration.");
            return generateStrip();
        }
    }

    private int getRandomValueFromColumn(Column column)
    {
        List<Integer> values = Lists.newArrayList(column.getValues());
        int value = values.get(0);
        column.getValues().remove(value);
        return value;
    }

    public Column getRandomColumn(Set<Integer> usedIndexes, List<Column> baseColumns)
    {
        List<Column> usableColumns =
            baseColumns.stream().filter(column -> !column.getValues().isEmpty() && !usedIndexes.contains(column.getIndex())).collect(
                Collectors.toList());

        if (usableColumns.isEmpty())
        {
            throw new IllegalStateException("No eligible columns found");
        }
        usableColumns.sort(Collections.reverseOrder());
        int randomColumn = getRandomNumberInIndexRange(0, usableColumns.size());
        return usableColumns.get(randomColumn);
    }

    private int getRandomNumberInIndexRange(int min, int max)
    {
        if (min == max)
        {
            return max;
        }

        if (min > max)
        {
            throw new IllegalArgumentException("max must be greater than min");
        }

        max = max - 1;
        return ThreadLocalRandom.current().nextInt((max - min) + 1) + min;
    }
}
