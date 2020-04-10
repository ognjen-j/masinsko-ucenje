clear *
clc
podaci = importdata('data/random_linear_train.csv', ',', 1);
y = podaci.data(:, 2);
n = length(y);
x = [ones(n, 1), podaci.data(:, 1)];
teta = [0; 0];
teta_promjena = [0; 0];
maksimalan_broj_iteracija = 70000;
iteracija = 0;
epsilon = 10^-10;
alfa = 10^-8;
greska = 1;
greska_prethodna = 0;
plot(x(:, 2), y, 'x');
velicina_segmenta = 100;
hold on;
rb_segmenta = 0;
while (iteracija < maksimalan_broj_iteracija)
    pocetak_segmenta = 1;
    while (pocetak_segmenta < size(x, 1))
        segment_x = x(pocetak_segmenta:min(size(x, 1), pocetak_segmenta+velicina_segmenta), :);
        segment_y = y(pocetak_segmenta:min(size(y, 1), pocetak_segmenta+velicina_segmenta), :);
        y_pred = segment_x * teta;
        teta_promjena = segment_x' * (y_pred - segment_y);
        teta = teta - alfa * (2 / n) * teta_promjena;
        pocetak_segmenta = pocetak_segmenta + velicina_segmenta;
        rb_segmenta = rb_segmenta + 1;
    end
    iteracija = iteracija + 1;
end
y_pred = x * teta;
plot(x(:, 2), y_pred, 'red');
