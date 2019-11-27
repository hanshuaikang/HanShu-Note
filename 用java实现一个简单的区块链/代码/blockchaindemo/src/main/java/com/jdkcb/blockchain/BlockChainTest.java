package com.jdkcb.blockchain;

import com.jdkcb.blockchain.enity.Block;

/**
 * Created on 2018/3/10 0010.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
public class BlockChainTest {

    public static void main(String[] args) {
        //第1个区块
        Block firstBlock = new Block("我是第1个区块", "0");
        firstBlock.mineBlock(5);
        System.out.println("第1个区块hash: " + firstBlock.hash);

        //第2个区块
        Block secondBlock = new Block("我是第2个区块", firstBlock.hash);
        secondBlock.mineBlock(5);
        System.out.println("第2个区块hash: " + secondBlock.hash);

        //第3个区块
        Block thirdBlock = new Block("我是第3个区块", secondBlock.hash);
        thirdBlock.mineBlock(5);
        System.out.println("第3个区块hash: " + thirdBlock.hash);
    }
}

