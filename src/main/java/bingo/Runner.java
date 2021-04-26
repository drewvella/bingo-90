package bingo;

import bingo.dto.Strip;
import bingo.service.BingoStripGenerator;

import java.util.List;

public class Runner
{

    public static void main(String[] args)
    {
        BingoStripGenerator bingoStripGenerator = new BingoStripGenerator();
        String value = args[0];
        if(value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Number of strips is required.");
        }
        int noOfStrips = Integer.parseInt(value);
        System.out.println("Generating strips....");
        List<Strip> stripList = null;
        try
        {
            stripList = bingoStripGenerator.generateStripsThreaded(noOfStrips);
            stripList.forEach(Strip::print);
            System.out.println("Strips generated");
        }
        catch (Exception e)
        {
            System.out.println("Something went wrong!");
            e.printStackTrace();
        }
        System.exit(0);
    }
}
