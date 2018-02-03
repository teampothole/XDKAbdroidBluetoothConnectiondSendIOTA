package rs.iota;

import jota.pow.JCurl;
import jota.pow.SpongeFactory;
import rs.iota.jni.MAM;

import java.io.Serializable;

/**
 * A high-level helper class for consuming a MAM stream.
 */
public class MAMConsumer {
    private final MAMConsumerState state;

    public MAMConsumer(MAMMode mode, int[] root, int[] sideKey) {
        this.state = new MAMConsumerState(mode, root, sideKey);
    }

    public MAMConsumer(MAMMode mode, int[] root) {
        this(mode, root, MAMPublisher.EMPTY_SIDE_KEY);
    }

    public MAMConsumer(MAMConsumerState state) {
        this.state = state;
    }

    public int[] getAddress() {
        return getAddressFromRoot(state.root.clone());
    }

    public int[] getNextAddress() {
        return getAddressFromRoot(state.nextRoot.clone());
    }

    protected int[] getAddressFromRoot(int[] root) {
        if (state.mode != MAMMode.PUBLIC) {
            JCurl curl = new JCurl(SpongeFactory.Mode.CURLP27);
            curl.absorb(root);
            curl.absorb(state.sideKey);
            curl.squeeze(root, 0, JCurl.HASH_LENGTH);

        }
        return root;
    }

    public void setSideKey(int[] sideKey) {
        state.sideKey = sideKey.clone();
    }

    /**
     * Unmasks a provided payload using this consumer's state
     * @param payload
     * @return unmasked message
     * @throws Exception
     */
    public int[] unmask(int[] payload) throws Exception {
        MAM.DecodeResult result = MAM.decode(payload, state.sideKey, state.root);

        state.nextRoot = result.getNextRoot();

        return result.getMessage();
    }

    /**
     * This moves the consumer to the next root.
     * Should be called when no new messages are received at the current
     */
    public void moveToNextRoot() {
        state.root = state.nextRoot;
    }


    public final class MAMConsumerState implements Serializable {
        /**
         * Current MAM stream mode
         */
        private MAMMode mode;


        /**
         * Current channel root
         */
        private int[] root;

        /**
         * Next channel root
         */
        private int[] nextRoot;

        /**
         * Current side key
         */
        private int[] sideKey;


        protected MAMConsumerState() {
        }

        protected MAMConsumerState(MAMMode mode, int[] root) {
            this(mode, root, MAMPublisher.EMPTY_SIDE_KEY);
        }

        protected MAMConsumerState(MAMMode mode, int[] root, int[] sideKey) {
            this.mode = mode;
            this.root = root.clone();
            this.nextRoot = root.clone();
            this.sideKey = sideKey.clone();
        }

    }
}
