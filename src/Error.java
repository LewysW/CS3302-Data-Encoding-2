import java.util.BitSet;

public class Error {
    private boolean unique;
    private BitSet code;

    public Error(BitSet code) {
        setUnique(true);
        setCode(code);
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public BitSet getCode() {
        return code;
    }

    private void setCode(BitSet code) {
        this.code = code;
    }
}
