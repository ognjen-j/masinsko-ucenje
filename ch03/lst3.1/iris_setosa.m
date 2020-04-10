clear *
podaci = importdata('data/iris1.csv', ',', 1);
br_primjera = length(podaci.data);
x = [ones(br_primjera, 1), podaci.data(:, 3), podaci.data(:, 4)];
y = podaci.data(:, 5);
br_deskriptora = size(x, 2);
teta = zeros(br_deskriptora, 1);
tetaPromjena = zeros(br_deskriptora, 1);
maksimalni_broj_iteracija = 200000;
iteracija = 0;
epsilon = 10^-10;
alfa = 0.003;
while (iteracija < maksimalni_broj_iteracija)
    predikcija = (1 ./ (1 + exp(-x*teta)));
    tetaPromjena = ((predikcija - y)' * x) ./ (br_primjera * log(2));
    teta = teta - alfa .* tetaPromjena';
    iteracija = iteracija + 1;
end
figure;
scatter(x(y == 1, 2), x(y == 1, 3), 'bo');
hold on;
scatter(x(y == 0, 2), x(y == 0, 3), 'rx');
plot_x = [min(x(:, 2)) - 1, max(x(:, 2)) + 1];
plot_y = (-1 ./ teta(3)) .* (teta(2) .* plot_x + teta(1));
plot(plot_x, plot_y)
xlim([min(x(:, 2)) - 1, max(x(:, 2)) + 1]);
ylim([-1, 3]);
ylabel('Sirina latice');
xlabel('Duzina latice');