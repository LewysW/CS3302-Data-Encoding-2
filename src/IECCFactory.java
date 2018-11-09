/** Interface declaration for factory class.
 * @author Steve Linton
 */
public interface IECCFactory {

    /** Make a Hamming Code with parameter r.
     * @param r the Hamming parameter
     * @return the code
     */
    IECC makeHammingCode(int r);

    /** Make a Reed-Muller Code with parameters r and k.
     * @param k the length parameter
     * @param r the r parameter
     * @return the code
     */
    IECC makeReedMullerCode(int k, int r);
}
