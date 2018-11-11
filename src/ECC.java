import java.util.BitSet;

public abstract class ECC implements IECC {
    private int length;
    private int dimension;

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    public void setLen(int len) {
        this.length = len;
    }

    public void setDim(int dim) {
        this.dimension = dim;
    }

    @Override
    public abstract BitSet encode(BitSet plaintext, int len);

    @Override
    public BitSet decodeAlways(BitSet codetext, int len) {
        return null;
    }

    @Override
    public BitSet decodeIfUnique(BitSet codetext, int len) {
        return null;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
