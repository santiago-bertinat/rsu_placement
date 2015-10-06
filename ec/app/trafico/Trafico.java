package ec.app.trafico;

import java.util.*;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

public class Trafico extends Problem implements SimpleProblemForm {

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

        double costo=0;
        int tipo_infraestructura;
        for (int i=0;i<t_ind.genome.length;i++){
            tipo_infraestructura=(int) Math.floor(t_ind.genome[i]);
            costo+=t_spe.getPrecioAntena()[tipo_infraestructura];
        }
        return costo;

    }

    public double qos (Individual ind){
        FloatVectorIndividual t_ind = (FloatVectorIndividual)ind;
        FloatVectorSpecies t_spe = (FloatVectorSpecies)ind.species;
        double qos = 0;

        int[] vehiculos_cubiertos_rsu = new int[t_ind.genome.length];
        int[] vehiculos_cubiertos_segmento = new int[t_ind.genome.length];

        // Iterate through the segments
        for (int i = 0; i < t_ind.genome.length; i++){
            int tipo_infraestructura = (int) Math.floor(t_ind.genome[i]);
            if (tipo_infraestructura!=0) {

                double [] centro = findRsuCenter(t_ind, i, tipo_infraestructura, t_spe);
                //Circulo de cobertura
                double xcentro = centro[0];
                double ycentro = centro[1];
                double radio_circulo = t_spe.getRadioAntena()[tipo_infraestructura];

                for (int k = 0; k < t_ind.genome.length; k++){
                    double xini = t_spe.getLatitudes()[t_spe.getPtoInicialSegmento()[k]];
                    double yini = t_spe.getLongitudes()[t_spe.getPtoInicialSegmento()[k]];
                    double xfin = t_spe.getLatitudes()[t_spe.getPtoFinalSegmento()[k]];
                    double yfin = t_spe.getLongitudes()[t_spe.getPtoFinalSegmento()[k]];

                    double segment_length = twoPointsDistance(xini, yini, xfin, yfin);
                    double segment_coverage = 0;

                    // If rsu i belongs to k segment
                    if (k == i){
                        segment_coverage = segment_length / radio_circulo;
                    }else {
                        boolean start_inside = radio_circulo > twoPointsDistance(xcentro, ycentro, xini, yini);
                        boolean end_inside = radio_circulo > twoPointsDistance(xcentro, ycentro, xfin, yfin);

                        if (start_inside && end_inside){
                            segment_coverage = segment_length;
                        }
                        else if (start_inside || end_inside){
                            //Hay un punto adentro y uno afuera
                            double alpha;
                            double center_extreme_distance;

                            if (start_inside){
                                alpha = angleBetweenLines(xcentro, ycentro, xini, yini, xini, yini, xfin, yfin);
                                center_extreme_distance = twoPointsDistance(xcentro, ycentro, xini, yini);
                            }else{
                                alpha = angleBetweenLines(xcentro, ycentro, xfin, yfin, xini, yini, xfin, yfin);
                                center_extreme_distance = twoPointsDistance(xcentro, ycentro, xfin, yfin);
                            }

                            if (alpha != 0){
                                double beta = Math.asin(center_extreme_distance * Math.sin(alpha) / (double)radio_circulo);
                                segment_coverage = segment_length / (Math.sin(Math.PI - alpha - beta) * radio_circulo / Math.sin(alpha));
                            }
                            else{
                                //Los 3 puntos están alineados
                                if (start_inside){
                                    segment_coverage = segment_length / (radio_circulo - twoPointsDistance(xcentro, ycentro, xini, yini));
                                }
                                else{
                                    segment_coverage = segment_length / (radio_circulo - twoPointsDistance(xcentro, ycentro, xfin, yfin));
                                }
                            }

                        }
                        else if (pointToSegmentDistance(xcentro, ycentro, xini, yini, xfin, yfin) < radio_circulo){
                            //La recta intersecta el circulo, falta ver si el segmento tambien
                            double m = pointToSegmentDistance(xcentro, ycentro, xini, yini, xfin, yfin);
                            double dAC = twoPointsDistance(xcentro, ycentro, xini, yini);
                            double dBC = twoPointsDistance(xcentro, ycentro, xfin, yfin);
                            double dAB = twoPointsDistance(xini, yini, xfin, yfin);
                            double dAQ = Math.sqrt(Math.pow(dAC, 2) - Math.pow(m, 2));
                            double dQB = Math.sqrt(Math.pow(dBC, 2) - Math.pow(m, 2));
                            if (dAQ < dAB && dQB < dAB){
                                //El segmento intersecta el circulo
                                double lambda = Math.sqrt(Math.pow(radio_circulo,2) - Math.pow(m,2));
                                segment_coverage = segment_length / (2 * lambda);
                            }
                        }

                        double vehiculos_segmento = t_spe.getVehiculosSegmento()[i];
                        int vehiculos_actuales_cubiertos = (int)(segment_coverage * vehiculos_segmento);
                        int capacidad_total = t_spe.getCapacidadAntena()[tipo_infraestructura];

                        if (vehiculos_cubiertos_rsu[i] < capacidad_total && vehiculos_cubiertos_segmento[k] < vehiculos_segmento) {
                            double vehiculos_no_cubiertos = 0;
                            if ((capacidad_total - vehiculos_cubiertos_rsu[i]) < (vehiculos_segmento - vehiculos_cubiertos_segmento[k])) {
                                vehiculos_no_cubiertos = capacidad_total - vehiculos_cubiertos_rsu[i];
                            }else {
                                vehiculos_no_cubiertos = vehiculos_segmento - vehiculos_cubiertos_segmento[k];
                            }

                            if (vehiculos_no_cubiertos > vehiculos_actuales_cubiertos){
                                vehiculos_cubiertos_rsu[i] += vehiculos_actuales_cubiertos;
                                vehiculos_cubiertos_segmento[k] += vehiculos_actuales_cubiertos;
                            }else {
                                vehiculos_cubiertos_rsu[i]  += vehiculos_no_cubiertos;
                                vehiculos_cubiertos_segmento[k] += vehiculos_no_cubiertos;
                            }
                        }
                    }
                }
            }
        }
        for (int i = 0; i < t_ind.genome.length; i++) {
            qos += vehiculos_cubiertos_rsu[i];
        }

        return qos;
    }

    public double[] findRsuCenter (FloatVectorIndividual t_ind, int indice, int tipo_infraestructura, FloatVectorSpecies t_spe){

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

        double[] centro = {x, y};
        return centro;
    }

    public static double pointToSegmentDistance(double xp,double yp,double x1,double y1,double x2,double y2) {
        // Using Heron formula
        double dps, dpe, dse;
        dps = twoPointsDistance(xp,yp,x1,y1);
        dpe = twoPointsDistance(xp,yp,x2,y2);
        dse = twoPointsDistance(x1,y1,x2,y2);

        double s = (dps + dpe + dse) / (double)2;
        double area= Math.sqrt(s*(s-dps)*(s-dpe)*(s-dse));
        return 2*area/(double)dse;
    }

    public static double twoPointsDistance(double x1, double y1, double x2, double y2){
        double theta = x1 - x2;
        double dist = Math.sin(deg2rad(y1)) * Math.sin(deg2rad(y2)) + Math.cos(deg2rad(y1)) * Math.cos(deg2rad(y2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344 * 1000;

        return (dist);

        // double deglen=110.25*1000;
        // double x= x1-x2;
        // double y= (y1-y2)*Math.cos(x2);
        // return deglen* Math.sqrt(x*x + y*y);
    }

    public static double angleBetweenLines(double x1,double y1,double x2,double y2, double x3,double y3,double x4,double y4) {
        // Apply cosine theorem
        double a = twoPointsDistance(x1, y1, x2, y2);
        double b = twoPointsDistance(x3, y3, x4, y4);
        double c = twoPointsDistance(x1, y1, x4, y4);

        if((a + b == c) || (a + c == b) || (c + b == a)) {
          return 0;
        }
        double cosine = (c*c - a*a - b*b) / (-2 * a * b);
        if (cosine > 1) {
          return 0;
        }

        return Math.acos(cosine);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

}



