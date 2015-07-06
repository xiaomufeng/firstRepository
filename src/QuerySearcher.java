

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

public class QuerySearcher extends TinySearchEngine {
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		
		//term -> doc1, doc2, doc3, ...
		HashMap<Long, LinkedList<Long>> term2docs = new HashMap<Long,LinkedList<Long>>();
		
		//doc -> term1, term2, term3, ...
		HashMap<Long, LinkedList<Long>> doc2terms = new HashMap<Long,LinkedList<Long>>();
		
		//term -> word
		HashMap<Long, String> term2word = new HashMap<Long,String>();
		//word -> term
		HashMap<String, Long> word2term = new HashMap<String, Long>();
		
		//Step1: load index
		try {
            FileInputStream fis = new FileInputStream(term2docsFilePath);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] docToTermsArray = line.split("\t");
                if (docToTermsArray.length == 2) {
                    Long docId = Long.valueOf(docToTermsArray[0]);
                    String termIdsStr = docToTermsArray[1];
                    String[] termIdsArray = termIdsStr.split(" ");
                    LinkedList<Long> list = new LinkedList<Long>();
                    for (String str : termIdsArray) {
                        list.add(Long.valueOf(str));
                    }
                    term2docs.put(docId, list);
                }
            }
            br.close();
            isr.close();
            fis.close();
            System.out.println("term2docs size: "+term2docs.size());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			File f = new File(doc2termsFilePath);
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null) {
                String[] docToTermsArray = line.split("\t");
                if (docToTermsArray.length == 2) {
                    Long docId = Long.valueOf(docToTermsArray[0]);
                    String termIdsStr = docToTermsArray[1];
                    String[] termIdsArray = termIdsStr.split(" ");
                    LinkedList<Long> list = new LinkedList<Long>();
                    for (String str : termIdsArray) {
                        list.add(Long.valueOf(str));
                    }
                    doc2terms.put(docId, list);
                }
            }
            System.out.println("doc2terms size: "+doc2terms.size());
            br.close();
            isr.close();
            fis.close();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			
			File f = new File(term2wordFilePath);
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] docToTermsArray = line.split("\t");
                if (docToTermsArray.length == 2) {
                    Long termId = Long.valueOf(docToTermsArray[0]);
                    String word = docToTermsArray[1];
                    
                    term2word.put(termId, word);
                    
                    word2term.put(word, termId);//���Ӵʵ�hashֵ��ӳ��ʻ����query����û����word2term��ʱ���������ټ���ƥ��
                }
            }
            System.out.println("term2word size: "+term2word.size());
            System.out.println("word2term size: "+word2term.size());
            br.close();
            isr.close();
            fis.close();
            
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Long spaceTerm=GetHash(" ");
		
		PrintStream cout = null;
		try {
			cout = new PrintStream(new BufferedOutputStream(new FileOutputStream(resultFilePath)));
		} catch (FileNotFoundException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
		//Step2: search
		BufferedReader reader = null;
		try {
			File file = new File(queryFilePath);
			InputStream is = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(is,"UTF-8"); 
			
			reader = new BufferedReader(isr);
			int cnt=0;
			String line = null;
			
			while ((line = reader.readLine()) != null) {//for each query
				
				String[] arr=line.split("\t");
				if (arr.length==2) {
					String query=arr[1];
					cout.println("\n" + query);
					
					ArrayList<String> words=new ArrayList<String>();
					if (query.charAt(0)==' ') {
						words.add("<space>");
						query=query.substring(1);
					}
					//wordԤ�����ŵ�list��
					for (String word : query.replace("  ", " <space>").split(" ")) {
						if (! word.isEmpty()) words.add(word);
					}
					
					LinkedList<Long> terms=new LinkedList<Long>(); //��ѯ���� hashֵ list
					
					//Step2.A: Merge docs from all term in a query
					//��ȡquery�ʶ�Ӧ��ÿ���������Ĺ���doc
					LinkedList<Long> docsMerged = null;
					for (int i=0; i<words.size(); i++) {
						String word=words.get(i);
						
						if (word.equals("<space>")) {
							word=" ";
							Long term=spaceTerm;
							terms.add(term);
							//term2word.put(term, word);//���������࣬�ѿո��hash���뻹���ڴʱ��С�����
							continue;
						}
						
						Long term = word2term.get(word);
						if(null == term){//ֱ���жϴʿ��Ƿ���������ʵ�hashֵ�����û�У���ô�Ϳ϶�ƥ�䲻��
						    break;
						}
						terms.add(term);
						//term2word.put(term, word);
						
						LinkedList<Long> doc = term2docs.get(term);//�����ʲ��ҳ���doc����
						
						if (doc!=null) {
							
							if (docsMerged == null) {//first link
								docsMerged = doc;
							} else {//other link����ǰһ��doc���Ͻ��кϲ�
								HashSet<Long> docSet=new HashSet<Long>();
								for (Long term1 : doc) {//��ǰ��Ҫ�ϲ���doc
									for (Long term2 : docsMerged) {//ǰһ��doc����
										if (term1.equals(term2)) {
											docSet.add(term1);
										}
									}
								}
								docsMerged.clear();
								for (Long term3 : docSet) {
									docsMerged.add(term3);
								}
								
								if (docsMerged.size()==0)
									break;
							}
						} else {
							docsMerged = null;
							break;
						}
					}
					
					if (docsMerged==null || docsMerged.size()==0) continue;
					
					//Step2.B: Ranking all merged docs��������query����doc��ʤ�����ĵ÷�
					HashMap<Long, Double> doc2score = new HashMap<Long, Double>();
					
					for (Long doc : docsMerged) {
						//SubModule: Caculate similarity between doc and query
						//basic idea: count same terms in doc and query
						//ע�⣺���������Լ��㷽����������ĿҪ�󣬽�����ʾ�ã����޸�
						double score=0;
						LinkedList<Long> termsInDoc = doc2terms.get(doc);//ȡ��doc��Ӧ��term hash��
						
						score = getScore(terms,termsInDoc,spaceTerm);
						doc2score.put(doc, score);
					}
					//sorted by score
					ArrayList<Entry<Long, Double>> list = new ArrayList<Entry<Long, Double>>(doc2score.entrySet());   
					
					Collections.sort(list, new Comparator<Object>() {
						public int compare(Object e1, Object e2) {
							double score1 = ((Entry<Long, Double>) e1).getValue();
							double score2 = ((Entry<Long, Double>) e2).getValue();
							if (score1==score2) return 0;
							if (score1<score2) return 1;
							return -1;
						}
					});
					
					int cnt2=0;//ֻ��Ҫ���ǰ100�����
					for (Entry<Long, Double> e : list) {
						cnt2++;
						if (cnt2>100) break;
						
						Long doc=e.getKey();
						Double score=e.getValue();
						
						if (score<0) continue;//throw away too bad docs
						String printResult = doc + "\t" + score+"\t";
						//cout.print(doc + "\t" + score+"\t");
						cout.print(printResult);
						
						LinkedList<Long> termsInDoc = doc2terms.get(doc);
						int cnt4=0;
						for (Long term : termsInDoc) {
							cnt4++;
							String word=term2word.get(term);
							if (cnt4<termsInDoc.size()) 
								cout.print(word + " ");
							else
								cout.print(word);
						}
						
						cout.println();
					}
					
				} else {
					System.err.println("Bad Format: "+ line);
				}
			}
			reader.close();
		} catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {}
            }
        }
		
		cout.close();
		System.out.println("Qeury Search Done!");
	}
	
	public static double getScore(LinkedList<Long>queryDocIds,LinkedList<Long>oneDocIds,Long spaceTerm){
	    HashMap<Long,ArrayList<Integer>> postions = new HashMap<Long,ArrayList<Integer>>();
	    
        for (Long queryDocId : queryDocIds) {//for each query term
            if(queryDocId.equals(spaceTerm)){
                continue ;//query�����пո���Ĭ��doc��ƥ��
            }
            ArrayList<Integer> pos = new ArrayList<Integer>();//������doc�г��ֵ�λ�ü���
            for (int i = 0 ;i<oneDocIds.size();i++) {// for each doc term
                if (queryDocId.equals(oneDocIds.get(i))) {//������ƥ��ɹ�������λ��
                    pos.add(i);
                }
            }
            
            postions.put(queryDocId, pos);//һ��ƥ��
        }
        double size = queryDocIds.size();
        double score =size/distance(postions);
	    return score;
	}
	
	//����meger�����У���С����
	public static int distance(HashMap<Long,ArrayList<Integer>> postions){
	    Long queryDocId = 0L;
	    int size = 100;
	    ArrayList<Integer> postionSet = new ArrayList<Integer>();
	    
	    Set<Long> keys = postions.keySet();
	    for(Long key:keys){
	        ArrayList<Integer> temp = postions.get(key);
	        if(size>temp.size()){
	            size = temp.size();
	            queryDocId = key;//�����λ�ý��п�ʼ��������λ�ý��м���
	        }
	    }
	    
	    ArrayList<Integer> firstQuery = postions.get(queryDocId);
	    keys.remove(queryDocId);
	    
	    Integer distance = 100;
	    for(Integer s: firstQuery){
	        postionSet.add(s);
	        for(Long docId:keys){
	            Integer tempSmall=100;
	            Integer small;
	            Integer ps=0;
	            for(Integer ns:postions.get(docId)){
	                small = s-ns;
	                small = Math.abs(small);
	                if(tempSmall>small){
	                    tempSmall =small;
	                    ps=ns;
	                }
	            }
	            postionSet.add(ps);//���뵽������ 
	        }
	        
	        //����postionSet����С����ֵ����queryֵ
	            int min=postionSet.get(0);
	            int max=postionSet.get(0);
	            for(int i = 1 ;i< postionSet.size();i++){
	                if(postionSet.get(i)<min){
	                    min = postionSet.get(i);
	                }
	                if(postionSet.get(i)>max){
	                    max = postionSet.get(i);
	                }
	            }
	            
	        if(distance>max-min){
	            distance = max-min;
	        }
	        
	        postionSet.clear();//��գ��´�����
	    }
	    
	    return distance+1;
	}
	
		
}
