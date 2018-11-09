import java.util.BitSet;

/** Interface which is the main specification for CS3302 practical 2.
 * @author Steve Linton
 * A general interface for binary linear error correcting codes
 *
 * We use the BitSet class to represent a binary vector. The only
 * problem is that a BitSet does not know its length, so we have to
 * supply the length as an additional argument to some methods
 */
public interface IECC {

    /**
     * getter.
     * @return the length of the code, the number of bits in each encoded block
     */
    int getLength();

    /**
     * getter.
     * @return the dimension of the code, the number of bits in each plaintext block
     */
    int getDimension();

    /**
     * converts a vector of plaintext to the corresponding coded text.
     * @param plaintext the binary input
     * @param len the length of the plaintext
     * @return the encoded version of plaintext (padded with zeros to a whole number of blocks)
     */
    BitSet encode(BitSet plaintext, int len);

    /**
     * decodes a vector of coded text of any length, padding it to whole number of blocks with 0 bits
     * and then replacing each block with the plaintext corresponding to A closest codeword (in Hamming distance).
     * @param codetext the binary input
     * @param len the length of the codetext
     * @return the decoded version of plaintext (padded to a whole number of blocks)
     */
    BitSet decodeAlways(BitSet codetext, int len);

    /**
     * decodes a vector of coded text of any length, padding it to whole number of blocks with 0 bits
     * and then replacing each block with the plaintext corresponding to the uniquie closest codeword (in Hamming distance)
     * if there isn't one then it throws an exception.
     * @param codetext the binary input
     * @param len the length of the codetext
     * @return the decoded version of plaintext (padded to a whole number of blocks)
     * @throws UncorrectableErrorException if there is no uniquely best decoding
     */
    BitSet decodeIfUnique(BitSet codetext, int len) throws UncorrectableErrorException;

    /** toString method for convenience in error message etc.
     * @return a concise human-readable representation of the code
     */
    String toString();
}
