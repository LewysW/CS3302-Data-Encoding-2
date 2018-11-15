import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.BitSet;

public class ReedMullerCode extends ECC {

    public ReedMullerCode(int k, int r) {
        r = 1;
        k = 3;
        this.setLen((int) Math.pow(2, k));
        this.setDim(dimension(r, k));
        ArrayList<BitSet> genMatrix = new ArrayList<>();
        printMatrix(getMatrix(3, genMatrix));
    }

    private ArrayList<BitSet> generateMatrix(int r, int k, ArrayList<BitSet> matrix) {
        BitSet bitSet = new BitSet(this.getLength());

        if (r == 0) {
            bitSet.set(0, this.getLength());
            matrix.add(bitSet);
            return matrix;
        } else if (r == 1) {

        }

        return null;
    }

    /**
     * Generates a matrix with a fixed value of r and variable value of k
     * @return
     */
    private ArrayList<BitSet> getMatrix(int k) {
        ArrayList<BitSet> matrix = new ArrayList<>();
        ArrayList<BitSet> newMatrix = new ArrayList<>();

        for (int i = 0; i <= k; i++) {
            if (i == 0) {
                //Set row with all ones

                BitSet bitSet = new BitSet(1);
                bitSet.set(0, );
                matrix.add(bitSet);
            } else {
                //Set row with 2^n-1 ones and 2^n-1 zeros
                BitSet bitSet = new BitSet((int) Math.pow(2, k));
                int numOnes = (int) Math.pow(2, k - 1);
                bitSet.set(0, numOnes - 1, true);
                bitSet.set(numOnes, (2 * numOnes - 1), false);
                newMatrix.add(bitSet);

                //Set new matrix with two rows that are two fold the rows of the previous matrix
                for (int i = 0; i < matrix.size(); i++) {
                    bitSet = new BitSet((int) Math.pow(2, k));
                    int prevLen = (int) Math.pow(2, k - 1);

                    for (int j = 0; j < prevLen; j++) {
                        bitSet.set(j, matrix.get(i).get(j));
                        bitSet.set(j + prevLen, matrix.get(i).get(j));
                    }

                    newMatrix.add(bitSet);
                }
            }
        }
    }


    private int dimension(int r, int k) {
        int dimension = 0;

        for (int i = 0; i <= r; i++) {
            dimension += binomial(k, i);
        }

        return dimension;
    }

    private int binomial(int n, int k) {
        return (factorial(n)) / (factorial(k) * factorial(n - k));
    }

    private int factorial(int n) {
        int value = 1;

        for (int i = 2; i <= n; i++) {
            value *= i;
        }

        return value;
    }

    private void printMatrix(ArrayList<BitSet> matrix) {
        for (BitSet b : matrix) {
            for (int i = 0; i < this.getLength(); i++) {
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
