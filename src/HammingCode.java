import java.lang.Math;
import java.util.BitSet;

public class HammingCode extends ECC {

    /**
     * Constructor for HammingCode class
     * @param r - redundant bits
     */
    public HammingCode(int r) {
        //Length n is 2^r - 1 (where r is the number of parity bits)
        this.setLen((int) Math.pow(2, r) - 1);
        //Dimension is k, where k is n - r (length of code minus number of parity bits)
        this.setDim(this.getLength() - r);
    }

    /**
     * converts a vector of plaintext to the corresponding coded text.
     * @param plaintext the binary input
     * @param len the length of the plaintext
     * @return the encoded version of plaintext (padded with zeros to a whole number of blocks)
     */
    public BitSet encode(BitSet plaintext, int len) {
        printMatrix(plaintext, len);
        int size = encodedLength(this, len);

        BitSet encoding = new BitSet(size);
        encoding = setPlainText(encoding, plaintext, size);
        encoding = setParityBits(encoding, size);

        printMatrix(encoding, size);

        return encoding;
    }

    /**
     * Returns the length of a Code encoding a code of plainLength
     * @param c - Hamming Code object
     * @param plainLength - length of plaintext
     * @return - length of encoding
     */
    private int encodedLength(IECC c, int plainLength) {
        int d = c.getDimension();
        int l = c.getLength();
        return ((plainLength + d - 1) / d) * l;
    }

    /**
     * Assigns plaintext characters to a Hamming Encoding,
     * leaving spaces for parity bits to be set.
     * @param encoding - hamming encoding BitSet
     * @param plaintext - plain text BitSet
     * @param size - total size of Hamming Encoding including parity bits
     * @return
     */
    private BitSet setPlainText(BitSet encoding, BitSet plaintext, int size) {
        int exp = 0;
        int powTwo = (int) Math.pow(2, exp++);
        int textIndex = 0;

        for (int i = 1; i <= size; i++) {
            if (i == powTwo) {
                powTwo = (int) Math.pow(2, exp++);
            } else {
                encoding.set(i - 1, plaintext.get(textIndex++));
            }
        }

        return encoding;
    }

    /**
     * Sets the parity bits of encoding based on plaintext bits
     * @param encoding - BitSet to have parity bits set
     * @param size - size of encoding
     * @return encoding with correct parity
     */
    private BitSet setParityBits(BitSet encoding, int size) {
        int exp = 0;
        int powTwo = (int) Math.pow(2, exp++);
        int set = 0;

        //While the power computed is less than or equal to the size of the encoding
        while (powTwo <= size) {
            //Skip block of 2^n
            for (int offset = 0; (powTwo + offset) <= size; offset += 2 * powTwo) {
                //Iterate across current block of size 2^n and record number of set bits
                for (int i = powTwo; i <= (2* powTwo - 1); i++) {
                    if (encoding.get(offset + i - 1)) set++;
                }
            }

            //If number of set bits is odd, set parity bit
            if (set % 2 != 0) encoding.set(powTwo - 1);

            set = 0;
            powTwo = (int) Math.pow(2, exp++);
        }

        return encoding;
    }

    /**
     * Prints out a matrix representation of a bitset
     * @param matrix - BitSet to print
     * @param size - size of BitSet in bits
     */
    private void printMatrix(BitSet matrix, int size) {
        for (int i = 0; i < size; i++) {
            if (i == this.getLength()) System.out.println();

            if (matrix.get(i)) System.out.print(1);
            else System.out.print(0);
        }
        System.out.println();
    }
}
