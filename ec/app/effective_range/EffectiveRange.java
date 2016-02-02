package ec.app.effective_range;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

public class EffectiveRange extends Problem implements SimpleProblemForm {

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

        objectives[0] = costo(ind);
        objectives[1]  = qos(ind);

        ((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
        ind.evaluated = true;
    }


    public double costo(Individual ind){

        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;

        double costo = 0;
        int tipo_infraestructura;
        for (int i = 0; i < t_ind.genome.length; i++){
            tipo_infraestructura = (int) Math.floor(t_ind.genome[i]);
            costo += t_spe.getPrecioAntena()[tipo_infraestructura];
        }
        return costo;

    }

    public double qos (Individual ind){
        boolean debug;
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;


        double[][] road_side_units = new double[t_ind.genome.length][3];

        // Create RSU's
        for (int i = 0; i < t_ind.genome.length; i++){
            int tipo_infraestructura = (int)Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura != 0){
                // There is a RSU on the segment
                double [] centro = findRsuCenter(t_ind, i, tipo_infraestructura, t_spe);

                double radio_circulo = t_spe.getRadioAntena()[tipo_infraestructura];
                road_side_units[i] = new double[]{ centro[0], centro[1], radio_circulo };
            }
        }

        // Iterate through the segments
        for (int i = 0; i < t_ind.genome.length; i++){

            // Calculate each segment coverage
            double xini = t_spe.getSegmento(i)[0];
            double yini = t_spe.getSegmento(i)[1];
            double xfin = t_spe.getSegmento(i)[2];
            double yfin = t_spe.getSegmento(i)[3];

            double segment_length = t_spe.getDistanciaSegmento()[i];

            double divitions = 10;
            double module_section = segment_length / divitions;
            double intersections = 0;

            double coverered_distance = 0;

            double x_length = Math.abs(xini - xfin) / divitions;
            double y_length = Math.abs(yini - yfin) / divitions;

			double[] rsu_anterior = null;

            for (int j = 0; j < divitions; j++) {
                double x = xini;
                if (xini < xfin) {
                    x = xini + j * x_length;
                }else if (xini > xfin) {
                    x = xini - j * x_length;
                }

                double y = yini;
                if (yini < yfin) {
                    y = yini + j * y_length;
                }else if (yini > yfin) {
                    y = yini - j * y_length;
                }


                if(rsu_anterior != null && rsu_anterior[2] >= twoPointsDistance(rsu_anterior[0], rsu_anterior[1], x, y)) {
                        intersections++;
                }
                else{

                    for (int k = 0; k < road_side_units.length; k++) {
                        double[] rsu = road_side_units[k];
                        if (rsu[2] >= twoPointsDistance(rsu[0], rsu[1], x, y)) {
                            intersections++;
                            rsu_anterior = rsu;
                            break;
                        }
                    }
                }
            }

            qos += t_spe.getVehiculosSegmento()[i] * (intersections / divitions);
        }

        return qos;
    }

    public static double twoPointsDistance(double x1, double y1, double x2, double y2){
        double dLat = deg2rad((x1 - x2));
        double dLon = deg2rad((y1 - y2));

        double x = (dLon) * Math.cos(deg2rad((x1 + x2)/2));
        double dist = Math.sqrt(x*x + dLat*dLat) * 6371000;

        return (dist);
    }


    public static double[] findRsuCenter (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){

        double lat_ini = t_spe.getSegmento(indice)[0];
        double lng_ini = t_spe.getSegmento(indice)[1];
        double lat_fin = t_spe.getSegmento(indice)[2];
        double lng_fin = t_spe.getSegmento(indice)[3];
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

        double[] centro = {x, y};
        return centro;
    }

    private static double deg2rad(double deg) {
        return (deg / 180 * Math.PI);
    }

}



