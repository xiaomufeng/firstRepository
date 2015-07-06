import java.util.Date;

/**
 * @author Wu
 * 
 */
public class TinySearchEngine {
    
    public static String term2docsFilePath="data/term2docs.index";
    public static String doc2termsFilePath="data/doc2terms.index";
    public static String term2wordFilePath="data/term2word.index";
    
    //only for index builder
    public static String docsFilePath="data/titles.seg";
    
    //only for query
    public static String queryFilePath="data/query_pv.seg";
    public static String resultFilePath="data/search_result";
    
    public static Long GetHash(String s) {
        long h=0; 
        int i=s.length();
        while(i>0) h=107*h+s.charAt(--i);
        return h;
    }
    
    public static void main(String[] args) {
        System.err.println(GetHash("¼ÐÃÞ"));//2893116
    }
}
