import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import bingo.service.BingoStripGenerator;
import bingo.dto.Row;
import bingo.dto.Strip;
import bingo.dto.Ticket;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class BingoStripGeneratorTest
{

    private final BingoStripGenerator bingoStripGenerator = new BingoStripGenerator();

    @Test
    public void checkThatAllNumbersBetweenOneAndNinetyArePresentInStrip()
    {
        //6 tickets make up a whole strip resulting in all the numbers from 1-90. The total of these numbers should result in 4095.
        Strip strip = bingoStripGenerator.generateStrip();
        checkThatAllNumbersArePresent(strip);
    }

    @Test
    public void checkThatAllNumbersAreUniqueInStrip()
    {
        //6 tickets make up a whole strip resulting in all the numbers from 1-90. The total of these numbers should result in 4095.
        Strip strip = bingoStripGenerator.generateStrip();
        checkThatAllUniqueNumbersArePresent(strip);
    }

    @Test
    public void checkThatMultipleStripsAreGenerated()
    {
        List<Strip> strips = null;
        try
        {
            strips = bingoStripGenerator.generateStripsThreaded(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Assert.assertNotNull(strips);
        Assert.assertEquals("Check that 10 strips are generated", 100, strips.size());
    }

    @Test
    public void checkThatHundredThousandStripsAreGeneratedInLessThanTenSeconds()
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Strip> strips = null;
        try
        {
            strips = bingoStripGenerator.generateStripsThreaded(100000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        stopwatch.stop();
        Assert.assertNotNull(strips);
        Assert.assertEquals(100000,strips.size());
        long duration = stopwatch.elapsed().getSeconds();
        System.out.println("Took total of " + duration + " seconds");
        Assert.assertTrue("Check that 100K strips take less than 10 seconds", duration < 10L);
    }

    @Test
    public void checkThatTenThousandStripsAreGeneratedInLessThanTwoSeconds()
    {
        Stopwatch stopwatch = Stopwatch.createStarted();
        List<Strip> strips = null;
        try
        {
            strips = bingoStripGenerator.generateStripsThreaded(10000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        stopwatch.stop();
        Assert.assertNotNull(strips);
        Assert.assertEquals(10000,strips.size());
        long millis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        long duration = stopwatch.elapsed().getSeconds();
        System.out.println("Took total of " + millis + " ms");
        Assert.assertTrue("Check that 10K strips take less than 2 seconds", duration < 2L);
    }



    @Test
    public void printStrip()
    {
        Strip strip = bingoStripGenerator.generateStrip();
        strip.print();
    }

    @Test
    public void printMultipleStrips()
    {
        System.out.println("Printing 100 Strips..");
        List<Strip> strips = null;
        try
        {
            strips = bingoStripGenerator.generateStripsThreaded(100);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Assert.assertNotNull(strips);
        strips.forEach(Strip::print);
    }

    @Test
    public void checkAllBingoRules() {

        Strip strip  = bingoStripGenerator.generateStrip();
        checkThatAllNumbersArePresent(strip);
        checkThatAllUniqueNumbersArePresent(strip);
        checkThatThereAreNoEmptyColumns(strip);
        checkThatAllRowsContainFiveNumbers(strip);
    }


    @Test
    public void checkBingoRulesInMultipleStripts() {
        System.out.println("Checking Bingo ruless for 10K strips");
        List<Strip> results = null;
        try
        {
            results = bingoStripGenerator.generateStripsThreaded(10000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Assert.assertNotNull(results);
        results.forEach(strip -> {
            strip.print();
            checkThatAllNumbersArePresent(strip);
            checkThatAllUniqueNumbersArePresent(strip);
            checkThatThereAreNoEmptyColumns(strip);
            checkThatAllRowsContainFiveNumbers(strip);
        });
    }

    private void checkThatThereAreNoEmptyColumns(Strip strip)
    {
        for (Ticket ticket : strip.getTicketList())
        {
            Map<Integer,Integer> columnCount = Maps.newHashMap();
            while(columnCount.keySet().size() <=8) {
                columnCount.put(columnCount.keySet().size(),0);
            }
            for (Row row : ticket.getRows())
            {
                for (Integer integer : row.getColumnValues().keySet())
                {
                    columnCount.put(integer, columnCount.get(integer) + 1);
                }
            }
            Assert.assertFalse("Check that no column is empty", columnCount.containsValue(0));
        }

    }

    private void checkThatAllRowsContainFiveNumbers(Strip strip)
    {
        for (Ticket ticket : strip.getTicketList())
        {
            for (Row row : ticket.getRows())
            {
                Assert.assertEquals("Row contains 5 numbers", 5, row.getColumnValues().size());
            }
        }
    }

    private void checkThatAllUniqueNumbersArePresent(Strip strip)
    {
        Set<Integer> numbers = new HashSet<>();
        for (Ticket ticket : strip.getTicketList())
        {
            for (Row row : ticket.getRows())
            {
                numbers.addAll(row.getColumnValues().values());
            }
        }

        Assert.assertEquals("Check that there are 90 entries in a unique set", 90, numbers.size());
    }

    private void checkThatAllNumbersArePresent(Strip strip)
    {

        Set<Integer> nos = Sets.newHashSet();
        int sum = 0;
        for (Ticket ticket : strip.getTicketList())
        {
            for (Row row : ticket.getRows())
            {
                int rowSum = row.getColumnValues().values().stream().mapToInt(Integer::intValue).sum();
                sum = sum + rowSum;
            }
        }
        Assert.assertEquals("Check that total sum of all values is equal to 4095", 4095, sum);
    }
}
