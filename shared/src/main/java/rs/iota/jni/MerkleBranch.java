package rs.iota.jni;

public class MerkleBranch {
    private long INSTANCE_PTR = -1;

    private int index;
    private long cachedLength = -1;
    private int[] cachedSiblings = null;

    /**
     * Instances Private constructor.
     *
     * @param ptr address of merkle::MerkleBranch instance
     * @param index leaf index for this getBranch
     */
    MerkleBranch(long ptr, int index) {
        this.INSTANCE_PTR = ptr;
        this.index = index;
    }

    /**
     *
     * @return leaf index of this Merkle getBranch
     */
    public int getIndex() {
        return index;
    }

    /**
     *
     * @return total siblings getCount
     */
    public long getLength() {
        if (cachedLength == -1) {
            if (INSTANCE_PTR == -1) {
                throw new RuntimeException("Method was called after object deletion.");
            }
            cachedLength = nativeLength(INSTANCE_PTR);
        }

        return cachedLength;
    }

    public int[] getSiblings() {
        if (cachedSiblings == null) {
            if (INSTANCE_PTR == -1) {
                throw new RuntimeException("Method was called after object deletion.");
            }
            cachedSiblings = nativeSiblings(INSTANCE_PTR);
        }

        return cachedSiblings.clone();
    }

    public void delete() {
        if (INSTANCE_PTR != -1) {
            nativeDrop(INSTANCE_PTR);
            INSTANCE_PTR = -1;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        delete();
        super.finalize();
    }

    private native static void nativeDrop(long ptr);

    private native static long nativeLength(long ptr);

    private native static int[] nativeSiblings(long ptr);
}
