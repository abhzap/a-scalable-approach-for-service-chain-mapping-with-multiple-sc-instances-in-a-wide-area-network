package ILP;

public class FuncPt{
	private int func_id;   
    private double core_count;
    private double procDelay;
    
    public FuncPt(int func_id, double core_count){
    	this.func_id = func_id;
    	this.core_count = core_count;  
    	this.procDelay = 0.0;
    }    
    
    public FuncPt(int func_id, double core_count, double procDelay){
    	this.func_id = func_id;
    	this.core_count = core_count;
    	this.procDelay = procDelay;
    }
    
    public int getid(){
    	return this.func_id;
    }
    
    public double getcore(){
    	return this.core_count;
    }
    
    public double getProcDelay(){
    	return this.procDelay;
    }
}
