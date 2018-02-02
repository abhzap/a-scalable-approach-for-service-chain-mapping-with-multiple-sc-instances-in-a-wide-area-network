package ILP;

import java.util.Comparator;

public class IngressEgressConfigurationComparator implements Comparator<IngressEgressConfiguration>{
	
	public int compare(IngressEgressConfiguration ingeg1, IngressEgressConfiguration ingeg2){
		return ingeg1.shortest_path_length - ingeg2.shortest_path_length;
	}

}
