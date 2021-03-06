package com.xingkaichun.helloworldblockchain.core.utils;

import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.model.transaction.Transaction;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionOutput;
import com.xingkaichun.helloworldblockchain.core.model.transaction.TransactionType;
import com.xingkaichun.helloworldblockchain.crypto.HexUtil;
import com.xingkaichun.helloworldblockchain.crypto.SHA256Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 区块工具类
 *
 * @author 邢开春 xingkaichun@qq.com
 */
public class BlockUtil {

    private final static Logger logger = LoggerFactory.getLogger(BlockUtil.class);

    /**
     * 计算区块的Hash值
     * @param block 区块
     */
    public static String calculateBlockHash(Block block) {
        String input = block.getTimestamp()+block.getPreviousHash()+block.getHeight()+block.getMerkleRoot()+block.getConsensusValue();
        byte[] sha256Digest = SHA256Util.applySha256(input.getBytes());
        return HexUtil.bytesToHexString(sha256Digest) + block.getTimestamp();
    }

    /**
     * 区块中写入的默克尔树根是否正确
     */
    public static boolean isBlockWriteHashRight(Block block){
        String targetHash = calculateBlockHash(block);
        return targetHash.equals(block.getHash());
    }

    //region 默克尔树根
    /**
     * 计算区块的默克尔树根值
     * @param block 区块
     */
    public static String calculateBlockMerkleRoot(Block block) {
        List<Transaction> transactionList = block.getTransactions();
        return MerkleUtil.calculateTransactionMerkleRoot(transactionList);
    }
    /**
     * 区块中写入的默克尔树根是否正确
     */
    public static boolean isBlockWriteMerkleRootRight(Block block){
        String targetMerkleRoot = calculateBlockMerkleRoot(block);
        return targetMerkleRoot.equals(block.getMerkleRoot());
    }

    /**
     * 校验交易的属性是否与计算得来的一致
     */
    public static boolean isBlockTransactionWriteRight(@Nonnull Block block) {
        List<Transaction> transactions = block.getTransactions();
        if(transactions == null || transactions.size()==0){
            return true;
        }
        for(Transaction transaction:transactions){
            if(!isTransactionWriteRight(block, transaction)){
                return false;
            }
        }
        return true;
    }

    /**
     * 校验交易的属性是否与计算得来的一致
     */
    public static boolean isTransactionWriteRight(Block block, @Nonnull Transaction transaction) {
        //校验挖矿交易的时间戳
        if(block != null){
            if(transaction.getTransactionType() == TransactionType.MINER_AWARD){
                if(block.getTimestamp() != transaction.getTimestamp()){
                    return false;
                }
            }
        }
        if(!BlockchainHashUtil.isTransactionHashRight(transaction)){
            return false;
        }
        List<TransactionOutput> outputs = transaction.getOutputs();
        if(outputs == null || outputs.size()==0){
            return true;
        }
        for(TransactionOutput transactionOutput:outputs){
            if(!BlockchainHashUtil.isTransactionOutputHashRight(transaction,transactionOutput)){
                return false;
            }
        }
        return true;
    }
    //endregion
}
