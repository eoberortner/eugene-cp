package eugene.cp.examples;

import JaCoP.constraints.And;
import JaCoP.constraints.IfThen;
import JaCoP.constraints.Or;
import JaCoP.constraints.OrBool;
import JaCoP.constraints.PrimitiveConstraint;
import JaCoP.constraints.Reified;
import JaCoP.constraints.XeqC;
import JaCoP.constraints.XeqY;
import JaCoP.constraints.XgtC;
import JaCoP.constraints.XgteqC;
import JaCoP.constraints.XneqY;
import JaCoP.constraints.Xor;
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

public class PartStrength {

	private Store store;
	private IntVar[] variables;
	
	private static final int NR_OF_VARIABLES = 3;
	private static final int NR_OF_PARTS = 3;
	
	public PartStrength() {
		this.store = new Store();
		// let's create our variables
		this.variables = new IntVar[NR_OF_VARIABLES];
	}
	
	public void model() {

		/** my variables of interest **/
		for(int i=0; i<NR_OF_VARIABLES; i++) {
			variables[i] = new IntVar(store, "x"+i, 1, NR_OF_PARTS);
		}

		
		/** ``facts'' **/
		// part.strand variable
		IntVar partStrength = new IntVar(store, "part.strength");
		
		for(int i=1; i<=NR_OF_PARTS; i++) {
			// Part variables
			IntVar part = new IntVar(store, "part-"+i, i, i);
			BooleanVar bVar = new BooleanVar(store);
			
			/* every part has a strength of 100 times of the part's ID
			 * Examples: 
			 * part-1 -> part-1.strength = 100
			 * part-2 -> part-2.strength = 200
			 * ...
			 * part-N -> part-N.strength = N*100
			 */
			partStrength.addDom(i*100, i*100);

			/*
			 * IF c1 THEN c2
			 * -> Reified(c1, b) && Reified(c2, b) 
			 */
			store.impose(new Reified(new XeqC(partStrength, i), bVar));
			store.impose(new Reified(new XeqY(part, partStrength), bVar));
		}
		
		store.print();
	}

	public void query() {
		/*
		 * we want to get all parts whose strength is greater than N
		 */
		int N = 200;
		IntVar partStrength = (IntVar)store.findVariable("part.strength");
//		store.impose(new XgtC(partStrength, N));
		
		

		for(int i=0; i<NR_OF_VARIABLES; i++) {
			for(int p=1; p<=NR_OF_PARTS; p++) {
				IntVar part = (IntVar)store.findVariable("part-"+p);
	
				store.impose(
						new IfThen(
								new XgteqC(partStrength, N),
								new XeqY(variables[i], part)));
			}					
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

        search.setPrintInfo(true);
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
		PartStrength pp = new PartStrength();
		pp.model();
		pp.query();
		pp.solve();
	}

}
