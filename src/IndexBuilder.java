/**
 * 构建索引文件
 * 优化方案：
 * 1.把空格的替换，"<space>"换成"<>"，已经查阅源文件，没有这个符号出现；equals比较比较省时
 * 2.空格情况下的词汇表存放优化，可以再开始就放入空格的hash值，之后就不用每次比较放入
 * 3.异常打印语句删除，其实不算优化，但是也会影响程序时间
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;

/**
 * @author Wu
 */

public class IndexBuilder extends TinySearchEngine {

    /**
     * @param args
     */

    public static void main(String[] args) {
        // 倒排链: term -> doc1, doc2, doc3, ...
        HashMap<Long, LinkedList<Long>> term2docs = new HashMap<Long, LinkedList<Long>>();

        // 正排链: doc -> term1, term2, term3, ...
        HashMap<Long, LinkedList<Long>> doc2terms = new HashMap<Long, LinkedList<Long>>();

        // 词汇表: term -> word
        HashMap<Long, String> term2word = new HashMap<Long, String>();

        // 词汇表：word -> term，为了存放映射词汇表，节省时间
        HashMap<String, Long> word2term = new HashMap<String, Long>();

        Long spaceTerm = GetHash(" ");

        term2word.put(spaceTerm, " ");// 提前放置，节省时间

        BufferedReader reader = null;
        try {
            InputStream is = new FileInputStream(docsFilePath);
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");

            reader = new BufferedReader(isr);

            String line = null;

            while ((line = reader.readLine()) != null) {

                String[] arr = line.split("\t");
                if (arr.length == 2) {
                    Long doc = (Long) Long.parseLong(arr[0]);
                    String title = arr[1];

                    ArrayList<String> words = new ArrayList<String>();
                    if (title.charAt(0) == ' ') {
                        words.add("<space>");
                        title = title.substring(1);
                    }
                    for (String word : title.replace("  ", " <space>").split(" ")) {
                        if (!word.isEmpty()) words.add(word);
                    }

                    // title|word 转换 term
                    LinkedList<Long> terms = new LinkedList<Long>();
                    for (int i = 0; i < words.size(); i++) {
                        String word = words.get(i);

                        if (word.equals("<space>")) {// 这里可以优化，只要是空格的，就不需要一直重复放词汇表
                            terms.add(spaceTerm);// 转化成空格的hash值
                            continue;
                        }

                        Long term = 0L;// 这里可以优化，计算过的，就不用再重新计算
                        if (word2term.containsKey(word)) {
                            term = word2term.get(word);
                        } else {
                            term = GetHash(word);
                            word2term.put(word, term);
                        }

                        terms.add(term);
                        term2word.put(term, word);// 词汇表

                        if (!term2docs.containsKey(term)) {
                            term2docs.put(term, new LinkedList<Long>());
                        }

                        LinkedList<Long> docs = term2docs.get(term);
                        docs.add(doc);// 单条term对应的docs
                    }

                    doc2terms.put(doc, terms);

                } else {
                    // System.err.println("Bad Format: " + line);
                }

            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }

        //System.out.println("term2docs size:" + term2docs.size());

        try {
            FileOutputStream fos = new FileOutputStream(term2docsFilePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            Iterator<Entry<Long, LinkedList<Long>>> it = term2docs.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Long, LinkedList<Long>> temp = it.next();

                StringBuilder sb = new StringBuilder(temp.getKey().toString() + "\t");
                for (Long l : temp.getValue()) {
                    sb.append(l + " ");
                }
                sb.append("\n");
                bufferedWriter.write(sb.toString());
            }

            // 倒排文件###########################
            bufferedWriter.close();
            osw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //System.out.println("doc2terms size:" + doc2terms.size());

        try {
            FileOutputStream fos = new FileOutputStream(doc2termsFilePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            Iterator<Entry<Long, LinkedList<Long>>> it = doc2terms.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Long, LinkedList<Long>> temp = it.next();
                StringBuilder sb = new StringBuilder(temp.getKey().toString() + "\t");
                for (Long l : temp.getValue()) {
                    sb.append(l + " ");
                }
                sb.append("\n");
                bufferedWriter.write(sb.toString());
            }

            bufferedWriter.close();
            osw.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //System.out.println("term2word size:" + term2word.size());
        try {

            FileOutputStream fos = new FileOutputStream(term2wordFilePath);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(osw);

            Iterator<Entry<Long, String>> it = term2word.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Long, String> temp = it.next();
                StringBuilder sb = new StringBuilder(temp.getKey().toString() + "\t" + temp.getValue() + "\n");
                bufferedWriter.write(sb.toString());
            }

            // 倒排文件###########################
            bufferedWriter.close();
            osw.close();
            fos.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Build Index Done!");

    }
}
