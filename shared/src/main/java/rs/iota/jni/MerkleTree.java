package rs.iota.jni;

public class MerkleTree {
    private long INSTANCE_PTR;

    private long cachedSize = -1, cachedDepth = -1;
    private final int count;
    private int[] cachedSlice = null;

    /**
     * Creates a new MerkleTree.
     * The user has to make sure that each leaf (identified by key index on seed = startIndex + leafIndex) is only used once
     *
     * @param seed       trit-encoded seed
     * @param startIndex start index for keys on this seed
     * @param count      leaf count for this MerkleTree
     * @param security   key security level
     */
    public MerkleTree(int[] seed, int startIndex, int count, int security) {
        if (seed == null || seed.length % 243 != 0) {
            throw new IllegalArgumentException("Invalid seed parameter provided.");
        }
        this.INSTANCE_PTR = nativeCreate(seed, startIndex, count, security);
        this.count = count;
    }

    /**
     * Cleans up this instance.
     * You should call this if you no longer need this MerkleTree object.
     */
    public void delete() {
        if (INSTANCE_PTR != -1) {
            nativeDrop(INSTANCE_PTR);
            INSTANCE_PTR = -1;
        }
    }

    /**
     * Creates a getBranch for this MerkleTree at the given leaf index.
     *
     * @param index leaf index
     * @return
     */
    public MerkleBranch getBranch(int index) {
        if (INSTANCE_PTR == -1) {
            throw new RuntimeException("Method was called after object deletion.");
        }
        return new MerkleBranch(nativeBranch(INSTANCE_PTR, index), index);
    }


    /**
     * @return trit-encoded getRoot of this MerkleTree
     */
    public int[] getRoot() {
        return slice();
    }

    /**
     * @return trit-encoded getRoot of this MerkleTree
     */
    public int[] slice() {
        if (cachedSlice == null) {
            if (INSTANCE_PTR == -1) {
                throw new RuntimeException("Method was called after object deletion.");
            }
            cachedSlice = nativeSlice(INSTANCE_PTR);
        }

        return cachedSlice.clone();
    }

    /**
     * @return leaf getCount
     */
    public int getCount() {
        return this.count;
    }

    public long getSize() {
        if (cachedSize == -1) {
            if (INSTANCE_PTR == -1) {
                throw new RuntimeException("Method was called after object deletion.");
            }
            cachedSize = nativeSize(INSTANCE_PTR);
        }

        return cachedSize;
    }

    /**
     * The depth of a merkle tree is ceil(log2(count))
     *
     * @return depth of this merkle tree.
     */
    public long getDepth() {
        if (cachedDepth == -1) {
            if (INSTANCE_PTR == -1) {
                throw new RuntimeException("Method was called after object deletion.");
            }
            cachedDepth = nativeDepth(INSTANCE_PTR);
        }

        return cachedDepth;
    }

    @Override
    protected void finalize() throws Throwable {
        delete();

        super.finalize();
    }

    private static native long nativeCreate(int[] seed, int index, int count, int security);

    private static native long nativeDepth(long ptr);

    private static native long nativeSize(long ptr);

    private static native void nativeDrop(long ptr);

    private static native int[] nativeSlice(long ptr);

    private static native long nativeBranch(long ptr, int index);
}
