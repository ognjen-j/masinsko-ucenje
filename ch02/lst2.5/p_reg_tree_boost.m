% p_reg_tree_boost.m
clc
clear *
podaci = importdata('data/population.csv', ',', 3);
y = podaci.data(:, 54) / 10^7;
n = length(y);
x = linspace(0, 1, n)';
alfa = 0.01;
iteracija = 1;
maxmaksimalan_broj_iteracija = 10000;
vrijednosti = repelem(mean(y), length(y))';
for iteracija = 1:maxmaksimalan_broj_iteracija
    razlika = y - vrijednosti;
    granice = [];
    granice = particionisi_interval(razlika, 3, granice, 1);
    granice = sort(granice);
    granice = [1, granice, length(razlika)];
    for cnt = 1:length(granice) - 1
        srednje_y = mean(razlika(granice(cnt):granice(cnt+1)-1));
        vrijednosti(granice(cnt):granice(cnt+1)) = vrijednosti(granice(cnt):granice(cnt+1)) + alfa * srednje_y;
    end
end
figure
plot(x, y, 'x');
hold on;
plot(x, vrijednosti);
ylabel('Ukupna populacija (*10^7)');