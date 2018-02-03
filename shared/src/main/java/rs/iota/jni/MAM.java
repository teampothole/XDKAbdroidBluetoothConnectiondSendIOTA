package rs.iota.jni;

public final class MAM {

    public static class DecodeResult {
        private final int[] message;
        private final int[] nextRoot;

        DecodeResult(int[] message, int[] nextRoot) {
            this.message = message;
            this.nextRoot = nextRoot;
        }

        /**
         * @return trit-encoded decoded message
         */
        public int[] getMessage() {
            return message;
        }

        /**
         * @return trit-encoded next Merkle tree getRoot as determined from masked payload
         */
        public int[] getNextRoot() {
            return nextRoot;
        }
    }

    /**
     * Encodes a message using MAM.
     *
     *
     * @param seed trit-encoded seed. Length must be a multiple of 243
     * @param message trit-encoded message
     * @param sideKey trit-encoded side key. Length must be 243, can be all 0s
     * @param root trit-encoded getRoot of the current Merkle tree
     * @param siblings trit-encoded siblings
     * @param nextRoot trit-encoded getRoot of the next Merkle tree
     * @param start seed's key index where current Merkle tree starts at
     * @param index index of current message in Merkle tree
     * @param security security level
     * @return trit-encoded masked authenticated message
     */
    public static int[] encode(
            int[] seed,
            int[] message,
            int[] sideKey,
            int[] root,
            int[] siblings,
            int[] nextRoot,
            int start, int index, int security) {
        if (seed == null || seed.length % 243 != 0) {
            throw new IllegalArgumentException("seed");
        }

        if (sideKey == null || sideKey.length != 243) {
            throw new IllegalArgumentException("sideKey");
        }

        if (root == null || root.length != 243) {
            throw new IllegalArgumentException("getRoot");
        }

        if (nextRoot == null || nextRoot.length != 243) {
            throw new IllegalArgumentException("nextRoot");
        }

        if (message == null || siblings == null) {
            throw new IllegalArgumentException("message or siblings");
        }

        if (!(start >= 0 && index >= 0 && security > 0 && security <= 3)) {
            throw new IllegalArgumentException("start, index, security");
        }

        return nativeEncode(seed, message, sideKey, root, siblings, nextRoot, start, index, security);
    }

    public static native int[] nativeEncode(
            int[] seed,
            int[] message,
            int[] sideKey,
            int[] root,
            int[] siblings,
            int[] nextRoot,
            int start, int index, int security
    );

    /**
     * Decodes a masked authenticated message
     *
     * @param encodedMessage
     * @param sideKey trit-encoded side key
     * @param root trit-encoded current Merkle tree getRoot
     * @return result containing decoded payload & next Merkle tree getRoot
     * @throws Exception
     */
    public static DecodeResult decode(
            int[] encodedMessage,
            int[] sideKey,
            int[] root) throws Exception {
        if (encodedMessage == null) {
            throw new IllegalArgumentException("encodedMessage");
        }
        if (sideKey == null || sideKey.length != 243) {
            throw new IllegalArgumentException("sideKey");
        }
        if (root == null || root.length != 243) {
            throw new IllegalArgumentException("getRoot");
        }

        return nativeDecode(encodedMessage, sideKey, root);
    }

    public static native DecodeResult nativeDecode(
            int[] encodedMessage,
            int[] sideKey,
            int[] root
    ) throws Exception;
}
