package colGen.model.preprocess;

import java.util.ArrayList;
import java.util.List;

import com.sun.corba.se.spi.orbutil.fsm.Input;


public class KCombination {
	
	// generate actual subset by index sequence
	static ArrayList<Integer> getSubset(ArrayList<Integer> input, int[] subset) {
	    ArrayList<Integer> result = new ArrayList<Integer>(subset.length); 
	    for (int i = 0; i < subset.length; i++) 
	        result.add(i, input.get(subset[i]));   
	    return result;
	}
	
	
	//to find 'k' combinations out of 'n' numbers
	public static List<ArrayList<Integer>> findCombinations(ArrayList<Integer> nList, int k){
		
		List<ArrayList<Integer>> ksubsets = new ArrayList<ArrayList<Integer>>();
		//no DC node is selected
		if( k ==0 )
			return ksubsets;
		//subset of indices pointing to input array		
		int[] subIndices = new int[k];
//		System.out.println("n : " + nList.size() + " ; k : " + k);
		
		
		//check for valid size of K
		if(k <= nList.size()){
			
			//first index sequence: 0, 1, 2, .... k
			for(int i=0; (subIndices[i] = i) < k-1; i++);
			int subsetNum = 0;
			//get the subset corresponding to the above
			ksubsets.add(getSubset(nList,subIndices));
			subsetNum++;
			//print our the first subset that is added
//			System.out.println("Printing out the first subset");
			printCombination(ksubsets);			
			//infinite loop to generate 
			//the other subsets
			for(;;){
				int i;
				//find the position of item that can be incremented
				for(i = k-1; i>=0 && subIndices[i] == nList.size() - k + i; i--);				
				if(i < 0){
					break;
				}else{
					//increment this item
					subIndices[i]++;					
					//fill up remaining items
					for( ++i; i < k; i++ ){
						subIndices[i] = subIndices[i-1] + 1;
					}
					//get the k-subsets
					ksubsets.add(getSubset(nList,subIndices));
					subsetNum++;
//					System.out.println("Subset No. " +  subsetNum + " has been added!");
					//print out the subsets that have been generated
					printCombination(ksubsets);
				}
			}
		}
//		System.out.println("Complete list of Subsets of size k = " + k + " in a list of size n = " + nList.size() + " has length " + ksubsets.size());
		printCombination(ksubsets);
		return ksubsets;
	}
	
	//print the subsets
	public static void printCombination(List<ArrayList<Integer>> ksubsets){
		//go though the list of combinations
		for(ArrayList<Integer> placement : ksubsets){
			for(int vrtID : placement){
				System.out.print(vrtID + ", ");
			}
			System.out.println();
		}
	}

}
