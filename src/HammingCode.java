import java.lang.Math;

public class HammingCode extends ECC {

    /**
     * Constructor for HammingCode class
     * @param r - redundant bits
     */
    public HammingCode(int r) {
        this.setLen((int) Math.pow(2, r) - 1);
    }
}
