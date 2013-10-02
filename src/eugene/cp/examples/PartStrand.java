package eugene.cp.examples;

import JaCoP.constraints.And;
import JaCoP.constraints.IfThen;
import JaCoP.constraints.Or;
import JaCoP.constraints.PrimitiveConstraint;
import JaCoP.constraints.Reified;
import JaCoP.constraints.XeqC;
import JaCoP.constraints.XeqY;
import JaCoP.constraints.XneqY;
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

public class PartStrand {

	private Store store;
	private IntVar[] variables;
	
	private static final int NR_OF_VARIABLES = 18;
	private static final int NR_OF_PARTS = 380;
	
	public PartStrand() {
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
		IntVar partStrand = new IntVar(store, "part.strand");
		partStrand.addDom(-1, -1);  // negative strand
		partStrand.addDom(1, 1);    // positive strand
		
		for(int i=1; i<=NR_OF_PARTS; i++) {
			// Part variables
			IntVar part = new IntVar(store, "part-"+i, i, i);
			BooleanVar bVar = new BooleanVar(store);
			
			if(i % 2 == 1) {
				// every ``even'' part is on the positive strand (i.e. part.strand=1)
				store.impose(new Reified(new XeqC(partStrand, 1), bVar));
				store.impose(new Reified(new XeqC(part, i), bVar));
//						new IfThen(
//								new XeqC(partStrand, 1),
//								new XeqC(part, i)));
			} else {
				// every ``odd'' part is on the negative strand (i.e. part.strand=0)
				store.impose(new Reified(new XeqC(partStrand, 1), bVar));
				store.impose(new Reified(new XeqC(part, i), bVar));
//				store.impose(
//						new IfThen(
//								new XeqC(partStrand, -1),
//								new XeqC(part, i)));
			}
		}
	}

	public void query() {
		// here, we state a constraint saying that we're looking
		// for all parts on the positive strand
		IntVar partStrand = (IntVar)store.findVariable("part.strand");
		
		for(int i=0; i<NR_OF_VARIABLES; i++) {
			PrimitiveConstraint[] pc = new PrimitiveConstraint[NR_OF_PARTS];
			for(int p=1; p<=NR_OF_PARTS; p++) {
//				BooleanVar bVar = new BooleanVar(store);
				IntVar part = (IntVar)store.findVariable("part-"+p);
				if(p % 2 == 1) {
//					store.impose(new Reified(new XeqC(partStrand, 1), bVar));
//					store.impose(new Reified(new XeqY(variables[i], part), bVar));
					// every ``even'' part is on the positive strand (i.e. part.strand=1)
					pc[p-1] = new And(
									new XeqC(partStrand, 1),
									new XeqY(variables[i], part));
				} else {
					// every ``odd'' part is on the negative strand (i.e. part.strand=0)
//					store.impose(new Reified(new XeqC(partStrand, -1), bVar));
//					store.impose(new Reified(new XeqY(variables[i], part), bVar));
					pc[p-1] = new And(
							new XeqC(partStrand, -1),
							new XeqY(variables[i], part));
				}
			}
			
			store.impose(new Or(pc));
		}
		

		// we want to have all parts on the positive strand		
		/* Eugene Rule: FOR ALL parts : (FORWARD|REVERSE) */
		store.impose(new XeqC(partStrand, 1));
		
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
        search.getSolutionListener().recordSolutions(false);
                
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
		PartStrand pp = new PartStrand();
		pp.model();
		pp.query();
		pp.solve();
	}

}
