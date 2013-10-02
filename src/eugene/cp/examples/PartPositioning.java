package eugene.cp.examples;

import JaCoP.constraints.And;
import JaCoP.constraints.Element;
import JaCoP.constraints.IfThen;
import JaCoP.constraints.Or;
import JaCoP.constraints.PrimitiveConstraint;
import JaCoP.constraints.Reified;
import JaCoP.constraints.XeqC;
import JaCoP.constraints.XeqY;
import JaCoP.constraints.XltY;
import JaCoP.constraints.XneqY;
import JaCoP.constraints.XplusCeqZ;
import JaCoP.core.BooleanVar;
import JaCoP.core.Domain;
import JaCoP.core.IntVar;
import JaCoP.core.Store;
import JaCoP.search.DepthFirstSearch;
import JaCoP.search.IndomainMin;
import JaCoP.search.IndomainSimpleRandom;
import JaCoP.search.LargestDomain;
import JaCoP.search.MostConstrainedDynamic;
import JaCoP.search.Search;
import JaCoP.search.SelectChoicePoint;
import JaCoP.search.SimpleSelect;

public class PartPositioning {

	private Store store;
	private IntVar[] variables;
	
	private static final int NR_OF_VARIABLES = 300;
	private static final int NR_OF_PARTS = 300;
	
	public PartPositioning() {
		this.store = new Store();
	}
	
	public void model() {

		this.variables = new IntVar[NR_OF_VARIABLES];
		for(int i=0; i<NR_OF_VARIABLES; i++) {
			this.variables[i] = new IntVar(store, "x"+i, 1, NR_OF_PARTS);
		}
		
		/*
		 * the parts variable contains all part-ids
		 */
		IntVar[] parts = new IntVar[NR_OF_PARTS];
		for(int i=1; i<=NR_OF_PARTS; i++) {
			parts[i-1] =  new IntVar(store, "part-"+i, i, i);

			/*
			 * every part can be positioned everywhere
			 */
			IntVar partIndex = new IntVar(store, "part-"+i+".index", 1, NR_OF_VARIABLES);
			store.impose(new Element(partIndex, this.variables, parts[i-1]));
		}
		
		
		/*
		 * now, we need to define a finite relation between the parts 
		 * and their possible position
		 */

		for(int i=1; i<NR_OF_PARTS; i++) {
			IntVar idxA = (IntVar)store.findVariable("part-"+i+".index");
			IntVar idxB = (IntVar)store.findVariable("part-"+(i+1)+".index");
			/*
			 * A BEFORE B
			 */
//			store.impose(new XltY(idxA, idxB));

			/*
			 * A NEXTTO B
			 */			
			store.impose(new XplusCeqZ(idxA, 1, idxB));
		}
		
		store.print();
	}

	
	public void solve() {
    	Search<IntVar> search = new DepthFirstSearch<IntVar>(); 

        SelectChoicePoint<IntVar> select = null;
    	select = new SimpleSelect<IntVar>(
    				this.variables, 
    				new LargestDomain<IntVar>(),
    				new IndomainMin<IntVar>()); 
    	search.getSolutionListener().searchAll(true);   

//        search.setPrintInfo(true);
        search.getSolutionListener().recordSolutions(true);
                
		long T1 = System.nanoTime();

        // SOLVE
		try {
			search.labeling(store, select);
		} catch(Exception e) {
			e.printStackTrace();
		}

		long T2 = System.nanoTime();
		
		double nProcessing = (T2 - T1) * Math.pow(10, -9);
		System.out.println("processing time: "+nProcessing+"sec");

		// print all solutions
		printSolutions(
				search.getSolutionListener().getSolutions());
	}
	
	private void printSolutions(Domain[][] domains) {
		if(null != domains) {
			for(Domain[] domain : domains) {
				if(null == domain)
					break;
				for(int i=0; i<domain.length; i++) {
					System.out.print(domain[i]+", ");
				}
				System.out.println();				
			}
		}
	}
	
	public static void main(String[] args) {
//		for(int i=1; i<=50; i++) {
			PartPositioning pp = new PartPositioning();
			pp.model();
			pp.solve();
//		}
	}

}
