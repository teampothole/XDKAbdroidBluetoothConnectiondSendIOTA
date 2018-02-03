package rs.iota;

import jota.pow.JCurl;
import jota.pow.SpongeFactory;
import rs.iota.jni.MAM;
import rs.iota.jni.MerkleBranch;
import rs.iota.jni.MerkleTree;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A high-level helper class for a publishing MAM stream.
 * The state can be serialized for restoring from a persisted instance.
 */
public class MAMPublisher {

    public final static int[] EMPTY_SIDE_KEY = new int[243];

    static {
        Arrays.fill(EMPTY_SIDE_KEY, 0);
    }

    private final MAMPublisherState state;
    private Options options = null;
    private MerkleTree currentTree = null, nextTree = null;


    public MAMPublisher(Options options) {
        this.options = options;

        state = new MAMPublisherState(options,
                new Options(options).setStartIndex(options.getStartIndex() + options.getTreeSize()));

        createTrees();
    }

    public MAMPublisher(MAMPublisherState state) {
        this.state = state;

        createTrees();
    }


    public void delete() {
        if (currentTree != null) currentTree.delete();
        if (nextTree != null) nextTree.delete();
    }

    public int[] mask(int[] message) {
        return mask(message, EMPTY_SIDE_KEY);
    }

    /**
     * Masks a trit-encoded message using MAM.
     *
     * @param message
     * @param sideKey
     * @return masked message
     */
    public int[] mask(int[] message, int[] sideKey) {
        MerkleBranch branch = currentTree.getBranch(state.currentTreeIndex);

        int[] encoded = MAM.encode(state.currentTreeOptions.seed,
                message, sideKey, currentTree.getRoot(), branch.getSiblings(), nextTree.getRoot(),
                state.currentTreeOptions.startIndex, state.currentTreeIndex, state.currentTreeOptions.security
        );

        branch.delete();

        state.currentTreeIndex++;

        if (state.currentTreeIndex == state.currentTreeOptions.treeSize) {
            migrateTree();
        }

        return encoded;
    }

    /**
     * @param mode PUBLIC or PRIVATE
     * @return Current MAM stream address
     */
    public int[] getAddress(MAMMode mode) {
        if (mode == MAMMode.RESTRICTED) {
            throw new RuntimeException("Restricted MAM mode must use a side key.");
        }

        return getAddress(mode, EMPTY_SIDE_KEY);
    }

    /**
     * Determines the address that messages should currently be published to.
     * There are three different modes available:
     * <p>
     * - PUBLIC: address = current tree root
     * - PRIVATE: address = H(current tree root)
     * - RESTRICTED: address = H(current tree root + side key)
     *
     * @param mode
     * @param sideKey
     * @return
     */
    public int[] getAddress(MAMMode mode, int[] sideKey) {
        int[] root = currentTree.getRoot();
        if (mode != MAMMode.PUBLIC) {
            JCurl curl = new JCurl(SpongeFactory.Mode.CURLP27);
            curl.absorb(root);
            curl.absorb(sideKey);
            curl.squeeze(root, 0, JCurl.HASH_LENGTH);

            return root;
        }
        return root;
    }

    /**
     * The current tree's root is part of the key to decoding a MAM stream.
     * This should be shared with caution.
     *
     * @return the current tree's root
     */
    public int[] getRoot() {
        return currentTree.getRoot();
    }

    /**
     * @return a copy of the current tree's options
     */
    public Options getCurrentTreeOptions() {
        return new Options(state.currentTreeOptions);
    }

    /**
     * @return a copy of the next tree's options
     */
    public Options getNextTreeOptions() {
        return new Options(state.nextTreeOptions);
    }

    /**
     * This will fork the MAM stream and the next Merkle tree will be recalculated based on the provided options.
     *
     * @param options Options for the new MerkleTree
     */
    public void setNextTreeOptions(Options options) {
        state.nextTreeOptions = new Options(options);
        createNextTree();
    }

    /**
     * @return number of messages remaining before the publisher has to switch to the next tree
     */
    public int remainingMessagesForCurrentTree() {
        return state.currentTreeOptions.treeSize - state.currentTreeIndex;
    }

    /**
     * This will set the currently next tree to be the current tree and calculate a new next tree.
     */
    public void migrateTree() {
        if (currentTree != null) currentTree.delete();

        currentTree = nextTree;
        state.currentTreeOptions = state.nextTreeOptions;
        state.nextTreeOptions = new Options(state.currentTreeOptions);
        state.currentTreeIndex = 0;
        nextTree = null;

        createNextTree();
    }

    private void createCurrentTree() {
        if (currentTree != null)
            currentTree.delete();
        currentTree = state.currentTreeOptions.createTree();
    }

    private void createNextTree() {
        if (nextTree != null)
            nextTree.delete();
        nextTree = state.nextTreeOptions.createTree();
    }

    /**
     * Creates the JNI MerkleTree instances
     */
    private void createTrees() {
        createCurrentTree();
        createNextTree();
    }

    /**
     * @return serialisable state for this MAMPublisher
     */
    public MAMPublisherState getState() {
        return state;
    }

    /**
     * These options are used for the creation of each new Merkle tree.
     */
    public final static class Options implements Serializable {
        private int treeSize = 4;
        private int security = 2;
        private int startIndex = 0;
        private int[] seed = null;

        private Options() {

        }

        private Options(Options other) {
            this.treeSize = other.treeSize;
            this.security = other.security;
            this.startIndex = other.startIndex;
            this.seed = other.seed.clone();
        }

        public int getTreeSize() {
            return treeSize;
        }

        private Options setTreeSize(int treeSize) {
            this.treeSize = treeSize;
            return this;
        }

        public int getSecurity() {
            return security;
        }

        private Options setSecurity(int security) {
            this.security = security;
            return this;
        }

        public int getStartIndex() {
            return startIndex;
        }

        private Options setStartIndex(int startIndex) {
            this.startIndex = startIndex;
            return this;
        }

        public MerkleTree createTree() {
            return new MerkleTree(seed, startIndex, treeSize, security);
        }

        private Options setSeed(int[] seed) {
            this.seed = seed;
            return this;
        }

        public final static class Builder {
            private Options opt = new Options();

            public Options build() {
                if (opt.seed == null)
                    throw new RuntimeException("You must set a seed.");
                return opt;
            }

            public Builder setSeed(int[] seed) {
                opt.setSeed(seed);
                return this;
            }

            public Builder setTreeSize(int treeSize) {
                opt.setTreeSize(treeSize);
                return this;
            }

            public Builder setSecurity(int security) {
                opt.setSecurity(security);
                return this;
            }

            public Builder setStartIndex(int startIndex) {
                opt.setStartIndex(startIndex);
                return this;
            }

        }
    }

    /**
     * Serialisable state of this MAM publisher
     */
    public final class MAMPublisherState implements Serializable {
        /**
         * Describes the current Merkle tree's parameters.
         */
        private Options currentTreeOptions;
        /**
         * Holds the last used tree leaf index of the current tree.
         */
        private int currentTreeIndex = 0;
        /**
         * Describes the next Merkle tree's parameters
         */
        private Options nextTreeOptions;


        protected MAMPublisherState() {
        }

        protected MAMPublisherState(Options currentOptions, Options nextOptions) {
            this.currentTreeOptions = new Options(currentOptions);
            this.nextTreeOptions = new Options(nextOptions);
        }
    }


}
