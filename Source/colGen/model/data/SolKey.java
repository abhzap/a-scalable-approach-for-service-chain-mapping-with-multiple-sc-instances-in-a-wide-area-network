package colGen.model.data;


public class SolKey implements java.io.Serializable{
	
	public int coreCount;
	public double Traffic;    
 
    
    public SolKey(int coreCount, double Traffic){
    	this.coreCount = coreCount;    
    	this.Traffic = Traffic; 
    }    

    
    @Override
	public boolean equals(Object obj)
	{
		boolean result = false;
		if(obj == null || obj.getClass() != getClass()){
			result = false;
		} else {
			SolKey o = (SolKey) obj;
			if( (this.coreCount==o.coreCount)&&(this.Traffic==o.Traffic) ){
				result = true;
			}
		}
	    return result;
	}	
	
	@Override
	public int hashCode()
	{
		Double d = new Double(this.Traffic);
	    return this.coreCount + d.hashCode();
	}
    
}
