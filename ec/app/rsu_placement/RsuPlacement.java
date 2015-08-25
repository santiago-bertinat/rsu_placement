package ec.app.rsu_placement;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

import components.Rsu;
import components.Point;
import components.Segment;
import components.RsuType;


public class RsuPlacement extends Problem implements SimpleProblemForm {

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
        objectives[0] = cost(ind);
        objectives[1] = qos(ind);

        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;

        // for (int i = 0; i < t_ind.genome.length; i++){
        //     System.out.println(t_ind.genome[i]);
        // }

        // System.out.println("RESULT");
        // System.out.println(objectives[0] + " " + objectives[1]);
        // System.out.println("---------------------------------");

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
    }


    public double cost(Individual ind){
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;

        double costo = 0;
        int rsu_type;
        for (int i = 0; i < t_ind.genome.length; i++){
            rsu_type = (int)Math.floor(t_ind.genome[i]);
            if (rsu_type != 0) {
                costo += t_spe.getRsuTypes().get(rsu_type - 1).cost;
            }
        }
        return costo;
    }

    public double qos(Individual ind){
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;

        // Create RSU's
        ArrayList<Rsu> road_side_units = new ArrayList<Rsu>();
        for (int i = 0; i < t_ind.genome.length; i++){
            int rsu_type = (int)Math.floor(t_ind.genome[i]);
            if (rsu_type != 0){
                // There is a RSU on the segment
                Point center = findRsuCenter(t_ind, i, rsu_type, t_spe);
                double radius = t_spe.getRsuTypes().get(rsu_type - 1).radius;
                road_side_units.add(new Rsu(center, radius));
            }
        }

        double ideal = 0;
        for (Segment segment : t_spe.getSegments()) {
            ideal += segment.distance() * segment.importance;
        }
        // System.out.println("IDEAL:" + ideal);

        // Iterate through the segments
        if (road_side_units.size() > 0) {
            for (int i = 0; i < t_ind.genome.length; i++){

                // Calculate each segment coverage
                Segment segment = t_spe.getSegments().get(i);

                double segment_length = Point.twoPointsDistance(segment.start, segment.end);
                double divitions = 10;
                double module_section = segment_length / divitions;
                double intersections = 0;

                double coverered_distance = 0;

                double x_length = Math.abs(segment.start.x - segment.end.x) / divitions;
                double y_length = Math.abs(segment.start.y - segment.end.y) / divitions;

                for (int j = 0; j < divitions; j++) {
                    double x = segment.start.x;
                    if (segment.start.x < segment.end.x) {
                        x = segment.start.x + j * x_length;
                    }else if (segment.start.x > segment.end.x) {
                        x = segment.start.x - j * x_length;
                    }

                    double y = segment.start.y;
                    if (segment.start.y < segment.end.y) {
                        y = segment.start.y + j * y_length;
                    }else if (segment.start.y > segment.end.y) {
                        y = segment.start.y - j * y_length;
                    }


                    Point aux_point = new Point(x, y);
                    for (Rsu rsu : road_side_units) {
                        if (rsu.belongsToRsu(aux_point)) {
                            intersections++;
                            break;
                        }
                    }
                }

                coverered_distance = intersections * module_section;
                qos += coverered_distance * segment.importance;
            }
        }

        return qos;
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



