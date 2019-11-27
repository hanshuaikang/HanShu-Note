package com.jdkcb.blockchain;

import com.jdkcb.blockchain.enity.Block;
import com.jdkcb.blockchain.utils.JsonUtil;

import java.util.ArrayList;

/**
 * Created on 2018/3/10 0010.
 *
 * @author zlf
 * @email i@merryyou.cn
 * @since 1.0
 */
public class BlockChainListTest {

    //这玩意就是我们的区块链，存储我们所有的区块信息。（简陋版）
    public static ArrayList<Block> blockChain = new ArrayList();

    //挖矿的难度，就是计算出来的hash前几个字符是0才是合法的。
    public static int difficulty = 5;

    public static void main(String[] args) {
        blockChain.add(new Block("我是第1个区块", "0"));
        blockChain.get(0).mineBlock(difficulty);

        blockChain.add(new Block("我是第2个区块", blockChain.get(blockChain.size() - 1).hash));
        blockChain.get(1).mineBlock(difficulty);

        blockChain.add(new Block("我是第3个区块", blockChain.get(blockChain.size() - 1).hash));
        blockChain.get(2).mineBlock(difficulty);

        System.out.println("区块链是否合法: " + isChainValid());
        System.out.println(JsonUtil.toJson(blockChain));
    }


    public static Boolean isChainValid(){

        Block currentBlock;
        Block previousBlock;
        boolean flag = true;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');

        //循环遍历列表检验hash
        for(int i=1;i<blockChain.size();i++){
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            //比较注册的hash和计算的hash
            if(!currentBlock.hash.equals(currentBlock.calculateHash())){
                System.out.println("当前hash不相等");
                flag=false;
            }
            //比较当前的前一个hash与注册的前一个hash
            if(!previousBlock.hash.equals(currentBlock.previousHash)){
                System.out.println("前一个hash不相等");
                flag=false;
            }

            //检查该区块是不是已经被算出来过了。
            if(!currentBlock.hash.substring( 0, difficulty).equals(hashTarget)) {
                System.out.println("这个区块还没有被开采，也就是你这个区块他不是合格的");
                flag=false;
            }
        }

        return flag;
    }
}