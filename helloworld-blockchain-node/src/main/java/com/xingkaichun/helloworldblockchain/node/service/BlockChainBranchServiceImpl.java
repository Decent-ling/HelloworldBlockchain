package com.xingkaichun.helloworldblockchain.node.service;

import com.xingkaichun.helloworldblockchain.core.model.Block;
import com.xingkaichun.helloworldblockchain.core.utils.BigIntegerUtil;
import com.xingkaichun.helloworldblockchain.node.dao.BlockChainBranchDao;
import com.xingkaichun.helloworldblockchain.node.dto.blockchainbranch.BlockchainBranchBlockDto;
import com.xingkaichun.helloworldblockchain.node.model.BlockchainBranchBlockEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

/**
 *
 * @author 邢开春 xingkaichun@qq.com
 */
@Service
public class BlockChainBranchServiceImpl implements BlockChainBranchService {

    @Autowired
    private BlockChainBranchDao blockChainBranchDao;
    @Autowired
    private BlockChainCoreService blockChainCoreService;


    private Map<String,String> blockHeightBlockHashMap = new HashMap<>();
    private boolean isRefreshCache = false;

    /**
     * 将分支加载到内存
     */
    private void refreshCache(){
        List<BlockchainBranchBlockEntity> blockchainBranchBlockEntityList = blockChainBranchDao.queryAllBlockchainBranchBlock();
        if(blockchainBranchBlockEntityList != null){
            for(BlockchainBranchBlockEntity entity:blockchainBranchBlockEntityList){
                blockHeightBlockHashMap.put(String.valueOf(entity.getBlockHeight()),entity.getBlockHash());
            }
        }
        isRefreshCache = true;
    }

    @Override
    public boolean isFork(BigInteger blockHeight,String blockHash){
        String stringBlockHeight = String.valueOf(blockHeight);
        String blockHashTemp = blockHeightBlockHashMap.get(stringBlockHeight);
        if(blockHashTemp == null){
            return false;
        }
        return !blockHashTemp.equals(blockHash);
    }

    @Override
    public BigInteger getFixBlockHashMaxBlockHeight(BigInteger blockHeight){
        BigInteger nearBlockHeight = BigInteger.ZERO;
        Set<String> set = blockHeightBlockHashMap.keySet();
        for(String stringBlockHeight:set){
            BigInteger intBlockHeight = new BigInteger(stringBlockHeight);
            if(BigIntegerUtil.isLessThan(intBlockHeight,blockHeight)  && BigIntegerUtil.isGreateThan(intBlockHeight,nearBlockHeight)){
                nearBlockHeight = intBlockHeight;
            }
        }
        return nearBlockHeight;
    }

    @Override
    public boolean isBlockchainConfirmABranch() {
        List<BlockchainBranchBlockEntity> blockchainBranchBlockEntityList = blockChainBranchDao.queryAllBlockchainBranchBlock();
        if(blockchainBranchBlockEntityList == null || blockchainBranchBlockEntityList.size()==0){
            return false;
        }
        return true;
    }

    @Override
    public void branchchainBranchHandler() throws Exception {
        if(!isRefreshCache){
            refreshCache();
        }
        List<BlockchainBranchBlockEntity> blockchainBranchBlockEntityList = blockChainBranchDao.queryAllBlockchainBranchBlock();
        if(blockchainBranchBlockEntityList == null || blockchainBranchBlockEntityList.size()==0){
            return;
        }
        blockchainBranchBlockEntityList.sort(Comparator.comparing(BlockchainBranchBlockEntity::getBlockHeight));
        for(int i=0;i<blockchainBranchBlockEntityList.size();i++){
            BlockchainBranchBlockEntity entity = blockchainBranchBlockEntityList.get(i);
            Block block = blockChainCoreService.queryNoTransactionBlockDtoByBlockHeight(entity.getBlockHeight());
            if(block == null){
                return;
            }
            if(entity.getBlockHash().equals(block.getHash()) && BigIntegerUtil.isEquals(entity.getBlockHeight(),block.getHeight())){
                continue;
            }
            BigInteger deleteBlockHeight;
            if(i==0){
                deleteBlockHeight = BigInteger.ONE;
            }else {
                deleteBlockHeight = blockchainBranchBlockEntityList.get(i-1).getBlockHeight().add(BigInteger.ONE);
            }
            blockChainCoreService.removeBlocksUtilBlockHeightLessThan(deleteBlockHeight);
            return;
        }
    }

    @Override
    public List<BlockchainBranchBlockDto> queryBlockchainBranch() {
        List<BlockchainBranchBlockEntity> blockchainBranchBlockEntityList = blockChainBranchDao.queryAllBlockchainBranchBlock();
        if(blockchainBranchBlockEntityList == null || blockchainBranchBlockEntityList.size()==0){
            return null;
        }
        return classCast(blockchainBranchBlockEntityList);
    }

    @Transactional
    @Override
    public void updateBranchchainBranch(List<BlockchainBranchBlockDto> blockList) throws Exception {
        blockChainBranchDao.removeAll();
        for(BlockchainBranchBlockDto blockchainBranchBlockDto:blockList){
            BlockchainBranchBlockEntity entity = classCast(blockchainBranchBlockDto);
            blockChainBranchDao.add(entity);
        }
        refreshCache();
        branchchainBranchHandler();
    }

    private List<BlockchainBranchBlockDto> classCast(List<BlockchainBranchBlockEntity> blockchainBranchBlockEntityList) {
        if(blockchainBranchBlockEntityList == null || blockchainBranchBlockEntityList.size()==0){
            return null;
        }
        List<BlockchainBranchBlockDto> dtoList = new ArrayList<>();
        for(BlockchainBranchBlockEntity entity:blockchainBranchBlockEntityList){
            dtoList.add(classCast(entity));
        }
        return dtoList;
    }

    private BlockchainBranchBlockDto classCast(BlockchainBranchBlockEntity entity) {
        BlockchainBranchBlockDto dto = new BlockchainBranchBlockDto();
        dto.setBlockHash(entity.getBlockHash());
        dto.setBlockHeight(entity.getBlockHeight());
        return dto;
    }

    private BlockchainBranchBlockEntity classCast(BlockchainBranchBlockDto dto) {
        BlockchainBranchBlockEntity entity = new BlockchainBranchBlockEntity();
        entity.setBlockHash(dto.getBlockHash());
        entity.setBlockHeight(dto.getBlockHeight());
        return entity;
    }
}
