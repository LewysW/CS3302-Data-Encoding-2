public class ECCFactory implements IECCFactory {
    public ECCFactory() {

    }

    @Override
    public IECC makeHammingCode(int r) {
        return new HammingCode(r);
    }

    @Override
    public IECC makeReedMullerCode(int k, int r) {
        return new ReedMullerCode(k, r);
    }
}
