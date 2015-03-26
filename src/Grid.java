/* Grid.java
 * ---------------
 * This program aims to calculate the average temerature of a 2D array of
 * plates using parallel programming.  The 2D array of plates has a border
 * of plates with a preset temperature.  To calculate the temperature of
 * the inner plates, the average of the four surrounding plates is calculated.
 * Because initial iterations many values have not been calculated, and are
 * therefore considered to be 0, many iterations are needed to calculate a
 * acurate average.  The program shall continue iterating over the array until
 * the sum of error, or difference between new values being calculated and
 * old values, is sufficiantly low.
 *
 * @author Alex Kraemer
 * @version October 14, 2014
 */


/*
 * The Grid class contains all of the variables and methods needed to create
 * an array of plate objects and solve the grid.
 */
public class Grid implements Runnable {

    /*
     * gridSize and NUMTHREADS are both variables that can be changed.  With
     * the condition that gridSize is atleast 2 larger than NUMTHREADS and that
     * both are positive integers, the program will handle and distribute work
     * to its threads evenly.
     */
    final static int gridSize = 100;
    final static int NUMTHREADS = 4;

    // Several variables to keep track of thread boundries, id's, grid error
    // as well as a 2D array of plates that will hold temeratures.
    static Plate[][] plateGrid;
    static double gridError = 100;
    int threadIteration = 0;
    int threadID;
    int startRow;
    int endRow;

    public Grid(){

    }

    // This constuctor defines the thread boundries and thread id.
    public Grid(int start, int end, int id) {
        startRow = start;
        endRow = end;
        threadID = id;
    }

    // This method calculates the average of the sum of each plate object.
    public double calculateGridAverage() {
        double tempAverage = 0;
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                tempAverage += plateGrid[i][j].getTemp();
            }
        }
        return (tempAverage / (gridSize * gridSize));
    }

    /*
     * solveGrid takes each plate in the plate array and calculates a new value
     * for it.  After a new value is calculated, the cell's error is calculated
     * and added to this iterations total error.  The new value is then placed
     * in that cell.
     */
    public void solveGrid() {
        double temp;
        double err = 0;
        for (int i = 1; i < gridSize - 1; i++) {
            for (int j = startRow; j < endRow; j++) {
                temp = plateGrid[i + 1][j].getTemp() + plateGrid[i][j + 1]
                        .getTemp() + plateGrid[i - 1][j].getTemp() + plateGrid
                                [i][j - 1].getTemp();

                err += Math.abs((temp * .25) - plateGrid[i][j].getTemp());
                plateGrid[i][j].setTemp(temp * .25);
            }
        }
        gridError = err;
    }

    // makeGrid instantiates each plate in the grid to zero, then sets the
    // outside values.
    public void makeGrid() {
        plateGrid = new Plate[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                plateGrid[i][j] = new Plate(0.0);
            }
        }

        for (int i = 1; i < gridSize - 1; i++) {
            plateGrid[i][0].setTemp(44.0);
            plateGrid[i][gridSize - 1].setTemp(80.0);
            plateGrid[0][i].setTemp(25.0);
            plateGrid[gridSize - 1][i].setTemp(92.0);
        }
    }

    // Corners only need to be calculated once, as border values do not change.
    public void solveCorners() {
        plateGrid[0][0].setTemp((plateGrid[0][1].getTemp() +
                plateGrid[1][0].getTemp()) * .5);

        plateGrid[0][gridSize - 1].setTemp((plateGrid[0][gridSize - 2].getTemp()
                + plateGrid[1][gridSize - 1].getTemp()) * .5);

        plateGrid[gridSize - 1][gridSize - 1].setTemp((plateGrid[gridSize - 1]
                [gridSize - 2].getTemp() + plateGrid[gridSize - 2]
                        [gridSize - 1].getTemp()) * .5);

        plateGrid[gridSize - 1][0].setTemp((plateGrid[gridSize - 1][1].getTemp()
                + plateGrid[gridSize - 2][0].getTemp()) * .5);
    }

    @Override
    public void run() {
        while (gridError > .005) {
            threadIteration++;
            solveGrid();
        }
        System.out.println("Thread " + threadID + " ran: " + threadIteration +
                " times");
    }

    /*
     * In the main, the program initializes the grid object and the grid objects
     * the program will use to set the thread boundries.
     *
     * @param args the command line arguments
     * @throws java.lang.Exception
     */
    public static void main(String[] args) throws Exception {
        Grid temp = new Grid();
        Grid[] area = new Grid[NUMTHREADS];
        Thread[] threads = new Thread[NUMTHREADS];
        temp.makeGrid();

        /*
         * Here the program determines the thread boundries.  As the borders of
         * the plate array do not need to be recalculated, idecies 0 and
         * gridSize are ignored.  Giving each thread an area from s to
         * (gridSize / NUMTHREADS + s) ensures that grid size is split evenly
         * between all of the threads with bias towards the last thread.  The
         * last thread may receive up to (NUMTHREADS - 1) extra rows for
         * calculation.
         */
        int s = 1;
        for (int i = 0; i < area.length - 1; i++) {
            area[i] = new Grid(s, (gridSize / NUMTHREADS + (s)), i);
            threads[i] = new Thread(area[i]);
            s += gridSize / NUMTHREADS;
        }
        area[area.length - 1] = new Grid(s, gridSize - 1, NUMTHREADS - 1);
        threads[threads.length - 1] = new Thread(area[area.length - 1]);

        temp.solveCorners();

        // Spawn each thread and run their respective areas of the grid.
        for (Thread thread : threads) {
            thread.start();
        }

        // Join the threads to ensure all children have finished processing
        // before program termination.
        for (Thread thread : threads) {
            thread.join();
        }

        // Display statistics to the user.
        System.out.println("Total grid error: " + gridError);
        System.out.println("Grid average: " + temp.calculateGridAverage());
    }
}

/*
 * A simple plate class to hold temperature values, including a constructor
 * a getter and a setter.
 *
 */
class Plate {
    double temp;

    public Plate(double temperature) {
        temp = temperature;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double newTemp) {
        temp = newTemp;
    }
}
