/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package ec.vector.breed;

import ec.vector.*;
import ec.*;
import ec.util.*;

import java.util.*;
import ec.app.trafico.Calculos;

/*
 * VectorMutationPipeline.java
 *
 * Created: Tue Mar 13 15:03:12 EST 2001
 * By: Sean Luke
 */


/**
 *
 VectorMutationPipeline is a BreedingPipeline which implements a simple default Mutation
 for VectorIndividuals.  Normally it takes an individual and returns a mutated
 child individual. VectorMutationPipeline works by calling defaultMutate(...) on the
 parent individual.
 <p><b>Typical Number of Individuals Produced Per <tt>produce(...)</tt> call</b><br>
 (however many its source produces)
 <p><b>Number of Sources</b><br>
 1
 <p><b>Default Base</b><br>
 vector.mutate (not that it matters)
 * @author Sean Luke
 * @version 1.0
 */

public class LocalQosOptimumPipeline extends BreedingPipeline
    {
    public static final int NUM_SOURCES = 1;

    public Parameter defaultBase() { return VectorDefaults.base(); }

    /** Returns 1 */
    public int numSources() { return NUM_SOURCES; }

    public int produce(final int min,
        final int max,
        final int start,
        final int subpopulation,
        final Individual[] inds,
        final EvolutionState state,
        final int thread)
        {
        // grab individuals from our source and stick 'em right into inds.
        // we'll modify them from there
        int n = sources[0].produce(min,max,start,subpopulation,inds,state,thread);

        // should we bother?
        if (!state.random[thread].nextBoolean(likelihood))
            return reproduce(n, start, subpopulation, inds, state, thread, false);  // DON'T produce children from source -- we already did

        // clone the individuals if necessary
            if (!(sources[0] instanceof BreedingPipeline))
                for(int q = start; q < n + start; q++)
                    inds[q] = (Individual)(inds[q].clone());

        // find local optimum
        int individuo_sorteado = state.random[thread].nextInt(n) + start;
        FloatVectorIndividual ind = (FloatVectorIndividual)inds[individuo_sorteado];
        FloatVectorSpecies spe = (FloatVectorSpecies)inds[individuo_sorteado].species;

        int posicion_sorteada = state.random[thread].nextInt(ind.genome.length);
        int tipo_infraestructura = (int) Math.floor(ind.genome[posicion_sorteada]);

        double max_qos = Calculos.qos(ind);
        double max_position = ind.genome[posicion_sorteada];
        if (tipo_infraestructura != 0) {
          for (int i = 0; i <= 5; i++){
            float cambio_de_posicion = (float)0.2 * (float)i;
            if (cambio_de_posicion == 1)
              cambio_de_posicion = (float)0.99;
            ind.genome[posicion_sorteada] =  tipo_infraestructura + cambio_de_posicion;

            double qos_nuevo = Calculos.qos(ind);
            if (qos_nuevo > max_qos) {
              max_qos = qos_nuevo;
              max_position = ind.genome[posicion_sorteada];
            }
          }

          ind.genome[posicion_sorteada] = (float) max_position;
          ((VectorIndividual)inds[individuo_sorteado]).evaluated = false;
        }

        return n;
    }

    }
