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
hold on;
while (abs(greska_prethodna-greska) > epsilon && iteracija < maksimalan_broj_iteracija)
    greska_prethodna = greska;
    y_pred = x * teta;
    teta_promjena = x' * (y_pred - y);
    teta = teta - alfa * (2 / n) * teta_promjena;
    greska = sum((y_pred - y).^2) / n;
    if (greska < greska_prethodna)
        alfa = alfa * 1.5;
    else
        alfa = alfa / 1.5;
    end
    iteracija = iteracija + 1;
end
plot(x(:, 2), y_pred, 'red');