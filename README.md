### Bingo 90 Strip Generator

A Bingo 90 Strip should follow the following rules:

- A strip is made up of 6 tickets
- Each ticket is made up of 9 columns and 3 rows.
- Each row contains five numbers and four blank spaces.
- Each ticket column consists of one, two or three numbers and never three blanks.
  -- The first column contains numbers from 1 to 9 (only nine),
  -- The second column numbers from 10 to 19 (ten), the third, 20 to 29 and so on up -- until
  -- The last column, which contains numbers from 80 to 90 (eleven).
- Numbers in the ticket columns are ordered from top to bottom (ASC)
- All numbers are unique across all tickets.

## Pre-requisites 
- Java 11
- Maven

## Libraries used

- https://mvnrepository.com/artifact/com.google.guava/guava for additional utility methods for list/set manipulations.
- https://mvnrepository.com/artifact/org.apache.commons/commons-lang3 for string manipulation utilities.
- https://mvnrepository.com/artifact/junit/junit for unit tests
- https://mvnrepository.com/artifact/org.projectlombok/lombok  to reduce boilerplate code
- https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12 - logging

## Running the application
- Download the sources
- In the application folder type `mvn compile`
- In the application folder type `mvn exec:java -Dexec.mainClass=bingo.Runner -Dexec.args="<noOfStrips>"` replacing noOfStrips with a valid number.


## Testing the application
All tests can be found in the BingoStripGeneratorTest class. They can be run using `mvn test`

## Further Information
This has proven to be quite a challenging task. The main issue is to generate enough working row/column placeholder combinations i.e. having enough column values in every row in every ticket. I think any algorithm will always hit some sort of "collision" when it comes to allocating columns to rows. I also think it is almost impossible to have an algorithm fast enough to compute all those combinations while adhering to the set rules so the approach I took for generating large amounts of tickets was split in two:
1. Having an algorithm which runs until it can compute a working combination for a Strip (i.e. adhering to all the rules)
2. The Strip that has been generated has already computed the row/column positions therefore it is then a simple matter of replacing the values for each row column with another value for that column.

Unfortunately generating large amounts of strips using this approach still takes a few seconds and I needed to add some multithreading into the mix. Generating 10K takes less than 5 seconds, while generating 100K takes less than 10 seconds. 

As a plus I did learn a lot about Bingo 90 though! :)

