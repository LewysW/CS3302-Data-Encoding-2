import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;


class ECCPublicTests {

    private static IECCFactory f;
    private static IECC[] hammingCodes;
    private static IECC[][]  rmCodes;

    private static BitSet randomVector(int n, int seed) {
        Random r = new Random(seed);
        BitSet b = new BitSet(n);
        for (int i = 0; i < n; i++) {
            b.set(i, r.nextBoolean());
        }
        return b;

    }

    private static BitSet randomVector(int n) {
        return randomVector(n, n);
    }

    private static final int MIN_HAMMING = 3;
    private static final int MAX_HAMMING = 4;
    private static final int MIN_RM_K = 2;
    private static final int MAX_RM_K = 5;
    private static final int RM_K_CUTOFF = 5;
    private static final int RM_K_TRANSITIONAL = 2;
    private static final int RM_K_BOUND2 = 2;


    @BeforeAll
    static void setUp() throws Exception {
        f = new ECCFactory();
        hammingCodes = new IECC[MAX_HAMMING + 1];
        for (int i = MIN_HAMMING; i <= MAX_HAMMING; i++) {
            hammingCodes[i] = f.makeHammingCode(i);
        }
        rmCodes = new IECC[MAX_RM_K + 1][];
        for (int i = MIN_RM_K; i <= MAX_RM_K; i++) {
            rmCodes[i] = new IECC[i];
            for (int j = (i < RM_K_CUTOFF) ? 0 : (i == RM_K_CUTOFF) ? RM_K_TRANSITIONAL : i - RM_K_BOUND2; j < i; j++) {
                rmCodes[i][j] = f.makeReedMullerCode(i, j);
            }
        }
    }

    @Test
    void testLengths() {
        for (int i = MIN_HAMMING; i <= MAX_HAMMING; i++) {
            assertEquals(hammingCodes[i].getLength(), (1 << i) - 1);
        }
        for (int i = MIN_RM_K; i <= MAX_RM_K; i++) {
            if (rmCodes[i] != null) {
                for (int j = 0; j < i; j++) {
                    if (rmCodes[i][j] != null) {
                        assertEquals(rmCodes[i][j].getLength(), 1 << i);
                    }
                }
            }
        }
    }

    private static final int[][] RM_DIMS = {
        null, null,
        {1, 3},
        {1, 4, 7},
        {1, 5, 11, 15},
        {0, 0, 16, 26, 31}};


    @Test
    void testDims() {
        for (int i = MIN_HAMMING; i <= MAX_HAMMING; i++) {
            assertEquals(hammingCodes[i].getDimension(), (1 << i) - i - 1);
        }
        for (int i = MIN_RM_K; i <= MAX_RM_K; i++) {
            if (rmCodes[i] != null) {
                for (int j = 0; j < i; j++) {
                    if (rmCodes[i][j] != null) {
                        assertEquals(rmCodes[i][j].getDimension(), RM_DIMS[i][j]);
                    }
                }
            }
        }
    }

    private static final int MAX_VEC_LEN = 30;

    void testEncDecClean(IECC c) {
        for (int i = 0; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            int d = c.getDimension();
            int l = c.getLength();
            int enclen = ((i + d - 1) / d) * l;
            assertEquals(p, c.decodeAlways(c.encode(p, i), enclen));
        }
    }

    @Test
    void testEncDecClean() {
        for (int r = MIN_HAMMING; r <= MAX_HAMMING; r++) {
            testEncDecClean(hammingCodes[r]);
        }
        for (int i = MIN_RM_K; i <= MAX_RM_K; i++) {
            if (rmCodes[i] != null) {
                for (int j = 0; j < i; j++) {
                    if (rmCodes[i][j] != null) {
                        testEncDecClean(rmCodes[i][j]);
                    }
                }
            }
        }
    }

    private int encodedLength(IECC c, int plainLength) {
        int d = c.getDimension();
        int l = c.getLength();
        return ((plainLength + d - 1) / d) * l;
    }


    void testEncDecOneError(IECC c) {
        Random ran = new Random();
        for (int i = 1; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            int enclen = encodedLength(c, i);
            BitSet ciph = c.encode(p, i);
            int errpos = ran.nextInt(enclen);
            ciph.set(errpos, !ciph.get(errpos));
            assertEquals(p, c.decodeAlways(ciph, enclen), c.toString() + " does not decode one error");
        }
    }

    @Test
    void testEncDecOneError() {
        for (int r = MIN_HAMMING; r <= MAX_HAMMING; r++) {
            testEncDecOneError(hammingCodes[r]);
        }
        for (int i = MIN_RM_K; i <= MAX_RM_K; i++) {
            if (rmCodes[i] != null) {
                for (int j = 0; j < i - 1; j++) {
                    if (rmCodes[i][j] != null) {
                        testEncDecOneError(rmCodes[i][j]);
                    }
                }
            }
        }
    }

    void testEncDecTwoErrors(IECC c) {
        Random ran = new Random();
        for (int i = 1; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            int enclen = encodedLength(c, i);
            BitSet ciph = c.encode(p, i);

            ArrayList<Integer> errpos = new ArrayList<>();

            int errno;
            while (errpos.size() < 2) {
                errno = ran.nextInt(enclen);

                if (!errpos.contains(errno)) {
                    errpos.add(errno);
                }
            }

            for (Integer error : errpos) {
                ciph.set(error, !ciph.get(error));
            }
            assertEquals(p, c.decodeAlways(ciph, enclen), c.toString() + " does not decode one error");
        }
    }

    @Test
    void testEncDecTwoErrors() {
        ECCFactory eccFactory = new ECCFactory();

        for (int k = 0; k <= 4; k++) {
            for (int r = 0; r < k; r++) {
                if ((int) Math.pow(2, (k - r)) >= 5) {
                    ReedMullerCode reedMullerCode = (ReedMullerCode) eccFactory.makeReedMullerCode(k, r);
                    if (reedMullerCode != null) testEncDecTwoErrors(reedMullerCode);
                }
            }
        }
    }


    void testEncDecThreeErrors(IECC c) {
        Random ran = new Random();
        for (int i = 1; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            int enclen = encodedLength(c, i);
            BitSet ciph = c.encode(p, i);

            ArrayList<Integer> errpos = new ArrayList<>();

            int errno;
            while (errpos.size() < 3) {
                errno = ran.nextInt(enclen);

                if (!errpos.contains(errno)) {
                    errpos.add(errno);
                }
            }

            for (Integer error : errpos) {
                ciph.set(error, !ciph.get(error));
            }
            assertEquals(p, c.decodeAlways(ciph, enclen), c.toString() + " does not decode one error");
        }
    }

    @Test
    void testEncDecThreeErrors() {
        ECCFactory eccFactory = new ECCFactory();
        ReedMullerCode reedMullerCode = (ReedMullerCode) eccFactory.makeReedMullerCode(4, 1);
        testEncDecThreeErrors(reedMullerCode);
        reedMullerCode  = (ReedMullerCode) eccFactory.makeReedMullerCode(5, 2);
        testEncDecThreeErrors(reedMullerCode);
        reedMullerCode  = (ReedMullerCode) eccFactory.makeReedMullerCode(6, 3);
        testEncDecThreeErrors(reedMullerCode);
    }


    /**
     * Tests that decode if unique throws UncorrectableErrorException if
     * the syndrome is not in the hashmap for 2 errors with a code able
     * to correct only 1 error
     */
    @Test
    void testEncDecIfUniqueTwoErrorsException() {
        Random ran = new Random();
        int exceptionCount = 0;
        ECCFactory eccFactory = new ECCFactory();
        ReedMullerCode reedMullerCode = (ReedMullerCode) eccFactory.makeReedMullerCode(3, 1);
        for (int i = 2; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            BitSet ciph = reedMullerCode.encode(p, i);
            int enclen = encodedLength(reedMullerCode, i);
            int err;
            ArrayList<Integer> errors = new ArrayList<>();
            while (errors.size() < 2) {
                err = ran.nextInt(enclen);

                if (!errors.contains(err)) {
                    errors.add(err);
                    ciph.flip(err);
                }
            }

            try {
                reedMullerCode.decodeIfUnique(ciph, enclen);
            } catch (UncorrectableErrorException e) {
                exceptionCount++;
            }


        }

        assertTrue(exceptionCount > 0);
    }

    /**
     * Tests that decode if unique throws UncorrectableErrorException if
     * the syndrome is not in the hashmap for 4 errors with a code able
     * to correct only 3 error
     */
    @Test
    void testEncDecIfUniqueFourErrorsException() {
        Random ran = new Random();
        int exceptionCount = 0;
        ECCFactory eccFactory = new ECCFactory();
        ReedMullerCode reedMullerCode = (ReedMullerCode) eccFactory.makeReedMullerCode(4, 1);
        for (int i = 4; i < MAX_VEC_LEN; i++) {
            BitSet p = randomVector(i);
            BitSet ciph = reedMullerCode.encode(p, i);
            int enclen = encodedLength(reedMullerCode, i);
            int err;
            ArrayList<Integer> errors = new ArrayList<>();
            while (errors.size() < 4) {
                err = ran.nextInt(enclen);

                if (!errors.contains(err)) {
                    errors.add(err);
                    ciph.flip(err);
                }
            }

            try {
                reedMullerCode.decodeIfUnique(ciph, enclen);
            } catch (UncorrectableErrorException e) {
                exceptionCount++;
            }


        }

        assertTrue(exceptionCount > 0);
    }

    /**
     * Tests that something is returned for decode always even if missing from the syndrome table.
     */
    @Test
    void testDecAlwaysMissingSyndrome() {
        ECCFactory eccFactory = new ECCFactory();
        ReedMullerCode reedMullerCode = (ReedMullerCode) eccFactory.makeReedMullerCode(3, 1);

        //Generate an encoding with the first bit set.
        BitSet encoded = new BitSet(reedMullerCode.getLength());
        encoded.set(0);

        //Generates syndrome for encoding
        BitSet syndrome = reedMullerCode.matrixMult(encoded, reedMullerCode.getParCheckMatrix());

        //Gets decoding of code text
        BitSet originalDecoding = reedMullerCode.decodeAlways(encoded, reedMullerCode.getLength());

        //Removes the syndrome from the syndrome table
        reedMullerCode.getSynTable().remove(syndrome);

        //Gets the "best guess" using the shortest distance syndrome to generate a second decoding
        BitSet decoded = reedMullerCode.decodeAlways(encoded, reedMullerCode.getLength());

        //Finds shortest distance syndrome
        int shortest = 100;
        int temp;
        for (BitSet syn : reedMullerCode.getSynTable().keySet()) {
            if ((temp = reedMullerCode.distance(syn, syndrome, reedMullerCode.getLength())) < shortest) {
                shortest = temp;
            }
        }


        //Tests that both decodings were successful
        assertNotNull(decoded);
        assertNotNull(originalDecoding);

        //Tests that the decodings were not the same
        assertNotEquals(decoded, originalDecoding);

        //Tests that the decoding used corresponds to the closest syndrome
        assertEquals(shortest, reedMullerCode.distance(syndrome, reedMullerCode.getClosest(syndrome), reedMullerCode.getLength()));
    }


}
