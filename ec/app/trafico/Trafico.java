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
        if( !( ind instanceof FloatVectorIndividual ) )
            state.output.fatal( "The individuals for this problem should be FloatVectorIndividuals." );

        System.out.println("#######");
        long startTime1 = System.currentTimeMillis();
        FloatVectorIndividual temp = (FloatVectorIndividual)ind;
        float[] genome = temp.genome;
        int numDecisionVars = genome.length;

        double[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();

        long startTime2 = System.currentTimeMillis();
        objectives[0] = costo(ind);
        long endTime2 = System.currentTimeMillis();
        System.out.print("Total cost: ");
        System.out.println(endTime2 - startTime2);

        long startTime3 = System.currentTimeMillis();
        objectives[1]  = qos(ind);
        long endTime3 = System.currentTimeMillis();
        System.out.print("Total qos: ");
        System.out.println(endTime3 - startTime3);

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
        long endTime1 = System.currentTimeMillis();
        System.out.print("Total evaluation: ");
        System.out.println(endTime1 - startTime1);
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
        boolean debug;
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;


        ArrayList<Circle> road_side_units = new ArrayList<Circle>();

        long startTime1 = System.currentTimeMillis();
        // Create RSU's
        for (int i = 0; i < t_ind.genome.length; i++){
            int tipo_infraestructura = (int)Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura != 0){
                // There is a RSU on the segment
                double [] centro = encontrar_centro_antena(t_ind, i, tipo_infraestructura, t_spe);

                Point center = new Point(centro[0], centro[1]);
                double radio_circulo = t_spe.getRadioAntena()[tipo_infraestructura];
                road_side_units.add(new Circle(center, radio_circulo));
            }
        }
        long endTime1 = System.currentTimeMillis();
        System.out.print("Create RSUs: ");
        System.out.println(endTime1 - startTime1);

        long startTime2 = System.currentTimeMillis();
        // Iterate through the segments
        for (int i = 0; i < t_ind.genome.length; i++){

            // Calculate each segment coverage
            double start_x = t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[i]];
            double start_y = t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[i]];
            double end_x = t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[i]];
            double end_y = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[i]];

            LineSegment segment = new LineSegment(new Point(start_x, start_y), new Point(end_x, end_y));
            double segment_length = Point.twoPointsDistance(segment.start, segment.end);
            double divitions = 10;
            double module_section = segment_length / divitions;
            double intersections = 0;

            double coverered_distance = 0;

            double x_length = Math.abs(segment.start.x - segment.end.x) / divitions;
            double y_length = Math.abs(segment.start.y - segment.end.y) / divitions;


			Circle rsu_anterior=null;

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

                if(rsu_anterior!=null && rsu_anterior.belongsToCircle(aux_point)) {
                        intersections++;
                }
                else{

                    for (Circle rsu : road_side_units) {
                        if (rsu.belongsToCircle(aux_point)) {
                            intersections++;
                            rsu_anterior=rsu;
                            break;
                        }
                    }
                }
            }

            coverered_distance = intersections * module_section;
            qos += t_spe.getCantidadVehiculosSegmento()[i] * (coverered_distance)/(double)(t_spe.getVelocidadSegmento()[i]*1000);
        }
        long endTime2 = System.currentTimeMillis();
        System.out.print("Itarate through segments: ");
        System.out.println(endTime2 - startTime2);

        return qos;
    }


    public double [] encontrar_centro_antena (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){

        double lat_ini= t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lng_ini= t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[indice]];
        double lat_fin= t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[indice]];
        double lng_fin= t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[indice]];
        double rsu_position = t_ind.genome[indice] - tipo_infraestructura;

        double x_length = Math.abs(lat_ini - lat_fin) * rsu_position;
        double y_length = Math.abs(lng_ini - lng_fin) * rsu_position;

        double x = lat_ini;
        if (lat_ini < lat_fin) {
          x = lat_ini + x_length;
        }else if (lat_ini > lat_fin) {
          x = lat_ini - x_length;
        }

        double y = lng_ini;
        if (lng_ini < lng_fin) {
          y = lng_ini + y_length;
        }else if (lng_ini > lng_fin) {
          y = lng_ini - y_length;
        }

        double[] point = {x, y};
        return point;
    }

}



