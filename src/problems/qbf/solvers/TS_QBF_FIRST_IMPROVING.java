package problems.qbf.solvers;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import metaheuristics.tabusearch.AbstractTS;
import problems.qbf.QBF_Inverse;
import solutions.Solution;



/**
 * Metaheuristic TS (Tabu Search) for obtaining an optimal solution to a QBF
 * (Quadractive Binary Function -- {@link #}).
 * Since by default this TS considers minimization problems, an inverse QBF
 *  function is adopted.
 *
 * @author ccavellucci, fusberti
 */
public class TS_QBF_FIRST_IMPROVING extends AbstractTS<Integer> {

    private final Integer fake = -1;

    /**
     * Constructor for the TS_QBF_FIRST_IMPROVING class. An inverse QBF objective function is
     * passed as argument for the superclass constructor.
     *
     * @param tenure
     *            The Tabu tenure parameter.
     * @param time
     *            Total amount of time which the TS will be executed.
     * @param filename
     *            Name of the file for which the objective function parameters
     *            should be read.
     * @throws IOException
     *             necessary for I/O operations.
     */
    public TS_QBF_FIRST_IMPROVING(Integer tenure, Integer time, String filename) throws IOException {
        super(new QBF_Inverse(filename), tenure, time);
    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeCL()
     */
    @Override
    public ArrayList<Integer> makeCL() {

        ArrayList<Integer> _CL = new ArrayList<Integer>();
        for (int i = 0; i < ObjFunction.getDomainSize(); i++) {
            Integer cand = i;
            _CL.add(cand);
        }

        return _CL;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeRCL()
     */
    @Override
    public ArrayList<Integer> makeRCL() {

        ArrayList<Integer> _RCL = new ArrayList<Integer>();

        return _RCL;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#makeTL()
     */
    @Override
    public ArrayDeque<Integer> makeTL() {

        ArrayDeque<Integer> _TS = new ArrayDeque<Integer>(2*tenure);
        for (int i=0; i<2*tenure; i++) {
            _TS.add(fake);
        }

        return _TS;

    }

    /* (non-Javadoc)
     * @see metaheuristics.tabusearch.AbstractTS#updateCL()
     */
    @Override
    public void updateCL() {
        ObjFunction.getDomainSize();
        int[] pesos = ObjFunction.getWeights();
        int pesoAtual = ObjFunction.getCurrentWeight();
        int maxPeso = ObjFunction.getMaxWeight();

        Iterator<Integer> iterator = CL.iterator();
        while (iterator.hasNext()) {
            Integer valor = iterator.next();
            if (pesos[valor] + pesoAtual > maxPeso) {
                iterator.remove();
            }
        }

        for (int i = 0; i < ObjFunction.getDomainSize(); i++){
            if (pesos[i] + pesoAtual <= maxPeso && !CL.contains(i) && !sol.contains(i)) {
                CL.add(i);
            }
        }

    }

    /**
     * {@inheritDoc}
     *
     * This createEmptySol instantiates an empty solution and it attributes a
     * zero cost, since it is known that a QBF solution with all variables set
     * to zero has also zero cost.
     */
    @Override
    public Solution<Integer> createEmptySol() {
        Solution<Integer> sol = new Solution<Integer>();
        sol.cost = 0.0;
        return sol;
    }

    /**
     * {@inheritDoc}
     *
     * The local search operator developed for the QBF objective function is
     * composed by the neighborhood moves Insertion, Removal and 2-Exchange.
     */
    @Override
    public Solution<Integer> neighborhoodMove() {
        Double minDeltaCost;
        Integer bestCandIn = null, bestCandOut = null;

        minDeltaCost = Double.POSITIVE_INFINITY;
        updateCL();
        Collections.shuffle(CL);
        // Evaluate insertions
        for (Integer candIn : CL) {
            Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
            if (!TL.contains(candIn) || sol.cost+deltaCost < bestSol.cost) {
                if (deltaCost < 0) {
                    TL.poll();
                    sol.add(candIn);
                    CL.remove(candIn);
                    TL.add(candIn);
                    ObjFunction.evaluate(sol);

                    return null;
                }
            }
        }
        // Evaluate removals
        for (Integer candOut : sol) {
            Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
            if (!TL.contains(candOut) || sol.cost+deltaCost < bestSol.cost) {
                if (deltaCost < 0) {
                    TL.poll();
                    sol.remove(candOut);
                    CL.add(candOut);
                    TL.add(candOut);
                    ObjFunction.evaluate(sol);

                    return null;
                }
            }
        }
        // Evaluate exchanges
        for (Integer candIn : CL) {
            for (Integer candOut : sol) {
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
                if ((!TL.contains(candIn) && !TL.contains(candOut)) || sol.cost+deltaCost < bestSol.cost) {
                    if (deltaCost < 0) {
                        TL.poll();
                        sol.add(candIn);
                        CL.remove(candIn);
                        TL.add(candIn);

                        TL.poll();
                        sol.remove(candOut);
                        CL.add(candOut);
                        TL.add(candOut);
                        ObjFunction.evaluate(sol);

                        return null;
                    }
                }
            }
        }

        // Evaluate insertions
        for (Integer candIn : CL) {
            Double deltaCost = ObjFunction.evaluateInsertionCost(candIn, sol);
            if (!TL.contains(candIn) || sol.cost+deltaCost < bestSol.cost) {
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = candIn;
                    bestCandOut = null;
                }
            }
        }
        // Evaluate removals
        for (Integer candOut : sol) {
            Double deltaCost = ObjFunction.evaluateRemovalCost(candOut, sol);
            if (!TL.contains(candOut) || sol.cost+deltaCost < bestSol.cost) {
                if (deltaCost < minDeltaCost) {
                    minDeltaCost = deltaCost;
                    bestCandIn = null;
                    bestCandOut = candOut;
                }
            }
        }
        // Evaluate exchanges
        for (Integer candIn : CL) {
            for (Integer candOut : sol) {
                Double deltaCost = ObjFunction.evaluateExchangeCost(candIn, candOut, sol);
                if ((!TL.contains(candIn) && !TL.contains(candOut)) || sol.cost+deltaCost < bestSol.cost) {
                    if (deltaCost < minDeltaCost) {
                        minDeltaCost = deltaCost;
                        bestCandIn = candIn;
                        bestCandOut = candOut;
                    }
                }
            }
        }

        // Implement the best non-tabu move
        TL.poll();
        if (bestCandOut != null) {
            sol.remove(bestCandOut);
            CL.add(bestCandOut);
            TL.add(bestCandOut);
        } else {
            TL.add(fake);
        }
        TL.poll();
        if (bestCandIn != null) {
            sol.add(bestCandIn);
            CL.remove(bestCandIn);
            TL.add(bestCandIn);
        } else {
            TL.add(fake);
        }
        ObjFunction.evaluate(sol);

        return null;
    }

    /**
     * A main method used for testing the TS metaheuristic.
     *
     */
    public static void main(String[] args) throws IOException {

        long startTime = System.currentTimeMillis();
        TS_QBF_FIRST_IMPROVING tabusearch = new TS_QBF_FIRST_IMPROVING(40, 100000, "instances/kqbf/kqbf200");
        Solution<Integer> bestSol = tabusearch.solve();
        System.out.println("maxVal = " + bestSol);
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("Time = "+(double)totalTime/(double)1000+" seg");

    }

}
