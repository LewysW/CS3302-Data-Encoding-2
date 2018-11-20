import java.lang.reflect.Array;
import java.util.*;

public abstract class ECC implements IECC {
    private int length;
    private int dimension;
    private int distance;
    private ArrayList<BitSet> genMatrix;
    private ArrayList<BitSet> parCheckMatrix;
    private HashMap<BitSet, BitSet> synTable = new HashMap<>();
    private static final int NONE = -1;

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    protected void setLen(int len) {
        this.length = len;
    }

    protected void setDim(int dim) {
        this.dimension = dim;
    }

    public ArrayList<BitSet> getGenMatrix() {
        return genMatrix;
    }

    protected void setGenMatrix(ArrayList<BitSet> genMatrix) {
        this.genMatrix = genMatrix;
    }

    public ArrayList<BitSet> getParCheckMatrix() {
        return parCheckMatrix;
    }

    protected void setParCheckMatrix(ArrayList<BitSet> parCheckMatrix) {
        this.parCheckMatrix = parCheckMatrix;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public HashMap<BitSet, BitSet> getSynTable() {
        return synTable;
    }

    public void setSynTable(HashMap<BitSet, BitSet> synTable) {
        this.synTable = synTable;
    }

    protected HashMap<BitSet, BitSet> genSynTable() {
        ArrayList<BitSet> parityMatrix = getParCheckMatrix();
        int maxErrSize = (getDistance() - 1) / 2;

        ArrayList<BitSet> errors = new ArrayList<>();
        int arr[] = new int[getLength()];
        for (int i = 0; i < getLength(); i++) arr[i] = i;
        int n = arr.length;

        for (int i = 0; i <= maxErrSize; i++) {
            getErrorCodes(arr, n , i, errors);
        }

        for (BitSet error : errors) {
            synTable.put(getSyndrome(error, parityMatrix), error);
        }


        System.out.println("Num Correctable Errors: " + maxErrSize);

        for (BitSet syndrome: synTable.keySet()) {
            System.out.print("Syndrome: ");
            for (int i = 0; i < (getLength() - getDimension()); i++) {
                if (syndrome.get(i)) {
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }

            System.out.print(", Error: ");

            BitSet error = synTable.get(syndrome);
            for (int i = 0; i < getLength(); i++) {
                if (error.get(i)) {
                    System.out.print(1);
                } else {
                    System.out.print(0);
                }
            }
            System.out.println();
        }


        return null;
    }

    protected BitSet getSyndrome(BitSet bitSet, ArrayList<BitSet> matrix) {
        BitSet result = new BitSet(getLength() - getDimension());
        int total = 0;

        for (int col = 0; col < (getLength() - getDimension()); col++) {
            for (int element = 0; element < matrix.size(); element++) {
                total += (matrix.get(element).get(col) && bitSet.get(element)) ? 1 : 0;
            }

            result.set(col, total % 2 != 0);
            total = 0;
        }


        return result;
    }

    /**
     * BEGIN CITATION
     * https://www.geeksforgeeks.org/print-all-possible-combinations-of-r-elements-in-a-given-array-of-size-n/
     * @param arr
     * @param data
     * @param start
     * @param end
     * @param index
     * @param r
     */
    private void combinationUtil(int arr[], int data[], int start,
                                int end, int index, int r, ArrayList<BitSet> errorCodes)
    {
        if (index == r) {
            BitSet error = new BitSet(getLength());
            for (int j=0; j<r; j++) {
                error.set(data[j]);
            }
            errorCodes.add(error);
            return;
        }

        for (int i=start; i<=end && end-i+1 >= r-index; i++) {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r, errorCodes);
        }
    }

    private void getErrorCodes(int arr[], int n, int r, ArrayList<BitSet> errCodes) {
        int data[]=new int[r];

        combinationUtil(arr, data, 0, n-1, 0, r, errCodes);
    }
    /**
     * END CITATION
     */


    /**
     * Calculates the binomial coefficient of two numbers n and k
     * @param n - n elements
     * @param k - k elements
     * @return - binomial coefficient
     */
    protected double binomial(long n, long k) {
        return (factorial(n)) / (factorial(k) * factorial(n - k));
    }

    /**
     * Calculates factorial of a given number n
     * @param n - number to calculate factorial of
     * @return - n!
     */
    protected double factorial(long n) {
        double value = 1;

        for (long i = 2; i <= n; i++) {
            value *= i;
        }

        return value;
    }

    /**
     * Generates a parity check matrix using the generator matrix
     * @return - parity check matrix
     */
    protected ArrayList<BitSet> genParityMatrix() {
        ArrayList<BitSet> parityMatrix = new ArrayList<>();
        ArrayList<BitSet> matrix = getGenMatrix();
        int length = getLength() - matrix.size();

        //Adds parity bits of generator matrix to top of parity check matrix
        for (int row = 0; row < matrix.size(); row++) {
            parityMatrix.add(new BitSet(length));
            for (int col = matrix.size(), pbit = 0; col < getLength(); col++, pbit++) {
                parityMatrix.get(row).set(pbit, matrix.get(row).get(col));
            }
        }

        //Adds identity matrix to bottom of parity check matrix
        for (int row = parityMatrix.size(), col = 0; row < getLength(); row++, col++) {
            parityMatrix.add(new BitSet(length));

            if (length == 1) {
                parityMatrix.get(row).set(0);
            } else {
                parityMatrix.get(row).set(col);
            }
        }

        return parityMatrix;
    }

    /**
     * Puts a given matrix into standard form.
     *
     * Does this by processing matrix line-by-line,
     * moving row with left most 1 bit for current column to top of unprocessed matrix.
     * Then eliminates all 1 bits above and below by adding current row to the other rows.
     * If there is no set bit in the current column at the correct row, columns are swapped until there is.
     * @param genMatrix - matrix to standardise
     */
    protected void standardise(ArrayList<BitSet> genMatrix) {
        //Iterates over each row, rearranging the matrix to get the leftmost one in the correct position
        for (int row = 0, col = 0; row < genMatrix.size(); row++, col++) {
            int setRow = NONE;
            int nextCol = col + 1;

            //Repeat while there is no one in the current column
            while(setRow == NONE) {

                setRow = getSetRow(genMatrix, row, col);

                if (setRow == NONE) {
                    swapColumns(genMatrix, col, nextCol++);
                } else if (setRow != row) {
                    Collections.swap(genMatrix, setRow, row);
                }
            }

            clearColumn(genMatrix, row, col);
        }
    }

    /**
     * Gets the index of a row in a subset of a matrix that has a 1 bit set in a given column
     * @param genMatrix - matrix to find row in
     * @param startRow - beginning of matrix to search from
     * @param col - column to check bit of
     * @return - index of row containing set bit, or -1 if none
     */
    private int getSetRow(ArrayList<BitSet> genMatrix, int startRow, int col) {
        for (int row = startRow; row < genMatrix.size(); row++) {
            if (genMatrix.get(row).get(col)) {
                return row;
            }
        }
        return -1;
    }

    /**
     * Clears set bits above and below given row in the given column by adding rows together
     * @param genMatrix - matrix to operate on
     * @param row - row to add to every other row
     * @param col - column to clear (aside from given row)
     */
    private void clearColumn(ArrayList<BitSet> genMatrix, int row, int col) {
        for (BitSet bs : genMatrix) {
            if (bs != genMatrix.get(row) && bs.get(col)) {
                bs.xor(genMatrix.get(row));
            }
        }
    }

    /**
     * Swaps two columns in a given matrix
     * @param matrix - matrix to operate on
     * @param col1 - first column to swap
     * @param col2 - second column to swap
     */
    public void swapColumns(ArrayList<BitSet> matrix, int col1, int col2) {
        if (col1 >= getLength() || col2 >= getLength()) return;

        for (int i = 0; i < matrix.size(); i++) {
            boolean temp = matrix.get(i).get(col1);
            matrix.get(i).set(col1, matrix.get(i).get(col2));
            matrix.get(i).set(col2, temp);
        }
    }

    /**
     * Prints a matrix
     * @param matrix - to print
     */
    protected void printMatrix(ArrayList<BitSet> matrix, int len) {
        for (BitSet b : matrix) {
            for (int i = 0; i < len; i++) {
                if (b.get(i)) System.out.print("1 ");
                else System.out.print("0 ");
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public BitSet encode(BitSet plaintext, int len) {

        return null;
    }

    @Override
    public abstract BitSet decodeAlways(BitSet codetext, int len);

    @Override
    public BitSet decodeIfUnique(BitSet codetext, int len) {
        if (this instanceof HammingCode) {
            return this.decodeAlways(codetext, len);
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
