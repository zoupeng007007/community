package com.zoupeng.community.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词过滤器
 */
@Component
@Slf4j
public class SensitiveFilter {//服务启动就初始化

    private static final String REPLACEMENT = "***";

    //根节点
    TrieNode root = new TrieNode();

    @PostConstruct//表示一个初始化方法，启动后就进行前缀树的构建
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive.txt");//从编译的classes文件中读取文件
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {//读一行
                //添加到前缀树
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            log.error("加载敏感词文件失败" + e.getMessage());
        }

    }

    /**
     * 将一个敏感词添加到前缀树当中
     *
     * @param keyword
     */
    private void addKeyWord(String keyword) {
        TrieNode temp = root;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = temp.getSubNode(c);
            if (subNode == null) {
                //初始化子节点
                subNode = new TrieNode();
                temp.addSubNode(c, subNode);
            }
            //指针指向子节点，进入下一轮
            temp = subNode;
        }
        //标记最后一个字符
        temp.setKeywordEnd(true);
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        //指针1指向树
        TrieNode temp = root;
        //指针2 ，3
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();//用于存储过滤后的字符串

        while (position < text.length()) {
            char c = text.charAt(position);
            //跳过符号
            if (isSymbol(c)) {
                //如果指针1处于根节点，将此符号记录结果，让指针2向下走一步
                if (temp == root) {
                    begin++;
                }
                sb.append(c);
                //指针3一定会想后移动
                position++;
                continue;
            }
            //不是符号，检查下级节点
            temp = temp.getSubNode(c);
            if (temp == null) {
                //以begin开头的字符串开头的不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置
                position = ++begin;
                //指针1重新指向根节点
                temp = root;
            }else if (temp.isKeywordEnd()){
                //发现敏感词，将begin-position替换
                sb.append(REPLACEMENT);
                //2 3指针 移到3指针后一个位置
                begin = ++position;
                //指针1重新指向根节点
                temp = root;
            }else{
                //检查下一个字符
                position ++;
            }
        }
        //将最后一批字符加入结果
        sb.append(text.substring(begin));
        return sb.toString();
    }

    /**
     * 判断是否为符号
     *
     * @param c
     * @return
     */
    private boolean isSymbol(Character c) {
        //如果是特殊符号false  ,0x2E80->0x9FFF表示东亚文字范围，非特殊符号
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    //前缀树
    private class TrieNode {//前缀树节点
        //关键词结束的标识
        private boolean isKeywordEnd = false;

        //子节点(key是下级节点的字符，value是下级节点)
        private Map<Character, TrieNode> subNOdes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNOdes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c) {
            return subNOdes.get(c);
        }
    }
}
