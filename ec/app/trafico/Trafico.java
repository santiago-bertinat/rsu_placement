package ec.app.trafico;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

import ec.app.trafico.geometry.Circle;
import ec.app.trafico.geometry.Point;
import ec.app.trafico.geometry.LineSegment;


public class Trafico extends Problem implements SimpleProblemForm {

    private static final long serialVersionUID = 1L;


    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state, base);
    }



    public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum) {
        System.out.println("Evaluate");
        if( !( ind instanceof FloatVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be FloatVectorIndividuals." );

        FloatVectorIndividual temp = (FloatVectorIndividual)ind;
        float[] genome = temp.genome;
        int numDecisionVars = genome.length;

        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

        objectives[0] = costo(ind);
        objectives[1]  = qos(ind);

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
        // System.out.println("Finish evaluate");
    }


    public double costo(Individual ind){

        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;

        double costo=0;
        int tipo_infraestructura;
        for (int i=0;i<t_ind.genome.length;i++){
            tipo_infraestructura=(int) Math.floor(t_ind.genome[i]);
            costo+=t_spe.getPrecioAntena()[tipo_infraestructura];
        }
        return costo;

    }

    public double qos (Individual ind){
        // System.out.println("QoS");
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;

        ArrayList<Circle> road_side_units = new ArrayList<Circle>();

        for (int i = 0; i < t_ind.genome.length; i++){
            // System.out.print(t_ind.genome[i]);
            // System.out.println(",");
        }
        // Create RSU's
        for (int i = 0; i < t_ind.genome.length; i++){
            int tipo_infraestructura = (int)Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura != 0){
                // There is a RSU on the segment
                double [] centro = encontrar_centro_antena(t_ind, i, tipo_infraestructura, t_spe);

                Point center = new Point(centro[0], centro[1]);
                double radio_circulo = t_spe.getRadioAntena()[tipo_infraestructura];
                // System.out.println("Circle");
                // System.out.println((center.x - 36.7) * 100);
                // System.out.println((center.y + 4.43) * -100);
                // System.out.println(radio_circulo);
                road_side_units.add(new Circle(center, radio_circulo));
            }
        }

        // Iterate through the segments
        if (road_side_units.size() > 0) {
            for (int i = 0; i < t_ind.genome.length; i++){

                // Calculate each segment coverage
                double start_x = t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[i]];
                double start_y = t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[i]];
                double end_x = t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[i]];
                double end_y = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[i]];

                LineSegment segment = new LineSegment(new Point(start_x, start_y), new Point(end_x, end_y));
                ArrayList<LineSegment> intersections = new ArrayList<LineSegment>();

                // System.out.println("SEGMENT");
                segment.print();


                // System.out.println("####");
                // System.out.println("Intersecion");
                for (Circle rsu : road_side_units) {
                  LineSegment intersection = rsu.lineIntersection(segment);
                  if (intersection != null) {
                    // intersection.print();
                    // System.out.println("####");
                    intersections.add(intersection);
                  }
                }

                ArrayList<LineSegment> combinations;
                if (intersections.size() > 1) {
                    combinations = LineSegment.combineSegments(intersections);
                } else {
                    combinations = intersections;
                }


                // System.out.println("####");
                // System.out.println("Combination");
                double coverered_distance = 0;
                for (LineSegment combination : combinations) {
                  // combination.print();
                  // System.out.println("####");
                  coverered_distance += Point.twoPointsDistance(combination.start, combination.end);
                }

                double total_distance = Point.twoPointsDistance(segment.start, segment.end);
                double coverage = total_distance / coverered_distance;


                // System.out.print("total_distance:");
                // System.out.println(total_distance);
                // System.out.print("coverered_distance:");
                // System.out.println(coverered_distance);
                // System.out.print("Segment:");
                // System.out.println(i);
                // System.out.print("Coverage:");
                // System.out.println(coverage);
                qos += t_spe.getCantidadVehiculosSegmento()[i] * (coverage)/(double)(t_spe.getVelocidadSegmento()[i]*1000);
            }
        }

        System.out.print("QoS:");
        System.out.println(qos);
        return qos;
    }


    public double [] encontrar_centro_antena (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){

        double lat_ini= t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lng_ini= t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lat_fin= t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[indice]];
        double lng_fin= t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[indice]];

        double a=(lng_fin-lng_ini)/(double)(lat_fin-lat_ini);
        double lambda = t_ind.genome[indice]-tipo_infraestructura;
        double [] centro = new double [2];
        if (lat_fin>lat_ini)
            centro[0]= lat_ini + Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda/(double)Math.sqrt(1+(a*a));
        else
            centro[0]= lat_ini - Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*lambda/(double)Math.sqrt(1+(a*a));

        if (lng_fin>lng_ini)
            centro[1]= lng_ini + Math.abs(Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*a*lambda/(double)Math.sqrt(1+(a*a)));
        else
            centro[1]= lng_ini - Math.abs(Math.sqrt((lng_fin-lng_ini)*(lng_fin-lng_ini) + (lat_fin-lat_ini)*(lat_fin-lat_ini))*a*lambda/(double)Math.sqrt(1+(a*a)));
        return centro;
    }

}



