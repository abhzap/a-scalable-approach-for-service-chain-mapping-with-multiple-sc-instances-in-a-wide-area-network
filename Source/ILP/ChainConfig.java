package ILP;
import java.util.ArrayList;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class ChainConfig {
	public int chain_length;
    public ArrayList<BaseVertex> node_seq;
    
    public ChainConfig(int chain_index, ArrayList<BaseVertex> node_seq){
    	this.chain_length = chain_index;
    	this.node_seq = new ArrayList<BaseVertex>(node_seq);
    }
    
    public int getChainLength(){
    	return this.chain_length;
    }    
   
    
}
