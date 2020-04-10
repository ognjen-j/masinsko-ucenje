function [granica] = particije(podaci, min_tacaka_u_intervalu)
n = length(podaci);
srednja_vrijednost = mean(podaci);
ukupna_greska = sum((podaci - srednja_vrijednost).^2) / n;
granica = 1;
greska_za_najbolju_granicu = -1;
for ind = min_tacaka_u_intervalu:n - min_tacaka_u_intervalu
    srednja_vr_particije = [mean(podaci(1:ind-1)), mean(podaci(ind:n))];
    greske_particija = sum((podaci(1:ind-1) - srednja_vr_particije(1)).^2) / (ind - 2) + ...
        sum((podaci(ind:n) - srednja_vr_particije(2)).^2) / (n - ind);
    if (ukupna_greska - greske_particija) > greska_za_najbolju_granicu
        granica = ind;
        greska_za_najbolju_granicu = ukupna_greska - greske_particija;
    end
end
end
