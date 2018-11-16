import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;

public abstract class ECC implements IECC {
    private int length;
    private int dimension;
    private ArrayList<BitSet> genMatrix;

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

    public void standardise(ArrayList<BitSet> genMatrix) {
        //Collections.swap(genMatrix, row1, row2);

    }

    public boolean isStandard() {
        //for (int i = 0; i <  )
        return false;
    }

    /**
     * Swaps two columns in a given matrix
     * @param matrix - matrix to operate on
     * @param col1 - first column to swap
     * @param col2 - second column to swap
     */
    public void swapColumns(ArrayList<BitSet> matrix, int col1, int col2) {
        if (col1 >= this.getLength() || col2 >= this.getLength()) return;

        for (int i = 0; i < this.getDimension(); i++) {
            boolean temp = matrix.get(i).get(col1);
            matrix.get(i).set(col1, matrix.get(i).get(col2));
            matrix.get(i).set(col2, temp);
        }

    }

    @Override
    public BitSet encode(BitSet plaintext, int len) {
        BitSet bsMatrix = new BitSet(len);
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
