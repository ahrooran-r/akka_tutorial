package learn.tutorial._8_multi_threaded_blockchain_example;

import learn.tutorial._6_blockchain_core.Block;
import learn.tutorial._6_blockchain_core.HashResult;
import learn.tutorial._6_blockchain_core.utils.BlockChainUtils;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlockMiner implements Runnable {

    private Block block;
    private int firstNonce;
    private HashResult hashResult;
    private int difficultyLevel;

    @Override
    public void run() {
        HashResult hashResult = BlockChainUtils.mineBlock(block, difficultyLevel, firstNonce, firstNonce + 1000);
        if (hashResult != null) {
            this.hashResult.foundAHash(hashResult.getHash(), hashResult.getNonce());
        }
    }
}
