package ec.app.trafico;

import ec.vector.*;
import ec.*;
import ec.util.*;

public class Calculos {
    public static double costo(Individual ind){

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

    public static double qos (Individual ind){
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
                    double xini = t_spe.getSegmento(k)[0];
                    double yini = t_spe.getSegmento(k)[1];
                    double xfin = t_spe.getSegmento(k)[2];
                    double yfin = t_spe.getSegmento(k)[3];

                    double segment_length = t_spe.getDistanciaSegmento()[k];
                    double segment_coverage = 0;

                    boolean start_inside = radio_circulo >= twoPointsDistance(xcentro, ycentro, xini, yini);
                    boolean end_inside = radio_circulo >= twoPointsDistance(xcentro, ycentro, xfin, yfin);

                    if (start_inside && end_inside){
                        segment_coverage = 1;
                    }
                    else if (start_inside || end_inside){
                        //Hay un punto adentro y uno afuera
                        double alpha;
                        double center_extreme_distance;

                        if (start_inside){
                            alpha = angleBetweenLines(xcentro, ycentro, xini, yini, xini, yini, xfin, yfin);
                            center_extreme_distance = twoPointsDistance(xcentro, ycentro, xini, yini);
                        }else{
                            alpha = angleBetweenLines(xcentro, ycentro, xfin, yfin, xfin, yfin, xini, yini);
                            center_extreme_distance = twoPointsDistance(xcentro, ycentro, xfin, yfin);
                        }

                        if (alpha != 0){
                            double beta = Math.asin(center_extreme_distance * Math.sin(alpha) / (double)radio_circulo);
                            segment_coverage = (Math.sin(Math.PI - alpha - beta) * radio_circulo / Math.sin(alpha)) / segment_length;
                        }
                        else{
                            //Los 3 puntos están alineados
                            if (start_inside){
                                segment_coverage = (radio_circulo - twoPointsDistance(xcentro, ycentro, xini, yini)) / segment_length;
                            }
                            else{
                                segment_coverage = (radio_circulo - twoPointsDistance(xcentro, ycentro, xfin, yfin)) / segment_length;
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
                            segment_coverage = (2 * lambda) / segment_length;
                        }
                    }

                    double vehiculos_segmento = t_spe.getVehiculosSegmento()[k];
                    int vehiculos_actuales_cubiertos = (int)(segment_coverage * vehiculos_segmento);
                    int capacidad_total = t_spe.getCapacidadAntena()[tipo_infraestructura];

                    if (vehiculos_cubiertos_rsu[i] < capacidad_total && vehiculos_cubiertos_segmento[k] < vehiculos_segmento) {
                        double vehiculos_no_cubiertos = 0;
                        double capacidad_actual_rsu = capacidad_total - vehiculos_cubiertos_rsu[i];
                        double vehiculos_restantes_segmento = vehiculos_segmento - vehiculos_cubiertos_segmento[k];

                        vehiculos_no_cubiertos = Math.min(Math.min(capacidad_actual_rsu, vehiculos_restantes_segmento), vehiculos_actuales_cubiertos);
                        vehiculos_cubiertos_rsu[i]  += vehiculos_no_cubiertos;
                        vehiculos_cubiertos_segmento[k] += vehiculos_no_cubiertos;
                    }
                }
            }
        }
        for (int i = 0; i < t_ind.genome.length; i++) {
            qos += vehiculos_cubiertos_segmento[i];
        }
        return qos;
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
        double dLat = deg2rad((x1 - x2));
        double dLon = deg2rad((y1 - y2));

        double x = (dLon) * Math.cos(deg2rad((x1 + x2)/2));
        double dist = Math.sqrt(x*x + dLat*dLat) * 6371000;

        return (dist);
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
        return (deg / 180 * Math.PI);
    }
}
