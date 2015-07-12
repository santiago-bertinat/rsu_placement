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
import components.*;

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

public class LocalOptimumPipeline extends BreedingPipeline
    {
    public static final String P_MUTATION = "mutate";
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
                for(int q=start;q<n+start;q++)
                    inds[q] = (Individual)(inds[q].clone());

        // find local optimum
        System.out.println("Optimo");
        for(int q = start; q < n + start; q++) {
            // Create RSU's
            ArrayList<Rsu> road_side_units = new ArrayList<Rsu>();
            FloatVectorIndividual ind = (FloatVectorIndividual)inds[q];
            FloatVectorSpecies spe = (FloatVectorSpecies)inds[q].species;

            for (int i = 0; i < ind.genome.length; i++){
                int rsu_type = (int)Math.floor(ind.genome[i]);
                if (rsu_type != 0){
                    // There is a RSU on the segment
                    Point center = findRsuCenter(ind, i, rsu_type, spe);
                    double radius = spe.getRsuTypes().get(rsu_type - 1).radius;
                    road_side_units.add(new Rsu(center, radius));
                }
            }


            for(int i = 0; i < road_side_units.size() * 1; i++) {
                int random_position = state.random[thread].nextInt(road_side_units.size());

                Rsu rsu = road_side_units.get(random_position);
                if (isRsuIntersected(road_side_units, rsu)) {
                    double rsu_position = ind.genome[random_position] - Math.floor(ind.genome[random_position]);
                    double new_position = 1 - rsu_position;

                    System.out.println(rsu_position);
                    System.out.println(new_position);
                    rsu.center = findRsuCenter(ind, random_position, (int)Math.floor(ind.genome[random_position]), spe);
                    if (isRsuIntersected(road_side_units, rsu)) {
                        road_side_units.remove(rsu);
                        ind.genome[random_position] = 0;
                        System.out.println(0);
                    } else {
                        ind.genome[random_position] = (float)new_position + (float)Math.floor(ind.genome[random_position]);
                        System.out.println("a algo");
                    }
                    ((VectorIndividual)inds[q]).evaluated = false;
                }
            }

        }

        return n;
    }

    public boolean isRsuIntersected(ArrayList<Rsu> road_side_units, Rsu rsu) {
       for (Rsu aux_rsu : road_side_units) {
            double centers_distance = Point.twoPointsDistance(aux_rsu.center, rsu.center);
            if (aux_rsu != rsu && aux_rsu.radius > (centers_distance + rsu.radius) * 0.9) {
                return true;
            }
        }

        return false;
    }

    public Point findRsuCenter (FloatVectorIndividual t_ind, int index, int rsu_type, FloatVectorSpecies t_spe){
        Segment segment = t_spe.getSegments().get(index);
        double rsu_position = t_ind.genome[index] - rsu_type;

        double x_length = Math.abs(segment.start.x - segment.end.x) * rsu_position;
        double y_length = Math.abs(segment.start.y - segment.end.y) * rsu_position;

        double x = segment.start.x;
        if (segment.start.x < segment.end.x) {
            x = segment.start.x + x_length;
        }else if (segment.start.x > segment.end.x) {
            x = segment.start.x - x_length;
        }

        double y = segment.start.y;
        if (segment.start.y < segment.end.y) {
            y = segment.start.y + y_length;
        }else if (segment.start.y > segment.end.y) {
            y = segment.start.y - y_length;
        }

        return new Point(x, y);
    }

    }


