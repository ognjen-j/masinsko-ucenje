function [granice] = particionisi_interval(podaci, min_tacaka_u_intervalu, granice, pocetak_prethodnog_intervala)
n = length(podaci);
pocetak = 1;
kraj = n;
granica = particije(podaci, min_tacaka_u_intervalu);
if granica == pocetak || granica == kraj
    return
else
    granice = [granice, pocetak_prethodnog_intervala + granica - 1];
    if granica - pocetak > min_tacaka_u_intervalu
        granice = particionisi_interval(podaci(1:granica-1), min_tacaka_u_intervalu, granice, pocetak_prethodnog_intervala);
    end
    if kraj - granica > min_tacaka_u_intervalu
        granice = particionisi_interval(podaci(granica:n), min_tacaka_u_intervalu, granice, pocetak_prethodnog_intervala+granica-1);
    end
    return
end
end
