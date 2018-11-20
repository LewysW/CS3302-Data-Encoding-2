import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public class ReedMullerCode extends ECC {

    public ReedMullerCode(int k, int r) {
        setDistance((int) Math.pow(2, k - r));
        setLen((int) Math.pow(2, k));
        setDim(dimension(r, k));
        setGenMatrix(generateMatrix(k, r));
        standardise(getGenMatrix());
        setParCheckMatrix(genParityMatrix());

        System.out.println("k: " + k);
        System.out.println("r: " + r);

        System.out.println("Generator Matrix:");
        printMatrix(getGenMatrix(), getLength());
        System.out.println("Parity Check Matrix:");
        printMatrix(getParCheckMatrix(), getLength() - getGenMatrix().size());
        System.out.println("Syndrome Table:");
        genSynTable();
        System.out.println();
        System.out.println("------------------------------------");
    }

    /**
     * Generates a matrix with a fixed value of r and variable value of k
     * @k - k value of matrix
     * @return - generated matrix
     */
    private ArrayList<BitSet> generateMatrix(int k, int r) {
        ArrayList<BitSet> matrix = new ArrayList<>();
        ArrayList<BitSet> newMatrix = new ArrayList<>();

        for (int i = 0; i <= k; i++) {
            if (i == 0) {
                //Set row with all ones
                BitSet bitSet = new BitSet(1);
                bitSet.set(0);
                matrix.add(bitSet);
            } else {
                //Set row with 2^n-1 ones and 2^n-1 zeros
                BitSet bitSet = new BitSet((int) Math.pow(2, i));
                int numBits = (int) Math.pow(2, i - 1);
                bitSet.set(numBits, (2 * numBits), false);
                bitSet.set(0, numBits, true);
                newMatrix.add(bitSet);

                //Set new matrix with two rows that are two fold (two repetitions) the rows of the previous matrix
                for (int row = 0; row < matrix.size(); row++) {
                    bitSet = new BitSet((int) Math.pow(2, i));
                    int prevLen = (int) Math.pow(2, i - 1);

                    for (int bit = 0; bit < prevLen; bit++) {
                        bitSet.set(bit, matrix.get(row).get(bit));
                        bitSet.set(bit + prevLen, matrix.get(row).get(bit));
                    }

                    newMatrix.add(bitSet);
                }

                matrix = (ArrayList<BitSet>) newMatrix.clone();
                newMatrix.clear();
            }
        }
        invert(matrix);
        expand(matrix, r);
        standardise(matrix);
        return matrix;
    }

    /**
     * Flips and mirrors the rows of a given matrix
     * @param matrix
     * @return
     */
    private ArrayList<BitSet> invert(ArrayList<BitSet> matrix) {
        //Reverses ordering of rows in matrix
        Collections.reverse(matrix);
        BitSet temp = new BitSet(getLength());

        //Mirrors positions of bits in rows
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = getLength() - 1, k = 0; j >= 0; j--, k++) {
                temp.set(k, matrix.get(i).get(j));
            }

            matrix.set(i, (BitSet) temp.clone());
            temp.clear();
        }

        return matrix;
    }

    /**
     * Expands a given matrix of r = 1 to include the r-degree polynomials of a given value of r
     * e.g. if k = 3, r = 2, calculates x1x2, x1x3, x2x3 and adds each vector to matrix
     * @param matrix - matrix to expand
     * @param r - degree to expand to
     * @return expanded matrix
     */
    private ArrayList<BitSet> expand(ArrayList<BitSet> matrix, int r) {
        ArrayList<ArrayList<Integer>> products = getCombos(matrix.size());
        ArrayList<BitSet> temp = new ArrayList<>();

        //Iterates over each set of combinations of indices
        for (ArrayList<Integer> factors : products) {
            int index = 1;
            BitSet bs1 = new BitSet(getLength());
            BitSet bs2;

            //Limits number of rows based on degree, r
            if (factors.size() <= r) {

                //Iterates through list of indices of factors to multiply
                while (index < factors.size()) {
                    //Sets first factor to first item in list if index is 1
                    if (index == 1) {
                        bs1 = matrix.get(factors.get(index - 1));
                    }

                    //Sets second factor to next item in list
                    bs2 = matrix.get(factors.get(index++));

                    //If end of factors is reached, add result of vector multiplication to matrix
                    if (index == factors.size()) {
                        temp.add(multiply(bs1, bs2));

                        /*Otherwise store result as first factor for next multiplication*/
                    } else {
                        bs1 = multiply(bs1, bs2);
                    }
                }
            }
        }
        //TODO - possibly remove if it makes no difference to standardisation?
        //Add items in reverse order to matrix
        for (int i = temp.size() - 1; i >= 0; i--) matrix.add(temp.get(i));
        return matrix;
    }

    /**
     * BEGIN CITATION:
     * Function to get all possible unique combinations of matrix indexes from 1 to 2^n - 1
     * Code source: https://stackoverflow.com/questions/37835286/generate-all-possible-combinations-java
     * @matrix - to get combinations of indices of
     * @return - list of permutations of indices
     */
    protected ArrayList<ArrayList<Integer>> getCombos(int upperbound) {
        ArrayList<Integer> combos = new ArrayList<>();
        ArrayList<ArrayList<Integer>> products = new ArrayList<>();
        for (int i = 1; i < upperbound; i++) combos.add(i);

        int n = combos.size();
        int N = (int) Math.pow(2d, Double.valueOf(n));

        //Iterate from 1 to upper bound of list of possible permutations
        for (int i = 1; i < N; i++) {
            //Generate binary representation of current permutation in range 1 - 2^n
            String code = Long.toBinaryString(N | i).substring(1);
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
     * END CITATION
     */

    /**
     * Multiplies two BitSet objects using logical and ^
     * @param bs1 - first bitset
     * @param bs2 - second bitset
     * @return - result of multiplication
     */
    private BitSet multiply(BitSet bs1, BitSet bs2) {
        BitSet bitSet = (BitSet) bs1.clone();
        bitSet.and(bs2);
        return bitSet;
    }


    /**
     * Calulates the dimension of Reed-Muller code using the sum of k choose i in the range i = 0 to r
     * @param r
     * @param k
     * @return
     */
    private int dimension(int r, int k) {
        int dimension = 0;

        for (int i = 0; i <= r; i++) {
            dimension += binomial(k, i);
        }

        return dimension;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public BitSet decodeAlways(BitSet codetext, int len) {
        return null;
    }

    @Override
    public BitSet decodeIfUnique(BitSet codetext, int len) {
        return super.decodeIfUnique(codetext, len);
    }
}
