# Grid.java
 This program aims to calculate the average temerature of a 2D array of
 plates using parallel programming. The 2D array of plates has a border
 of plates with a preset temperature. To calculate the temperature of
 the inner plates, the average of the four surrounding plates is calculated.
 Because initial iterations many values have not been calculated, and are
 therefore considered to be 0, many iterations are needed to calculate a
 acurate average. The program shall continue iterating over the array until
 the sum of error, or difference between new values being calculated and
 old values, is sufficiantly low.
