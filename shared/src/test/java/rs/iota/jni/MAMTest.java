package rs.iota.jni;

import jota.utils.Converter;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andreas C. Osowski
 */

public class MAMTest {

    @Test
    public void testMAM() {
        IOTA.loadLibrary();

        final String SEED = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9ABCDEFGHIJKLMNOPQRSTUVWXYZ9ABCDEFGHIJKLMNOPQRSTUVWXYZ9";
        final String MESSAGE = "HOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOIHOI";
        final String SIDE_KEY = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        int START = 1, COUNT = 4, NEXT_START = START + COUNT, NEXT_COUNT = 2, INDEX = 1;
        int SECURITY = 2;

        int[] seedTrits = Converter.trits(SEED);
        int[] messageTrits = Converter.trits(MESSAGE);
        int[] sidekeyTrits = Converter.trits(SIDE_KEY);

        MerkleTree currentTree = new MerkleTree(seedTrits, START, COUNT, SECURITY);
        MerkleTree nextTree = new MerkleTree(seedTrits, NEXT_START, NEXT_COUNT, SECURITY);

        MerkleBranch indexBranch = currentTree.getBranch(INDEX);
        int[] siblings = indexBranch.getSiblings();

        int[] currentRoot = currentTree.getRoot();
        int[] nextRoot = nextTree.getRoot();

        int[] maskedPayload = MAM.encode(seedTrits, messageTrits, sidekeyTrits, currentRoot, siblings, nextRoot, START, INDEX, SECURITY);

        try {
            MAM.DecodeResult result = MAM.decode(maskedPayload, sidekeyTrits, currentRoot);
            Assert.assertArrayEquals(result.getMessage(), messageTrits);
            Assert.assertArrayEquals(result.getNextRoot(), nextRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentTree.delete();
        nextTree.delete();
        indexBranch.delete();
    }
}
