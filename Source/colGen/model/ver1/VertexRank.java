package colGen.model.ver1;

import edu.asu.emit.qyan.alg.model.abstracts.BaseVertex;

public class VertexRank implements Comparable<VertexRank> {
	public BaseVertex vertex;
	public Double rankValue;
	
	public VertexRank(BaseVertex vertex, Double rankValue){
		this.vertex = vertex;
		this.rankValue = rankValue;
	}
	
	public int compareTo(VertexRank compareVertex){
		//sort in descending order
		return compareVertex.rankValue.compareTo(this.rankValue);
		//sort in ascending order
//		return this.rankValue.compareTo(compareVertex.rankValue);
	}
}
