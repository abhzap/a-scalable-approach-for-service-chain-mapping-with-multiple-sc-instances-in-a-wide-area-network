package colGen.model.data;

public class SolValue implements java.io.Serializable{
	
	public int validInstanceCount;
	public double totalBwUsed;
    public double avgBwUsed;
    
    //update the bandwidth values
    public SolValue(){
    	this.validInstanceCount = 0;
    	this.totalBwUsed = 0.0;
    	this.avgBwUsed = 0.0;
    }
    
    //update the total BW Value
    public void updateTotalBwValue(double bwValue){
    	//add the bandwidth value
    	this.totalBwUsed = this.totalBwUsed + bwValue;
    	//increase the instance count
    	this.validInstanceCount = this.validInstanceCount + 1;
    }
    
    //update the average BW value
    public void calculateAvgTrafficValue(){
    	this.avgBwUsed = this.totalBwUsed/this.validInstanceCount;
    }
}
