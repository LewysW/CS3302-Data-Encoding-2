import java.lang.Math;
import java.util.BitSet;
import java.util.HashMap;

public class HammingCode extends ECC {

    /**
     * Constructor for HammingCode class
     * @param r - parity bits
     */
    public HammingCode(int r) {
        setDistance(3);
        //Length n is 2^r - 1 (where r is the number of parity bits)
        setLen((int) Math.pow(2, r) - 1);
        //Dimension is k, where k is n - r (length of code minus number of parity bits)
        setDim(getLength() - r);
    }

    /**
     * converts a vector of plaintext to the corresponding coded text.
     * @param plaintext the binary input
     * @param len the length of the plaintext
     * @return the encoded version of plaintext (padded with zeros to a whole number of blocks)
     */
    public BitSet encode(BitSet plaintext, int len) {
        int size = encodedLength(len);

        BitSet encoding = new BitSet(size);
        setPlainText(encoding, plaintext, size);
        setParityBits(encoding, size);

        return encoding;
    }

    /**
     * Checks if number is a power of two
     * @param x - number to check
     * @return - true or false
     */
    boolean powerOfTwo(int x) {
        return (x != 0) && ((x & (x - 1)) == 0);
    }

    /**
     * Decodes message by correcting any 1-bit error
     * and stripping the parity bits
     * @param codetext - message to be decoded
     * @param len - length of message
     * @return - decoded message
     */
    public BitSet decodeAlways(BitSet codetext, int len) {
        return stripParity(correctError(codetext, len), len);
    }

    /**
     * Strips parity bits from encoding
     * @param codetext - encoding to strip parity
     * @param len - length of encoding
     * @return decoded BitSet
     */
    private BitSet stripParity(BitSet codetext, int len) {
        BitSet decoding = new BitSet(len);
        int index = 0;

        for (int i = 1; i <= len; i++) {
            if (!powerOfTwo(i)) decoding.set(index++, codetext.get(i - 1));
        }

        return decoding;
    }

    /**
     * Corrects codetext with a single error
     * @param codetext - to be corrected
     * @param len - length of codetext
     * @return corrected codetext{
            if
     */
    BitSet correctError(BitSet codetext, int len) {
        HashMap<Integer, Boolean> pbits = new HashMap<>();
        int errOffset = 0;
        int exp = 0;

        //Records current parity bits of codetext and sets each to 0
        for (int i = 1; i < len; i = (int) Math.pow(2, ++exp)) {
            pbits.put(i, codetext.get(i - 1));
            codetext.set(i - 1, false);

        }

        //Recomputes the parity bits of the codetext
        BitSet corrected = setParityBits(codetext, len);

        //Works out the offset of the error bit using differences between original and recomputed parity bits
        exp = 0;
        for (int i = 1; i < len; i = (int) Math.pow(2, ++exp)) {
            if (!pbits.get(i).equals(corrected.get(i - 1))) errOffset += i;
            codetext.set(i - 1, pbits.get(i));
        }

        //Corrects the error bit
        corrected = codetext;
        if (errOffset > 0) corrected.flip(errOffset - 1);
        return corrected;
    }

    /**
     * Returns the length of a Code encoding a code of plainLength
     * @param plainLength - length of plaintext
     * @return - length of encoding
     */
    private int encodedLength(int plainLength) {
        int d = getDimension();
        int l = getLength();
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Length: ");
        sb.append(getLength());
        sb.append(" Dimension: ");
        sb.append(getDimension());
        sb.append(" Parity Bits: ");
        sb.append(getLength() - getDimension());
        return sb.toString();
    }

    /**
     * Prints out a matrix representation of a bitset
     * @param matrix - BitSet to print
     * @param size - size of BitSet in bits
     */
    private void printMatrix(BitSet matrix, int size) {
        for (int i = 0; i < size; i++) {
            if (i == getLength()) System.out.println();

            if (matrix.get(i)) System.out.print(1);
            else System.out.print(0);
        }
        System.out.println();
    }
}
