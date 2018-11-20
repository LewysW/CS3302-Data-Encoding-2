public class ECCFactory implements IECCFactory {
    public ECCFactory() {

    }

    @Override
    public IECC makeHammingCode(int r) {
        try {
            return new HammingCode(r);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public IECC makeReedMullerCode(int k, int r) {
        try {
            return new ReedMullerCode(k, r);
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            return null;
        }
    }
}
