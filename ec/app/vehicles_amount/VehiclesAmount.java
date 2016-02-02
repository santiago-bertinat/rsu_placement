package ec.app.vehicles_amount;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;
import ec.app.vehicles_amount.Calculos;

public class VehiclesAmount extends Problem implements SimpleProblemForm {

    private static final long serialVersionUID = 1L;


    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state, base);
    }


    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        if( !( ind instanceof FloatVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be FloatVectorIndividuals." );

        FloatVectorIndividual temp = (FloatVectorIndividual)ind;
        float[] genome = temp.genome;
        int numDecisionVars = genome.length;

        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

        objectives[0] = Calculos.costo(ind);
        objectives[1] = Calculos.qos(ind);

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
    }

}



