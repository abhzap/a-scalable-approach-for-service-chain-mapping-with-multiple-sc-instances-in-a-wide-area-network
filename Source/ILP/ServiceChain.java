package ILP;
import java.util.ArrayList;

public class ServiceChain {
	   public int chain_index;
       public ArrayList<Integer> chain_seq;
       public int chain_size;
       public double latReq;
       
       //constructor
       public ServiceChain(int chain_index, ArrayList<Integer> chain_seq){
    	   this.chain_index = chain_index;    	   
    	   //new ArrayList object
    	   this.chain_seq = new ArrayList<Integer>(chain_seq);  
    	   this.chain_size = chain_seq.size();
    	   this.latReq = 100000.0;
       }
       
       public ServiceChain(int chain_index, ArrayList<Integer> chain_seq, double latReq){
    	   this.chain_index = chain_index;    	   
    	   //new ArrayList object
    	   this.chain_seq = new ArrayList<Integer>(chain_seq);  
    	   this.chain_size = chain_seq.size();
    	   this.latReq = latReq;
       }
       
       public ArrayList<Integer> getChainSeq(){
    	   return this.chain_seq;
       }
       
       public int getChainIndex(){
    	   return this.chain_index;
       }
       
       public int getChainSize(){
    	   return this.chain_size;
       }
       
       public double getLatReq(){
    	   return this.latReq;
       }
             
}
