import java.lang.reflect.Array;
import java.util.*;

public abstract class ECC implements IECC {
    private int length;
    private int dimension;
    private int distance;
    private ArrayList<BitSet> genMatrix;
    private ArrayList<BitSet> parCheckMatrix;
    private HashMap<BitSet, Error> synTable;
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

    public HashMap<BitSet, Error> getSynTable() {
        return synTable;
    }

    public void setSynTable(HashMap<BitSet, Error> synTable) {
        this.synTable = synTable;
    }

    protected HashMap<BitSet, BitSet> genSynTable() {
        ArrayList<BitSet> parityMatrix = getParCheckMatrix();
        int maxErrSize = (getDistance() - 1) / 2;
        int ubound = 0;
        BitSet error = new BitSet(getLength());
        //TODO add zero code

        for (int i = 0; i < maxErrSize; i++) ubound += binomial(getLength(), i);
        System.out.println(ubound);

        return null;
    }

    private ArrayList<ArrayList<Integer>> getErrorCodes(int upperbound, int errorNum) {
        ArrayList<Integer> combos = new ArrayList<>();
        ArrayList<ArrayList<Integer>> products = new ArrayList<>();
        for (int i = 1; i < upperbound; i++) combos.add(i);

        int n = combos.size();
        int N = errorNum;

        //Iterate from 1 to upper bound of list of possible permutations
        for (int i = 1; i < N; i++) {
            //Generate binary representation of current permutation in range 1 - 2^n
            String code = Integer.toBinaryString(N | i).substring(1);
            products.add(new ArrayList<>());

            //Iterate through bits in code
            for (int j = 0; j < n; j++) {
                //Checks if column in code corresponding to index 'j' is set
                if (code.charAt(j) == '1') {
                    products.get(i - 1).add(combos.get(j));
                }
            }
        }
        return products;
    }

    /**
     * Calculates the binomial coefficient of two numbers n and k
     * @param n - n elements
     * @param k - k elements
     * @return - binomial coefficient
     */
    protected int binomial(int n, int k) {
        return (factorial(n)) / (factorial(k) * factorial(n - k));
    }

    /**
     * Calculates factorial of a given number n
     * @param n - number to calculate factorial of
     * @return - n!
     */
    protected int factorial(int n) {
        int value = 1;

        for (int i = 2; i <= n; i++) {
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
